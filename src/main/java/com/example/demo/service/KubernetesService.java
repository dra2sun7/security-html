package com.example.demo.service;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobStatus;
import io.fabric8.kubernetes.client.*;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayInputStream;

import java.util.ArrayList;
import java.util.List;

@Service
public class KubernetesService {
    private LogWatch logWatch;
    public List<String> deployJobFromYaml(String apiServer, String token) {
        List<String> logMessage = new ArrayList<>();

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

                    logWatch = logWatch(kubernetesClient, nodeName+"kubebench");

                    System.out.println("==============   " + nodeName+"Job created successfully.   ==============");

                    logMessage.add("==============   "+nodeName+" Job information.   ==============");
                    
                    inputStream.close();

                    waitForJobCompletion(kubernetesClient, nodeName+"kubebench");
                    if (logWatch != null) {
                        logWatch.close();
                    }
                    PodList podList = kubernetesClient.pods().inNamespace("default").list();
                    String log = null;
                    for (Pod pod : podList.getItems()) {
                        String podName = pod.getMetadata().getName();
                        if (podName.startsWith(nodeName+"kubebench")) {

                            log = kubernetesClient.pods().inNamespace("default").withName(podName).getLog();

                            System.out.println(log);

                            logMessage.add(log);

                            break;
                        }
                    }
                    deleteJob(kubernetesClient,nodeName+"kubebench");
                }
            } catch (IOException e) {

                logMessage.add("There is a trouble with Cluster");

                throw new RuntimeException(e);
            }
        } catch (KubernetesClientException e) {
            logMessage.add("There is a trouble with Cluster");
            e.printStackTrace();
            // Handle Kubernetes client errors
        }
        return logMessage;
    }

    private String loadYamlTemplateFromFile() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/application.yml")) {
            byte[] bytes = StreamUtils.copyToByteArray(inputStream);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
    private void waitForJobCompletion(KubernetesClient kubernetesClient, String jobName) {
        long timeoutMillis = 5 * 60 * 1000; // 최대 대기 시간 (예: 10분)
        long pollingIntervalMillis = 5000; // 상태 확인 간격 (예: 5초)

        long startTime = System.currentTimeMillis();
        boolean jobCompleted = false;

        while (!jobCompleted && System.currentTimeMillis() - startTime < timeoutMillis) {
            Job job = kubernetesClient.batch().v1().jobs().inNamespace("default").withName(jobName).get();
            if (job != null) {
                JobStatus jobStatus = job.getStatus();
                if (jobStatus != null && jobStatus.getSucceeded() != null && jobStatus.getSucceeded() > 0) {
                    jobCompleted = true;
                }
            }

            if (!jobCompleted) {
                try {
                    Thread.sleep(pollingIntervalMillis); // 일정 간격으로 상태 확인
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        if (jobCompleted) {
            System.out.println("Job completed successfully.");
        } else {
            System.err.println("Timeout waiting for Job completion.");
        }
    }

    private LogWatch logWatch(KubernetesClient kubernetesClient, String podName) {
        return kubernetesClient.pods().inNamespace("default").withName(podName).watchLog(System.out);
    }
    private void deleteJob(KubernetesClient kubernetesClient, String jobName) {
        try {
            kubernetesClient.batch().v1().jobs().inNamespace("default").withName(jobName).delete();

            System.out.println("Job deleted successfully.");
        } catch (KubernetesClientException e) {
            e.printStackTrace();
            System.err.println("Error while deleting Job.");
        }
    }
}