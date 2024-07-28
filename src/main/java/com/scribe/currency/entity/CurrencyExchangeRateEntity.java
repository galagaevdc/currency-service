package com.scribe.currency.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "currency_exchange_rate", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"from_currency_id", "to_currency_id"})
})
public class CurrencyExchangeRateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_currency_id", nullable = false)
    private CurrencyEntity from;

    @ManyToOne
    @JoinColumn(name = "to_currency_id", nullable = false)
    private CurrencyEntity to;

    private BigDecimal conversionRate;

    @Column(name = "actualization_date", nullable = false)
    private LocalDateTime actualizationDate;
}
