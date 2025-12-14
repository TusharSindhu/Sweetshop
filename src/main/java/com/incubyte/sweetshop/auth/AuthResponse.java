package com.incubyte.sweetshop.auth;

// DTO for sending the JWT token and user info back to the client
public record AuthResponse(String jwtToken, String username) {}