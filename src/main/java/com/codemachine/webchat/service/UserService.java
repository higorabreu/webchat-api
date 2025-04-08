package com.codemachine.webchat.service;

import com.codemachine.webchat.dto.ResponseUser;
import com.codemachine.webchat.domain.User;
import com.codemachine.webchat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<ResponseUser> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> new ResponseUser(
                        user.getId(),
                        user.getEmail(),
                        user.getUsername(),
                        user.getName()))
                .collect(Collectors.toList());
    }

    public boolean checkUserExists(String username) {
        try {
            return userRepository.existsByUsername(username);
        } catch (DataAccessException ex) {
            throw new RuntimeException();
        }
    }
}




