package com.srll.auth.controller;

import com.srll.auth.entity.User;
import com.srll.auth.repository.UserRepository;
import com.srll.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Map<Long, String>>> getUsernames(@RequestBody List<Long> userIds) {
        Map<Long, String> result = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
