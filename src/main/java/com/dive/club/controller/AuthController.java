package com.dive.club.controller;

import com.dive.club.entity.Manager;
import com.dive.club.entity.SuperManager;
import com.dive.club.entity.User;
import com.dive.club.enums.UserRole;
import com.dive.club.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;

/**
 * Authentication Controller
 * Handles login and authentication requests
 */
@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    /**
     * Test login endpoint for development/demo
     * Allows quick login with predefined test accounts
     */
    @PostMapping("/test-login")
    public String testLogin(@RequestParam("testRole") String testRole, HttpServletRequest request) {
        log.info("Test login requested with role: {}", testRole);

        User user = null;
        String email = null;

        // Select user based on test role
        switch (testRole) {
            case "GUEST":
                email = "member@diveclub.com";
                break;
            case "MANAGER":
                email = "manager1@diveclub.com";
                break;
            case "SUPER":
                email = "admin@diveclub.com";
                break;
            default:
                log.warn("Invalid test role: {}", testRole);
                return "redirect:/login?error=Invalid role selected";
        }

        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            log.error("Test user not found: {}", email);
            return "redirect:/login?error=Test user not found";
        }

        user = userOptional.get();

        // Create authentication token and set in security context
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Save authentication to session - CRITICAL for persisting login
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        log.info("User logged in successfully: {} ({})", user.getEmail(), user.getRole());

        // Redirect to home page
        return "redirect:/";
    }

    /**
     * NCU Portal login endpoint (placeholder)
     * In production, this would integrate with actual NCU Portal OAuth/SAML
     */
    @PostMapping("/ncu-portal")
    public String ncuPortalLogin() {
        log.info("NCU Portal login requested");
        // TODO: Implement NCU Portal authentication
        return "redirect:/login?error=NCU Portal login not yet implemented";
    }
}
