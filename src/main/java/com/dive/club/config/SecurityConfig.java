package com.dive.club.config;

import com.dive.club.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security Configuration
 * Defines authentication and authorization rules
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final CustomUserDetailsService userDetailsService;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for REST API (enable in
                                                                       // production with proper
                                                                       // setup)
                                .authorizeHttpRequests(auth -> auth
                                                // Public endpoints - anyone can access
                                                .requestMatchers(
                                                                "/",
                                                                "/login",
                                                                "/auth/**",
                                                                "/api/activities",
                                                                "/api/activities/search",
                                                                "/api/activities/category/**",
                                                                "/h2-console/**",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**")
                                                .permitAll()

                                                // Activity pages - require authentication
                                                .requestMatchers(
                                                                "/activity/list",
                                                                "/activity/{id}")
                                                .authenticated()

                                                // Manager pages - ROLE_MANAGER or ROLE_ADMIN
                                                .requestMatchers(
                                                                "/activity/create",
                                                                "/activity/edit/**",
                                                                "/activity/my-list",
                                                                "/activity/delete/**",
                                                                "/activity/submit/**",
                                                                "/activity/cancel/**",
                                                                "/activities/create",
                                                                "/activities/edit/**")
                                                .hasAnyRole("MANAGER", "ADMIN")

                                                // SuperManager pages - ROLE_ADMIN
                                                .requestMatchers(
                                                                "/activity/audit/**",
                                                                "/activities/audit",
                                                                "/activities/audit/**")
                                                .hasRole("ADMIN")

                                                // API endpoints - Activity detail
                                                .requestMatchers("/api/activities/{id}").authenticated()

                                                // Manager API endpoints - ROLE_MANAGER or ROLE_ADMIN
                                                .requestMatchers(
                                                                "/api/activities/my",
                                                                "/api/activities/{id}/submit")
                                                .hasAnyRole("MANAGER", "ADMIN")

                                                // SuperManager API endpoints - ROLE_ADMIN
                                                .requestMatchers(
                                                                "/api/activities/pending",
                                                                "/api/activities/{id}/audit")
                                                .hasRole("ADMIN")

                                                // All other requests require authentication
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/", true)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/")
                                                .permitAll())
                                .headers(headers -> headers
                                                .frameOptions(frameOptions -> frameOptions.disable()) // For H2 Console
                                );

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
                provider.setUserDetailsService(userDetailsService);
                provider.setPasswordEncoder(passwordEncoder());
                return provider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }
}
