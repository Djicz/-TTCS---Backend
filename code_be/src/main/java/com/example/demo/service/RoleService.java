package com.example.demo.service;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Transactional
    public void assignUploaderRole(User user) {
        boolean hasUploaderRole = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_UPLOADER"));
        if (!hasUploaderRole) {
            roleRepository.findByName("ROLE_UPLOADER").ifPresent(role -> {
                user.getRoles().add(role);
                userRepository.save(user);
            });
        }
    }

    public java.util.List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public java.util.List<Role> findAllById(java.util.List<Long> ids) {
        return roleRepository.findAllById(ids);
    }
}
