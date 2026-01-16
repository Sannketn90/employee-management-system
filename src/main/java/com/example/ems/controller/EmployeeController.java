package com.example.ems.controller;

import com.example.ems.entity.models.EmployeeModel;
import com.example.ems.entity.response.PageResponse;
import com.example.ems.exception.DuplicateEmployeeException;
import com.example.ems.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /* ===================== CREATE / UPDATE ===================== */
    @PostMapping
    public ResponseEntity<EmployeeModel> createEmployee(
            @Valid @RequestBody final EmployeeModel employeeModel) throws DuplicateEmployeeException {
        return ResponseEntity.ok(
                employeeService.saveOrUpdateEmployee(null, employeeModel));
    }

    @PutMapping("/{employeeId}")
    public ResponseEntity<EmployeeModel> updateEmployee(
            @PathVariable final String employeeId,
            @Valid @RequestBody final EmployeeModel employeeModel) throws DuplicateEmployeeException {
        return ResponseEntity.ok(
                employeeService.saveOrUpdateEmployee(employeeId, employeeModel));
    }

    /* ===================== DELETE ===================== */
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> deleteEmployee(
            @PathVariable final String employeeId) {
        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.noContent().build();
    }

    /* ===================== FIND BY ID ===================== */
    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeModel> getEmployeeById(
            @PathVariable final String employeeId) {
        return ResponseEntity.ok(
                employeeService.findById(employeeId));
    }

    /* ===================== PAGINATION + FILTER ===================== */
    @GetMapping
    public ResponseEntity<PageResponse<EmployeeModel>> getAllEmployees(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size,
            @RequestParam(defaultValue = "id") final String sortBy,
            @RequestParam(defaultValue = "asc") final String sortDir,
            @RequestParam(required = false) final String department,
            @RequestParam(required = false) final Double minSalary,
            @RequestParam(required = false) final String name) {

        return ResponseEntity.ok(
                employeeService.findAllEmployeesWithFilter(
                        page, size, sortBy, sortDir, department, minSalary, name
                )
        );
    }

}
