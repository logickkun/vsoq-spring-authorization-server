// vsoq-spring-authorization-server
// src/main/java/com/logickkun/vsoq/spring/authorization/server/config/RemoteAuthNProvider.java
package com.logickkun.vsoq.spring.authorization.server.config;

import com.logickkun.vsoq.spring.authorization.server.vo.AuthNVerifyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RemoteAuthNProvider implements AuthenticationProvider {

    private final RemoteAuthNClient client;
    private final AuthnProps props;


    @Override
    public Authentication authenticate(Authentication authentication) {
        if (!(authentication instanceof UsernamePasswordAuthenticationToken up)) {
            return null;
        }

        String username = Objects.toString(up.getName(), "");
        String rawPassword = Objects.toString(up.getCredentials(), "");

        AuthNVerifyResponse resp = client.verify(username, rawPassword)
                .block(Duration.ofMillis(Math.max(1L, props.readTimeout().toMillis())));

        if (resp == null || !resp.isSuccess()) {
            throw new BadCredentialsException("Invalid credentials");
        }

        var authorities = resp.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
