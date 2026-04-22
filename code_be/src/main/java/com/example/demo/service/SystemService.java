package com.example.demo.service;

import com.example.demo.entity.SystemConfig;
import com.example.demo.repository.SearchHistoryRepository;
import com.example.demo.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemService {
    private final SystemConfigRepository systemConfigRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    public String getConfig(String key, String defaultValue) {
        return systemConfigRepository.findById(key)
                .map(SystemConfig::getValue).orElse(defaultValue);
    }

    @Transactional
    public void setConfig(String key, String value) {
        systemConfigRepository.save(new SystemConfig(key, value));
    }

    public List<?> getTopSearchKeywords() {
        return searchHistoryRepository.findTop10ByOrderBySearchCountDesc();
    }
}
