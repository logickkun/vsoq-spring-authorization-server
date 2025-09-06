package com.logickkun.vsoq.spring.authorization.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** application.yml 의 auth.issuer 바인딩 */
@ConfigurationProperties(prefix = "auth")
public record AuthProps(String issuer) {}