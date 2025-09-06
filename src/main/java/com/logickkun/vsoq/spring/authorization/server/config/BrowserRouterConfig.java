package com.logickkun.vsoq.spring.authorization.server.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BrowserRouterConfig {

    @GetMapping({
            "/",
            "/api",
            "/{single:^(?!api$)(?!.*\\.)[A-Za-z0-9\\-]+}",
            "/{first:^(?!api$)(?!.*\\.)[A-Za-z0-9\\-]+}/{second:^(?!.*\\.)[A-Za-z0-9\\-]+}"
    })
    public String getIndex(HttpServletRequest request) {
        return "/index.html";
    }
}

