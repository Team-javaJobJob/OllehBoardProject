package com.example.ollethboardproject.service;

import com.example.ollethboardproject.domain.entity.Community;
import com.example.ollethboardproject.domain.entity.Image;
import com.example.ollethboardproject.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;
    @Value("${upload.path}")
    private String uploadPath;

    // local 절대 경로 이미지 저장 test
    public Image save(MultipartFile file, Community community) throws Exception {
        // 오리지날 네임 저장할지 여부
        String fileName = getUniqueFileName(file);
        // 저장할 파일 경로 생성
        Path filePath = Path.of(uploadPath, fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        // 데이터베이스에 이미지 정보 저장
        Image image = Image.of(fileName, filePath.toString(), community);
        imageRepository.save(image);
        return image;
    }

    public void deleteImageByCommunity(Community community) {
        imageRepository.delete(imageRepository.findByCommunityId(community.getId()));
    }

    // 고유한 파일명 추출
    public String getUniqueFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        return UUID.randomUUID() + "." + extension;
    }

}