package com.make.side.security;

import com.make.side.entity.Member;
import com.make.side.repository.MemberJpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationHelper {

    private final MemberJpaRepository memberRepository;

    public AuthenticationHelper(MemberJpaRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * Extract current authenticated member from JWT token.
     * Maps Keycloak 'sub' claim to Member entity via externalId.
     */
    public Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("No authenticated user");
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String externalId = jwt.getSubject();  // 'sub' claim from Keycloak

            return memberRepository.findByExternalId(externalId)
                .orElseThrow(() -> new SecurityException(
                    "Member not found for authenticated user: " + externalId
                ));
        }

        throw new SecurityException("Invalid authentication type");
    }

    /**
     * Get current member ID.
     */
    public Long getCurrentMemberId() {
        return getCurrentMember().getId();
    }

    /**
     * Check if current user is authenticated.
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}
