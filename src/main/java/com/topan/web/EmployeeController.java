package com.topan.web;

import com.topan.entity.Employee;
import com.topan.service.EmployeeService;
import com.topan.service.UploadingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/users")
public class EmployeeController {

    private final UploadingService uploadingService;
    private final EmployeeService employeeService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadEmployees(@RequestParam("file") MultipartFile file) {
        return uploadingService.upload(file);
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getEmployees(
            @RequestParam BigDecimal minSalary,
            @RequestParam BigDecimal maxSalary,
            @RequestParam int offset,
            @RequestParam(defaultValue = "30") int limit,
            @RequestParam(defaultValue = "+id") String sort
    ) {
        List<Employee> employees = employeeService.getEmployees(minSalary, maxSalary, offset, limit, sort);
        if (null != employees)
            return ResponseEntity.ok(employees);
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}


