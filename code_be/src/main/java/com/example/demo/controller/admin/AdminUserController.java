package com.example.demo.controller.admin;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        //Loc admin va anonymus
        List<Role> manageableRoles = roleRepository.findAll().stream()
                .filter(role -> !role.getName().equals("ROLE_ADMIN") && !role.getName().equals("ROLE_ANONYMOUS"))
                .toList();
        model.addAttribute("allRoles", manageableRoles);
        return "admin/users";
    }
    
    //set khoa/mo trang thai hoat dong user
    @PostMapping("/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", 
            "Đã " + (user.isEnabled() ? "kích hoạt" : "khóa") + " người dùng " + user.getUsername());
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/update-roles")
    public String updateUserRoles(@PathVariable Long id, 
                                  @RequestParam(required = false) List<Long> roleIds,
                                  RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        //Giu lai roll admin va anonymus
        Set<Role> preservedRoles = user.getRoles().stream()
                .filter(role -> role.getName().equals("ROLE_ADMIN") || role.getName().equals("ROLE_ANONYMOUS"))
                .collect(Collectors.toSet());

        Set<Role> newRoles = new HashSet<>(preservedRoles);
        if (roleIds != null) {
            List<Role> addedRoles = roleRepository.findAllById(roleIds);
            for (Role r : addedRoles) {
                if (!r.getName().equals("ROLE_ADMIN") && !r.getName().equals("ROLE_ANONYMOUS")) {
                    newRoles.add(r); //Neu khong phai admin va anonymus thi them vao roll
                }
            }
        }
        
        user.setRoles(newRoles);
        userRepository.save(user);
        
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật quyền cho " + user.getUsername() + " thành công!");
        return "redirect:/admin/users";
    }

    //xoa user
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Caution: Deleting a user might fail if they have stories, comments, etc.
        // In a real app, we'd handle cascading or soft-deletion.
        try {
            // userRepository.delete(user);
            // redirectAttributes.addFlashAttribute("successMessage", "Đã xóa người dùng " + user.getUsername());
            userService.deleteUserWithData(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa người dùng và toàn bộ dữ liệu liên quan thành công.");
        } catch (Exception e) {
            // redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa người dùng này (có ràng buộc dữ liệu).");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xóa người dùng: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }

    //tao user
    @PostMapping("/create")
    public String createUser(@RequestParam String username,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam(required = false) List<Long> roleIds,
                             RedirectAttributes redirectAttributes) {
        if (userRepository.existsByUsername(username)) { //neu ton tai username thi bao loi
            redirectAttributes.addFlashAttribute("errorMessage", "Username đã tồn tại!");
            return "redirect:/admin/users";
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .enabled(true)
                .roles(new HashSet<>())
                .build();

        if (roleIds != null) {
            List<Role> addedRoles = roleRepository.findAllById(roleIds);
            for (Role r : addedRoles) {
                //khong duoc phep them roll admin va anonymus
                if (!r.getName().equals("ROLE_ADMIN") && !r.getName().equals("ROLE_ANONYMOUS")) {
                    user.getRoles().add(r);
                }
            }
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Đã tạo người dùng mới thành công!");
        return "redirect:/admin/users";
    }
}
