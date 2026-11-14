package com.ecommerce.shop.payment.internal;

import com.ecommerce.shop.shared.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payments", description = "Payment status (payments are created automatically after checkout)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
class PaymentController {

    private final PaymentService payments;

    @Operation(summary = "Payment of one of my orders")
    @GetMapping("/orders/{orderId}")
    PaymentService.PaymentResponse forOrder(@AuthenticationPrincipal Jwt jwt, @PathVariable long orderId) {
        return payments.forOrder(CurrentUser.id(jwt), orderId);
    }

    @Operation(summary = "All payments (ADMIN)")
    @GetMapping("/admin")
    Page<PaymentService.PaymentResponse> all(@ParameterObject @PageableDefault(size = 20, sort = "id",
            direction = Sort.Direction.DESC) Pageable pageable) {
        return payments.all(pageable);
    }
}
