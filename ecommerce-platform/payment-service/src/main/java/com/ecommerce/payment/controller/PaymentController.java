package com.ecommerce.payment.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.exception.ForbiddenException;
import com.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/** X-User-ID / X-User-Roles are set by the API gateway from the verified JWT. */
@Tag(name = "Payments", description = "Payment status")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /** Payment status of one of the caller's orders. */
    @GetMapping("/orders/{orderId}")
    public PaymentResponse forMyOrder(@Parameter(hidden = true) @RequestHeader("X-User-ID") String userId, @PathVariable Long orderId) {
        return paymentService.getForOrder(orderId, userId);
    }

    @GetMapping("/admin")
    public Page<PaymentResponse> all(@Parameter(hidden = true) @RequestHeader(value = "X-User-Roles", required = false) String roles,
                                     @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)
                                     Pageable pageable) {
        boolean admin = roles != null && Arrays.stream(roles.split(",")).map(String::trim)
                .anyMatch("ADMIN"::equalsIgnoreCase);
        if (!admin) {
            throw new ForbiddenException("Admin role required");
        }
        return paymentService.getAll(pageable);
    }
}
