package com.campushub.controller;

import com.campushub.entity.UserVerification;
import com.campushub.service.VerificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/me")
public class VerificationController {

    private final VerificationService verificationService;

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    private Path uploadBasePath;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostConstruct
    public void init() {
        Path path = Paths.get(uploadDir);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(uploadDir);
        }
        this.uploadBasePath = path.normalize().toAbsolutePath();
    }

    @PostMapping("/verification")
    public UserVerification submitVerification(
        @RequestParam("images") List<MultipartFile> images,
        @RequestParam("realName") String realName,
        @RequestParam("studentId") String studentId,
        Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);

        if (images == null || images.isEmpty()) {
            throw new RuntimeException("请至少上传一张证件图片");
        }
        if (images.size() > 3) {
            throw new RuntimeException("最多上传3张图片");
        }

        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                throw new RuntimeException("上传文件不能为空");
            }
            String contentType = image.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                throw new RuntimeException("仅支持 JPG 和 PNG 格式的图片");
            }
            if (image.getSize() > 5 * 1024 * 1024) {
                throw new RuntimeException("图片大小不能超过 5MB");
            }
        }

        List<String> imageUrls = new ArrayList<>();
        try {
            Path userDir = uploadBasePath.resolve("verifications").resolve(String.valueOf(userId));
            Files.createDirectories(userDir);

            for (MultipartFile image : images) {
                String originalName = image.getOriginalFilename();
                String ext = ".jpg";
                if (originalName != null && originalName.contains(".")) {
                    ext = originalName.substring(originalName.lastIndexOf("."));
                }
                String filename = UUID.randomUUID().toString() + ext;
                Path filePath = userDir.resolve(filename);
                image.transferTo(filePath.toFile());
                imageUrls.add(filename);
            }
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败：" + e.getMessage());
        }

        return verificationService.submit(userId, realName, studentId, imageUrls);
    }

    @GetMapping("/verification")
    public UserVerification getMyVerification(Authentication authentication) {
        return verificationService.getMyVerification(getCurrentUserId(authentication));
    }

    @GetMapping("/verification/images/{filename}")
    public ResponseEntity<Resource> getVerificationImage(
        @PathVariable String filename,
        Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        var filePath = uploadBasePath.resolve("verifications").resolve(String.valueOf(userId)).resolve(filename);
        Resource resource = new FileSystemResource(filePath);
        if (!resource.exists()) {
            throw new RuntimeException("图片文件不存在");
        }
        String contentType = filename.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
            .body(resource);
    }

    private Long getCurrentUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
