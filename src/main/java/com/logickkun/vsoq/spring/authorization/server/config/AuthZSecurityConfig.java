package com.logickkun.vsoq.spring.authorization.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
public class AuthZSecurityConfig {

    /**
     * [Order(1)] Spring Authorization Server 전용 체인
     *
     * - securityMatcher(...):
     *   SAS가 노출하는 모든 프로토콜 엔드포인트 집합에만 이 체인이 적용됨
     *   예) /oauth2/authorize, /oauth2/token, /.well-known/openid-configuration, /oauth2/jwks 등
     *
     * - with(authorizationServer, config -> config.oidc(...)):
     *   OIDC 기능 활성화 (well-known, userinfo, logout 등)
     *
     * - exceptionHandling().authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")):
     *   SAS 엔드포인트 접근 시 인증이 없으면 /login 으로 보내 로그인부터 하도록 함
     *
     * - csrf().ignoringRequestMatchers(...):
     *   SAS 엔드포인트에 대해 CSRF 검사 제외 (프로토콜 엔드포인트 특성상 필요 없음)
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authZSecurityFilterChain(HttpSecurity http) throws Exception {
        var authorizationServer = OAuth2AuthorizationServerConfigurer.authorizationServer();

        return http

                .securityMatcher(authorizationServer.getEndpointsMatcher())
                .with(authorizationServer, config -> config
                        .oidc(Customizer.withDefaults()))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("http://localhost:8080/login")))
                .csrf(csrf -> csrf.
                        ignoringRequestMatchers(authorizationServer.getEndpointsMatcher()))
                .build();

    }


    /**
     * [Order(2)] 일반 애플리케이션 체인
     *
     * - authorizeHttpRequests(...):
     *   /login 및 정적 리소스, /.well-known/**는 모두 허용
     *   그 외는 인증 필요
     *
     * - authenticationProvider(remoteAuthNProvider):
     *   /login POST 시 입력된 username/password를 이 Provider가 받아서
     *   내부적으로 "AuthN 서버의 /internal/authn/verify"를 HTTP로 호출해 검증.
     *   성공 시 Authentication을 만들어 SecurityContext에 넣어줌.
     *
     * - formLogin(loginPage("/login")):
     *   커스텀 로그인 페이지를 사용하겠다는 의미.
     *   => 반드시 /login GET을 렌더링하는 컨트롤러/뷰를 제공해야 함 (기본 로그인 페이지 미제공).
     *   (기본 로그인 페이지를 쓰려면 loginPage 지정 없이 formLogin(Customizer.withDefaults()))
     *
     * - logout("/logout"):
     *   표준 로그아웃 엔드포인트 지정(필요에 따라 handler/성공 URL 추가 구성 가능)
     */
    @Bean
    @Order(2)
    public SecurityFilterChain applicationSecurityFilterChain(HttpSecurity http,
                                                              AuthenticationProvider remoteAuthNProvider // your bean that calls AuthN server
    ) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                // 로그인 화면 및 정적 리소스 허용
                                "/login",
                                "/auth/login",
                                "/index.html",
                                "/vite.svg",
                                "/assets/**",
                                "/favicon.ico",

                                // OIDC 디스커버리 문서 등을 SPA/게이트웨이에서 조회할 수 있도록 허용
                                "/.well-known/**"
                        )
                        .permitAll()

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // JSON 로그인은 CSRF 제외
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/auth/login")
                )
                // 폼 로그인 시도(username/password) → remoteAuthNProvider가 AuthN 서버에 위임
                .authenticationProvider(remoteAuthNProvider)

                // 커스텀 로그인 페이지 사용
                .formLogin(form -> form
                        .loginPage("/login")      // custom login view (or default if you don't render one)
                        .permitAll()
                )
                // 로그아웃 엔드포인트
                .logout(logout -> logout
                        .logoutUrl("/logout")
                );

        return http.build();
    }


}
