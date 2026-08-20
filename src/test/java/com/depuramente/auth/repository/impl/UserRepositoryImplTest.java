package com.depuramente.auth.repository.impl;

import com.depuramente.auth.model.DPMUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    @Mock
    private DynamoDbTable<DPMUser> table;

    @InjectMocks
    private UserRepositoryImpl repository;

    @Test
    void findByIdReturnsUserAndBuildsPartitionKeyRequest() {
        DPMUser user = user("user@example.com");
        when(table.getItem(any(Consumer.class))).thenReturn(user);

        Optional<DPMUser> result = repository.findById("user@example.com");

        assertTrue(result.isPresent());
        assertSame(user, result.get());
        ArgumentCaptor<Consumer<GetItemEnhancedRequest.Builder>> captor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(table).getItem(captor.capture());
        GetItemEnhancedRequest.Builder requestBuilder = GetItemEnhancedRequest.builder();
        captor.getValue().accept(requestBuilder);
        GetItemEnhancedRequest request = requestBuilder.build();
        assertEquals("user@example.com", request.key().partitionKeyValue().s());
        verifyNoMoreInteractions(table);
    }

    @Test
    void findByIdReturnsEmptyWhenUserDoesNotExist() {
        when(table.getItem(any(Consumer.class))).thenReturn(null);

        Optional<DPMUser> result = repository.findById("missing@example.com");

        assertTrue(result.isEmpty());
        verify(table).getItem(any(Consumer.class));
    }

    @Test
    void saveDelegatesUserToTable() {
        DPMUser user = user("user@example.com");

        repository.save(user);

        verify(table).putItem(user);
        verifyNoMoreInteractions(table);
    }

    @Test
    void deleteBuildsPartitionKeyRequest() {
        repository.delete("user@example.com");

        ArgumentCaptor<Consumer<DeleteItemEnhancedRequest.Builder>> captor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(table).deleteItem(captor.capture());
        DeleteItemEnhancedRequest.Builder requestBuilder = DeleteItemEnhancedRequest.builder();
        captor.getValue().accept(requestBuilder);
        DeleteItemEnhancedRequest request = requestBuilder.build();
        assertEquals("user@example.com", request.key().partitionKeyValue().s());
        verifyNoMoreInteractions(table);
    }

    private static DPMUser user(String username) {
        DPMUser user = new DPMUser();
        user.setUsername(username);
        return user;
    }
}
