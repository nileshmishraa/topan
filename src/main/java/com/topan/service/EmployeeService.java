package com.topan.service;

import com.topan.entity.Employee;

import java.math.BigDecimal;
import java.util.List;

public interface EmployeeService {
    void saveEmployees(List<Employee> employees);

    void saveEmployee(Employee employees);

    List<String> findDuplicateLogins(List<Employee> employees);

    List<String> findDuplicateIds(List<Employee> employees);

    Employee findByEmployeeId(String employeeId);
    List<Employee> getEmployees(BigDecimal minSalary, BigDecimal maxSalary, int offset, int limit, String sort);


}
