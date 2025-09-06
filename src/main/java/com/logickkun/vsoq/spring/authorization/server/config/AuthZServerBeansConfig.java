
package com.logickkun.vsoq.spring.authorization.server.config;

import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.security.oauth2.server.authorization.client.*;
import org.springframework.security.oauth2.server.authorization.settings.*;


import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.util.UUID;

@Configuration
@EnableConfigurationProperties(AuthProps.class)
public class AuthZServerBeansConfig {

    /** 필수 ①: RegisteredClientRepository — PKCE 공개 클라이언트(SPA) 예시 */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient spa = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("vsoq-spa")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE) // 공개 클라이언트
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/auth/callback") // 게이트웨이 기준 콜백
                .postLogoutRedirectUri("http://localhost:8080/")
                .scope(OidcScopes.OPENID)
                .scope("profile")
                .scope("offline_access")
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)               // PKCE 필수
                        .requireAuthorizationConsent(false)  // 동의 화면 생략(원하면 true)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        .refreshTokenTimeToLive(Duration.ofDays(14))
                        .reuseRefreshTokens(false)
                        .build())
                .build();

        RegisteredClient svc = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("svc-client")
                .clientSecret("{noop}svc-secret") // 테스트용
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("read")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(spa, svc); // 🔽 두 개 리턴
    }

    /** 필수 ②: AuthorizationService (토큰 저장소) — 메모리 구현 */
//    @Bean
//    @ConditionalOnMissingBean(OAuth2AuthorizationService.class)
//    public OAuth2AuthorizationService authorizationService(RegisteredClientRepository clients) {
//        return new InMemoryOAuth2AuthorizationService();
//    }

    /** 권장: Consent 저장소 — 메모리 구현 */
//    @Bean
//    public OAuth2AuthorizationConsentService authorizationConsentService() {
//        return new InMemoryOAuth2AuthorizationConsentService();
//    }

    /** 필수 ③: JWKSource — 토큰 서명 키(DEV용 RSA 2048) */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsa = generateRsa();
        JWKSet jwkSet = new JWKSet(rsa);
        return (selector, ctx) -> selector.select(jwkSet);
    }

    private static RSAKey generateRsa() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            return new RSAKey.Builder((java.security.interfaces.RSAPublicKey) kp.getPublic())
                    .privateKey(kp.getPrivate())
                    .keyID(UUID.randomUUID().toString())
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate RSA key", e);
        }
    }

    /** 필수 ④: AuthorizationServerSettings — issuer 반드시 설정 */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings(AuthProps props) {
        // issuer는 리소스 서버/클라이언트가 바라보는 외부 주소와 정확히 일치해야 함
        return AuthorizationServerSettings.builder()
                .issuer(props.issuer())   // 예: http://localhost:8082 or https://auth.vsoq.local
                .build();
    }
}
