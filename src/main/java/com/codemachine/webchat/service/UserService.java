package com.codemachine.webchat.service;

import com.codemachine.webchat.domain.User;
import com.codemachine.webchat.domain.UserRepository;
import com.codemachine.webchat.dto.RequestLoginUser;
import com.codemachine.webchat.dto.RequestRegisterUser;
import com.codemachine.webchat.service.exceptions.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Service
public class UserService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

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

            return generateToken(existingUser);

        } catch (UserNotFoundException | InvalidPasswordException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UserLoginFailureException();
        }
    }


}




