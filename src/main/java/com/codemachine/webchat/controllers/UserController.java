package com.codemachine.webchat.controllers;

import com.codemachine.webchat.dto.ResponseUser;
import com.codemachine.webchat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    //Get User By id
    //Get User By username


    //Check if user exists
    @GetMapping("/user/exists/{username}")
    public ResponseEntity<Boolean> checkUserExists(@PathVariable String username) {
        try {
            boolean exists = userService.checkUserExists(username);
            return ResponseEntity.ok(exists);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    //Get All Users
    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> getAllUsers() {
        try {
            List<ResponseUser> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
