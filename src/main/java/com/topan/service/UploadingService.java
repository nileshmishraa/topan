package com.topan.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface UploadingService {
    ResponseEntity<String> upload(MultipartFile file);
}
