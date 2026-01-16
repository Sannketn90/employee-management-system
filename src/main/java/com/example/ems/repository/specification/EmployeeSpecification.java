package com.example.ems.repository.specification;

import com.example.ems.entity.Employee;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * JPA Specifications for dynamic querying of Employee entities.
 * Provides reusable filters for advanced search and pagination.
 */
public class EmployeeSpecification {

    /**
     * Filter by department (exact match).
     *
     * @param department the department name
     * @return Specification<Employee>
     */
    public static Specification<Employee> hasDepartment(String department) {
        return (root, query, cb) -> (department == null || department.isBlank())
                ? null
                : cb.equal(root.get("department"), department.trim());
    }

    /**
     * Filter employees with salary greater than a value.
     *
     * @param minSalary minimum salary
     * @return Specification<Employee>
     */
    public static Specification<Employee> salaryGreaterThan(Double minSalary) {
        return (root, query, cb) -> (minSalary == null) ? null : cb.greaterThan(root.get("salary"), minSalary);
    }

    /**
     * Filter employees with salary less than a value.
     *
     * @param maxSalary maximum salary
     * @return Specification<Employee>
     */
    public static Specification<Employee> salaryLessThan(Double maxSalary) {
        return (root, query, cb) -> (maxSalary == null) ? null : cb.lessThan(root.get("salary"), maxSalary);
    }

    /**
     * Filter by name (case-insensitive, partial match).
     *
     * @param name employee name
     * @return Specification<Employee>
     */
    public static Specification<Employee> nameContains(String name) {
        return (root, query, cb) -> (name == null || name.isBlank())
                ? null
                : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase().trim() + "%");
    }

    /**
     * Filter employees who joined after or on a certain date.
     *
     * @param fromDate date in "yyyy-MM-dd" format
     * @return Specification<Employee>
     */
    public static Specification<Employee> joiningDateAfter(String fromDate) {
        if (fromDate == null || fromDate.isBlank()) return null;

        try {
            LocalDate date = LocalDate.parse(fromDate);
            return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("joiningDate"), date);
        } catch (DateTimeParseException e) {
            return null; // Invalid date ignored
        }
    }

    /**
     * Filter employees who joined before or on a certain date.
     *
     * @param toDate date in "yyyy-MM-dd" format
     * @return Specification<Employee>
     */
    public static Specification<Employee> joiningDateBefore(String toDate) {
        if (toDate == null || toDate.isBlank()) return null;

        try {
            LocalDate date = LocalDate.parse(toDate);
            return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("joiningDate"), date);
        } catch (DateTimeParseException e) {
            return null; // Invalid date ignored
        }
    }
}
