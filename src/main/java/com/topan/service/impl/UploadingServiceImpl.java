package com.topan.service.impl;

import com.topan.entity.Employee;
import com.topan.service.UploadingService;
import com.topan.util.CsvParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadingServiceImpl implements UploadingService {

    private final EmployeeServiceImpl employeeService;

    private final Set<String> uploadingFiles = new HashSet<>();

    /**
     * @param file File which user uploads
     */
    @Override
    public ResponseEntity<String> upload(MultipartFile file) {
        try {
            if (file.getBytes().length == 0)
                return ResponseEntity.badRequest().body("File empty");

            // Check if another upload is already in progress
            if (!uploadingFiles.add(file.getOriginalFilename())) {
                return ResponseEntity.badRequest().body("Another upload is already in progress. Please try again later.");
            }

            // Validate file format
            if (!Objects.equals(file.getContentType(), "text/csv")) {
                uploadingFiles.remove(file.getOriginalFilename());
                return ResponseEntity.badRequest().body("Invalid file format. Please upload a CSV file.");
            }

            // Read file bytes with UTF-8 encoding
            byte[] fileBytes = file.getBytes();
            String fileContent = new String(fileBytes, StandardCharsets.UTF_8);

            // Parse CSV file and process data
            List<Employee> employees = CsvParser.parse(new ByteArrayInputStream(fileContent.getBytes()));

            // Check if any row fails validation
            if (!validateEmployees(employees)) {
                uploadingFiles.remove(file.getOriginalFilename());
                return ResponseEntity.badRequest().body("One or more rows fail validation. The entire file is rejected.");
            }

            uploadTransitionally(employees);

            uploadingFiles.remove(file.getOriginalFilename());
            return ResponseEntity.ok("File uploaded successfully.");
        } catch (IOException e) {
            uploadingFiles.remove(file.getOriginalFilename());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing the file.");
        } catch (Exception e) {
            uploadingFiles.remove(file.getOriginalFilename());
            throw new RuntimeException(e);
        }
    }

    @Transactional
    void uploadTransitionally(List<Employee> employees) {
        for (Employee employee : employees) {
            Employee existingEmployee = employeeService.findByEmployeeId(employee.getId());
            if (null != existingEmployee) {
                // Update existing employee entry
                existingEmployee.setLogin(employee.getLogin());
                existingEmployee.setName(employee.getName());
                existingEmployee.setSalary(employee.getSalary());
            } else employeeService.saveEmployee(employee);
        }
    }

    private boolean validateEmployees(List<Employee> employees) {
        Set<String> uniqueIds = new HashSet<>();
        Set<String> uniqueLogins = new HashSet<>();

        for (Employee employee : employees) {
            // Ignore comment rows
            if (employee.getId().startsWith("#")) {
                continue;
            }

            // Check if all columns are filled
            if (isEmpty(employee.getId()) || isEmpty(employee.getLogin())
                    || isEmpty(employee.getName()) || employee.getSalary() == null) {
                return false;
            }

            // Check for duplicate IDs and logins
            if (!uniqueIds.add(employee.getId()) || !uniqueLogins.add(employee.getLogin())) {
                return false;
            }
        }

        return true;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
