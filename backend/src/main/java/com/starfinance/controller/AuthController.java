package com.starfinance.controller;

import com.starfinance.dto.*;
import com.starfinance.entity.Customer;
import com.starfinance.entity.Employee;
import com.starfinance.repository.CustomerRepository;
import com.starfinance.repository.EmployeeRepository;
import com.starfinance.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.starfinance.service.AuthService;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final AuthService authService;


    @PostMapping("/register/customer")
    public ResponseEntity<CustomerResponse> registerCustomer(@Valid @RequestBody RegisterRequest request) {
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already registered.");
        }
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setKnNumber(null);
        customer.setAadhaar(null);
        customer.setPanCard(null);
        customer.setKycVerified(false);
        customer.setOauthProvider(null);
        customer.setOauthId(null);
        Customer saved = customerRepository.save(customer);
        return ResponseEntity.ok(mapToCustomerResponse(saved));
    }


    @PostMapping("/login/customer/password")
    public ResponseEntity<JwtResponse> loginCustomer(@Valid @RequestBody LoginRequest request) {
        String email = request.getEmail();

        Optional<Employee> employee = employeeRepository.findByUsername(email);

        if (employee.isPresent()) {
            throw new org.springframework.security.authentication.BadCredentialsException(
                    "User must log in via the Employee portal."
            );
        }

        if (customerRepository.findByEmail(email).isEmpty()) {
            throw new org.springframework.security.authentication.BadCredentialsException(
                    "Invalid username or password."
            );
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtService.generateToken(authentication);
        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    @PostMapping("/login/employee/password")
    public ResponseEntity<JwtResponse> loginEmployee(@Valid @RequestBody LoginRequest request) {
        String username = request.getEmail();

        Optional<Employee> employee = employeeRepository.findByUsername(username);

        if (employee.isEmpty()) {
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid username or password.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtService.generateToken(authentication);

        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    @PostMapping("/forgot-password/customer")
    public ResponseEntity<Void> resetCustomerPassword(@RequestBody PasswordResetRequest request) {
        try {
            authService.resetCustomerPassword(
                    request.getEmail(),
                    request.getDateOfBirth(),
                    request.getNewPassword()
            );

            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification failed: Email or date of birth is incorrect.");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Password reset failed: " + e.getMessage());
        }
    }

    @PostMapping("/forgot-password/employee")
    public ResponseEntity<String> resetEmployeePassword(@RequestBody PasswordResetRequest request) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Employee password resets cannot be performed via public self-service. Please contact your Bank Administrator.");
    }

    public static CustomerResponse mapToCustomerResponse(Customer customer) {
        if (customer == null) return null;
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .knNumber(customer.getKnNumber())
                .aadhaar(customer.getAadhaar())
                .panCard(customer.getPanCard())
                .bankAccountNumber(customer.getBankAccountNumber())
                .maskedAadhaar(CustomerResponse.maskAadhaar(customer.getAadhaar()))
                .maskedPanCard(CustomerResponse.maskPan(customer.getPanCard()))
                .kycStatus(customer.getKycStatus())
                .kycVerified(customer.getKycVerified())
                .maskedBankAccountNumber(CustomerResponse.maskAccountNumber(customer.getBankAccountNumber()))
                .city(customer.getCity())
                .state(customer.getState())
                .pinCode(customer.getPinCode())
                .fullAddress(customer.getFullAddress())
                .dateOfBirth(customer.getDateOfBirth())
                .gender(customer.getGender())
                .mobileNumber(customer.getMobileNumber())
                .occupation(customer.getOccupation())
                .income(customer.getIncome())
                .ifscCode(customer.getIfscCode())
                .existingLoans(customer.getExistingLoans())
                .passportNumber(customer.getPassportNumber())
                .role(customer.getRole())
                .build();
    }

    @GetMapping("/oauth/success")
    public ResponseEntity<JwtResponse> oauthSuccess(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new JwtResponse("Authentication failed: No valid OAuth user found."));
        }
        String jwt = jwtService.generateToken(authentication);
        response.addHeader("Authorization", "Bearer " + jwt);
        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    @PostMapping("/customer/change-password")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> changeCustomerPassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        try {
            authService.changeCustomerPassword(email, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok("Password updated successfully.");
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Password change failed: " + e.getMessage());
        }
    }


    @PostMapping("/employee/change-password")
    @PreAuthorize("hasAnyRole('BANK_STAFF', 'BANK_ADMIN')")
    public ResponseEntity<String> changeEmployeePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        try {
            authService.changeEmployeePassword(username, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok("Password updated successfully.");
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Employee password change failed: " + e.getMessage());
        }
    }
}