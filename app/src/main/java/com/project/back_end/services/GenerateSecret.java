package com.project.back_end.services;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import javax.crypto.SecretKey;

public class GenerateSecret {
    public static void main(String[] args) {
        // Generate a secure key for HS256
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        
        // Convert to base64 string
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
        
        System.out.println("Generated JWT Secret (base64): " + base64Key);
        System.out.println("Key length (bytes): " + key.getEncoded().length);
        System.out.println("Key length (bits): " + (key.getEncoded().length * 8));
    }
}