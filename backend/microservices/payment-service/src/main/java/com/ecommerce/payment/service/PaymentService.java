package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.exception.ResourceNotFoundException;
import com.ecommerce.payment.messaging.OrderCreatedEvent;
import com.ecommerce.payment.messaging.PaymentResultEvent;
import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.model.PaymentStatus;
import com.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    static final String PAYMENT_RESULT_BINDING = "paymentResult-out-0";

    private final PaymentRepository repository;
    private final PaymentProvider provider;
    private final StreamBridge streamBridge;

    /**
     * Charges an order exactly once. On redelivery of the same event the stored outcome is
     * re-published instead of charging again (idempotent consumer). If the order was
     * cancelled before we got here, nothing is charged.
     */
    @Transactional
    public void processPayment(OrderCreatedEvent event) {
        Payment payment = repository.findByOrderId(event.orderId()).orElse(null);
        if (payment == null) {
            payment = new Payment();
            payment.setOrderId(event.orderId());
            payment.setUserId(event.userId());
            payment.setAmount(event.totalAmount());

            PaymentProvider.Result result = provider.charge(event.orderId(), event.totalAmount());
            payment.setStatus(result.success() ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
            payment.setTransactionRef(result.transactionRef());
            payment.setFailureReason(result.failureReason());
            payment = repository.saveAndFlush(payment);
            log.info("Order {}: payment {} ({})", event.orderId(), payment.getStatus(),
                    result.success() ? result.transactionRef() : result.failureReason());
        } else {
            log.info("Order {} already processed ({}); re-publishing result", event.orderId(), payment.getStatus());
        }

        if (payment.getStatus() == PaymentStatus.SKIPPED) {
            return; // order cancelled earlier: no charge, no result needed
        }
        publishResult(payment);
    }

    /** Refunds a captured payment, or records that the order must not be charged later. */
    @Transactional
    public void handleOrderCancelled(Long orderId, String userId, BigDecimal amount) {
        Payment payment = repository.findByOrderId(orderId).orElse(null);
        if (payment == null) {
            Payment skipped = new Payment();
            skipped.setOrderId(orderId);
            skipped.setUserId(userId);
            skipped.setAmount(amount);
            skipped.setStatus(PaymentStatus.SKIPPED);
            skipped.setFailureReason("Order cancelled before payment");
            repository.save(skipped);
            log.info("Order {} cancelled before payment; it will not be charged", orderId);
            return;
        }
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            provider.refund(payment.getTransactionRef(), payment.getAmount());
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundedAt(LocalDateTime.now());
            log.info("Order {} cancelled; payment {} refunded", orderId, payment.getId());
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponse getForOrder(Long orderId, String userId) {
        return repository.findByOrderId(orderId)
                .filter(p -> userId == null || p.getUserId().equals(userId))
                .map(PaymentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("No payment found for order " + orderId));
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAll(Pageable pageable) {
        return repository.findAll(pageable).map(PaymentResponse::from);
    }

    private void publishResult(Payment payment) {
        PaymentResultEvent.Status status = payment.getStatus() == PaymentStatus.FAILED
                ? PaymentResultEvent.Status.FAILED : PaymentResultEvent.Status.COMPLETED;
        PaymentResultEvent result = new PaymentResultEvent(payment.getOrderId(), payment.getUserId(),
                payment.getId(), status, payment.getAmount(), payment.getFailureReason(), LocalDateTime.now());
        if (!streamBridge.send(PAYMENT_RESULT_BINDING, result)) {
            // Throwing makes the broker redeliver the order event; the stored payment is then re-published.
            throw new IllegalStateException("Could not publish payment result for order " + payment.getOrderId());
        }
    }
}
