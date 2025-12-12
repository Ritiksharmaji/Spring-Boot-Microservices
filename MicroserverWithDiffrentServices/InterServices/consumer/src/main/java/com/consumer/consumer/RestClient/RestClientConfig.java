package com.consumer.consumer.RestClient;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {


//    @Bean
//    @LoadBalanced
//    public RestClient restClient(RestClient.Builder builder){
////        String  PROVIDER_url = "http://localhost:8081";
//        String  PROVIDER_url = "http://provider";
//
//        return builder.baseUrl(PROVIDER_url)
//                .build();
//    }
    @Bean
    @LoadBalanced
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder
                .baseUrl("http://provider")
                .build();
    }
}
