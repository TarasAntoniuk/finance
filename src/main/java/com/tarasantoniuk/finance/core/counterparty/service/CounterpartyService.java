package com.tarasantoniuk.finance.core.counterparty.service;

import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyRequestDto;
import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyResponseDto;

public interface CounterpartyService {

    CounterpartyResponseDto create(CounterpartyRequestDto request);

    CounterpartyResponseDto getById(Long id);

    PageResponse<CounterpartyResponseDto> getAll(int page, int size);

    CounterpartyResponseDto update(Long id, CounterpartyRequestDto request);

    void delete(Long id);

    void activate(Long id);

    void deactivate(Long id);
}