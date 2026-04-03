package com.mateuszoriol.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterResponse {
    private Long id;
    private String username;
    private String email;
}