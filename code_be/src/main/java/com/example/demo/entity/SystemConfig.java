package com.example.demo.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "system_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfig {
    @Id
    @Column(name = "config_key", unique = true, nullable = false)
    private String key;
    @Column(name = "config_value", nullable = false)
    private String value;
}
