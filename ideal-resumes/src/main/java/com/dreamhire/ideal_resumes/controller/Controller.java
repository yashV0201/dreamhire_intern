package com.dreamhire.ideal_resumes.controller;

import com.dreamhire.ideal_resumes.services.IdealResumeServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("profile/{name}")
@RequiredArgsConstructor
public class Controller {

    private final IdealResumeServices idealResumeServices;

    @PostMapping("upload-cv")
    public ResponseEntity<String> uploadIdealResume(@PathVariable String name, @RequestParam("file") List<MultipartFile> fileList) throws IOException {
        return idealResumeServices.uploadIdealResume(name,fileList);
    }
}
