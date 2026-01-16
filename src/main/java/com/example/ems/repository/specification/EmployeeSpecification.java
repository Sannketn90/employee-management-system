package com.example.ems.repository.specification;

import com.example.ems.entity.Employee;
import org.springframework.data.jpa.domain.Specification;

public class EmployeeSpecification {

    public static Specification<Employee> hasDepartment(String department) {
        return (root, query, cb) ->
                department == null ? null :
                        cb.equal(root.get("department"), department);
    }

    public static Specification<Employee> salaryGreaterThan(Double salary) {
        return (root, query, cb) ->
                salary == null ? null :
                        cb.greaterThan(root.get("salary"), salary);
    }

    public static Specification<Employee> nameContains(String name) {
        return (root, query, cb) ->
                name == null ? null :
                        cb.like(cb.lower(root.get("name")),
                                "%" + name.toLowerCase() + "%");
    }
}
