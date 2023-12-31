package com.example.spring.CafeManagerApplication.service.ServiceImpl;

import com.example.spring.CafeManagerApplication.dto.*;
import com.example.spring.CafeManagerApplication.entity.RefreshToken;
import com.example.spring.CafeManagerApplication.entity.Role;
import com.example.spring.CafeManagerApplication.entity.UserEntity;
import com.example.spring.CafeManagerApplication.exception.TokenRefreshException;
import com.example.spring.CafeManagerApplication.exception.UserNotAllow;
import com.example.spring.CafeManagerApplication.exception.UserNotFound;
import com.example.spring.CafeManagerApplication.repository.RoleRepository;
import com.example.spring.CafeManagerApplication.repository.UserRepository;
import com.example.spring.CafeManagerApplication.security.JwtGenerator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;

@Service
public class  AuthenticationService implements com.example.spring.CafeManagerApplication.service.AuthenticationService {
    private final UserRepository userRepository;
    private final JwtGenerator jwtGenerator;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    @Autowired
    RefreshTokenService refreshTokenService;

    public AuthenticationService(UserRepository userRepository, JwtGenerator jwtGenerator, RoleRepository roleRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtGenerator = jwtGenerator;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public ResponseEntity<?> register(@RequestBody RegisterDto registerDto) {

        if(registerDto.getUsername().isEmpty() || registerDto.getPassword().isEmpty()
            || registerDto.getEmail().isEmpty() ||registerDto.getContactNumber().isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("Something wrong"),HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByUsername(registerDto.getUsername())){
            return new ResponseEntity<>( new MessageResponse("username already exist"), HttpStatus.BAD_REQUEST);
        }

        UserEntity user = new UserEntity();

        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode((registerDto.getPassword())));
        user.setEmail(registerDto.getEmail());
        user.setContactNumber(registerDto.getContactNumber());

        Role role = roleRepository.findByName("user").orElseThrow();
        user.setRoles(Collections.singletonList(role));

        userRepository.save(user);

        return new ResponseEntity<>(new MessageResponse("Sign up successfully"), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> login(LoginDto loginDto) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getUsername(),
                            loginDto.getPassword()
                    )
            );
        }catch (Exception e){
            throw new BadCredentialsException("username or password is incorrect");
        }

        UserEntity user = userRepository.findByUsername(loginDto.getUsername()).orElseThrow();

        if(!user.getActive()) {
            throw new UserNotAllow("Wait for admin to grant you access to this application");
        }
        Map<String,List<String>> roles = mapRole(user.getRoles());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);


        SecurityContextHolder.getContext().setAuthentication(authentication);

        var token = jwtGenerator.generateToken(roles,user);
        return new ResponseEntity<>(new AuthResponseDTO(token,refreshToken.getToken(), user.getUsername()),HttpStatus.OK);
    }



    private Map<String, List<String>> mapRole(List<Role> roles){
        List<String> roleName = roles.stream().map(Role::getName).toList();
        Map<String,List<String>> authorities = new HashMap<>();
        authorities.put("role",roleName);
        return authorities;
    }

    public ResponseEntity<?> refreshtoken(RefreshTokenRequest request){
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    Map<String,List<String>> roles = mapRole(user.getRoles());
                    String token = jwtGenerator.generateToken(roles,user);
                    return ResponseEntity.ok(new RefreshTokenResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(
                        "Refresh token is not in database!"));
    }
}
