package com.depuramente.auth.config;

import com.depuramente.auth.model.DPMUser;
import com.depuramente.auth.model.RefreshToken;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

/** Creates AWS clients and typed DynamoDB tables used by the service. */
@EnableConfigurationProperties(AwsProperties.class)
@Configuration
public class DynamoDBConfig {

    private final AwsProperties awsProperties;

    public DynamoDBConfig(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
    }

    @Bean
    @Profile("local")
    public DynamoDbClient dynamoDbLocalClient() {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(awsProperties.endpoint()))
                .region(Region.of(awsProperties.region()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("dummy", "dummy")
                        )
                )
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient enhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    @Bean
    public DynamoDbTable<DPMUser> appUserTable() {
        return enhancedClient(dynamoDbLocalClient()).table(DPMUser.tableName, TableSchema.fromBean(DPMUser.class));
    }

    @Bean
    public DynamoDbTable<RefreshToken> refreshTokenTable() {
        return enhancedClient(dynamoDbLocalClient()).table(RefreshToken.tableName, TableSchema.fromBean(RefreshToken.class));
    }
}
