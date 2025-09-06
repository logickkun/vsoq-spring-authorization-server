package com.logickkun.vsoq.spring.authorization.server.svc;

import com.logickkun.vsoq.spring.authorization.server.config.AuthProps;
import com.logickkun.vsoq.spring.authorization.server.impl.JwtService;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NimbusJwtService implements JwtService {

    private final JWKSource<SecurityContext> jwkSource;
    private final AuthProps authProps; // issuer 등

    @Override
    public String createAccess(String username, List<String> roles) {
        return signJwt(username, roles, Duration.ofMinutes(15));
    }

    @Override
    public String createRefresh(String username, List<String> roles) {
        return signJwt(username, roles, Duration.ofDays(14));
    }

    private String signJwt(String sub, List<String> roles, Duration ttl) {
        try {
            // 1) JWK 선택
            JWKSelector selector = new JWKSelector(new JWKMatcher.Builder().keyType(KeyType.RSA).build());
            List<JWK> jwks = jwkSource.get(selector, null);
            RSAKey rsa = (RSAKey) jwks.get(0);

            // 2) Claims
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(authProps.issuer())
                    .subject(sub)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plus(ttl)))
                    .claim("roles", roles)
                    .jwtID(UUID.randomUUID().toString())
                    .build();

            // 3) 서명
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(rsa.getKeyID())
                    .type(JOSEObjectType.JWT)
                    .build();

            SignedJWT jwt = new SignedJWT(header, claims);
            RSASSASigner signer = new RSASSASigner(rsa.toPrivateKey());
            jwt.sign(signer);

            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("JWT signing failed", e);
        }
    }

    // Nimbus가 요구하는 컨텍스트 더미
    static class JWKSelectionContext implements SecurityContext {}
}

