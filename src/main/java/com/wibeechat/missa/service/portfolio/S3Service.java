package com.wibeechat.missa.service.portfolio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class S3Service {

        private final S3Client s3Client;
        private final String bucketName;
        private final Region region;
        private final S3Presigner s3Presigner;

        public S3Service(@Value("${aws.s3.bucketName}") String bucketName) {
                // S3 클라이언트 초기화
                this.region = Region.AP_NORTHEAST_2;
                this.bucketName = bucketName;
                this.s3Client = S3Client.builder()
                                .region(region)
                                .credentialsProvider(DefaultCredentialsProvider.create())
                                .build();
                this.s3Presigner = S3Presigner.builder()
                                .region(region)
                                .build();
        }

        public String uploadFile(File file, String keyName) {
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(keyName)
                                .build();

                s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));

                Duration expiration = Duration.ofMinutes(15);
                // 파일 URL 생성
                String fileUrl = generatePresignedUrl(keyName, expiration);
                return fileUrl;
        }

        public String generatePresignedUrl(String keyName, Duration expiration) {
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                                .bucket(bucketName)
                                .key(keyName)
                                .build();

                GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                                .getObjectRequest(getObjectRequest)
                                .signatureDuration(expiration)
                                .build();

                return s3Presigner.presignGetObject(presignRequest).url().toString();
        }

        public List<String> getPortfolioUrlsForUser(String userId) {
                String prefix = "portfolios/" + userId + "/"; // S3 버킷 내 userId 기반 경로

                // S3에서 객체 목록 가져오기
                ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                                .bucket(bucketName)
                                .prefix(prefix)
                                .build();
                ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

                Duration expiration = Duration.ofMinutes(15);
                // S3 객체 키에서 URL 생성
                return listObjectsResponse.contents().stream()
                                .map(s3Object -> generatePresignedUrl(s3Object.key(), expiration)) // 프리사인드 URL 생성
                                .collect(Collectors.toList());
        }

}
