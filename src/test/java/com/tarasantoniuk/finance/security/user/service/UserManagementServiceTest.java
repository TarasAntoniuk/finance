package com.tarasantoniuk.finance.security.user.service;

import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;
import com.tarasantoniuk.finance.security.jwt.JwtPrincipal;
import com.tarasantoniuk.finance.security.user.dto.UserDetailDto;
import com.tarasantoniuk.finance.security.user.dto.UserSummaryDto;
import com.tarasantoniuk.finance.security.user.entity.User;
import com.tarasantoniuk.finance.security.user.enums.UserRole;
import com.tarasantoniuk.finance.security.user.exception.LastAdminException;
import com.tarasantoniuk.finance.security.user.exception.SelfModificationException;
import com.tarasantoniuk.finance.security.user.mapper.UserMapper;
import com.tarasantoniuk.finance.security.user.repository.UserRepository;
import com.tarasantoniuk.finance.security.token.repository.RefreshTokenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static com.tarasantoniuk.finance.security.common.TestDataFactorySecurity.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRevocationService userRevocationService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private UserManagementService userManagementService;

    @BeforeEach
    void setUp() {
        setCurrentUser(100L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listUsers_ShouldReturnPaginatedResponse() {

        User user = createUser(1L, "user@example.com", UserRole.USER, true);
        Page<User> page = new PageImpl<>(List.of(user), PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id")), 1);
        UserSummaryDto expectedDto = createUserSummaryDto(1L, "user@example.com", UserRole.USER, true);



        when(userRepository.findAll(PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id")))).thenReturn(page);
        when(userMapper.toSummaryDto(user)).thenReturn(expectedDto);

        PageResponse<UserSummaryDto> response = userManagementService.listUsers(0, 20);

        assertEquals(1, response.getContent().size());
        assertEquals("user@example.com", response.getContent().get(0).getEmail());
        assertEquals(UserRole.USER, response.getContent().get(0).getRole());
        assertEquals(0, response.getMetadata().getCurrentPage());
        assertEquals(1, response.getMetadata().getTotalElements());
    }

    @Test
    void getUser_WhenExists_ShouldReturnDetailDto() {

        User user = createUser(1L, "user@example.com", UserRole.USER, true);
        UserDetailDto expectedDto = createUserDetailDto(1L, "user@example.com", UserRole.USER, true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDetailDto(user)).thenReturn(expectedDto);

        UserDetailDto dto = userManagementService.getUser(1L);

        assertEquals(1L, dto.getId());
        assertEquals("user@example.com", dto.getEmail());
        assertEquals(UserRole.USER, dto.getRole());
        assertTrue(dto.getIsActive());
    }

    @Test
    void getUser_WhenNotFound_ShouldThrowException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userManagementService.getUser(99L));
    }

    @Test
    void changeRole_WhenUserExists_ShouldUpdateRole() {
        User user = createUser(1L, "user@example.com", UserRole.USER, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userManagementService.changeRole(1L, UserRole.ADMIN);

        assertEquals(UserRole.ADMIN, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void changeRole_WhenNotFound_ShouldThrowException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userManagementService.changeRole(99L, UserRole.ADMIN));
    }

    @Test
    void setActive_WhenUserExists_ShouldUpdateStatus() {
        User user = createUser(1L, "user@example.com", UserRole.USER, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userManagementService.setActive(1L, false);

        assertFalse(user.getIsActive());
        verify(userRepository).save(user);
    }

    @Test
    void setActive_WhenNotFound_ShouldThrowException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userManagementService.setActive(99L, false));
    }

    @Test
    void changeRole_WhenSelfModification_ShouldThrowException() {
        setCurrentUser(1L);

        assertThrows(SelfModificationException.class,
                () -> userManagementService.changeRole(1L, UserRole.GUEST));
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void changeRole_WhenLastAdmin_ShouldThrowException() {
        User admin = createUser(2L, "admin@example.com", UserRole.ADMIN, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);

        assertThrows(LastAdminException.class,
                () -> userManagementService.changeRole(2L, UserRole.USER));
        verify(userRepository, never()).save(any());
    }

    @Test
    void changeRole_WhenMultipleAdmins_ShouldAllowDemotion() {
        User admin = createUser(2L, "admin@example.com", UserRole.ADMIN, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(2L);

        userManagementService.changeRole(2L, UserRole.USER);

        assertEquals(UserRole.USER, admin.getRole());
        verify(userRepository).save(admin);
    }

    @Test
    void setActive_WhenSelfModification_ShouldThrowException() {
        setCurrentUser(1L);

        assertThrows(SelfModificationException.class,
                () -> userManagementService.setActive(1L, false));
        verify(userRepository, never()).findById(anyLong());
    }

    private void setCurrentUser(Long userId) {
        JwtPrincipal principal = new JwtPrincipal(userId, "admin@example.com", UserRole.ADMIN, null);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
