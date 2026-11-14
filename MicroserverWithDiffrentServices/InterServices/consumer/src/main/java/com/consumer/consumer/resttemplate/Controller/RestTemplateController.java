package com.consumer.consumer.resttemplate.Controller;

import com.consumer.consumer.resttemplate.Services.RestTemplateClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/rest-template")
public class RestTemplateController {
   private final RestTemplateClient restTemplateClient;

    public RestTemplateController(RestTemplateClient restTemplateClient) {
        this.restTemplateClient = restTemplateClient;
    }

    @GetMapping("/instance")
    public String getInstance(){
//        RestTemplate restTemplate = new RestTemplate();
//        String response = restTemplate.
//                getForObject("http://localhost:8081/instance-info", String.class);
//
//        return response;
        // =========== other way doing with proper struccture
       String response =  restTemplateClient.getInstance();
        return response;
    }
}
