package com.demokafka.producer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Supplier;

@Configuration
public class KafkaProducerStream {

    @Bean
    public Supplier<RiderLocation> sendRiderLocation(){
            return ()-> {
                RiderLocation location = new RiderLocation("Ritik new", 50.8, 44.3);
                System.out.println("Sending location:"+ location.getRiderId());
                return location;
            };
    }
}
