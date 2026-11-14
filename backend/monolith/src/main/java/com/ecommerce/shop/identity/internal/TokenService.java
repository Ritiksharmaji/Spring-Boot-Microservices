package com.ecommerce.shop.identity.internal;

import com.ecommerce.shop.shared.security.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/** Issues HS256 access tokens: sub = user id, roles, email, name. */
@Service
@RequiredArgsConstructor
class TokenService {

    private final JwtEncoder encoder;
    private final SecurityProperties props;

    String issue(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiresAt(now.plus(props.tokenTtl()))
                .claim("roles", user.getRole() == com.ecommerce.shop.identity.Role.ADMIN
                        ? List.of("ADMIN", "CUSTOMER") : List.of("CUSTOMER"))
                .claim("email", user.getEmail())
                .claim("name", user.fullName())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    long ttlSeconds() {
        return props.tokenTtl().toSeconds();
    }
}
