package com.example.demo.service;

<<<<<<< HEAD
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
=======
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;d
>>>>>>> 3ce8db97b28df6b3ece1c5f0398e9bf4033baf17
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
<<<<<<< HEAD
=======
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeList;
>>>>>>> 3ce8db97b28df6b3ece1c5f0398e9bf4033baf17

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayInputStream;
<<<<<<< HEAD
=======
import java.util.List;
>>>>>>> 3ce8db97b28df6b3ece1c5f0398e9bf4033baf17


@Service
public class KubernetesService {

    public void deployJobFromYaml(String apiServer, String token) {
        Config config = new ConfigBuilder()
                .withMasterUrl(apiServer)
                .withOauthToken(token)
                .withTrustCerts(true)
<<<<<<< HEAD
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
=======
                .build();


        try (KubernetesClient kubernetesClient = new DefaultKubernetesClient(config)) {
            NodeList nodeList = kubernetesClient.nodes().list();

            for (Node node : nodeList.getItems()) {
                try {
                    String nodeName = node.getMetadata().getName();
                    String yamlTemplate = loadYamlTemplateFromFile();
                    String yamlContent = yamlTemplate.replace("${nodeName}", nodeName);

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

                    String log = kubernetesClient.pods().inNamespace("default").withName(nodeName+"KubeBench").tailingLines(100).getLog();
                    System.out.println("Logs: ");
                    System.out.println(log);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
>>>>>>> 3ce8db97b28df6b3ece1c5f0398e9bf4033baf17
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