package com.app_ecom.clients;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ProductServiceClientConfig {

    @Bean
    public ProductServiceClient productServiceClient(RestClient.Builder loadBalancedBuilder) {
        RestClient restClient = loadBalancedBuilder.clone().baseUrl("http://product-service").build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(ProductServiceClient.class);
    }
}
