package com.example.demo.service;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
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
                NodeList nodeList = kubernetesClient.nodes().list();

                for (Node node : nodeList.getItems()) {
                    String nodeName = node.getMetadata().getName();
                    String yamlTemplate = loadYamlTemplateFromFile();
                    String yamlContent = yamlTemplate.replace("${nodeName}", nodeName);
                    InputStream inputStream = new ByteArrayInputStream(yamlContent.getBytes());

                    kubernetesClient.load(inputStream).create();
                    System.out.println("==============   " + nodeName+"Job created successfully.   ==============");
                    inputStream.close();

                    Thread.sleep(60000); // Sleep for one minute

                    PodList podList = kubernetesClient.pods().inNamespace("default").list();
                    String log = null;
                    for (Pod pod : podList.getItems()) {
                        String podName = pod.getMetadata().getName();
                        System.out.println(nodeName+"kubebench");
                        if (podName.startsWith(nodeName+"kubebench")) {
                            log = kubernetesClient.pods().inNamespace("default").withName(podName).getLog();
                            System.out.println(log);
                            break;
                        }
                    }

                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
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
