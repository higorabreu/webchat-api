package com.codemachine.webchat.service;

import com.codemachine.webchat.domain.User;
import com.codemachine.webchat.domain.UserRepository;
import com.codemachine.webchat.dto.RequestLoginUser;
import com.codemachine.webchat.dto.RequestRegisterUser;
import com.codemachine.webchat.service.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    private UserRepository userRepository;

    public boolean emailAlreadyRegistered(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean usernameAlreadyRegistered(String username) {
        return userRepository.existsByUsername(username);
    }

    public void registerUser(RequestRegisterUser data)
            throws EmailAlreadyRegisteredException, UsernameAlreadyRegisteredException, UserRegisterFailureException {

        try {
            if (emailAlreadyRegistered(data.email())) {
                throw new EmailAlreadyRegisteredException();
            }
            if (usernameAlreadyRegistered(data.username())) {
                throw new UsernameAlreadyRegisteredException();
            }

            User user = new User();
            user.setEmail(data.email());
            user.setUsername(data.username());
            user.setName(data.name());
            user.setPassword(passwordEncoder.encode(data.password()));

            userRepository.save(user);

        } catch (Exception ex) {
            throw new UserRegisterFailureException();
        }
    }

    public void loginUser(RequestLoginUser data)
            throws UserNotFoundException, InvalidPasswordException, UserLoginFailureException {

        try {
            Optional<User> existingUserOptional = userRepository.findByUsername(data.username());
            if (existingUserOptional.isPresent()) {
                User existingUser = existingUserOptional.get();
                if (!passwordEncoder.matches(data.password(), existingUser.getPassword())) {
                    throw new InvalidPasswordException();
                }
            } else {
                throw new UserNotFoundException();
            }
        } catch (Exception ex) {
            throw new UserLoginFailureException();
        }
    }



}
