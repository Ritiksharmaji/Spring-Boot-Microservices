package com.demokafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

// creating a producer to  produce the message.
@RestController
@RequestMapping("/api")
public class KafkaProducer {

    private final KafkaTemplate<String , String> kafkaTemplate;
    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam String message){

        kafkaTemplate.send("my-topic", message);
        return "Message send:"+ message;
    }
}
