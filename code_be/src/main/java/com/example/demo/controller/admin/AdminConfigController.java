package com.example.demo.controller.admin;
import com.example.demo.entity.SystemConfig;
import com.example.demo.repository.SearchHistoryRepository;
import com.example.demo.repository.SystemConfigRepository;
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
    private final SystemConfigRepository systemConfigRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    @GetMapping
    public String showConfigDashboard(Model model) {
        String maintenanceMode = systemConfigRepository.findById("MAINTENANCE_MODE")
                .map(SystemConfig::getValue).orElse("false");
        String adsEnabled = systemConfigRepository.findById("ADS_ENABLED")
                .map(SystemConfig::getValue).orElse("true");
        model.addAttribute("maintenanceMode", Boolean.parseBoolean(maintenanceMode));
        model.addAttribute("adsEnabled", Boolean.parseBoolean(adsEnabled));
        model.addAttribute("hotKeywords", searchHistoryRepository.findTop10ByOrderBySearchCountDesc());
        return "admin/config-dashboard";
    }
    @PostMapping("/update")
    public String updateConfig(@RequestParam(required = false) boolean maintenanceMode,
            @RequestParam(required = false) boolean adsEnabled,
            RedirectAttributes redirectAttributes) {
        systemConfigRepository.save(new SystemConfig("MAINTENANCE_MODE", String.valueOf(maintenanceMode)));
        systemConfigRepository.save(new SystemConfig("ADS_ENABLED", String.valueOf(adsEnabled)));
        redirectAttributes.addFlashAttribute("success", "System configuration updated successfully.");
        return "redirect:/admin/config";
    }
}
