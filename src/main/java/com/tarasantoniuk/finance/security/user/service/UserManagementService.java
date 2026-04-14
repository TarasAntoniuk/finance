package com.tarasantoniuk.finance.security.user.service;

import com.tarasantoniuk.finance.common.dto.PageMetadata;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserManagementService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserRevocationService userRevocationService;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserManagementService(UserRepository userRepository,
                                 UserMapper userMapper,
                                 UserRevocationService userRevocationService,
                                 RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userRevocationService = userRevocationService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserSummaryDto> listUsers(int page, int size) {
        Page<User> userPage = userRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id")));

        List<UserSummaryDto> content = userPage.getContent().stream()
                .map(userMapper::toSummaryDto)
                .toList();

        PageMetadata metadata = PageMetadata.builder()
                .currentPage(userPage.getNumber())
                .totalPages(userPage.getTotalPages())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();

        return PageResponse.<UserSummaryDto>builder()
                .content(content)
                .metadata(metadata)
                .build();
    }

    @Transactional(readOnly = true)
    public UserDetailDto getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toDetailDto(user);
    }

    public void changeRole(Long id, UserRole role) {
        Long currentUserId = getCurrentUserId();
        if (id.equals(currentUserId)) {
            throw new SelfModificationException("Cannot change your own role");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (user.getRole() == UserRole.ADMIN && role != UserRole.ADMIN) {
            long adminCount = userRepository.countByRole(UserRole.ADMIN);
            if (adminCount <= 1) {
                throw new LastAdminException("Cannot remove the last admin role");
            }
        }

        user.setRole(role);
        userRepository.save(user);

        userRevocationService.revoke(id);
        refreshTokenRepository.revokeAllByUserId(id);
    }

    public void setActive(Long id, boolean active) {
        Long currentUserId = getCurrentUserId();
        if (id.equals(currentUserId)) {
            throw new SelfModificationException("Cannot change your own status");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setIsActive(active);
        userRepository.save(user);

        if (!active) {
            userRevocationService.revoke(id);
            refreshTokenRepository.revokeAllByUserId(id);
        }
    }

    private Long getCurrentUserId() {
        JwtPrincipal principal = (JwtPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return principal.userId();
    }

}
