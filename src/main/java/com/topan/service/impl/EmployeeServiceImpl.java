package com.topan.service.impl;

import com.topan.entity.Employee;
import com.topan.repository.EmployeeRepository;
import com.topan.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    public List<String> findDuplicateIds(List<Employee> employees) {
        List<String> duplicateIds = new ArrayList<>();
        List<String> ids = employees.stream().map(Employee::getId).toList();
        for (String id : ids) {
            if (employeeRepository.existsById(id)) {
                duplicateIds.add(id);
            }
        }
        return duplicateIds;
    }

    @Override
    public List<String> findDuplicateLogins(List<Employee> employees) {
        List<String> duplicateLogins = new ArrayList<>();
        List<String> logins = employees.stream().map(Employee::getLogin).toList();
        for (String login : logins) {
            if (employeeRepository.existsByLogin(login)) {
                duplicateLogins.add(login);
            }
        }
        return duplicateLogins;
    }

    @Override
    public void saveEmployees(List<Employee> employees) {
        employeeRepository.saveAll(employees);
    }

    /**
     * @param employees entity
     */
    @Override
    public void saveEmployee(Employee employees) {
        employeeRepository.save(employees);
    }

    @Override
    public Employee findByEmployeeId(String employeeId) {
        return employeeRepository.findById(employeeId).orElseThrow(() -> new NoSuchElementException("Employee not found"));
    }

    @Override
    public List<Employee> getEmployees(BigDecimal minSalary, BigDecimal maxSalary, int offset, int limit, String sort) {
        Sort.Direction direction = Sort.Direction.ASC;
        String sortField;

        if (sort.startsWith("-")) {
            direction = Sort.Direction.DESC;
            sortField = sort.substring(1);
        } else if (sort.startsWith("+")) {
            sortField = sort.substring(1);
        } else {
            sortField = sort;
        }

        Sort sorting = Sort.by(direction, sortField);
        Pageable pageable = PageRequest.of(offset, limit, sorting);

        return employeeRepository.findBySalaryBetween(minSalary, maxSalary, pageable);
    }
}

