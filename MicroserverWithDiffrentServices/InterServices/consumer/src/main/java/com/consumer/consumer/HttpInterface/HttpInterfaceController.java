package com.consumer.consumer.HttpInterface;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/httpinterface")
public class HttpInterfaceController {
    private final ProviderHttpInterface providerHttpInterface;

    public HttpInterfaceController(ProviderHttpInterface providerHttpInterface) {
        this.providerHttpInterface = providerHttpInterface;
    }

    @GetMapping("/instance")
    public String getInstance(){
    return  providerHttpInterface.getInstance();
    }
}
