package com.depuramente.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AWS endpoint settings used by the DynamoDB client.
 * @param region AWS region name
 * @param endpoint optional endpoint override, such as DynamoDB Local
 */
@ConfigurationProperties(prefix = "aws")
public record AwsProperties(
        String region,
        String endpoint
) {
}
