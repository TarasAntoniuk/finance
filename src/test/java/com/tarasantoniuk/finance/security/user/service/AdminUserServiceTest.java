package com.tarasantoniuk.finance.security.user.service;

import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;
import com.tarasantoniuk.finance.security.user.dto.UserDetailDto;
import com.tarasantoniuk.finance.security.user.dto.UserSummaryDto;
import com.tarasantoniuk.finance.security.user.entity.User;
import com.tarasantoniuk.finance.security.user.enums.UserRole;
import com.tarasantoniuk.finance.security.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static com.tarasantoniuk.finance.security.common.TestDataFactorySecurity.createUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    void listUsers_ShouldReturnPaginatedResponse() {
        User user = createUser(1L, "user@example.com", UserRole.USER, true);
        Page<User> page = new PageImpl<>(List.of(user), PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id")), 1);
        when(userRepository.findAll(PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id")))).thenReturn(page);

        PageResponse<UserSummaryDto> response = adminUserService.listUsers(0, 20);

        assertEquals(1, response.getContent().size());
        assertEquals("user@example.com", response.getContent().get(0).getEmail());
        assertEquals(UserRole.USER, response.getContent().get(0).getRole());
        assertEquals(0, response.getMetadata().getCurrentPage());
        assertEquals(1, response.getMetadata().getTotalElements());
    }

    @Test
    void getUser_WhenExists_ShouldReturnDetailDto() {
        User user = createUser(1L, "user@example.com", UserRole.USER, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDetailDto dto = adminUserService.getUser(1L);

        assertEquals(1L, dto.getId());
        assertEquals("user@example.com", dto.getEmail());
        assertEquals(UserRole.USER, dto.getRole());
        assertTrue(dto.getIsActive());
    }

    @Test
    void getUser_WhenNotFound_ShouldThrowException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminUserService.getUser(99L));
    }

    @Test
    void changeRole_WhenUserExists_ShouldUpdateRole() {
        User user = createUser(1L, "user@example.com", UserRole.USER, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminUserService.changeRole(1L, UserRole.ADMIN);

        assertEquals(UserRole.ADMIN, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void changeRole_WhenNotFound_ShouldThrowException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminUserService.changeRole(99L, UserRole.ADMIN));
    }

    @Test
    void setActive_WhenUserExists_ShouldUpdateStatus() {
        User user = createUser(1L, "user@example.com", UserRole.USER, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminUserService.setActive(1L, false);

        assertFalse(user.getIsActive());
        verify(userRepository).save(user);
    }

    @Test
    void setActive_WhenNotFound_ShouldThrowException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminUserService.setActive(99L, false));
    }

    @Test
    void getUser_WhenUserHasOrganization_ShouldReturnOrgId() {
        User user = createUser(1L, "user@example.com", UserRole.USER, true);
        Organization org = new Organization();
        org.setId(42L);
        user.setOrganization(org);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDetailDto dto = adminUserService.getUser(1L);

        assertEquals(42L, dto.getOrganizationId());
    }

    @Test
    void getUser_WhenUserHasNoOrganization_ShouldReturnNullOrgId() {
        User user = createUser(1L, "user@example.com", UserRole.USER, true);
        user.setOrganization(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDetailDto dto = adminUserService.getUser(1L);

        assertNull(dto.getOrganizationId());
    }

}
