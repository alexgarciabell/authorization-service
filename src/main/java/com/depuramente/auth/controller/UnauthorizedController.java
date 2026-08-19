package com.depuramente.auth.controller;

import com.depuramente.auth.dto.AuthRequest;
import com.depuramente.auth.model.DPMUser;
import com.depuramente.auth.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UnauthorizedController {
    private final UserRepository repository;

    public UnauthorizedController(UserRepository repository) {
        this.repository = repository;

    }

    @PostMapping("/user")
    public ResponseEntity<DPMUser> getUsers(@RequestBody AuthRequest request) {
        var response = repository.findById(request.id());
        return ResponseEntity.ok(response.orElse(null));
    }
}
