package com.example.ems.exception;


public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(final String employeeId) {
        super("Employee with id " + employeeId + " not found");
    }
}
