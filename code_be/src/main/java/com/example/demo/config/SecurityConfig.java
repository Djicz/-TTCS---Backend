package com.example.demo.config;

import com.example.demo.service.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    @Order(1)
    public SecurityFilterChain appSecurityFilterChain(HttpSecurity http, AuthService authService, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                .securityMatcher("/app/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/app/login", "/app/register", "/app/forgot-password", "/app/stories/**", "/app/story/**", "/app/")
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/register", "/login", "/css/**", "/js/**",
                                "/images/**", "/stories", "/story/**", "/api/**", "/error", "/forgot-password")
                        .permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/admin/genres/create", "/admin/genres/*/delete").hasRole("ADMIN")
                        .requestMatchers("/admin/genres/**").hasAnyRole("ADMIN", "MOD")
                        .requestMatchers("/admin/stories/**").hasAnyRole("ADMIN", "MOD")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/mod/**").hasAnyRole("ADMIN", "MOD")
                        .requestMatchers("/uploader/**", "/reader/**").authenticated()
                        .anyRequest().authenticated())
                .anonymous(anonymous -> anonymous.authorities("ROLE_ANONYMOUS"))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
