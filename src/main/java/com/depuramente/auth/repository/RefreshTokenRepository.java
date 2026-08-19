package com.depuramente.auth.repository;

import com.depuramente.auth.model.RefreshToken;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository {

    void save(RefreshToken token);

    Optional<RefreshToken> findById(String username, String id);

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUsername(String username);

    void delete(String username, String id);

}
