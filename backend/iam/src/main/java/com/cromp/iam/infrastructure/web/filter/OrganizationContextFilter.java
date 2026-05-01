package com.cromp.iam.infrastructure.web.filter;

import com.cromp.iam.application.port.OrganizationContextPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OrganizationContextFilter extends OncePerRequestFilter {

    private final OrganizationContextPort organizationContextPort;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Long orgId = (Long) request.getAttribute("organizationId");
        if (orgId != null) {
            organizationContextPort.setCurrentOrganizationId(orgId);
        } else {
            organizationContextPort.clear();
        }
        filterChain.doFilter(request, response);
        organizationContextPort.clear(); // очистка после запроса
    }
}