package com.consumer.consumer.RestClient;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Map;

@RestController
@RequestMapping("/api/rest-client")
public class RestClientController {

    private final RestClientService restClientService;

    public RestClientController(RestClientService restClientService) {
        this.restClientService = restClientService;
    }

    @GetMapping("/instance")
    public String getInstnace(){
        RestClient defaultClient = RestClient.create();
//       String respose =  defaultClient.get()
//                .uri("http://localhost:8081/instance-info")
//                .retrieve()
//                .body(String.class);
//       return  respose;

        // ======== other way ======
        return restClientService.getInstance();
    }
}
