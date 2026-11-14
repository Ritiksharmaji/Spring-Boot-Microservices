package com.ecommerce.shop.identity.internal;

import com.ecommerce.shop.shared.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.ecommerce.shop.identity.internal.IdentityDtos.*;

@Tag(name = "Addresses", description = "Address book of the signed-in customer (the default one is used at checkout)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/addresses")
class AddressController {

    private final AddressService addresses;

    @Operation(summary = "My addresses (default first)")
    @GetMapping
    List<AddressResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return addresses.list(CurrentUser.id(jwt));
    }

    @Operation(summary = "Add an address (the first one becomes the default)")
    @PostMapping
    ResponseEntity<AddressResponse> create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addresses.create(CurrentUser.id(jwt), request));
    }

    @Operation(summary = "Update an address (isDefault=true makes it the default)")
    @PutMapping("/{id}")
    AddressResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable long id, @Valid @RequestBody AddressRequest request) {
        return addresses.update(CurrentUser.id(jwt), id, request);
    }

    @Operation(summary = "Delete an address")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        addresses.delete(CurrentUser.id(jwt), id);
        return ResponseEntity.noContent().build();
    }
}
