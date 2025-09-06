package com.logickkun.vsoq.spring.authorization.server.config;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AuthZ → AuthN 위임 호출에 필요한 연결 정보를 바인딩한다.
 *
 * application.yml 예시:
 * authn:
 *   realms:
 *     web:
 *       base-uri: http://localhost:8081
 *       verify-path: /internal/authn/verify
 *       key: ${AUTHZ_INTERNAL_KEY_WEB:local-dev-key}
 *       connect-timeout: 1500ms
 *       read-timeout: 2000ms
 */
@ConfigurationProperties(prefix = "authn.realms.web")
public record AuthnProps(
        /**
         * AuthN 서버의 베이스 URI (스킴/호스트/포트 포함).
         * 예: http://localhost:8081
         */
        URI baseUri,

        /**
         * 계정 검증 엔드포인트 상대 경로.
         * 예: /internal/authn/verify
         */
        String verifyPath,

        /**
         * AuthZ ↔ AuthN 내부 통신 보호용 공유 키.
         * HTTP 헤더(예: X-AuthZ-Key)에 실어 보낸다.
         */
        String key,

        /**
         * TCP 연결 타임아웃.
         */
        Duration connectTimeout,

        /**
         * 응답 수신 타임아웃(서버 처리 포함).
         */
        Duration readTimeout
) { }
