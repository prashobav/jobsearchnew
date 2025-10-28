package com.jobsearch.controller;

import com.jobsearch.dto.*;
import com.jobsearch.entity.ERole;
import com.jobsearch.entity.Role;
import com.jobsearch.entity.User;
import com.jobsearch.repository.RoleRepository;
import com.jobsearch.repository.UserRepository;
import com.jobsearch.security.JwtUtils;
import com.jobsearch.security.UserPrincipal;
import com.jobsearch.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    PasswordResetService passwordResetService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        
        // First, check if user exists in database
        User user = userRepository.findByUsername(loginRequest.getUsername()).orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username '" + loginRequest.getUsername() + "' does not exist. Please check your username or create a new account."));
        }
        
        // Check if password matches
        if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Password is incorrect for username '" + loginRequest.getUsername() + "'. Please check your password."));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Authentication failed. Please try again."));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        boolean success = passwordResetService.createPasswordResetToken(request.getEmail());
        
        if (success) {
            return ResponseEntity.ok(new MessageResponse("If your email exists in our system, you will receive a password reset link."));
        } else {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Too many reset attempts. Please try again later."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        boolean isValidToken = passwordResetService.validatePasswordResetToken(request.getToken());
        
        if (!isValidToken) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid or expired reset token."));
        }
        
        boolean success = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        
        if (success) {
            return ResponseEntity.ok(new MessageResponse("Password has been reset successfully!"));
        } else {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to reset password. Please try again."));
        }
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        boolean isValid = passwordResetService.validatePasswordResetToken(token);
        
        if (isValid) {
            return ResponseEntity.ok(new MessageResponse("Token is valid."));
        } else {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid or expired token."));
        }
    }

    // Debug endpoint to check users - remove in production
    @GetMapping("/debug/users")
    public ResponseEntity<?> getUsers() {
        List<User> users = userRepository.findAll();
        List<String> userInfo = users.stream().map(user -> 
            "Username: '" + user.getUsername() + "', Email: '" + user.getEmail() + "', ID: " + user.getId()
        ).collect(Collectors.toList());
        
        return ResponseEntity.ok(userInfo);
    }

    // Debug endpoint to check specific user
    @GetMapping("/debug/user/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("User '" + username + "' not found in database."));
        }
        
        return ResponseEntity.ok("Found user: Username='" + user.getUsername() + 
                                "', Email='" + user.getEmail() + 
                                "', ID=" + user.getId() + 
                                ", Roles=" + user.getRoles().size());
    }
}