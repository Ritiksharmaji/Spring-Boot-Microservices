package com.ecommerce.shop.payment.internal;

import com.ecommerce.shop.ordering.OrderPayments;
import com.ecommerce.shop.ordering.OrderPlaced;
import com.ecommerce.shop.ordering.OrderStatus;
import com.ecommerce.shop.ordering.OrderStatusChanged;
import com.ecommerce.shop.ordering.PaymentMethod;
import com.ecommerce.shop.shared.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Event listeners run asynchronously AFTER the publishing transaction commits, each in its own
 * transaction; their publication is tracked by the event registry and retried if they fail.
 */
@Slf4j
@Service
@RequiredArgsConstructor
class PaymentService {

    private final PaymentRepository payments;
    private final PaymentProvider provider;
    private final OrderPayments orderPayments;

    /** Charges the order exactly once (unique orderId); redelivery re-reports the stored outcome. */
    @ApplicationModuleListener
    void on(OrderPlaced event) {
        Payment payment = payments.findByOrderId(event.orderId()).orElse(null);
        if (payment == null) {
            payment = new Payment();
            payment.setOrderId(event.orderId());
            payment.setUserId(event.userId());
            payment.setAmount(event.totalAmount());
            if (event.paymentMethod() == PaymentMethod.CASH) {
                payment.setStatus(PaymentStatus.CASH_ON_DELIVERY);       // nothing to charge now
            } else {
                PaymentProvider.Result result = provider.charge(event.orderId(), event.totalAmount());
                payment.setStatus(result.success() ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
                payment.setTransactionRef(result.reference());
                payment.setFailureReason(result.failureReason());
            }
            payments.saveAndFlush(payment);
            log.info("Order {}: payment {}", event.orderId(), payment.getStatus());
        }
        switch (payment.getStatus()) {
            case COMPLETED -> orderPayments.paymentSucceeded(event.orderId());
            case FAILED -> orderPayments.paymentFailed(event.orderId(), payment.getFailureReason());
            case CASH_ON_DELIVERY -> orderPayments.cashOnDeliveryAccepted(event.orderId());
            default -> log.info("Order {} not charged ({})", event.orderId(), payment.getStatus());
        }
    }

    /**
     * Cancelled: refund a captured card payment, drop an uncollected cash payment, or make sure an
     * order cancelled before payment never gets charged. Delivered: cash has been collected.
     */
    @ApplicationModuleListener
    void on(OrderStatusChanged event) {
        if (event.status() == OrderStatus.DELIVERED) {
            payments.findByOrderId(event.orderId())
                    .filter(p -> p.getStatus() == PaymentStatus.CASH_ON_DELIVERY)
                    .ifPresent(p -> p.setStatus(PaymentStatus.COMPLETED));
            return;
        }
        if (event.status() != OrderStatus.CANCELLED) {
            return;
        }
        payments.findByOrderId(event.orderId()).ifPresentOrElse(payment -> {
            if (payment.getStatus() == PaymentStatus.COMPLETED) {
                provider.refund(payment.getTransactionRef(), payment.getAmount());
                payment.setStatus(PaymentStatus.REFUNDED);
                payment.setRefundedAt(LocalDateTime.now());
                orderPayments.paymentRefunded(event.orderId());
                log.info("Order {} cancelled: payment {} refunded", event.orderId(), payment.getId());
            } else if (payment.getStatus() == PaymentStatus.CASH_ON_DELIVERY) {
                payment.setStatus(PaymentStatus.SKIPPED);
                payment.setFailureReason("Order cancelled before delivery");
            }
        }, () -> {
            Payment skipped = new Payment();
            skipped.setOrderId(event.orderId());
            skipped.setUserId(event.userId());
            skipped.setAmount(event.totalAmount());
            skipped.setStatus(PaymentStatus.SKIPPED);
            skipped.setFailureReason("Order cancelled before payment");
            payments.save(skipped);
        });
    }

    @Transactional(readOnly = true)
    public PaymentResponse forOrder(long userId, long orderId) {
        return payments.findByOrderId(orderId)
                .filter(p -> p.getUserId() == userId)
                .map(PaymentResponse::from)
                .orElseThrow(() -> BusinessException.notFound("No payment found for order " + orderId));
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> all(Pageable pageable) {
        return payments.findAll(pageable).map(PaymentResponse::from);
    }

    record PaymentResponse(Long id, Long orderId, Long userId, java.math.BigDecimal amount, PaymentStatus status,
                           String transactionRef, String failureReason, LocalDateTime createdAt, LocalDateTime refundedAt) {
        static PaymentResponse from(Payment p) {
            return new PaymentResponse(p.getId(), p.getOrderId(), p.getUserId(), p.getAmount(), p.getStatus(),
                    p.getTransactionRef(), p.getFailureReason(), p.getCreatedAt(), p.getRefundedAt());
        }
    }
}
