package com.mateuszoriol.userservice.service;

import com.mateuszoriol.userservice.dto.MeResponse;
import com.mateuszoriol.userservice.dto.RegisterRequest;
import com.mateuszoriol.userservice.dto.RegisterResponse;
import com.mateuszoriol.userservice.entity.Role;
import com.mateuszoriol.userservice.entity.User;
import com.mateuszoriol.userservice.exception.UserAlreadyExistsException;
import com.mateuszoriol.userservice.exception.UserNotFoundException;
import com.mateuszoriol.userservice.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        );
    }

    public User findByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public MeResponse getMe(String username) {
        User user = findByUsernameOrThrow(username);

        return new MeResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}