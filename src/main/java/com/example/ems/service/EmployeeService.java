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
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static com.example.ems.repository.specification.EmployeeSpecification.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    private static final List<String> ALLOWED_SORT_FIELDS = Arrays.asList(
            "id", "name", "email", "department", "salary", "joiningDate"
    );

    /* ===================== VALIDATION ===================== */
    private void validateEmployeeModel(final EmployeeModel model) {
        if (model == null) throw new IllegalArgumentException("Employee data cannot be null");
        if (isNullOrEmpty(model.getName())) throw new IllegalArgumentException("Employee name is required");
        if (isNullOrEmpty(model.getEmail())) throw new IllegalArgumentException("Employee email is required");
        if (model.getSalary() != null && model.getSalary() < 0) throw new IllegalArgumentException("Salary cannot be negative");

        model.setName(model.getName().trim());
        model.setEmail(model.getEmail().trim());
        if (model.getDepartment() != null) model.setDepartment(model.getDepartment().trim());
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String sanitizeSortBy(String sortBy) {
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            log.warn("Invalid sort field '{}', defaulting to 'id'", sortBy);
            return "id";
        }
        return sortBy;
    }

    /* ===================== CREATE / UPDATE ===================== */
    @Transactional
    public EmployeeModel saveOrUpdateEmployee(final String employeeId, final EmployeeModel model) throws DuplicateEmployeeException {
        validateEmployeeModel(model);

        final boolean isUpdate = employeeId != null && !employeeId.isBlank();
        final Employee employee;

        if (isUpdate) {
            employee = employeeRepository.findById(employeeId.trim())
                    .orElseThrow(() ->
                            new EmployeeNotFoundException(
                                    "Employee with id " + employeeId + " not found"));

            // MapStruct partial update
            employeeMapper.updateEntityFromModel(model, employee);

            final Employee updated = employeeRepository.save(employee);
            log.info("Updated employee: {}", updated.getId());
            return employeeMapper.toEmployeeModel(updated);
        } else {
            // CREATE
            if (employeeRepository.existsByEmailIgnoreCase(model.getEmail())) {
                throw new DuplicateEmployeeException(
                        "Employee with email " + model.getEmail() + " already exists");
            }

            employee = employeeMapper.toEmployeeEntity(model);
            final Employee saved = employeeRepository.save(employee);
            log.info("Created employee: {}", saved.getId());
            return employeeMapper.toEmployeeModel(saved);
        }
    }


    /* ===================== DELETE ===================== */
    @Transactional
    public void deleteEmployee(final String employeeId) {
        final Employee employee = employeeRepository.findById(employeeId.trim())
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee with id " + employeeId + " not found"));

        employeeRepository.delete(employee);
        log.info("Deleted employee: {}", employee.getId());
    }

    /* ===================== FIND BY ID ===================== */
    @Transactional(readOnly = true)
    public EmployeeModel findById(final String employeeId) {
        final Employee employee = employeeRepository.findById(employeeId.trim())
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee with id " + employeeId + " not found"));
        return employeeMapper.toEmployeeModel(employee);
    }

    /* ===================== PAGINATION + FILTER ===================== */
    @Transactional(readOnly = true)
    public PageResponse<EmployeeModel> findAllEmployeesWithFilter(
            int page, int size, String sortBy, String sortDir,
            String department, Double minSalary, String name) {

        sortBy = sanitizeSortBy(sortBy);
        final Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        final Pageable pageable = PageRequest.of(page, size, sort);

        final Specification<Employee> specification = Specification.allOf(
                hasDepartment(department),
                salaryGreaterThan(minSalary),
                nameContains(name)
        );

        final Page<EmployeeModel> result = employeeRepository
                .findAll(specification, pageable)
                .map(employeeMapper::toEmployeeModel);

        log.info("Fetched page {} of employees (size {})", page, size);

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
