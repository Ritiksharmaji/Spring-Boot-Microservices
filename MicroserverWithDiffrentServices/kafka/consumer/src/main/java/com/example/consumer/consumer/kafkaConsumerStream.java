package com.example.consumer.consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class kafkaConsumerStream {

    @Bean
    public Consumer<RiderLocation> processRiderLocation(){
        return location ->{
            System.out.println("Recevied: "+ location.getRiderId() + "@ "+ location.getLongitude() + " ," + location.getLongitude());

        };
    }
}
