package com.mateuszoriol.userservice.controller;

import com.mateuszoriol.userservice.dto.AuthResponse;
import com.mateuszoriol.userservice.dto.LoginRequest;
import com.mateuszoriol.userservice.dto.MeResponse;
import com.mateuszoriol.userservice.dto.RegisterRequest;
import com.mateuszoriol.userservice.dto.RegisterResponse;
import com.mateuszoriol.userservice.service.AuthService;
import com.mateuszoriol.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public MeResponse me(Principal principal) {
        return userService.getMe(principal.getName());
    }
}