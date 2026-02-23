package com.tarasantoniuk.finance.security.audit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;

class ClientIpResolverTest {

    private ClientIpResolver clientIpResolver;

    @BeforeEach
    void setUp() {
        clientIpResolver = new ClientIpResolver();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    // ========== NO REQUEST CONTEXT ==========

    @Test
    void resolve_WhenNoRequestContext_ShouldReturnUnknown() {
        RequestContextHolder.resetRequestAttributes();

        String ip = clientIpResolver.resolve();

        assertEquals("unknown", ip);
    }

    // ========== X-FORWARDED-FOR HEADER ==========

    @Test
    void resolve_WhenXForwardedForPresent_ShouldReturnFirstIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.50");
        request.setRemoteAddr("10.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String ip = clientIpResolver.resolve();

        assertEquals("203.0.113.50", ip);
    }

    @Test
    void resolve_WhenXForwardedForHasMultipleIps_ShouldReturnFirstOnly() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.50, 70.41.3.18, 150.172.238.178");
        request.setRemoteAddr("10.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String ip = clientIpResolver.resolve();

        assertEquals("203.0.113.50", ip);
    }

    @Test
    void resolve_WhenXForwardedForHasWhitespace_ShouldTrimResult() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "  203.0.113.50  , 70.41.3.18");
        request.setRemoteAddr("10.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String ip = clientIpResolver.resolve();

        assertEquals("203.0.113.50", ip);
    }

    @Test
    void resolve_WhenXForwardedForIsEmpty_ShouldFallBackToRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "");
        request.setRemoteAddr("192.168.1.100");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String ip = clientIpResolver.resolve();

        assertEquals("192.168.1.100", ip);
    }

    // ========== REMOTE ADDR FALLBACK ==========

    @Test
    void resolve_WhenNoXForwardedFor_ShouldReturnRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String ip = clientIpResolver.resolve();

        assertEquals("192.168.1.100", ip);
    }

    @Test
    void resolve_WhenRemoteAddrIsIpv6_ShouldReturnIpv6() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("::1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String ip = clientIpResolver.resolve();

        assertEquals("::1", ip);
    }
}
