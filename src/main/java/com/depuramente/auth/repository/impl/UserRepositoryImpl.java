package com.depuramente.auth.repository.impl;

import com.depuramente.auth.model.DPMUser;
import com.depuramente.auth.repository.UserRepository;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final DynamoDbTable<DPMUser> table;

    public UserRepositoryImpl(DynamoDbTable<DPMUser> table) {
        this.table = table;
    }

    @Override
    public Optional<DPMUser> findById(String email) {
        return Optional.ofNullable(table.getItem(r -> r.key(k -> k.partitionValue(email))));
    }

    @Override
    public void save(DPMUser user) {
        table.putItem(user);
    }

    @Override
    public void delete(String email) {
        table.deleteItem(r -> r.key(k -> k.partitionValue(email)));
    }
}
