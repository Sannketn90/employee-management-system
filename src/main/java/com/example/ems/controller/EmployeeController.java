package com.example.ems.controller;

import com.example.ems.entity.models.EmployeeModel;
import com.example.ems.entity.response.PageResponse;
import com.example.ems.exception.DuplicateEmployeeException;
import com.example.ems.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing Employee operations.
 * Supports CRUD operations, pagination, filtering, and advanced search.
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // ===================== CREATE / UPDATE =====================

    /**
     * Create a new employee.
     *
     * @param employeeModel the employee details
     * @return created EmployeeModel
     * @throws DuplicateEmployeeException if an employee with the same email already exists
     */
    @PostMapping
    public ResponseEntity<EmployeeModel> createEmployee(
            @Valid @RequestBody final EmployeeModel employeeModel) throws DuplicateEmployeeException {
        return ResponseEntity.ok(employeeService.saveOrUpdateEmployee(null, employeeModel));
    }

    /**
     * Update an existing employee.
     *
     * @param employeeId    the ID of the employee to update
     * @param employeeModel the updated employee details
     * @return updated EmployeeModel
     * @throws DuplicateEmployeeException if email already exists for another employee
     */
    @PutMapping("/{employeeId}")
    public ResponseEntity<EmployeeModel> updateEmployee(
            @PathVariable final String employeeId,
            @Valid @RequestBody final EmployeeModel employeeModel) throws DuplicateEmployeeException {
        return ResponseEntity.ok(employeeService.saveOrUpdateEmployee(employeeId, employeeModel));
    }

    // ===================== DELETE =====================

    /**
     * Delete an employee by ID.
     *
     * @param employeeId the employee ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable final String employeeId) {
        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.noContent().build();
    }

    // ===================== FIND BY ID =====================

    /**
     * Get an employee by ID.
     *
     * @param employeeId the employee ID
     * @return EmployeeModel
     */
    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeModel> getEmployeeById(@PathVariable final String employeeId) {
        return ResponseEntity.ok(employeeService.findById(employeeId));
    }

    // ===================== PAGINATION + FILTER =====================

    /**
     * Get paginated list of employees with optional filters.
     *
     * @param page       page number
     * @param size       page size
     * @param sortBy     field to sort by
     * @param sortDir    sort direction (asc/desc)
     * @param department optional department filter
     * @param minSalary  optional minimum salary filter
     * @param name       optional name search
     * @return PageResponse<EmployeeModel>
     */
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

    // ===================== ADVANCED SEARCH =====================

    /**
     * Advanced employee search with multiple optional filters.
     *
     * @param page            page number
     * @param size            page size
     * @param sortBy          sort field
     * @param sortDir         sort direction
     * @param name            optional name filter
     * @param department      optional department filter
     * @param minSalary       optional minimum salary
     * @param maxSalary       optional maximum salary
     * @param joiningDateFrom optional joining date from (yyyy-MM-dd)
     * @param joiningDateTo   optional joining date to (yyyy-MM-dd)
     * @return PageResponse<EmployeeModel>
     */
    @GetMapping("/search")
    public ResponseEntity<PageResponse<EmployeeModel>> searchEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(required = false) String joiningDateFrom,
            @RequestParam(required = false) String joiningDateTo) {

        return ResponseEntity.ok(employeeService.searchEmployees(
                page, size, sortBy, sortDir, name, department, minSalary, maxSalary, joiningDateFrom, joiningDateTo
        ));
    }
}
