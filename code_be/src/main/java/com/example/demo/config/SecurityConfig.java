package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/register", "/login", "/css/**", "/js/**",
                                "/images/**", "/stories", "/story/**", "/api/**", "/error")
                        .permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/admin/genres/create", "/admin/genres/*/delete").hasRole("ADMIN")
                        .requestMatchers("/admin/genres/**").hasAnyRole("ADMIN", "MOD")
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
