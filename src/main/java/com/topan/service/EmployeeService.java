package com.topan.service;

import com.topan.entity.Employee;

import java.util.List;

public interface EmployeeService {
    void saveEmployees(List<Employee> employees);

    void saveEmployee(Employee employees);

    List<String> findDuplicateLogins(List<Employee> employees);

    List<String> findDuplicateIds(List<Employee> employees);

    Employee findByEmployeeId(String employeeId);

}
