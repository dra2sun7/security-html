package com.example.demo.controller;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.demo.service.KubernetesService;

import java.util.List;

@Controller
public class TestController {
    private final KubernetesService kubernetesService;

    public TestController(KubernetesService kubernetesService) {
        this.kubernetesService = kubernetesService;
    }

    @RequestMapping(value = "/home")
    public String home(){
        return "hello";
    }

    @PostMapping(value = "/printNodes")
    public String printNodes(@RequestParam String apiserver, @RequestParam String token, Model model) {
        List<String> nodeNames = kubernetesService.getNodeNames(apiserver, token);
        model.addAttribute("nodeNames", nodeNames);
        return "hello";
    }
}
