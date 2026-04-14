package com.tarasantoniuk.finance.security.common;

import com.tarasantoniuk.finance.security.user.dto.UserDetailDto;
import com.tarasantoniuk.finance.security.user.dto.UserSummaryDto;
import com.tarasantoniuk.finance.security.user.entity.User;
import com.tarasantoniuk.finance.security.user.enums.UserRole;

public class TestDataFactorySecurity {

    public static User createUser(Long id, String email, UserRole role, boolean active) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setRole(role);
        user.setIsActive(active);
        return user;
    }

    public static UserDetailDto createUserDetailDto(Long id, String email, UserRole role, boolean active) {
        UserDetailDto dto = new UserDetailDto();
        dto.setId(id);
        dto.setEmail(email);
        dto.setRole(role);
        dto.setIsActive(active);
        return dto;
    }

    public static UserSummaryDto createUserSummaryDto(Long id, String email, UserRole role, boolean active) {
        return new UserSummaryDto(id, email, role, active);
    }
}
