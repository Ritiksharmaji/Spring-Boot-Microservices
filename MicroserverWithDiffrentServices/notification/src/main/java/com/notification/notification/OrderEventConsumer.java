package com.notification.notification;


import com.notification.notification.payload.OrderCreateEvent;
import com.notification.notification.payload.OrderStatus;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderEventConsumer {

//    @RabbitListener(queues = "${rabbitmq.queue.name}")
//    public void handleOrderEvent(Map<String, Object> orderEvent){
//        System.out.println("Received Order Event:"+ orderEvent);
//
//        Long orderId = Long.valueOf(orderEvent.get("orderId").toString());
//        String status = orderEvent.get("status").toString();
//
//        // now after getting message we can send them to
//        // update Database
//        // send emails
//        // send notifiaction
//        // generate Invoice
//        // send seller notification
//
//
//    }

    // ============ by dto ==============
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleOrderEvent(OrderCreateEvent orderEvent){
        System.out.println("Received Order Event:"+ orderEvent);

        Long orderId = orderEvent.getOrderId();
        OrderStatus status = orderEvent.getStatus();
        System.out.println("Order ID:"+ orderId);
        System.out.println("Order status:"+ status);

        // now after getting message we can send them to
        // update Database
        // send emails
        // send notifiaction
        // generate Invoice
        // send seller notification


    }
}
