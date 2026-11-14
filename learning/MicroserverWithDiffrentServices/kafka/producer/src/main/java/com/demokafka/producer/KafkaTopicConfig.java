package com.demokafka.producer;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic createMyTopic(){
        return new NewTopic("my-topic-java-based", 3, (short) 1);
    }
}
