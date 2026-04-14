package com.tarasantoniuk.finance.security.token.entity;

import com.tarasantoniuk.finance.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "blacklisted_tokens", indexes = {
        @Index(name = "idx_blacklisted_token_expires", columnList = "expiresAt")
})
public class BlacklistedToken extends BaseEntity {

    @Id
    @Column(length = 64, nullable = false)
    private String jti;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public BlacklistedToken() {
    }

    public BlacklistedToken(String jti, LocalDateTime expiresAt) {
        this.jti = jti;
        this.expiresAt = expiresAt;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
