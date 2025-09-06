// vsoq-spring-authorization-server
// src/main/java/com/logickkun/vsoq/spring/authorization/server/config/RemoteAuthNClient.java
package com.logickkun.vsoq.spring.authorization.server.config;

import com.logickkun.vsoq.spring.authorization.server.vo.AuthZVo;
import com.logickkun.vsoq.spring.authorization.server.vo.AuthNVerifyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class RemoteAuthNClient {

    private final WebClient authnWebClient; // PropsConfig에서 주입된 WebClient
    private final AuthnProps props;         // baseUri, verifyPath, key, timeouts

    public reactor.core.publisher.Mono<AuthNVerifyResponse> verify(String username, String password) {
        URI uri = props.baseUri().resolve(props.verifyPath());
        AuthZVo body = new AuthZVo(username, password);

        return authnWebClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-AuthZ-Key", props.key())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(AuthNVerifyResponse.class)
                // 네트워크/역직렬화 오류는 실패로 매핑(Provider에서 BadCredentials로 통일)
                .onErrorReturn(new AuthNVerifyResponse(false, java.util.List.of()));
    }
}
