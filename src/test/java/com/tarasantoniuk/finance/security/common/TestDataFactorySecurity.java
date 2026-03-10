package com.tarasantoniuk.finance.security.common;

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
}
