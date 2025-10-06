package com.geeksmetrics.attendance_mgmt.dto;

import lombok.Data;

@Data
public class CheckOutRequest {
    private Double latitude;
    private Double longitude;
}