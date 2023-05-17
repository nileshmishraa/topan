package com.topan.web;

import com.topan.entity.Employee;
import com.topan.service.EmployeeService;
import com.topan.service.impl.UploadingServiceImpl;
import com.topan.util.CustomMultipartFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    @Test
    public void testUploadGoodFileWith5000Entries() throws IOException {
        // Prepare a mock MultipartFile object with a good file containing 5000 entries
        MultipartFile file = new CustomMultipartFile("good_file.csv", "good_file","text/csv", generateCSVContent(5000).readAllBytes()) {
        };

        // Mock the uploadingService to return a success response
        Mockito.when(uploadingService.upload(file)).thenReturn(ResponseEntity.ok("File uploaded successfully."));

        // Invoke the uploadEmployees method
        ResponseEntity<String> response = employeeController.uploadEmployees(file);

        // Assert the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("File uploaded successfully.", response.getBody());
    }

    private InputStream generateCSVContent(int numEntries) {
        // Generate CSV content with the specified number of entries
        StringBuilder content = new StringBuilder();
        content.append("id,name,login,salary\n");
        for (int i = 1; i <= numEntries; i++) {
            content.append("e").append(String.format("%04d", i)).append(",John Doe,John,").append(i * 1000).append("\n");
        }
        return new ByteArrayInputStream(content.toString().getBytes());
    }

    @Test
    public void testConcurrentUploadsOfFilesWith5000Entries() throws InterruptedException {
        MultipartFile file1 = new MockMultipartFile("file", "file1.csv", "text/csv", "CSV content 1".getBytes());
        MultipartFile file2 = new MockMultipartFile("file", "file2.csv", "text/csv", "CSV content 2".getBytes());

        Mockito.when(uploadingService.upload(Mockito.any(MultipartFile.class))).thenReturn(ResponseEntity.ok("File uploaded successfully."));

        // Create an ExecutorService with two threads
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // Submit two tasks to upload the files concurrently
        executorService.submit(() -> employeeController.uploadEmployees(file1));
        executorService.submit(() -> employeeController.uploadEmployees(file2));

        // Shutdown the ExecutorService and wait for the tasks to complete
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        // Verify that both uploads completed successfully
        Mockito.verify(uploadingService, Mockito.times(2)).upload(Mockito.any(MultipartFile.class));
    }

    @Test
    public void testConcurrentUploadsOfFilesWith5000Entries_1() throws InterruptedException {
        CustomMultipartFile file1 = createCustomMultipartFile("file1.csv", "CSV content 1");
        CustomMultipartFile file2 = createCustomMultipartFile("file2.csv", "CSV content 2");

        Mockito.when(uploadingService.upload(Mockito.any(MultipartFile.class))).thenReturn(ResponseEntity.ok("File uploaded successfully."));

        // Create an ExecutorService with two threads
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // Submit two tasks to upload the files concurrently
        executorService.submit(() -> employeeController.uploadEmployees(file1));
        executorService.submit(() -> employeeController.uploadEmployees(file2));

        // Shutdown the ExecutorService and wait for the tasks to complete
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        // Verify that both uploads completed successfully
        Mockito.verify(uploadingService, Mockito.times(2)).upload(Mockito.any(MultipartFile.class));
    }

    private CustomMultipartFile createCustomMultipartFile(String filename, String content) {
        return new CustomMultipartFile(filename, filename,"text/csv",content.getBytes());
    }

}
