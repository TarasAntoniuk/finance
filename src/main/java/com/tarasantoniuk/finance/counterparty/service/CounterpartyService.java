package com.tarasantoniuk.finance.counterparty.service;

import com.tarasantoniuk.finance.counterparty.dto.CounterpartyRequestDto;
import com.tarasantoniuk.finance.counterparty.dto.CounterpartyResponseDto;

import java.util.List;

public interface CounterpartyService {

    CounterpartyResponseDto create(CounterpartyRequestDto request);

    CounterpartyResponseDto getById(Long id);

    List<CounterpartyResponseDto> getAll();

    CounterpartyResponseDto update(Long id, CounterpartyRequestDto request);

    void delete(Long id);

    void activate(Long id);

    void deactivate(Long id);
}