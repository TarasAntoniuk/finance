package com.tarasantoniuk.finance.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.provisioning")
public record ProvisioningProperties(Long defaultOrganizationId) {
}
