package com.mateuszoriol.userservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mateuszoriol.userservice.dto.AuthResponse;
import com.mateuszoriol.userservice.dto.MeResponse;
import com.mateuszoriol.userservice.dto.RegisterResponse;
import com.mateuszoriol.userservice.exception.GlobalExceptionHandler;
import com.mateuszoriol.userservice.exception.InvalidCredentialsException;
import com.mateuszoriol.userservice.exception.UserAlreadyExistsException;
import com.mateuszoriol.userservice.security.JwtAuthenticationFilter;
import com.mateuszoriol.userservice.service.AuthService;
import com.mateuszoriol.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldRegisterUserAndReturnCreated() throws Exception {
        RegisterResponse response = new RegisterResponse(
                1L,
                "mateusz_1",
                "mateusz@example.com"
        );

        when(userService.register(any())).thenReturn(response);

        String requestBody = """
                {
                  "username": "mateusz_1",
                  "email": "mateusz@example.com",
                  "password": "Secret123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("mateusz_1"))
                .andExpect(jsonPath("$.email").value("mateusz@example.com"));
    }

    @Test
    void shouldReturnBadRequestForInvalidRegisterPayload() throws Exception {
        String requestBody = """
                {
                  "username": "ab",
                  "email": "wrong-email",
                  "password": "abc"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    void shouldReturnConflictWhenUsernameAlreadyExists() throws Exception {
        when(userService.register(any()))
                .thenThrow(new UserAlreadyExistsException("Username already exists"));

        String requestBody = """
                {
                  "username": "mateusz_1",
                  "email": "mateusz@example.com",
                  "password": "Secret123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Username already exists"));
    }

    @Test
    void shouldLoginAndReturnToken() throws Exception {
        when(authService.login(any())).thenReturn(new AuthResponse("jwt-token"));

        String requestBody = """
                {
                  "username": "mateusz_1",
                  "password": "Secret123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void shouldReturnUnauthorizedForInvalidLogin() throws Exception {
        doThrow(new InvalidCredentialsException("Invalid username or password"))
                .when(authService).login(any());

        String requestBody = """
            {
              "username": "mateusz_1",
              "password": "WrongPassword1!"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    @Test
    void shouldReturnBadRequestForInvalidLoginPayload() throws Exception {
        String requestBody = """
                {
                  "username": "",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    void shouldReturnCurrentUserForMeEndpoint() throws Exception {
        MeResponse response = new MeResponse(
                1L,
                "mateusz_1",
                "mateusz@example.com",
                "USER"
        );

        when(userService.getMe("mateusz_1")).thenReturn(response);

        mockMvc.perform(get("/api/auth/me")
                        .principal(() -> "mateusz_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("mateusz_1"))
                .andExpect(jsonPath("$.email").value("mateusz@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }
}