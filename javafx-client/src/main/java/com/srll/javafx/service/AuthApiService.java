package com.srll.javafx.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.srll.javafx.http.ApiClient;
import com.srll.javafx.http.dto.ApiResponse;
import com.srll.javafx.http.dto.AuthResponse;

import java.util.Map;

public class AuthApiService {

    public AuthResponse login(String username, String password) {
        Map<String, String> body = Map.of(
                "username", username,
                "password", password
        );
        ApiResponse<AuthResponse> response = ApiClient.post(
                "/api/auth/login",
                body,
                new TypeReference<>() {}
        );
        return response.data();
    }

    public AuthResponse register(String username, String email, String password) {
        Map<String, String> body = Map.of(
                "username", username,
                "email",    email,
                "password", password
        );
        ApiResponse<AuthResponse> response = ApiClient.post(
                "/api/auth/register",
                body,
                new TypeReference<>() {}
        );
        return response.data();
    }
}
