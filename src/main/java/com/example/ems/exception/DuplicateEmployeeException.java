package com.example.ems.exception;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class DuplicateEmployeeException extends Throwable {
    public DuplicateEmployeeException(@Email(message = "Invalid email format") @NotBlank(message = "Email is required") String email) {
        super("Employee with email " + email + " already exists");
    }
}
