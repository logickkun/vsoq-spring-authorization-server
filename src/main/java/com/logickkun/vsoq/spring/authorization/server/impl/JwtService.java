package com.logickkun.vsoq.spring.authorization.server.impl;

import java.util.List;

/**
 * JWT 발급 서비스 인터페이스.
 */
public interface JwtService {
    String createAccess(String username, List<String> roles);
    String createRefresh(String username, List<String> roles);
}
