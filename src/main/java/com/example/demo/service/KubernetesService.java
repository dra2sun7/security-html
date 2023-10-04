package com.example.demo.service;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayInputStream;


@Service
public class KubernetesService {

    public void deployJobFromYaml(String apiServer, String token) {
        Config config = new ConfigBuilder()
                .withMasterUrl(apiServer)
                .withOauthToken(token)
                .withTrustCerts(true)
                .withNamespace("default")
                .build();

        try (KubernetesClient kubernetesClient = new DefaultKubernetesClient(config)) {
            try {
                String yamlTemplate = loadYamlTemplateFromFile();
                String yamlContent = yamlTemplate.replace("${nodeName}", "worker1");
                System.out.println("yaml 파일의 내용 : \n"+yamlContent);
                InputStream inputStream = new ByteArrayInputStream(yamlContent.getBytes());

                kubernetesClient.load(inputStream).create();
                System.out.println("Job created successfully.");
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (KubernetesClientException e) {
            e.printStackTrace();
            // Handle Kubernetes client errors
        }
    }

    private String loadYamlTemplateFromFile() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/application.yml")) {
            byte[] bytes = StreamUtils.copyToByteArray(inputStream);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
}