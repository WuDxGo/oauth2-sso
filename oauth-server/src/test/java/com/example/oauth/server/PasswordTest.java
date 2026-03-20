package com.example.oauth.server;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String encodedPassword = "{bcrypt}$2a$10$sk5I31WqTxIyGoWmFo8xYuX4sR4offE3KkK1JqIH2IVLgEYxz.ika";
        
        System.out.println("Raw password: " + rawPassword);
        System.out.println("Encoded password: " + encodedPassword);
        System.out.println("Matches: " + encoder.matches(rawPassword, encodedPassword));
        
        // Generate new password
        String newEncoded = encoder.encode(rawPassword);
        System.out.println("New encoded: " + newEncoded);
        System.out.println("New matches: " + encoder.matches(rawPassword, newEncoded));
    }
}
