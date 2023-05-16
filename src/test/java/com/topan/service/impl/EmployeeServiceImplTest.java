package com.topan.service.impl;


import com.topan.entity.Employee;
import com.topan.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    public void testFindDuplicateIds_NoDuplicates_ReturnsEmptyList() {
        // Prepare test data
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("1", "john", "John Doe", new BigDecimal("5000.0")));
        employees.add(new Employee("2", "jane", "Jane Smith", new BigDecimal("6000.0")));

        // Mock the repository behavior
        when(employeeRepository.existsById("1")).thenReturn(false);
        when(employeeRepository.existsById("2")).thenReturn(false);

        // Perform the operation
        List<String> duplicateIds = employeeService.findDuplicateIds(employees);

        // Verify the result
        assertTrue(duplicateIds.isEmpty());
    }

    @Test
    public void testFindDuplicateIds_WithDuplicates_ReturnsDuplicateIdsList() {
        // Prepare test data
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("1", "john", "John Doe", new BigDecimal("5000.0")));
        employees.add(new Employee("2", "jane", "Jane Smith", new BigDecimal("6000.0")));
        employees.add(new Employee("1", "alice", "Alice Johnson", new BigDecimal("7000.0")));
        when(employeeRepository.existsById("1")).thenReturn(true);
        List<String> duplicateIds = employeeService.findDuplicateIds(employees);
        assertEquals(2, duplicateIds.size());
        assertTrue(duplicateIds.contains("1"));
    }

    @Test
    public void testFindByEmployeeId_ExistingEmployee_ReturnsEmployee() {
        Employee employee = new Employee("1", "john", "John Doe", new BigDecimal("5000.0"));
        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));
        Employee result = employeeService.findByEmployeeId("1");
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("john", result.getLogin());
        assertEquals("John Doe", result.getName());
        assertEquals(new BigDecimal("5000.0"), result.getSalary());
    }

    @Test
    public void testFindByEmployeeId_NonExistingEmployee_ThrowsNoSuchElementException() {
        when(employeeRepository.findById("1")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> employeeService.findByEmployeeId("1"));
    }


}
