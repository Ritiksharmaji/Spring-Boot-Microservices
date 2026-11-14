//package com.demokafka.producer;
//
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.web.bind.annotation.*;
//
//// creating a producer to  produce the message.
//@RestController
//@RequestMapping("/api")
//public class KafkaProducer {
//    // way-1: by sending normal String
////    private final KafkaTemplate<String , String> kafkaTemplate;
////    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
////        this.kafkaTemplate = kafkaTemplate;
////    }
////
////    @PostMapping("/send")
////    public String sendMessage(@RequestParam String message){
////
////
////        kafkaTemplate.send("my-topic", message);
////        return "Message send:"+ message;
////    }
//
//    //way-2: by sending object
//    private final KafkaTemplate<String , RiderLocation> kafkaTemplate;
//
//    public KafkaProducer(KafkaTemplate<String, RiderLocation> kafkaTemplate) {
//        this.kafkaTemplate = kafkaTemplate;
//    }
//
//    @PostMapping("/send")
//    public String sendMessage(@RequestParam String message){
//        RiderLocation riderLocation = new RiderLocation("Ritik sharma", 28.61, 77.23);
//
//       // kafkaTemplate.send("my-topic", riderLocation);
//        kafkaTemplate.send("rider-location-topic", riderLocation);
//        return "Message send:"+ riderLocation.getRiderId();
//    }
//}
