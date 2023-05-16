package com.topan.web;

import com.topan.service.UploadingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/users")
public class EmployeeController {

    private final UploadingService uploadingService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadEmployees(@RequestParam("file") MultipartFile file) {
        return uploadingService.upload(file);
    }


}


