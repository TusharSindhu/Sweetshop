package com.incubyte.sweetshop.auth;

import com.incubyte.sweetshop.user.User;
import com.incubyte.sweetshop.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request) {
        // 1. Check if user exists (Skipping for now to keep it simple "Green", will refactor later)

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
}
