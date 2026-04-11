package com.cleancity.backend.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${AWS_REGION:${aws.region:ap-south-1}}")
    private String fallbackRegion;

    @Value("${AWS_ACCESS_KEY_ID:${aws.access-key-id:}}")
    private String fallbackAccessKeyId;

    @Value("${AWS_SECRET_ACCESS_KEY:${aws.secret-access-key:}}")
    private String fallbackSecretAccessKey;

    @Bean
    public S3Client s3Client() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        
        String region = dotenv.get("AWS_REGION", fallbackRegion);
        String accessKey = dotenv.get("AWS_ACCESS_KEY_ID", fallbackAccessKeyId);
        String secretKey = dotenv.get("AWS_SECRET_ACCESS_KEY", fallbackSecretAccessKey);

        System.out.println("=================================================");
        System.out.println("INITIALIZING S3 CLIENT VIA DOTENV");
        System.out.println("AWS_REGION: " + region);
        System.out.println("AWS_ACCESS_KEY_ID LOADED: " + accessKey);
        System.out.println("=================================================");
        
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}
