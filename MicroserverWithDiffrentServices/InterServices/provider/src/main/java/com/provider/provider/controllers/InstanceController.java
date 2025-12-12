package com.provider.provider.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InstanceController {

    @Value("${server.port}")
    private String port;

    private final String instanceId = java.util.UUID.randomUUID().toString();


    @GetMapping("/instance-info")
    public String getInstanceInfo(){
        System.out.print("Request recevied at instance running on port:" + port);
        return "returning served by port:"+ port+ ". instnace Id:" +instanceId ;
    }
}
