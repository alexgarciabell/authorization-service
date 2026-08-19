package com.depuramente.auth.repository.impl;

import com.depuramente.auth.model.RefreshToken;
import com.depuramente.auth.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;

import java.util.List;
import java.util.Optional;

@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    @Autowired
    private DynamoDbTable<RefreshToken> table;
    @Autowired
    private DynamoDbEnhancedClient client;

    @Override
    public void save(RefreshToken token) {
        table.putItem(token);
    }

    @Override
    public Optional<RefreshToken> findById(String username, String id) {
        return Optional.of(table.getItem(buildKey(username, id)));
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return table.scan()
                .items()
                .stream()
                .filter(rt -> rt.getToken().equals(token))
                .findAny();
    }

    @Override
    public List<RefreshToken> findAllByUsername(String username) {
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(buildKey(username)))
                .build();
        return table.query(request)
                .items()
                .stream()
                .toList();
    }

    @Override
    public void delete(String username, String id) {
        table.deleteItem(buildKey(username, id));
    }

    private Key buildKey(String username, String id) {
        return Key.builder()
                .partitionValue(username)
                .sortValue(id)
                .build();
    }

    private Key buildKey(String username) {
        return Key.builder()
                .partitionValue(username)
                .build();
    }
}
