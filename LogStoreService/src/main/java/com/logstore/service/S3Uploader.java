package com.logstore.service;

import java.io.File;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Uploader {

	private final S3Client s3Client;

	@Value("${aws.s3.bucket}")
	private String bucketName;

	public S3Uploader(@Value("${aws.access.key}") String accessKey, @Value("${aws.secret.key}") String secretKey) {

		AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
		this.s3Client = S3Client.builder().region(Region.EU_NORTH_1)
				.credentialsProvider(StaticCredentialsProvider.create(awsCredentials)).build();
	}

	public void uploadToS3(File file) {
		String key = "logs/" + file.getName();
		PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(key).build();
		s3Client.putObject(putObjectRequest, RequestBody.fromFile(Paths.get(file.getAbsolutePath())));

		// Delete file after upload
//		file.delete();
	}
}
