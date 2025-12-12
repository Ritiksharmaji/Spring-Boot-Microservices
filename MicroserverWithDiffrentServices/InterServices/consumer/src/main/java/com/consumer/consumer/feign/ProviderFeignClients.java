package com.consumer.consumer.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "provider-service", url = "http://localhost:8081")
public interface ProviderFeignClients {
    @GetMapping("/instance-info")
    String getInstanceInfo();
}
