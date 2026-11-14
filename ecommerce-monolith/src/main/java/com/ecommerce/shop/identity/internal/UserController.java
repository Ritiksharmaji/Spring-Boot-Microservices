package com.ecommerce.shop.identity.internal;

import com.ecommerce.shop.shared.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import static com.ecommerce.shop.identity.internal.IdentityDtos.*;

@Tag(name = "Users", description = "Own profile; user administration (ADMIN)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
class UserController {

    private final IdentityService identity;

    @Operation(summary = "My profile")
    @GetMapping("/me")
    UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return identity.me(CurrentUser.id(jwt));
    }

    @Operation(summary = "Update my name, phone and delivery address")
    @PutMapping("/me")
    UserResponse updateMe(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ProfileRequest request) {
        return identity.updateProfile(CurrentUser.id(jwt), request);
    }

    @Operation(summary = "All users (ADMIN)")
    @GetMapping
    Page<UserResponse> list(@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return identity.list(pageable);
    }

    @Operation(summary = "One user (ADMIN)")
    @GetMapping("/{id}")
    UserResponse get(@PathVariable long id) {
        return identity.get(id);
    }

    @Operation(summary = "Change a user's role (ADMIN)")
    @PutMapping("/{id}/role")
    UserResponse changeRole(@PathVariable long id, @RequestBody RoleRequest request) {
        return identity.changeRole(id, request.role());
    }
}
