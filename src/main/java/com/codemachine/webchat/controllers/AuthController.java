package com.codemachine.webchat.controllers;

import com.codemachine.webchat.dto.RequestLoginUser;
import com.codemachine.webchat.service.AuthService;
import com.codemachine.webchat.dto.RequestRegisterUser;
import com.codemachine.webchat.service.exceptions.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    // POST /auth/register
    @PostMapping("/auth/register")
    public ResponseEntity<String> registerUser(@RequestBody @Valid RequestRegisterUser data) {
        try {
            authService.registerUser(data);
            return ResponseEntity.ok("User registered successfully.");
        } catch (EmailAlreadyRegisteredException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered.");
        } catch (UsernameAlreadyRegisteredException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already registered.");
        } catch (UserRegisterFailureException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to register user.");
        }

    }

    // POST /auth/login
    @PostMapping("/auth/login")
    public ResponseEntity<?> loginUser(@RequestBody @Valid RequestLoginUser data) {
        try {
            String token = authService.loginUser(data);
            String ttl = "86400";
            Map<String, String> response = new HashMap<>();
            response.put("access_token", token);
            return ResponseEntity.ok().body(response);
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } catch (InvalidPasswordException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password.");
        } catch (UserLoginFailureException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to login.");
        }
    }

    // POST /auth/check-token
    @PostMapping("/auth/check-token")
    public ResponseEntity<Boolean> isTokenValid(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, String> requestBody) {

        try {
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : null;

            String username = requestBody.get("username");

            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
            }

            if (username == null || username.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
            }

            boolean isValid = authService.checkTokenValidity(token, username);

            return ResponseEntity.ok(isValid);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
}
