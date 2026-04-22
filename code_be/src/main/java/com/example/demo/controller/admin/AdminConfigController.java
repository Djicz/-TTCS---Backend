package com.example.demo.controller.admin;

import com.example.demo.service.SystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/config")
@RequiredArgsConstructor
public class AdminConfigController {

    private final SystemService systemService;

    @GetMapping
    public String showConfigDashboard(Model model) {
        String maintenanceMode = systemService.getConfig("MAINTENANCE_MODE", "false");
        String adsEnabled = systemService.getConfig("ADS_ENABLED", "true");
        
        model.addAttribute("maintenanceMode", Boolean.parseBoolean(maintenanceMode));
        model.addAttribute("adsEnabled", Boolean.parseBoolean(adsEnabled));
        model.addAttribute("hotKeywords", systemService.getTopSearchKeywords());
        
        return "admin/config-dashboard";
    }

    @PostMapping("/update")
    public String updateConfig(@RequestParam(required = false) boolean maintenanceMode,
            @RequestParam(required = false) boolean adsEnabled,
            RedirectAttributes redirectAttributes) {
            
        systemService.setConfig("MAINTENANCE_MODE", String.valueOf(maintenanceMode));
        systemService.setConfig("ADS_ENABLED", String.valueOf(adsEnabled));
        
        redirectAttributes.addFlashAttribute("success", "System configuration updated successfully.");
        return "redirect:/admin/config";
    }
}
