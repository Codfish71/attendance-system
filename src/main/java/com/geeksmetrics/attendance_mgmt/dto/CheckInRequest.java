package com.geeksmetrics.attendance_mgmt.dto;


import lombok.Data;

@Data
public class CheckInRequest {
    private Double latitude;
    private Double longitude;
}
