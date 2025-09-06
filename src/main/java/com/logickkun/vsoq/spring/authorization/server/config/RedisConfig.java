// src/main/java/com/logickkun/vsoq/spring/authorization/server/config/RedisConfig.java
package com.logickkun.vsoq.spring.authorization.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;

@Configuration
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(); // spring.data.redis.* 설정 사용
    }

    @Bean
    public ObjectMapper authzObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));
        om.registerModule(new OAuth2AuthorizationServerJackson2Module());
        om.registerModule(new JavaTimeModule());
        return om;
    }

    // OAuth2Authorization 전용 템플릿 (이름 고정)
    @Bean(name = "oauth2AuthorizationRedisTemplate")
    public RedisTemplate<String, OAuth2Authorization> oauth2AuthorizationRedisTemplate(
            RedisConnectionFactory cf, ObjectMapper authzObjectMapper) {

        RedisTemplate<String, OAuth2Authorization> tpl = new RedisTemplate<>();
        tpl.setConnectionFactory(cf);

        StringRedisSerializer keySer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valSer = new GenericJackson2JsonRedisSerializer(authzObjectMapper);

        tpl.setKeySerializer(keySer);
        tpl.setHashKeySerializer(keySer);
        tpl.setValueSerializer(valSer);
        tpl.setHashValueSerializer(valSer);
        tpl.afterPropertiesSet();
        return tpl;
    }

    // 범용(Object,Object) 템플릿 (이름: redisTemplate)
    @Bean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(
            RedisConnectionFactory cf, ObjectMapper authzObjectMapper) {

        RedisTemplate<Object, Object> tpl = new RedisTemplate<>();
        tpl.setConnectionFactory(cf);

        StringRedisSerializer keySer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valSer = new GenericJackson2JsonRedisSerializer(authzObjectMapper);

        tpl.setKeySerializer(keySer);
        tpl.setHashKeySerializer(keySer);
        tpl.setValueSerializer(valSer);
        tpl.setHashValueSerializer(valSer);
        tpl.afterPropertiesSet();
        return tpl;
    }
}
