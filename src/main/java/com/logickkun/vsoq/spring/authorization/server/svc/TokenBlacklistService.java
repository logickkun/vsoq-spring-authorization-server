package com.logickkun.vsoq.spring.authorization.server.svc;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {
    private static final String KEY_PREFIX = "authz:blacklist:jti:";
    private final StringRedisTemplate srt;

    public TokenBlacklistService(StringRedisTemplate srt) { this.srt = srt; }

    public void blacklist(String jti, long ttlSeconds) {
        srt.opsForValue().set(KEY_PREFIX + jti, "1", ttlSeconds, TimeUnit.SECONDS);
    }

    public boolean isBlacklisted(String jti) {
        Boolean has = srt.hasKey(KEY_PREFIX + jti);
        return has != null && has;
    }
}
