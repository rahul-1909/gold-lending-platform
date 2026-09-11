package com.starfinance.service;

import com.starfinance.dto.NewEmployeeRequest;
import com.starfinance.entity.Employee;

public interface EmployeeCreationService {
    /**
     * Creates and saves a new Employee based on the request data.
     *
     * @param request The DTO containing new employee details.
     * @return The newly created Employee entity.
     * @throws IllegalArgumentException if the username already exists or role is invalid.
     */
    Employee createEmployee(NewEmployeeRequest request);
}