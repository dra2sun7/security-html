package com.example.demo.service;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class KubernetesService {

    public void deployJobFromYaml(String apiServer, String token) {
        // Initialize the Kubernetes client
        Config config = new ConfigBuilder()
                .withMasterUrl(apiServer)
                .withOauthToken(token)
                .withTrustCerts(true)
                .build();

        try (KubernetesClient kubernetesClient = new DefaultKubernetesClient(config)) {
            // Load the YAML file from Spring Boot's resources
            try (InputStream inputStream = new ClassPathResource("application.yaml").getInputStream()) {
                String yamlTemplate = loadYamlTemplate(inputStream);

                String yamlContent = yamlTemplate.replace("${nodeName}", "worker1");

                Job job = (Job) kubernetesClient.load(inputStream).get();

                kubernetesClient.batch().v1().jobs().inNamespace("default").create(job);

                System.out.println("Job created successfully.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (KubernetesClientException e) {
            e.printStackTrace();
            // Handle Kubernetes client errors
        }
    }

    private String loadYamlTemplate(InputStream inputStream) {
        try {
            byte[] bytes = StreamUtils.copyToByteArray(inputStream);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
