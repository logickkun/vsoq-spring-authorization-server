package com.logickkun.vsoq.spring.authorization.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** AuthN /internal/authn/verify 응답 매핑용(내부 전용) */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthNVerifyResponse {
    private boolean success;
    private List<String> roles;
}
