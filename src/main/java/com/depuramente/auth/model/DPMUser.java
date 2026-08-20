package com.depuramente.auth.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.Instant;
import java.util.Set;

/** DynamoDB representation of a registered user and authorization roles. */
@DynamoDbBean
public class DPMUser {

    public static final String tableName = "dpm-users";

    private String username;
    private String alias;
    private String password;
    private Set<DPMRole> roles;
    private Instant createdAt;
    private Instant updatedAt;

    public DPMUser() {
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @DynamoDbAttribute("username")
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @DynamoDbAttribute("password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @DynamoDbAttribute("roles")
    public Set<DPMRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<DPMRole> roles) {
        this.roles = roles;
    }

    @DynamoDbAttribute("created_at")
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @DynamoDbAttribute("updated_at")
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
