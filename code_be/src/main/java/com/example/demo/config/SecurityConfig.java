package com.example.demo.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.service.AuthService;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
        @Bean
        @Order(1)
        public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthService authService, JwtAuthenticationFilter jwtFilter) throws Exception {
                http
                                .securityMatcher("/app/**")
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/app/login", "/app/register", "/app/forgot-password", "/app/stories", "/app/story/**", "/app/")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                    response.setStatus(401);
                                                    response.setContentType("application/json");
                                                    response.setCharacterEncoding("UTF-8");
                                                    response.getWriter().write("{\"error\": \"Authentication required\"}");
                                                })
                                );
                return http.build();
        }

        @Bean
        @Order(2)
        public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/", "/register", "/login", "/css/**", "/js/**",
                                                                "/images/**", "/stories", "/story/**",
                                                                "/api/**", "/error")
                                                .permitAll()
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/mod/**").hasAnyRole("ADMIN", "MOD")
                                                .requestMatchers("/uploader/**").hasAnyRole("ADMIN", "UPLOADER")
                                                .anyRequest().authenticated())
//                                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                                .exceptionHandling(exception -> exception
                                                .accessDeniedHandler(customAccessDeniedHandler()))
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/", true)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/")
                                                .permitAll())
                                .sessionManagement(session -> session
                                                .maximumSessions(3) 
                                                .maxSessionsPreventsLogin(false));
                return http.build();
        }
        
        @Bean
        public AccessDeniedHandler customAccessDeniedHandler() {
            return (request, response, accessDeniedException) -> {
                response.sendRedirect("/error?msg=access-denied");
            };
        }
    
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
