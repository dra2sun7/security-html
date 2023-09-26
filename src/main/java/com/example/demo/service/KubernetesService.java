package com.example.demo.service;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayInputStream;
import java.util.List;


@Service
public class KubernetesService {

    public void deployJobFromYaml(String apiServer, String token) {
        Config config = new ConfigBuilder()
                .withMasterUrl(apiServer)
                .withOauthToken(token)
                .withTrustCerts(true)
                .build();

        try (KubernetesClient kubernetesClient = new DefaultKubernetesClient(config)) {
            try {
                String yamlTemplate = loadYamlTemplateFromFile();
                String yamlContent = yamlTemplate.replace("${nodeName}", "worker1");

                InputStream inputStream = new ByteArrayInputStream(yamlContent.getBytes());

                List<HasMetadata> resources = kubernetesClient.load(inputStream).get();

                Job job = null;
                for (HasMetadata resource : resources) {
                    if (resource instanceof Job) {
                        job = (Job) resource;
                        break;
                    }
                }

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

    private String loadYamlTemplateFromFile() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/application.yml")) {
            byte[] bytes = StreamUtils.copyToByteArray(inputStream);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
}