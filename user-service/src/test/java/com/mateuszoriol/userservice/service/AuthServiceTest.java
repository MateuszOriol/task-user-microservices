package com.mateuszoriol.userservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.mateuszoriol.userservice.dto.AuthResponse;
import com.mateuszoriol.userservice.dto.LoginRequest;
import com.mateuszoriol.userservice.entity.Role;
import com.mateuszoriol.userservice.entity.User;
import com.mateuszoriol.userservice.exception.InvalidCredentialsException;
import com.mateuszoriol.userservice.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldLoginSuccessfullyAndReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("mateusz_1");
        request.setPassword("Secret123!");

        User user = new User();
        user.setId(1L);
        user.setUsername("mateusz_1");
        user.setEmail("mateusz@example.com");
        user.setPasswordHash("hashed-password");
        user.setRole(Role.USER);

        when(userService.findByUsernameOrThrow("mateusz_1")).thenReturn(user);
        when(passwordEncoder.matches("Secret123!", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void shouldThrowWhenPasswordIsInvalid() {
        LoginRequest request = new LoginRequest();
        request.setUsername("mateusz_1");
        request.setPassword("WrongPassword1!");

        User user = new User();
        user.setUsername("mateusz_1");
        user.setPasswordHash("hashed-password");

        when(userService.findByUsernameOrThrow("mateusz_1")).thenReturn(user);
        when(passwordEncoder.matches("WrongPassword1!", "hashed-password")).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid username or password", exception.getMessage());
    }
}