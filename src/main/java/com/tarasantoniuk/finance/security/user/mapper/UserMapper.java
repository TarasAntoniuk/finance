package com.tarasantoniuk.finance.security.user.mapper;

import com.tarasantoniuk.finance.security.user.dto.UserDetailDto;
import com.tarasantoniuk.finance.security.user.dto.UserSummaryDto;
import com.tarasantoniuk.finance.security.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserSummaryDto toSummaryDto(User user);

    @Mapping(target = "organizationId",
            expression = "java(user.getOrganization() != null ? user.getOrganization().getId() : null)")
    UserDetailDto toDetailDto(User user);
}