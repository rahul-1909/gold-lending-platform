package com.starfinance.controller;

import com.starfinance.dto.*;
import com.starfinance.entity.Employee;
import com.starfinance.repository.EmployeeRepository;
import com.starfinance.service.AuthService;
import com.starfinance.service.EmployeeCreationService;
import com.starfinance.service.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;
import java.util.List;

@RestController
@RequestMapping("/api/customer/employee")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('BANK_STAFF', 'BANK_ADMIN')")
public class EmployeeActionsController {

    private final LoanApplicationService loanApplicationService;
    private final EmployeeRepository employeeRepository;
    private final EmployeeCreationService employeeCreationService;

    /**
     * Handles loan status updates initiated by bank employees. (Step 1, 4, 6)
     * POST /api/customer/employee/loan/{rid}/status
     */
    @PostMapping("/loan/{rid}/status")
    public ResponseEntity<Void> updateLoanStatus(
            @PathVariable String rid,
            @Valid @RequestBody LoanStatusUpdateRequest request,
            Authentication authentication) {

        try {
            // Pass the new status AND the rejection reason to the service
            loanApplicationService.updateLoanStatus(rid, request.getNewStatus(), request.getRejectionReason());

            // Return 204 NO_CONTENT, which is better for this update and fixes the previous parsing error (if you had one).
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan application not found with ID: " + rid);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update loan status: " + e.getMessage());
        }
    }

    private final AuthService authService;

    @PostMapping("/loan/{rid}/evaluate")
    public ResponseEntity<Void> submitEvaluationData(
            @PathVariable String rid,
            @Valid @RequestBody EvaluationDataRequest request) {

        try {
            loanApplicationService.completeGoldEvaluation(rid, request);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan application not found with ID: " + rid);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to submit evaluation data: " + e.getMessage());
        }
    }

    @PostMapping("/loan/{rid}/disburse")
    public ResponseEntity<Void> disburseLoan(@PathVariable String rid) {
        try {
            loanApplicationService.disburseLoan(rid);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan application not found.");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Disbursement failed: " + e.getMessage());
        }
    }

    @PostMapping("/loan/{rid}/collect-gold")
    public ResponseEntity<Void> collectGold(@PathVariable String rid) {
        try {
            loanApplicationService.collectGold(rid);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan application not found.");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Gold collection failed: " + e.getMessage());
        }
    }

    @PostMapping(value = "/create", produces = "application/json")
    @PreAuthorize("hasRole('BANK_ADMIN')")
    public ResponseEntity<EmployeeResponse> createNewEmployee(@Valid @RequestBody NewEmployeeRequest request) {
        try {
            Employee newEmployee = employeeCreationService.createEmployee(request);
            return new ResponseEntity<>(mapToEmployeeResponse(newEmployee), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create new employee: " + e.getMessage());
        }
    }

    @PostMapping("/admin-reset-password")
    @PreAuthorize("hasRole('BANK_ADMIN')")
    public ResponseEntity<Void> adminResetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetEmployeePassword(request.getEmail(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<EmployeeResponse> getEmployeeProfile(Authentication authentication) {
        String username = authentication.getName();
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        return ResponseEntity.ok(mapToEmployeeResponse(employee));
    }

    public static EmployeeResponse mapToEmployeeResponse(Employee employee) {
        if (employee == null) return null;
        return EmployeeResponse.builder()
                .id(employee.getId())
                .username(employee.getUsername())
                .fullName(employee.getFullName())
                .role(employee.getRole().name())
                .branchName(employee.getBranchName())
                .build();
    }

    /**
     * 💡 NEW ENDPOINT: Fetches all loan applications for the Employee Dashboard.
     * Angular will call GET /api/customer/employee/loans
     */
    @GetMapping("/loans")
    public ResponseEntity<List<LoanResponse>> getAllLoanApplications(Authentication authentication) {
        List<LoanResponse> loans = loanApplicationService.getAllLoanApplications();
        return ResponseEntity.ok(loans);
    }


    @GetMapping("/loan/{rid}")
    public ResponseEntity<LoanDetailsResponse> getLoanDetailsById(@PathVariable String rid, Authentication authentication) {

        try {
            LoanDetailsResponse details = loanApplicationService.getLoanDetailsByRid(rid);
            return ResponseEntity.ok(details);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan application not found with ID: " + rid);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve loan details.");
        }
    }
}