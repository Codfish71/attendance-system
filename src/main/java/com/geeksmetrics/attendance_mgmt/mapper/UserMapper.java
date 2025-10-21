package com.geeksmetrics.attendance_mgmt.mapper;

import com.geeksmetrics.attendance_mgmt.dto.UserDto;
import com.geeksmetrics.attendance_mgmt.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    // This is your existing method for the simple DTO
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        return dto;
    }

    // Add this new method for the detailed DTO
    public UserDto toDetailDto(User user) {
        if (user == null) {
            return null;
        }
        UserDto detailDto = new UserDto();
        detailDto.setId(user.getId());
        detailDto.setEmail(user.getEmail());
        detailDto.setFirstName(user.getFirstName());
        detailDto.setLastName(user.getLastName());
        detailDto.setHourlyRate(user.getHourlyRate());
        detailDto.setWeekendOvertimeMultiplier(user.getWeekendOvertimeMultiplier());
        detailDto.setHolidayOvertimeMultiplier(user.getHolidayOvertimeMultiplier());
        detailDto.setRegularOvertimeMultiplier(user.getRegularOvertimeMultiplier());
        detailDto.setRoles(user.getRoles());
        detailDto.setActive(user.getActive());
        detailDto.setCreatedAt(user.getCreatedAt());
        detailDto.setUpdatedAt(user.getUpdatedAt());
        return detailDto;
    }
}