package com.make.side.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PortRoutingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(PortRoutingFilter.class);

    @Value("${server.health-port:8081}")
    private int healthPort;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        int requestPort = httpRequest.getLocalPort();
        String requestUri = httpRequest.getRequestURI();

        // 8081 포트: /actuator/health 외 모든 요청 차단
        if (requestPort == healthPort && !requestUri.startsWith("/actuator/health")) {
            log.warn("[PORT-FILTER] 차단 - port={}, uri={} → 403", requestPort, requestUri);
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Only health check allowed on this port");
            return;
        }

        // 8080 포트: /actuator 접근 차단
        if (requestPort != healthPort && requestUri.startsWith("/actuator")) {
            log.warn("[PORT-FILTER] 차단 - port={}, uri={} → 404", requestPort, requestUri);
            httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        log.debug("[PORT-FILTER] 통과 - port={}, uri={}", requestPort, requestUri);
        chain.doFilter(request, response);
    }
}