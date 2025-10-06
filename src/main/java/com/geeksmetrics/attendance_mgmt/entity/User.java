package com.geeksmetrics.attendance_mgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private Double hourlyRate;

    @Column(nullable = false)
    private Double weekendOvertimeMultiplier = 1.5;

    @Column(nullable = false)
    private Double holidayOvertimeMultiplier = 2.0;

    @Column(nullable = false)
    private Double regularOvertimeMultiplier = 1.25;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false)
    private Boolean active = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Double getWeekendOvertimeMultiplier() {
        return weekendOvertimeMultiplier;
    }

    public void setWeekendOvertimeMultiplier(Double weekendOvertimeMultiplier) {
        this.weekendOvertimeMultiplier = weekendOvertimeMultiplier;
    }

    public Double getHolidayOvertimeMultiplier() {
        return holidayOvertimeMultiplier;
    }

    public void setHolidayOvertimeMultiplier(Double holidayOvertimeMultiplier) {
        this.holidayOvertimeMultiplier = holidayOvertimeMultiplier;
    }

    public Double getRegularOvertimeMultiplier() {
        return regularOvertimeMultiplier;
    }

    public void setRegularOvertimeMultiplier(Double regularOvertimeMultiplier) {
        this.regularOvertimeMultiplier = regularOvertimeMultiplier;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
