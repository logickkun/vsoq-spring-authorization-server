// src/main/java/com/logickkun/vsoq/spring/authorization/server/svc/RedisOAuth2AuthorizationService.java
package com.logickkun.vsoq.spring.authorization.server.svc;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Primary
@Component
public class RedisOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private static final String KEY_PREFIX = "authz:authorization:"; // authz:authorization:{id}
    private final RedisTemplate<String, OAuth2Authorization> tpl;

    public RedisOAuth2AuthorizationService(
            @Qualifier("oauth2AuthorizationRedisTemplate")
            RedisTemplate<String, OAuth2Authorization> tpl) {
        this.tpl = tpl;
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        Objects.requireNonNull(authorization, "authorization");
        String key = KEY_PREFIX + authorization.getId();
        ValueOperations<String, OAuth2Authorization> ops = tpl.opsForValue();
        ops.set(key, authorization);

        long ttlSeconds = computeTtlSeconds(authorization);
        if (ttlSeconds > 0) {
            tpl.expire(key, ttlSeconds, TimeUnit.SECONDS);
        } else {
            tpl.expire(key, 7, TimeUnit.DAYS);
        }
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        if (authorization == null) return;
        tpl.delete(KEY_PREFIX + authorization.getId());
    }

    @Override
    public OAuth2Authorization findById(String id) {
        return tpl.opsForValue().get(KEY_PREFIX + id);
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        // 최소 구현: 인덱스 없이 미구현
        return null;
    }

    private long computeTtlSeconds(OAuth2Authorization authz) {
        Instant now = Instant.now();
        Instant max = null;

        if (authz.getAccessToken() != null) {
            var c = authz.getAccessToken().getToken().getExpiresAt();
            if (c != null) max = (max == null || max.isBefore(c)) ? c : max;
        }
        if (authz.getRefreshToken() != null) {
            var c = authz.getRefreshToken().getToken().getExpiresAt();
            if (c != null) max = (max == null || max.isBefore(c)) ? c : max;
        }
        var code = authz.getToken(OAuth2AuthorizationCode.class);
        if (code != null && code.getToken().getExpiresAt() != null) {
            var c = code.getToken().getExpiresAt();
            max = (max == null || max.isBefore(c)) ? c : max;
        }

        if (max == null) return -1;
        long sec = max.getEpochSecond() - now.getEpochSecond();
        return Math.max(sec, 1);
    }
}
