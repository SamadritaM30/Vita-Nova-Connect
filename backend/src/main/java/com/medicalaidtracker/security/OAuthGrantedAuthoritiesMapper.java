package com.medicalaidtracker.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuthGrantedAuthoritiesMapper implements GrantedAuthoritiesMapper {

    private final OAuthAccountService accountService;

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(
            Collection<? extends GrantedAuthority> authorities
    ) {
        Map<String, Object> attributes = authorities.stream()
                .filter(a -> a instanceof OidcUserAuthority || a instanceof OAuth2UserAuthority)
                .findFirst()
                .map(this::attributes)
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        new OAuth2Error("missing_google_profile"), "Google profile attributes are missing."));

        Object verified = attributes.get("email_verified");
        if (!Boolean.TRUE.equals(verified)) {
            throw new OAuth2AuthenticationException(new OAuth2Error("email_not_verified"),
                    "A verified Google email is required.");
        }

        var user = accountService.provisionGoogleUser(
                string(attributes.get("email")),
                string(attributes.get("name")),
                string(attributes.get("picture")),
                string(attributes.get("sub")),
                true
        );
        var mapped = new HashSet<GrantedAuthority>(authorities);
        mapped.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return mapped;
    }

    private Map<String, Object> attributes(GrantedAuthority authority) {
        if (authority instanceof OidcUserAuthority oidc) {
            return oidc.getAttributes();
        }
        return ((OAuth2UserAuthority) authority).getAttributes();
    }

    private String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
