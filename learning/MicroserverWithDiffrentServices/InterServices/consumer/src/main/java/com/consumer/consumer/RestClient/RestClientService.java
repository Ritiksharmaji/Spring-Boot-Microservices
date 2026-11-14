package com.consumer.consumer.RestClient;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RestClientService {
    private final RestClient restClient;

    public RestClientService(RestClient restClient) {
        this.restClient = restClient;
    }
    public String getInstance(){
        return restClient.get().uri("/instance-info")
                .retrieve()
                .body(String.class);

    }
}
