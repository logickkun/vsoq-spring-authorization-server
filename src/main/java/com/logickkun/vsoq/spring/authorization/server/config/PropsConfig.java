package com.logickkun.vsoq.spring.authorization.server.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * 구성용 클래스:
 * 1) @ConfigurationProperties(AuthnProps) 활성화
 * 2) AuthN 호출용 WebClient 빈을 타임아웃 포함해 생성
 *
 * - Reactor Netty의 커넥션/응답 타임아웃을 설정.
 * - 응답 페이로드가 큰 경우를 대비해 기본 메모리 버퍼(코덱)도 넉넉히 조정 가능.
 */
@Configuration
@EnableConfigurationProperties({ AuthnProps.class })
public class PropsConfig {

    /**
     * AuthN 호출에 사용할 WebClient.
     *
     * 주의:
     * - 여기서 만든 WebClient는 "공통 커넥터 + 타임아웃"만 설정한다.
     * - 실제 요청 URL/헤더/바디는 RemoteAuthNClient에서 조립한다.
     */
    @Bean
    public WebClient authnWebClient(AuthnProps props) {
        // Reactor Netty HTTP 클라이언트에 타임아웃 적용
        HttpClient http = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(props.connectTimeout().toMillis()))
                .responseTimeout(props.readTimeout());

        // (선택) Codec 메모리 제한을 완화하고 싶다면 아래처럼 확장 가능
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB
                .build();

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(http))
                .exchangeStrategies(strategies)
                .build();
    }
}
