package com.consumer.consumer.WebClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/web-client")
public class WebClientController {

    @GetMapping("/instance")
    public Mono<String> getInstance(){
        WebClient webClient = WebClient.create();
       Mono<String> object =  webClient.get()
                .uri("http://localhost:8081/instance-info")
                .retrieve()
                .bodyToMono(String.class);

       return object;
    }
}


