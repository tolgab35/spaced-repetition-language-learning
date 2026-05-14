package com.srll.javafx.http.dto;

public record AuthResponse(String token, Long userId, String username, String role) {}
