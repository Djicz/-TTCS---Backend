package com.example.demo.controller.admin;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.service.RoleService;
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

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final RoleService roleService;

    @GetMapping //lay list user + roll
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        //Loc admin va anonymus
        List<Role> manageableRoles = roleService.getAllRoles().stream()
                .filter(role -> !role.getName().equals("ROLE_ADMIN") && !role.getName().equals("ROLE_ANONYMOUS"))
                .toList();
        model.addAttribute("allRoles", manageableRoles);
        return "admin/users";
    }
    
    //set khoa/mo trang thai hoat dong user
    @PostMapping("/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean isEnabled = userService.toggleUserStatus(id);
        User user = userService.getUserById(id);
        redirectAttributes.addFlashAttribute("successMessage", 
            "Đã " + (isEnabled ? "kích hoạt" : "khóa") + " người dùng " + user.getUsername());
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/update-roles")
    public String updateUserRoles(@PathVariable Long id, 
                                  @RequestParam(required = false) List<Long> roleIds,
                                  RedirectAttributes redirectAttributes) {
        User user = userService.getUserById(id);
        
        //Giu lai roll admin va anonymus
        Set<Role> preservedRoles = user.getRoles().stream()
                .filter(role -> role.getName().equals("ROLE_ADMIN") || role.getName().equals("ROLE_ANONYMOUS"))
                .collect(Collectors.toSet());

        Set<Role> newRoles = new HashSet<>(preservedRoles);
        if (roleIds != null) {
            List<Role> addedRoles = roleService.findAllById(roleIds);
            for (Role r : addedRoles) {
                if (!r.getName().equals("ROLE_ADMIN") && !r.getName().equals("ROLE_ANONYMOUS")) {
                    newRoles.add(r); //Neu khong phai admin va anonymus thi them vao roll
                }
            }
        }
        
        userService.updateUserRoles(id, newRoles);
        
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật quyền cho " + user.getUsername() + " thành công!");
        return "redirect:/admin/users";
    }

    //xoa user
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUserWithData(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa người dùng và toàn bộ dữ liệu liên quan thành công.");
        } catch (Exception e) {
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
        if (userService.existsByUsername(username)) { //neu ton tai username thi bao loi
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
            List<Role> addedRoles = roleService.findAllById(roleIds);
            for (Role r : addedRoles) {
                //khong duoc phep them roll admin va anonymus
                if (!r.getName().equals("ROLE_ADMIN") && !r.getName().equals("ROLE_ANONYMOUS")) {
                    user.getRoles().add(r);
                }
            }
        }

        userService.createUser(user);
        redirectAttributes.addFlashAttribute("successMessage", "Đã tạo người dùng mới thành công!");
        return "redirect:/admin/users";
    }
}
