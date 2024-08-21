package com.codemachine.webchat.controllers;

import com.codemachine.webchat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    //Get User By id
    //Get User By username
    //Get All Users

}
