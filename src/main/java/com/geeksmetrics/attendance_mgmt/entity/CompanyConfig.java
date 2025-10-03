package com.geeksmetrics.attendance_mgmt.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "company_config")
@Getter
@Setter
@NoArgsConstructor
public class CompanyConfig {
    @Id
    @Column(name = "config_key")
    private String configKey;

    @Column(name = "config_value", nullable = false)
    private String configValue;
}
