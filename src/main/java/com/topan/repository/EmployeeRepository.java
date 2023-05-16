package com.topan.repository;


import com.topan.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
    boolean existsByLogin(String login);

}
