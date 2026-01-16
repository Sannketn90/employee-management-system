package com.example.ems.service;

import com.example.ems.entity.Employee;
import com.example.ems.entity.models.EmployeeModel;
import com.example.ems.entity.response.PageResponse;
import com.example.ems.exception.DuplicateEmployeeException;
import com.example.ems.exception.EmployeeNotFoundException;
import com.example.ems.mapper.EmployeeMapper;
import com.example.ems.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static com.example.ems.repository.specification.EmployeeSpecification.*;

/**
 * Service layer for Employee-related operations.
 * Handles CRUD, pagination, filtering, and advanced search using JPA Specifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    private static final List<String> ALLOWED_SORT_FIELDS = Arrays.asList(
            "id", "name", "email", "department", "salary", "joiningDate"
    );

    // ===================== VALIDATION =====================

    /**
     * Validates the given EmployeeModel.
     *
     * @param model the employee data
     * @throws IllegalArgumentException if required fields are missing or invalid
     */
    private void validateEmployeeModel(final EmployeeModel model) {
        if (model == null) throw new IllegalArgumentException("Employee data cannot be null");
        if (isNullOrEmpty(model.getName())) throw new IllegalArgumentException("Employee name is required");
        if (isNullOrEmpty(model.getEmail())) throw new IllegalArgumentException("Employee email is required");
        if (model.getSalary() != null && model.getSalary() < 0)
            throw new IllegalArgumentException("Salary cannot be negative");

        model.setName(model.getName().trim());
        model.setEmail(model.getEmail().trim());
        if (model.getDepartment() != null) model.setDepartment(model.getDepartment().trim());
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Sanitizes the sortBy field to ensure only allowed fields are used.
     *
     * @param sortBy the requested sort field
     * @return validated sort field (defaults to "id" if invalid)
     */
    private String sanitizeSortBy(String sortBy) {
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            log.warn("Invalid sort field '{}', defaulting to 'id'", sortBy);
            return "id";
        }
        return sortBy;
    }

    // ===================== CREATE / UPDATE =====================

    /**
     * Create or update an employee.
     *
     * @param employeeId optional employee ID (for update)
     * @param model      the employee data
     * @return EmployeeModel
     * @throws DuplicateEmployeeException if an employee with the same email exists
     */
    @Transactional
    public EmployeeModel saveOrUpdateEmployee(final String employeeId, final EmployeeModel model) throws DuplicateEmployeeException {
        validateEmployeeModel(model);

        final boolean isUpdate = employeeId != null && !employeeId.isBlank();
        final Employee employee;

        if (isUpdate) {
            // Update existing employee
            employee = employeeRepository.findById(employeeId.trim())
                    .orElseThrow(() -> new EmployeeNotFoundException("Employee with id " + employeeId + " not found"));

            // Partial update using MapStruct
            employeeMapper.updateEntityFromModel(model, employee);

            final Employee updated = employeeRepository.save(employee);
            log.info("Updated employee: {}", updated.getId());
            return employeeMapper.toEmployeeModel(updated);
        } else {
            // Create new employee
            if (employeeRepository.existsByEmailIgnoreCase(model.getEmail())) {
                throw new DuplicateEmployeeException("Employee with email " + model.getEmail() + " already exists");
            }

            employee = employeeMapper.toEmployeeEntity(model);
            final Employee saved = employeeRepository.save(employee);
            log.info("Created employee: {}", saved.getId());
            return employeeMapper.toEmployeeModel(saved);
        }
    }

    // ===================== DELETE =====================

    /**
     * Delete an employee by ID.
     *
     * @param employeeId the employee ID
     * @throws EmployeeNotFoundException if employee does not exist
     */
    @Transactional
    public void deleteEmployee(final String employeeId) {
        final Employee employee = employeeRepository.findById(employeeId.trim())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee with id " + employeeId + " not found"));

        employeeRepository.delete(employee);
        log.info("Deleted employee: {}", employee.getId());
    }

    // ===================== FIND BY ID =====================

    /**
     * Find an employee by ID.
     *
     * @param employeeId employee ID
     * @return EmployeeModel
     * @throws EmployeeNotFoundException if employee not found
     */
    @Transactional(readOnly = true)
    public EmployeeModel findById(final String employeeId) {
        final Employee employee = employeeRepository.findById(employeeId.trim())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee with id " + employeeId + " not found"));
        return employeeMapper.toEmployeeModel(employee);
    }

    // ===================== PAGINATION + FILTER =====================

    /**
     * Get paginated employees with optional filters.
     *
     * @param page       page number
     * @param size       page size
     * @param sortBy     sort field
     * @param sortDir    sort direction
     * @param department optional department filter
     * @param minSalary  optional minimum salary
     * @param name       optional name search
     * @return PageResponse<EmployeeModel>
     */
    @Transactional(readOnly = true)
    public PageResponse<EmployeeModel> findAllEmployeesWithFilter(
            int page, int size, String sortBy, String sortDir,
            String department, Double minSalary, String name) {

        sortBy = sanitizeSortBy(sortBy);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Employee> spec = Specification.allOf(
                hasDepartment(department),
                salaryGreaterThan(minSalary),
                nameContains(name)
        );

        Page<EmployeeModel> result = employeeRepository.findAll(spec, pageable)
                .map(employeeMapper::toEmployeeModel);

        return PageResponse.<EmployeeModel>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }

    // ===================== ADVANCED SEARCH =====================

    /**
     * Advanced employee search with multiple filters.
     *
     * @param page            page number
     * @param size            page size
     * @param sortBy          sort field
     * @param sortDir         sort direction
     * @param name            optional name filter
     * @param department      optional department filter
     * @param minSalary       optional min salary
     * @param maxSalary       optional max salary
     * @param joiningDateFrom optional joining date from (yyyy-MM-dd)
     * @param joiningDateTo   optional joining date to (yyyy-MM-dd)
     * @return PageResponse<EmployeeModel>
     */
    @Transactional(readOnly = true)
    public PageResponse<EmployeeModel> searchEmployees(
            int page, int size, String sortBy, String sortDir,
            String name, String department, Double minSalary, Double maxSalary,
            String joiningDateFrom, String joiningDateTo) {

        sortBy = sanitizeSortBy(sortBy);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Employee> spec = Specification.allOf(
                nameContains(name),
                hasDepartment(department),
                salaryGreaterThan(minSalary),
                salaryLessThan(maxSalary),
                joiningDateAfter(joiningDateFrom),
                joiningDateBefore(joiningDateTo)
        );

        Page<EmployeeModel> result = employeeRepository.findAll(spec, pageable)
                .map(employeeMapper::toEmployeeModel);

        return PageResponse.<EmployeeModel>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }
}
