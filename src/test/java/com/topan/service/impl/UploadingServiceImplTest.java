package com.topan.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.topan.entity.Employee;
import com.topan.repository.EmployeeRepository;
import com.topan.service.EmployeeService;
import com.topan.util.CsvParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UploadingServiceImplTest {
    @Mock
    private EmployeeService employeeService;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private UploadingServiceImpl uploadingService;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Disabled
    public void testUpload_ValidFile_ReturnsSuccessResponse() throws IOException, CsvValidationException {
        var file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("employees.csv");
        when(file.getContentType()).thenReturn("text/csv");

        String fileContent = """
                id,login,name,salary
                1,john,john doe,1000.0
                2,jane,jane doe,2000.0
                """;
        byte[] fileBytes = fileContent.getBytes(StandardCharsets.UTF_8);
        when(file.getBytes()).thenReturn(fileBytes);

        var employees = Arrays.asList(
                new Employee("1", "john", "john doe", new BigDecimal("1000.0")),
                new Employee("2", "jane", "jane doe", new BigDecimal("2000.0"))
        );

        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
        CsvParser.parse(inputStream);

        when(CsvParser.parse(any())).thenReturn(employees);
        when(employeeService.findByEmployeeId("1")).thenReturn(null);
        when(employeeService.findByEmployeeId("2")).thenReturn(null);

        ResponseEntity<String> response = uploadingService.upload(file);

        verify(employeeService, times(2)).findByEmployeeId(anyString());
        verify(employeeService, times(2)).saveEmployee(any(Employee.class));

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals("File uploaded successfully.", response.getBody());
    }

    @Test
    public void testUpload_InvalidFileFormat_ReturnsErrorResponse() throws IOException {
        // Mock the MultipartFile
        MultipartFile file = mock(MultipartFile.class);
        byte[] fileBytes = "Test file content".getBytes();
        when(file.getBytes()).thenReturn(fileBytes);
        when(file.getOriginalFilename()).thenReturn("employees.csv");
        when(file.getContentType()).thenReturn("application/octet-stream");

        // Invoke the upload method
        ResponseEntity<String> response = uploadingService.upload(file);

        // Assert the response
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assert.assertEquals("Invalid file format. Please upload a CSV file.", response.getBody());

        // Make sure employeeService methods were not invoked
        verify(employeeService, never()).findByEmployeeId(anyString());
        verify(employeeService, never()).saveEmployee(any(Employee.class));
    }

    @Test
    public void testUpload_EmptyFile_ReturnsErrorResponse() {
        // Create an empty file
        MultipartFile file = new MockMultipartFile("employees.csv",
                "employees.csv",
                "text/csv",
                "".getBytes(StandardCharsets.UTF_8));

        // Perform upload
        ResponseEntity<String> response = uploadingService.upload(file);

        // Verify repository interaction
        verifyNoInteractions(employeeRepository);

        // Verify response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("File empty", response.getBody());
    }


    //testing new

    @Test
    public void parse_ValidInputStream_ReturnsListOfEmployees() throws IOException, CsvValidationException {
        // Mocked input data
        String csvData = "id,login,name,salary\n" +
                "1,johndoe,John Doe,10000.0\n" +
                "2,janesmith,Jane Smith,15000.0";

        // Create the input stream from the CSV data
        InputStream inputStream = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));

        // Mock the CSVReader and its behavior
        CSVReader mockedCsvReader = mock(CSVReader.class);


        Mockito.when(mockedCsvReader.readNext())
                .thenReturn(new String[]{"id", "login", "name", "salary"}) // Ignore the header line
                .thenReturn(new String[]{"1", "johndoe", "John Doe", "10000.0"})
                .thenReturn(new String[]{"2", "janesmith", "Jane Smith", "15000.0"})
                .thenReturn(null); // End of file

        List<Employee> employees = CsvParser.parse(inputStream);

        assertEquals(2, employees.size());

        Employee firstEmployee = employees.get(0);
        assertEquals("1", firstEmployee.getId());
        assertEquals("johndoe", firstEmployee.getLogin());
        assertEquals("John Doe", firstEmployee.getName());
        assertEquals(new BigDecimal("10000.0"), firstEmployee.getSalary());

        Employee secondEmployee = employees.get(1);
        assertEquals("2", secondEmployee.getId());
        assertEquals("janesmith", secondEmployee.getLogin());
        assertEquals("Jane Smith", secondEmployee.getName());
        assertEquals(new BigDecimal("15000.0"), secondEmployee.getSalary());
    }

    @Test
    public void parse_InvalidCsvFormat_ThrowsIOException() {
        // Mocked input data with invalid format (missing a column)
        String csvData = """
                id,login,name
                1,johndoe,John Doe
                2,janesmith,Jane Smith""";
        InputStream inputStream = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));
        assertThrows(IOException.class, () -> CsvParser.parse(inputStream));
    }

    @Test
    public void parse_InvalidSalaryFormat_ThrowsIOException() {
        // Mocked input data with invalid salary format (non-numeric value)
        String csvData = """
                id,login,name,salary
                1,johndoe,John Doe,10000.0
                2,janesmith,Jane Smith,invalid""";

        // Create the input stream from the CSV data
        InputStream inputStream = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));

        // Call the method under test and assert that it throws IOException
        assertThrows(IOException.class, () -> CsvParser.parse(inputStream));
    }
}
