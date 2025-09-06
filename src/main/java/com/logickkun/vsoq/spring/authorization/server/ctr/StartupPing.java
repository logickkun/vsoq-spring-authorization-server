package com.logickkun.vsoq.spring.authorization.server.ctr;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class StartupPing implements CommandLineRunner {
    private final StringRedisTemplate srt;

    @Override public void run(String... args) {
        srt.opsForValue().set("authz:ping", "pong", 60, TimeUnit.SECONDS);
        var v = srt.opsForValue().get("authz:ping");
        System.out.println("Redis ping = " + v); // pong 나오면 연결 OK
    }
}
