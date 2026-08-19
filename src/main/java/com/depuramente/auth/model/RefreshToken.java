package com.depuramente.auth.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;

@DynamoDbBean
public class RefreshToken {

    public static String tableName = "dpm-refresh-tokens";

    private String id;
    private String username;
    private String token;
    private Instant expiresAt;
    private boolean revoked;

    public RefreshToken() {
    }

    public RefreshToken(String id, String username, String token, Instant expiresAt, boolean revoked) {
        this.id = id;
        this.username = username;
        this.token = token;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    @DynamoDbSortKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDbPartitionKey
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @DynamoDbAttribute("refresh_token")
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @DynamoDbAttribute("expires_at")
    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    @DynamoDbAttribute("is_revoked")
    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
}
