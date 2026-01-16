package com.example.ems.repository;

import com.example.ems.entity.Employee;
import com.example.ems.entity.models.EmployeeModel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String>, JpaSpecificationExecutor<Employee> {

    boolean existsByEmailIgnoreCase(@Email(message = "Invalid email format") @NotBlank(message = "Email is required") String email);

}
