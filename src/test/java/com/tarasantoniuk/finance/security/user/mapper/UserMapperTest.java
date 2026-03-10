package com.tarasantoniuk.finance.security.user.mapper;


import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.security.user.dto.UserDetailDto;
import com.tarasantoniuk.finance.security.user.dto.UserSummaryDto;
import com.tarasantoniuk.finance.security.user.entity.User;
import com.tarasantoniuk.finance.security.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static com.tarasantoniuk.finance.security.common.TestDataFactorySecurity.createUser;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class) // не потрібен, але для консистентності
class UserMapperTest {

    private final UserMapper userMapper = new UserMapperImpl();

    @Test
    void toSummaryDto_ShouldMapAllFields() {
        User user = createUser(1L, "user@example.com", UserRole.USER, true);

        UserSummaryDto dto = userMapper.toSummaryDto(user);

        assertEquals(1L, dto.getId());
        assertEquals("user@example.com", dto.getEmail());
        assertEquals(UserRole.USER, dto.getRole());
        assertTrue(dto.getIsActive());
    }

    @Test
    void toDetailDto_ShouldMapAllFields() {
        User user = createUser(1L, "user@example.com", UserRole.USER, true);
        user.setFailedLoginAttempts(3);
        user.setLockedUntil(LocalDateTime.of(2026, 1, 1, 12, 0));

        UserDetailDto dto = userMapper.toDetailDto(user);

        assertEquals(1L, dto.getId());
        assertEquals("user@example.com", dto.getEmail());
        assertEquals(UserRole.USER, dto.getRole());
        assertTrue(dto.getIsActive());
        assertEquals(3, dto.getFailedLoginAttempts());
        assertEquals(LocalDateTime.of(2026, 1, 1, 12, 0), dto.getLockedUntil());
    }

    @Test
    void toDetailDto_WhenUserHasOrganization_ShouldReturnOrgId() {
        User user = createUser(1L, "user@example.com", UserRole.USER, true);
        Organization org = new Organization();
        org.setId(42L);
        user.setOrganization(org);

        UserDetailDto dto = userMapper.toDetailDto(user);

        assertEquals(42L, dto.getOrganizationId());
    }

    @Test
    void toDetailDto_WhenUserHasNoOrganization_ShouldReturnNullOrgId() {
        User user = createUser(1L, "user@example.com", UserRole.USER, true);

        UserDetailDto dto = userMapper.toDetailDto(user);

        assertNull(dto.getOrganizationId());
    }
}