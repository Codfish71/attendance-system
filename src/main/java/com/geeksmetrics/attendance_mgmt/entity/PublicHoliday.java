package com.geeksmetrics.attendance_mgmt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Getter @Setter
public class PublicHoliday {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDate holidayDate;
    private String country = "Kuwait";
}
