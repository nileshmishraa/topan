package com.topan.repository;


import com.topan.entity.Employee;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
    boolean existsByLogin(String login);
    List<Employee> findBySalaryBetween(BigDecimal minSalary, BigDecimal maxSalary, Pageable pageable);

}
