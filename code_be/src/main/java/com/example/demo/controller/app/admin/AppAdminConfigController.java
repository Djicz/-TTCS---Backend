package com.example.demo.controller.app.admin;
import com.example.demo.entity.SystemConfig;
import com.example.demo.repository.SearchHistoryRepository;
import com.example.demo.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/app/admin/config")
@RequiredArgsConstructor
public class AppAdminConfigController {
    private final SystemConfigRepository systemConfigRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    @GetMapping
    public ResponseEntity<?> showConfigDashboard() {
        String maintenanceMode = systemConfigRepository.findById("MAINTENANCE_MODE")
                .map(SystemConfig::getValue).orElse("false");
        String adsEnabled = systemConfigRepository.findById("ADS_ENABLED")
                .map(SystemConfig::getValue).orElse("true");
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("maintenanceMode", Boolean.parseBoolean(maintenanceMode));
        response.put("adsEnabled", Boolean.parseBoolean(adsEnabled));
        response.put("hotKeywords", searchHistoryRepository.findTop10ByOrderBySearchCountDesc());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/update")
    public ResponseEntity<?> updateConfig(@RequestParam(required = false) boolean maintenanceMode,
            @RequestParam(required = false) boolean adsEnabled) {
        systemConfigRepository.save(new SystemConfig("MAINTENANCE_MODE", String.valueOf(maintenanceMode)));
        systemConfigRepository.save(new SystemConfig("ADS_ENABLED", String.valueOf(adsEnabled)));
        return ResponseEntity.ok(java.util.Map.of("message", "System configuration updated successfully."));
    }
}
