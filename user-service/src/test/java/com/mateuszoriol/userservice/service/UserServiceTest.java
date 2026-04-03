package com.mateuszoriol.userservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mateuszoriol.userservice.dto.RegisterRequest;
import com.mateuszoriol.userservice.dto.RegisterResponse;
import com.mateuszoriol.userservice.entity.Role;
import com.mateuszoriol.userservice.entity.User;
import com.mateuszoriol.userservice.exception.UserAlreadyExistsException;
import com.mateuszoriol.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("mateusz_1");
        request.setEmail("mateusz@example.com");
        request.setPassword("Secret123!");

        when(userRepository.existsByUsername("mateusz_1")).thenReturn(false);
        when(userRepository.existsByEmail("mateusz@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret123!")).thenReturn("hashed-password");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("mateusz_1");
        savedUser.setEmail("mateusz@example.com");
        savedUser.setPasswordHash("hashed-password");
        savedUser.setRole(Role.USER);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterResponse response = userService.register(request);

        assertEquals(1L, response.getId());
        assertEquals("mateusz_1", response.getUsername());
        assertEquals("mateusz@example.com", response.getEmail());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("mateusz_1", capturedUser.getUsername());
        assertEquals("mateusz@example.com", capturedUser.getEmail());
        assertEquals("hashed-password", capturedUser.getPasswordHash());
        assertEquals(Role.USER, capturedUser.getRole());
    }

    @Test
    void shouldThrowWhenUsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("mateusz_1");
        request.setEmail("mateusz@example.com");
        request.setPassword("Secret123!");

        when(userRepository.existsByUsername("mateusz_1")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.register(request)
        );

        assertEquals("Username already exists", exception.getMessage());
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("mateusz_1");
        request.setEmail("mateusz@example.com");
        request.setPassword("Secret123!");

        when(userRepository.existsByUsername("mateusz_1")).thenReturn(false);
        when(userRepository.existsByEmail("mateusz@example.com")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.register(request)
        );

        assertEquals("Email already exists", exception.getMessage());
    }
}