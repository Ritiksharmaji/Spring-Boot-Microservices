package com.example.PKCE;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    @GetMapping("/api/home")
    public String home() {
        return "Public Home";
    }

    @GetMapping("/api/secure")
    public String secure(Authentication authentication) {
        return "Hello " + authentication.getName();
    }
    @GetMapping("/api/userDetails")
    public String secure(@AuthenticationPrincipal Jwt jwt) {
        String name = jwt.getClaim("preferred_username");
        return "Hello " + name;
    }
}
