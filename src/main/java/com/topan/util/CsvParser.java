package com.topan.util;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.topan.entity.Employee;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CsvParser {

    public static List<Employee> parse(InputStream inputStream) throws IOException, CsvValidationException {
        List<Employee> employees = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String[] header = reader.readNext(); // Ignore the header line

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length != 4) {
                    throw new IOException("Invalid CSV format. Each line should have 4 columns.");
                }
                String id = line[0];
                String login = line[1];
                String name = line[2];
                BigDecimal salary;

                try {
                    salary = new BigDecimal(line[3]);
                    if (salary.compareTo(BigDecimal.ZERO) < 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid salary format. Salary must be a decimal value >= 0.0.");
                }

                employees.add(new Employee(id, login, decodeUTF8(name), salary));
            }
        }

        return employees;
    }

    private static String decodeUTF8(String value) {
        try {
            return new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
}



