// vsoq-spring-authorization-server
// src/main/java/com/logickkun/vsoq/spring/authorization/server/auth/AuthZLoginController.java
package com.logickkun.vsoq.spring.authorization.server.ctr;

import com.logickkun.vsoq.spring.authorization.server.impl.JwtService;
import com.logickkun.vsoq.spring.authorization.server.vo.ApiResponse;
import com.logickkun.vsoq.spring.authorization.server.vo.AuthZVo;
import com.logickkun.vsoq.spring.authorization.server.vo.SessionVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthZLoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<SessionVo>> login(@RequestBody AuthZVo body) {

        log.info("AuthZLoginController.login() 로그인 진입");

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(body.getUsername().trim(), body.getPassword())
        );

        var roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        var username = body.getUsername().trim();

        // 1) 토큰 발급 (동일 JWK로 서명해야 게이트웨이 리소스서버가 검증 가능)
        String access  = jwtService.createAccess(username, roles);
        String refresh = jwtService.createRefresh(username, roles);

        // 2) 쿠키 세팅 (로컬 HTTP라 secure=false, 실제 HTTPS면 true)
        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", access)
                .httpOnly(true).secure(false)
                .path("/")                       // 모든 경로 전송
                .sameSite("Strict")              // OIDC 브라우저 리다이렉트가 필요하면 Lax 고려
                .maxAge(Duration.ofMinutes(15))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", refresh)
                .httpOnly(true).secure(false)
                .path("/auth")                   // 리프레시용 엔드포인트 범위로 제한
                .sameSite("Strict")
                .maxAge(Duration.ofDays(14))
                .build();

        SessionVo session = SessionVo.basic(username, roles);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new ApiResponse<>(true, "로그인 성공", session));
    }
}

