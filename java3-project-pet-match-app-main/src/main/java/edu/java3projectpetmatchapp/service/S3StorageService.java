package edu.java3projectpetmatchapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID; // Used for unique naming
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class S3StorageService {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    @Value("${aws.region}")
    private String awsRegion;

    private static final String DEFAULT_USER_PHOTO_KEY = "profile_default.png";
    private static final String DEFAULT_PET_PHOTO_KEY = "pet_default.png";

    private final S3Client s3Client;

    public S3StorageService(@Value("${aws.region}") String awsRegion) {
        this.s3Client = S3Client.builder()
                .region(Region.of(awsRegion))
                .build();
    }

    // For user profile photos
    public String uploadUserPhoto(MultipartFile file) throws IOException {
        return uploadFileInternal(file, "user-photos/");
    }

    // For pet photos
    public String uploadPetPhoto(MultipartFile file) throws IOException {
        return uploadFileInternal(file, "pet-photos/");
    }

    // Internal shared logic for uploading
    private String uploadFileInternal(MultipartFile file, String pathPrefix) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file.");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if(originalFilename != null && originalFilename.lastIndexOf(".") != -1) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFileName = pathPrefix + UUID.randomUUID() + fileExtension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uniqueFileName)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                awsRegion,
                uniqueFileName);
    }

    // Deleting Old Photo
    public void deleteFileFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty() || fileUrl.contains(DEFAULT_USER_PHOTO_KEY) || fileUrl.contains(DEFAULT_PET_PHOTO_KEY)) {
            return;
        }

        // Extract the file key from the URL
        String keyToDelete = extractKeyFromUrl(fileUrl);
        if(keyToDelete == null || keyToDelete.isEmpty()) {
            return;
        }

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(keyToDelete)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    private String extractKeyFromUrl(String fileUrl) {
        String patternString = "https://[^/]+/([^/].*)";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(fileUrl);

        if(matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public String getDefaultUserPhotoUrl() {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                awsRegion,
                DEFAULT_USER_PHOTO_KEY);
    }

    public String getDefaultPetPhotoUrl() {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                awsRegion,
                DEFAULT_PET_PHOTO_KEY);
    }
}