package com.consumer.consumer.feign;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feign")
public class FeignConroller {

    private final ProviderFeignClients providerFeignClients;

    public FeignConroller(ProviderFeignClients providerFeignClients) {
        this.providerFeignClients = providerFeignClients;
    }

    @GetMapping("/instance")
    public String getInstance() {
    return  providerFeignClients.getInstanceInfo();
    }

}


