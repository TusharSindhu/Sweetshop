package com.incubyte.sweetshop.auth;

import com.incubyte.sweetshop.user.User;
import com.incubyte.sweetshop.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest request) {
        // 1. Check if user exists (Skipping for now to keep it simple "Green", will refactor later)
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        // 2. Create User Entity
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        // 3. Hash the password before saving!
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("ROLE_USER");

        // 4. Save to DB
        userRepository.save(user);
    }
    public AuthResponse login(LoginRequest request) {
        // 1. Find user
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Validate Password
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // 3. Generate Token
        String token = jwtService.generateToken(user.getUsername());

        return new AuthResponse(token, user.getUsername(), user.getRole());
    }
}
