package com.tarasantoniuk.finance.core.country.entity;

import com.tarasantoniuk.finance.common.entity.BaseEntity;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "countries")
public class Country extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "iso_code", nullable = false, unique = true, length = 3)
    private String isoCode;

    @Column(name = "phone_code", length = 10)
    private String phoneCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id")
    private Currency currency;

    public Country() {
    }

    public Country(Long id, String name, String isoCode, String phoneCode, Currency currency) {
        this.id = id;
        this.name = name;
        this.isoCode = isoCode;
        this.phoneCode = phoneCode;
        this.currency = currency;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Country country = (Country) o;
        return Objects.equals(id, country.id) && Objects.equals(isoCode, country.isoCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, isoCode);
    }

    @Override
    public String toString() {
        return "Country{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isoCode='" + isoCode + '\'' +
                ", phoneCode='" + phoneCode + '\'' +
                '}';
    }
}