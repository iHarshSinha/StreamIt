package com.streamit.groupchatapp.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "Public endpoint works";
    }

    @GetMapping("/private")
    public String privateEndpoint() {
        return "Private endpoint works";
    }
}