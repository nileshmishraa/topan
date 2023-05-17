package com.topan.web;

import com.topan.entity.Employee;
import com.topan.service.EmployeeService;
import com.topan.service.impl.UploadingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class EmployeeControllerTest {

    @Mock
    private UploadingServiceImpl uploadingService;
    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUploadEmployees_ValidFile_ReturnsSuccessResponse() {
        // Mock the file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "employees.csv",
                MediaType.TEXT_PLAIN_VALUE,
                "id,login,name,salary\n1,johndoe,John Doe,5000.0".getBytes()
        );

        // Mock the response from the uploading service
        ResponseEntity<String> expectedResponse = ResponseEntity.ok("File uploaded successfully");
        when(uploadingService.upload(file)).thenReturn(expectedResponse);

        // Make the request to the controller
        ResponseEntity<String> response = employeeController.uploadEmployees(file);

        // Verify the uploading service was called
        verify(uploadingService, times(1)).upload(file);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("File uploaded successfully", response.getBody());
    }

    @Test
    public void testUploadEmployees_InvalidFileFormat_ReturnsErrorResponse() {
        // Mock the file with invalid format
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "employees.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "id,login,name,salary\n1,johndoe,John Doe,5000.0".getBytes()
        );

        // Mock the response from the uploading service
        ResponseEntity<String> expectedResponse = ResponseEntity.badRequest().body("Invalid file format. Please upload a CSV file.");
        when(uploadingService.upload(file)).thenReturn(expectedResponse);

        // Make the request to the controller
        ResponseEntity<String> response = employeeController.uploadEmployees(file);

        // Verify the uploading service was called
        verify(uploadingService, times(1)).upload(file);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid file format. Please upload a CSV file.", response.getBody());
    }

    @Test
    public void testGetUsers_ReturnsListOfEmployees() {
        // Mock employee data
        Employee employee1 = new Employee("e0001", "John", "john", new BigDecimal("1000.00"));
        Employee employee2 = new Employee("e0002", "Jane", "jane", new BigDecimal("2000.00"));
        List<Employee> employees = List.of(employee1, employee2);

        // Mock service method
        when(employeeService.getEmployees(any(BigDecimal.class), any(BigDecimal.class), anyInt(), anyInt(), anyString()))
                .thenReturn(employees);

        // Call the controller method
        ResponseEntity<List<Employee>> response = employeeController.getEmployees(new BigDecimal("0"), new BigDecimal("4000"), 0, 30, "+name");

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(employees, response.getBody());
    }

    @Test
    public void testGetUsers_ReturnsNotFoundWhenServiceReturnsNull() {
        // Mock service method returning null
        when(employeeService.getEmployees(any(BigDecimal.class), any(BigDecimal.class), anyInt(), anyInt(), anyString()))
                .thenReturn(null);

        // Call the controller method
        ResponseEntity<List<Employee>> response = employeeController.getEmployees(new BigDecimal("0"), new BigDecimal("4000"), 0, 30, "+name");

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
