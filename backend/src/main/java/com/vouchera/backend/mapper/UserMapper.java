package com.vouchera.backend.mapper;

import java.util.List;

import com.vouchera.backend.dto.UserResponse;
import com.vouchera.backend.dto.company.CompanyResponse;
import com.vouchera.backend.entity.User;

public class UserMapper {
    
    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        CompanyResponse company = user.getCompany() == null
            ? null
            : new CompanyResponse(
                user.getCompany().getId(),
                user.getCompany().getName(),
                user.getCompany().getCompanyStatus()
            );

        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getRole(),
            user.getAccountStatus(),
            user.getCreatedAt(),
            company
        );
    }

    public static List<UserResponse> toResponseList(List<User> users) {
        return users.stream().map(UserMapper::toResponse).toList();
    }
}
