package com.incubyte.sweetshop.auth;

// DTO for accepting username and password from the client
public record LoginRequest(String username, String password) {}
