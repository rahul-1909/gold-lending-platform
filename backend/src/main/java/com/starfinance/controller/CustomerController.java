package com.starfinance.controller;

import com.starfinance.entity.Customer;
import com.starfinance.repository.CustomerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.starfinance.dto.CustomerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<CustomerResponse> getCustomerProfile(Authentication authentication) {
        String email = authentication.getName();  // Extract from JWT subject
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found."));
        return ResponseEntity.ok(AuthController.mapToCustomerResponse(customer));
    }
}