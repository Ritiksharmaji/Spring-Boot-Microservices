package com.example.consumer.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    @KafkaListener(topics = "my-topic", groupId = "my-group")
    public void Listen(String message){
        System.out.println("Received message from 1 is:"+ message);
    }

    @KafkaListener(topics = "my-topic", groupId = "my-group")
    public void Listen2(String message){
        System.out.println("Received message 2 is:"+ message);
    }
}
