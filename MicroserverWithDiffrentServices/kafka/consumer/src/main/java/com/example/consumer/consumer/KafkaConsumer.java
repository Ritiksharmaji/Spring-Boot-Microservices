//package com.example.consumer.consumer;
//
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//@Component
//public class KafkaConsumer {
//
//    @KafkaListener(topics = "my-topic", groupId = "my-group")
//    public void Listen(String message){
//        System.out.println("Received message from 1 is:"+ message);
//    }
//
//    @KafkaListener(topics = "my-topic", groupId = "my-group")
//    public void Listen2(String message){
//        System.out.println("Received message 2 is:"+ message);
//    }
//
//    // third consumer for consuming the message or details of RiderLocation
//    //@KafkaListener(topics = "my-topic", groupId = "my-group-RiderLocation")
//    @KafkaListener(topics = "rider-location-topic", groupId = "my-group-RiderLocation")
//    public void RiderLocation(RiderLocation riderLocation){
//        System.out.println("Received message into RiderLocation consumer is:"+ riderLocation);
//        System.out.println("Received message into RiderLocation consumer is:"+ riderLocation.getRiderId()+ ":"+
//                + riderLocation.getLatitude() + ":" + riderLocation.getLongitude());
//    }
//}
