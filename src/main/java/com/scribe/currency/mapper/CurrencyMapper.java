package com.scribe.currency.mapper;

import com.scribe.currency.dto.CurrencyDto;
import com.scribe.currency.entity.CurrencyEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CurrencyMapper {
    CurrencyDto toDto(CurrencyEntity entity);
    CurrencyEntity toEntity(CurrencyDto dto);
}
