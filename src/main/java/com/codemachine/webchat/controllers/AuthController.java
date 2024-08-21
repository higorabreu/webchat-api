package com.codemachine.webchat.controllers;

import com.codemachine.webchat.dto.RequestLoginUser;
import com.codemachine.webchat.service.UserService;
import com.codemachine.webchat.dto.RequestRegisterUser;
import com.codemachine.webchat.service.exceptions.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    private UserService userService;

    // POST /register
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody @Valid RequestRegisterUser data) {
        try {
            userService.registerUser(data);
        } catch (EmailAlreadyRegisteredException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered.");
        } catch (UsernameAlreadyRegisteredException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already registered.");
        } catch (UserRegisterFailureException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to register user.");
        }
        return ResponseEntity.ok("User registered successfully.");
    }

    // POST /login
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody @Valid RequestLoginUser data) {
        try {
            userService.loginUser(data);
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } catch (InvalidPasswordException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password.");
        } catch (UserLoginFailureException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to login.");
        }
        return ResponseEntity.ok("User logged in successfully.");
    }

    //Logout


}
