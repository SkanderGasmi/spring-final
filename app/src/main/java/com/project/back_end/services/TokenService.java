package com.project.back_end.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenService {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:604800000}") // 7 days in milliseconds
    private long jwtExpiration;

    @Autowired
    public TokenService(AdminRepository adminRepository,
                        DoctorRepository doctorRepository,
                        PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Generate a JWT token for a user.
     * 
     * @param userId the user's ID
     * @param userType the type of user (ADMIN, DOCTOR, PATIENT)
     * @return the generated JWT token
     */
    public String generateToken(Long userId, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userType", userType);
        claims.put("userId", userId);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract the user identifier from the token.
     * 
     * @param token the JWT token
     * @return the user identifier (user ID)
     */
    public String extractIdentifier(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract the user ID from the token.
     * 
     * @param token the JWT token
     * @return the user ID
     */
    public Long extractUserId(String token) {
        try {
            String subject = extractClaim(token, Claims::getSubject);
            return Long.parseLong(subject);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extract the user type from the token.
     * 
     * @param token the JWT token
     * @return the user type (ADMIN, DOCTOR, PATIENT)
     */
    public String extractUserType(String token) {
        try {
            return extractClaim(token, claims -> claims.get("userType", String.class));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extract the email from the token (for backward compatibility).
     * 
     * @param token the JWT token
     * @return the email (if stored in token) or null
     */
    public String extractEmail(String token) {
        try {
            return extractClaim(token, claims -> claims.get("email", String.class));
        } catch (Exception e) {
            // For backward compatibility, try to get from subject if email was used as subject
            String subject = extractIdentifier(token);
            // Check if subject is an email (contains @)
            if (subject != null && subject.contains("@")) {
                return subject;
            }
            return null;
        }
    }

    /**
     * Extract patient ID from token.
     * 
     * @param token the JWT token
     * @return patient ID if user is a patient, null otherwise
     */
    public Long extractPatientId(String token) {
        try {
            String userType = extractUserType(token);
            if ("PATIENT".equals(userType)) {
                return extractUserId(token);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extract doctor ID from token.
     * 
     * @param token the JWT token
     * @return doctor ID if user is a doctor, null otherwise
     */
    public Long extractDoctorId(String token) {
        try {
            String userType = extractUserType(token);
            if ("DOCTOR".equals(userType)) {
                return extractUserId(token);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extract admin ID from token.
     * 
     * @param token the JWT token
     * @return admin ID if user is an admin, null otherwise
     */
    public Long extractAdminId(String token) {
        try {
            String userType = extractUserType(token);
            if ("ADMIN".equals(userType)) {
                return extractUserId(token);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Validate if a token is valid for a specific user type.
     * 
     * @param token the JWT token to validate
     * @param userType the expected user type (ADMIN, DOCTOR, PATIENT)
     * @return true if token is valid for the user type, false otherwise
     */
    public boolean validateToken(String token, String userType) {
        try {
            String tokenUserType = extractUserType(token);
            if (tokenUserType == null || !tokenUserType.equals(userType)) {
                return false;
            }

            Long userId = extractUserId(token);
            if (userId == null) {
                return false;
            }

            // Check if user exists in the appropriate repository
            switch (userType.toUpperCase()) {
                case "ADMIN":
                    return adminRepository.existsById(userId);
                case "DOCTOR":
                    return doctorRepository.existsById(userId);
                case "PATIENT":
                    return patientRepository.existsById(userId);
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate if a token is valid (regardless of user type).
     * 
     * @param token the JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            String userType = extractUserType(token);
            Long userId = extractUserId(token);
            
            if (userType == null || userId == null) {
                return false;
            }
            
            return validateToken(token, userType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate if token is authorized to access a specific resource.
     * 
     * @param token the JWT token
     * @param resourceOwnerId the ID of the resource owner
     * @param userType the expected user type
     * @return true if authorized, false otherwise
     */
    public boolean validateTokenForResource(String token, Long resourceOwnerId, String userType) {
        try {
            if (!validateToken(token, userType)) {
                return false;
            }
            
            Long userId = extractUserId(token);
            return userId != null && userId.equals(resourceOwnerId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if token is expired.
     * 
     * @param token the JWT token
     * @return true if expired, false otherwise
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extract expiration date from token.
     * 
     * @param token the JWT token
     * @return expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract a specific claim from the token.
     * 
     * @param token the JWT token
     * @param claimsResolver function to extract the claim
     * @return the claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from the token.
     * 
     * @param token the JWT token
     * @return all claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()    
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get the signing key for JWT tokens.
     * 
     * @return the secret key
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generate a token with custom claims.
     * 
     * @param userId the user ID
     * @param userType the user type
     * @param claims additional claims to include
     * @return the generated JWT token
     */
    public String generateTokenWithClaims(Long userId, String userType, Map<String, Object> claims) {
        if (claims == null) {
            claims = new HashMap<>();
        }
        claims.put("userType", userType);
        claims.put("userId", userId);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Refresh a token (generate new token with same claims).
     * 
     * @param token the old token
     * @return new refreshed token or null if invalid
     */
    public String refreshToken(String token) {
        try {
            Long userId = extractUserId(token);
            String userType = extractUserType(token);
            
            if (userId == null || userType == null) {
                return null;
            }
            
            // Extract all current claims except standard JWT claims
            Claims claims = extractAllClaims(token);
            Map<String, Object> customClaims = new HashMap<>();
            
            claims.forEach((key, value) -> {
                if (!key.equals("sub") && !key.equals("iat") && !key.equals("exp")) {
                    customClaims.put(key, value);
                }
            });
            
            return generateTokenWithClaims(userId, userType, customClaims);
        } catch (Exception e) {
            return null;
        }
    }
}