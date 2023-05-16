package com.topan.util;

import com.opencsv.exceptions.CsvValidationException;
import com.topan.entity.Employee;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CsvParserTest {

    @Test
    public void testParse_ValidCsv_ReturnsListOfEmployees() throws CsvValidationException, IOException {
        // Arrange
        String csvData = """
                id,login,name,salary
                1,nileshm,Nilesh M,5000.00
                2,chrisbrown,Chris Brown ,6000.50""";


        InputStream inputStream = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));
        List<Employee> employees = CsvParser.parse(inputStream);
        assertEquals(2, employees.size());

        Employee employee1 = employees.get(0);
        assertEquals("1", employee1.getId());
        assertEquals("nileshm", employee1.getLogin());
        assertEquals("Nilesh M", employee1.getName());
        assertEquals(new BigDecimal("5000.00"), employee1.getSalary());

        Employee employee2 = employees.get(1);
        assertEquals("2", employee2.getId());
        assertEquals("chrisbrown", employee2.getLogin());
        assertEquals("Chris Brown ", employee2.getName());
        assertEquals(new BigDecimal("6000.50"), employee2.getSalary());
    }


    @Test
    public void testParse_InvalidCsvFormat_ThrowsIOException() {
        // Arrange
        String csvData = "id,login,name\n" + // Invalid format, missing salary column
                "1,nileshm,Nilesh M";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvData.getBytes());

        assertThrows(IOException.class, () -> CsvParser.parse(inputStream));
    }


    @Test
    public void testParse_InvalidSalaryFormat_ThrowsIOException() {
        String csvData = "id,login,name,salary\n" +
                "1,nileshm,Nilesh M,invalid"; // Invalid salary format
        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvData.getBytes());
        assertThrows(IOException.class, () -> CsvParser.parse(inputStream));
    }
}
