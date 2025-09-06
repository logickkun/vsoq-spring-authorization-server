package com.logickkun.vsoq.spring.authorization.server.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.util.List;


/**
 * 로그인 결과(세션 뷰) 전송용 DTO.
 * - username/roles는 항상 제공
 * - access/refresh 토큰과 만료는 상황에 따라 null일 수 있으니 @JsonInclude로 생략
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionVo {

    private String id;

    private String username;

    private List<String> roles;          // 권한 리스트

    private String accessToken;          // JWT 액세스 토큰
    private String refreshToken;         // 리프레시 토큰

    private Instant accessTokenExpiry;   // 액세스 토큰 만료 시각
    private Instant refreshTokenExpiry;  // 리프레시 토큰 만료 시각

    /** 편의 생성자: 토큰 없이 기본 정보만 */
    public SessionVo(String username, List<String> roles) {
        this.username = username;
        this.roles = roles;
    }

    /** 편의 팩토리: 토큰 없이 */
    public static SessionVo basic(String username, List<String> roles) {
        return new SessionVo(username, roles);
    }

    /** 편의 팩토리: 토큰 포함 */
    public static SessionVo withTokens(
            String username, List<String> roles,
            String accessToken, Instant accessExp,
            String refreshToken, Instant refreshExp
    ) {
        return SessionVo.builder()
                .username(username)
                .roles(roles)
                .accessToken(accessToken)
                .accessTokenExpiry(accessExp)
                .refreshToken(refreshToken)
                .refreshTokenExpiry(refreshExp)
                .build();
    }
}
