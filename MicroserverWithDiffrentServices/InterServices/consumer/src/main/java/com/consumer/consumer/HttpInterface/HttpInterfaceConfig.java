package com.consumer.consumer.HttpInterface;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.client.support.RestTemplateAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class HttpInterfaceConfig {

    //  WebClient...
//    @Bean
//    public ProviderHttpInterface webClientHttpInterface(){
//        WebClient webClient = WebClient.create("http://localhost:8081");
//        WebClientAdapter adapter = WebClientAdapter.create(webClient);
//        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
////        RepositoryService service = factory.createClient(RepositoryService.class);
//        ProviderHttpInterface service = factory.createClient(ProviderHttpInterface.class);
//        return service;
//    }

//// For RestClient
//    @Bean
//    public ProviderHttpInterface webClientHttpInterface(){
//        RestClient restClient = RestClient.create("http://localhost:8081");
//        RestClientAdapter adapter = RestClientAdapter.create(restClient);
//        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
//        ProviderHttpInterface service = factory.createClient(ProviderHttpInterface.class);
//        return service;
//    }

    //// or RestTemplate...
    @Bean
    public ProviderHttpInterface webClientHttpInterface(){
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:8081"));
            RestTemplateAdapter adapter = RestTemplateAdapter.create(restTemplate);
            HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
            ProviderHttpInterface service = factory.createClient(ProviderHttpInterface.class);
        return service;
    }
}
