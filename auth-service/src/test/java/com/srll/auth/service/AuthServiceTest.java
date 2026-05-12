package com.srll.auth.service;

import com.srll.auth.dto.AuthResponse;
import com.srll.auth.dto.LoginRequest;
import com.srll.auth.dto.RegisterRequest;
import com.srll.auth.entity.Role;
import com.srll.auth.entity.User;
import com.srll.auth.repository.UserRepository;
import com.srll.auth.security.JwtService;
import com.srll.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();
    }

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(), eq(1L))).thenReturn("jwt_token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt_token");
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getUserId()).isEqualTo(1L);
    }

    @Test
    void register_duplicateUsername_throwsBusinessException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Username already taken");
    }

    @Test
    void register_duplicateEmail_throwsBusinessException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser, 1L)).thenReturn("jwt_token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt_token");
        assertThat(response.getUsername()).isEqualTo("testuser");
    }
}
