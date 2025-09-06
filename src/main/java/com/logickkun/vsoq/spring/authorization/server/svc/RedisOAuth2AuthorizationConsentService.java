// src/main/java/com/logickkun/vsoq/spring/authorization/server/svc/RedisOAuth2AuthorizationConsentService.java
package com.logickkun.vsoq.spring.authorization.server.svc;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.stereotype.Component;

@Component
public class RedisOAuth2AuthorizationConsentService implements OAuth2AuthorizationConsentService {

    private static final String KEY = "authz:consent"; // 해시: {registeredClientId|principalName} -> consent json
    private final RedisTemplate<Object, Object> tpl;

    public RedisOAuth2AuthorizationConsentService(
            @Qualifier("redisTemplate")
            RedisTemplate<Object, Object> tpl) {
        this.tpl = tpl;
    }

    private String field(String clientId, String principal) {
        return clientId + "|" + principal;
    }

    @Override
    public void save(OAuth2AuthorizationConsent consent) {
        HashOperations<Object, Object, Object> ops = tpl.opsForHash();
        ops.put(KEY, field(consent.getRegisteredClientId(), consent.getPrincipalName()), consent);
    }

    @Override
    public void remove(OAuth2AuthorizationConsent consent) {
        tpl.opsForHash().delete(KEY, field(consent.getRegisteredClientId(), consent.getPrincipalName()));
    }

    @Override
    public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
        Object v = tpl.opsForHash().get(KEY, field(registeredClientId, principalName));
        return (OAuth2AuthorizationConsent) v;
    }
}
