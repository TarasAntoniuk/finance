package com.tarasantoniuk.finance.common;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.Arrays;


class PropertyTest extends BaseIntegrationTest {

    @Autowired
    private Environment env;

    @Test
    void test() {
        System.out.println("=== Active profiles: " + Arrays.toString(env.getActiveProfiles()));
        System.out.println("=== ecb.sync.enabled = " + env.getProperty("ecb.sync.enabled"));
        System.out.println("=== ecb.sync.initial-load = " + env.getProperty("ecb.sync.initial-load"));
    }
}