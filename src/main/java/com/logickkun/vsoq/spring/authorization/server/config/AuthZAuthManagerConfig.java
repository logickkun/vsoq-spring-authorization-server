package com.logickkun.vsoq.spring.authorization.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;

@Configuration
public class AuthZAuthManagerConfig {

    @Bean
    public AuthenticationManager authZAuthenticationManager(RemoteAuthNProvider remoteAuthNProvider) {
        return new ProviderManager(remoteAuthNProvider);
    }
}
