package com.depuramente.auth.repository;

import com.depuramente.auth.model.DPMUser;
import java.util.Optional;

public interface UserRepository {

    Optional<DPMUser> findById(String email);

    void save(DPMUser user);

    void delete(String email);
}
