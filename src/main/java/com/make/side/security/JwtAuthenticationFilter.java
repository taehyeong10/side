package com.make.side.security;

import com.make.side.entity.Member;
import com.make.side.repository.MemberJpaRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that auto-creates Member records on first login from Keycloak.
 * Runs after JWT authentication but before authorization.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final MemberJpaRepository memberRepository;

    public JwtAuthenticationFilter(MemberJpaRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Only process if user is authenticated with JWT
        if (authentication != null && authentication.isAuthenticated()
            && authentication.getPrincipal() instanceof Jwt jwt) {

            String externalId = jwt.getSubject();  // Keycloak 'sub' claim
            String name = jwt.getClaimAsString("preferred_username");  // Keycloak username

            // Check if Member exists, create if not
            if (!memberRepository.findByExternalId(externalId).isPresent()) {
                createMember(externalId, name != null ? name : "User");
            }
        }

        filterChain.doFilter(request, response);
    }

    private void createMember(String externalId, String name) {
        try {
            // Find next available ID
            Long nextId = memberRepository.findAll()
                .stream()
                .map(Member::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1;

            Member member = new Member();
            // Use reflection to set private fields (alternative: add setter or builder)
            java.lang.reflect.Field idField = Member.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(member, nextId);

            java.lang.reflect.Field nameField = Member.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(member, name);

            member.setExternalId(externalId);

            java.lang.reflect.Field timeField = Member.class.getDeclaredField("time");
            timeField.setAccessible(true);
            timeField.set(member, java.time.Instant.now());

            memberRepository.save(member);

            logger.info("Auto-created member for external_id: " + externalId + " with ID: " + nextId);
        } catch (Exception e) {
            logger.error("Failed to auto-create member for external_id: " + externalId, e);
        }
    }
}
