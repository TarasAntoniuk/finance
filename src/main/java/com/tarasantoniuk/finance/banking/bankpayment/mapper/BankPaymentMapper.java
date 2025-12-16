package com.tarasantoniuk.finance.banking.bankpayment.mapper;

import com.tarasantoniuk.finance.banking.bankpayment.dto.BankPaymentRequestDto;
import com.tarasantoniuk.finance.banking.bankpayment.dto.BankPaymentResponseDto;
import com.tarasantoniuk.finance.banking.bankpayment.entity.BankPayment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BankPaymentMapper {

    /**
     * Map request DTO to entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "counterpartyBankAccount", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "postedAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BankPayment toEntity(BankPaymentRequestDto requestDto);

    /**
     * Map entity to response DTO
     */
    BankPaymentResponseDto toResponseDto(BankPayment payment);

    /**
     * Update existing entity from request DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "counterpartyBankAccount", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "postedAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(BankPaymentRequestDto requestDto, @MappingTarget BankPayment payment);
}