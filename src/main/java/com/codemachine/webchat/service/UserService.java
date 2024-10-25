package com.codemachine.webchat.service;

import com.codemachine.webchat.dto.ResponseUser;
import com.codemachine.webchat.util.JwtUtil;
import com.codemachine.webchat.domain.User;
import com.codemachine.webchat.domain.UserRepository;
import com.codemachine.webchat.dto.RequestLoginUser;
import com.codemachine.webchat.dto.RequestRegisterUser;
import com.codemachine.webchat.service.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, JwtUtil jwtUtil) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public boolean emailAlreadyRegistered(String email) {
        return userRepository.existsByEmail(email);
    }


    public boolean usernameAlreadyRegistered(String username) {
        return userRepository.existsByUsername(username);
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

        } catch (EmailAlreadyRegisteredException | UsernameAlreadyRegisteredException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UserRegisterFailureException();
        }
    }


    public String loginUser(RequestLoginUser data)
            throws UserNotFoundException, InvalidPasswordException, UserLoginFailureException {
        try {
            Optional<User> existingUserOptional = userRepository.findByUsername(data.username());

            if (existingUserOptional.isEmpty()) {
                throw new UserNotFoundException();
            }

            User existingUser = existingUserOptional.get();

            if (!passwordEncoder.matches(data.password(), existingUser.getPassword())) {
                throw new InvalidPasswordException();
            }

            return jwtUtil.generateToken(existingUser);

        } catch (UserNotFoundException | InvalidPasswordException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UserLoginFailureException();
        }
    }


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Optional<User> userOptional = userRepository.findByUsername(username);

            if (userOptional.isEmpty()) {
                throw new UserNotFoundException();
            }

            User user = userOptional.get();

            return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>()
            );

        } catch (Exception ex) {
            throw new UsernameNotFoundException("Error loading user");
        }
    }


    public boolean checkTokenValidity(String token, String username) {
        try {
            var userDetails = loadUserByUsername(username);
            return jwtUtil.validateToken(token, userDetails);
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify token");
        }
    }

}




