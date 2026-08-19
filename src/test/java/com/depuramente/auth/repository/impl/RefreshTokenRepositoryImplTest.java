package com.depuramente.auth.repository.impl;

import com.depuramente.auth.model.RefreshToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenRepositoryImplTest {

    @Mock
    private DynamoDbTable<RefreshToken> table;

    @Mock
    private PageIterable<RefreshToken> pages;

    @Mock
    private SdkIterable<RefreshToken> items;

    @InjectMocks
    private RefreshTokenRepositoryImpl repository;

    @Test
    void saveDelegatesToTable() {
        RefreshToken token = token("id-1", "user-1", "value-1");

        repository.save(token);

        verify(table).putItem(token);
        verifyNoMoreInteractions(table);
    }

    @Test
    void findByIdReturnsTokenAndBuildsCompositeKey() {
        RefreshToken token = token("id-1", "user-1", "value-1");
        when(table.getItem(any(Key.class))).thenReturn(token);

        Optional<RefreshToken> result = repository.findById("user-1", "id-1");

        assertTrue(result.isPresent());
        assertSame(token, result.get());

        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
        verify(table).getItem(keyCaptor.capture());
        assertEquals("user-1", keyCaptor.getValue().partitionKeyValue().s());
        assertEquals("id-1", keyCaptor.getValue().sortKeyValue().orElseThrow().s());
        verifyNoMoreInteractions(table);
    }

    @Test
    void findByIdPropagatesNullTableResultAsNullPointerException() {
        when(table.getItem(any(Key.class))).thenReturn(null);

        assertThrows(NullPointerException.class,
                () -> repository.findById("user-1", "missing"));
        verify(table).getItem(any(Key.class));
        verifyNoMoreInteractions(table);
    }

    @Test
    void findByTokenReturnsMatchingToken() {
        RefreshToken matching = token("id-1", "user-1", "value-1");
        RefreshToken other = token("id-2", "user-2", "value-2");
        when(table.scan()).thenReturn(pages);
        when(pages.items()).thenReturn(items);
        when(items.stream()).thenReturn(Stream.of(other, matching));

        Optional<RefreshToken> result = repository.findByToken("value-1");

        assertTrue(result.isPresent());
        assertSame(matching, result.get());
        verify(table).scan();
        verify(pages).items();
        verify(items).stream();
        verifyNoMoreInteractions(table, pages, items);
    }

    @Test
    void findByTokenReturnsEmptyWhenTokenIsNotFound() {
        when(table.scan()).thenReturn(pages);
        when(pages.items()).thenReturn(items);
        when(items.stream()).thenReturn(Stream.of(token("id-1", "user-1", "value-1")));

        Optional<RefreshToken> result = repository.findByToken("missing");

        assertTrue(result.isEmpty());
        verify(table).scan();
        verify(pages).items();
        verify(items).stream();
        verifyNoMoreInteractions(table, pages, items);
    }

    @Test
    void findAllByUsernameReturnsItemsFromPartitionQuery() {
        List<RefreshToken> tokens = List.of(
                token("id-1", "user-1", "value-1"),
                token("id-2", "user-1", "value-2")
        );
        when(table.query(any(QueryEnhancedRequest.class))).thenReturn(pages);
        when(pages.items()).thenReturn(items);
        when(items.stream()).thenReturn(tokens.stream());

        List<RefreshToken> result = repository.findAllByUsername("user-1");

        assertEquals(tokens, result);

        ArgumentCaptor<QueryEnhancedRequest> requestCaptor =
                ArgumentCaptor.forClass(QueryEnhancedRequest.class);
        verify(table).query(requestCaptor.capture());
        assertNotNull(requestCaptor.getValue().queryConditional());
        verify(pages).items();
        verify(items).stream();
        verifyNoMoreInteractions(table, pages, items);
    }

    @Test
    void deleteDelegatesToTableWithCompositeKey() {
        repository.delete("user-1", "id-1");

        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
        verify(table).deleteItem(keyCaptor.capture());
        assertEquals("user-1", keyCaptor.getValue().partitionKeyValue().s());
        assertEquals("id-1", keyCaptor.getValue().sortKeyValue().orElseThrow().s());
        verifyNoMoreInteractions(table);
    }

    private static RefreshToken token(String id, String username, String value) {
        return new RefreshToken(id, username, value, Instant.parse("2030-01-01T00:00:00Z"), false);
    }
}
