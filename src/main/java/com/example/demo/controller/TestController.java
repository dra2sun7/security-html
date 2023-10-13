package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.KubernetesService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TestController {
    private final KubernetesService kubernetesService;
    public TestController(KubernetesService kubernetesService) {
        this.kubernetesService = kubernetesService;
    }
    @RequestMapping(value = "/runCurl", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<List<String>> runCurl(@RequestParam String apiServer, @RequestParam String token, Model model){
        List<String> logMessage = kubernetesService.deployJobFromYaml(apiServer, token);
        return ResponseEntity.ok(logMessage);
    }
}