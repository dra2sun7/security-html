package com.example.demo.controller;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.demo.service.KubernetesService;

import java.util.List;

@Controller
public class TestController {
    private final KubernetesService kubernetesService;

    public TestController(KubernetesService kubernetesService) {
        this.kubernetesService = kubernetesService;
    }

    @GetMapping(value = "/home")
    public String home(){
        return "hello";
    }

    @PostMapping("/runCurl")
    public String runCurl(@RequestParam String apiServer, @RequestParam String token, Model model){
        kubernetesService.deployJobFromYaml(apiServer, token);

        return "hello";
    }
}