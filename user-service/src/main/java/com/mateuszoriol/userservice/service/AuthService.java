package com.mateuszoriol.userservice.service;

import com.mateuszoriol.userservice.dto.AuthResponse;
import com.mateuszoriol.userservice.dto.LoginRequest;
import com.mateuszoriol.userservice.entity.User;
import com.mateuszoriol.userservice.exception.InvalidCredentialsException;
import com.mateuszoriol.userservice.exception.UserNotFoundException;
import com.mateuszoriol.userservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        User user;

        try {
            user = userService.findByUsernameOrThrow(request.getUsername());
        } catch (UserNotFoundException ex) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }
}