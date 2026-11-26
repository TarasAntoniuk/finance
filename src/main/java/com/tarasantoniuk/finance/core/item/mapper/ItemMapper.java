package com.tarasantoniuk.finance.core.item.mapper;

import com.tarasantoniuk.finance.core.item.dto.ItemRequest;
import com.tarasantoniuk.finance.core.item.dto.ItemResponse;
import com.tarasantoniuk.finance.core.item.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Item toEntity(ItemRequest request);

    ItemResponse toResponse(Item item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(ItemRequest request, @MappingTarget Item item);
}
