package com.codemachine.webchat.controllers;

import com.codemachine.webchat.dto.ResponseUser;
import com.codemachine.webchat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    //Get User By id
    //Get User By username

    //Get All Users
    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> getAllUsers() {
        List<ResponseUser> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

}
