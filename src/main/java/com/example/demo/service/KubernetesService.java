package com.example.demo.service;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KubernetesService {
    public List<String> getNodeNames(String apiServer, String token) {
        Config config = new ConfigBuilder()
                .withMasterUrl(apiServer)
                .withOauthToken(token)
                .build();

        KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);

        NodeList nodeList = kubernetesClient.nodes().list();
        List<String> nodeNames = new ArrayList<>();

        for (Node node : nodeList.getItems()) {
            nodeNames.add(node.getMetadata().getName());
        }

        return nodeNames;
    }
}
