package com.srll.javafx.http.dto;

public record ApiResponse<T>(boolean success, T data, String message) {}
