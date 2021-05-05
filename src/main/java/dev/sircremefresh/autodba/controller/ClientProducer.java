package dev.sircremefresh.autodba.controller;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class ClientProducer {

	@Bean
	KubernetesClient makeDefaultKubernetesClient() {
		return new DefaultKubernetesClient();
	}

	@Bean(name = "namespaces")
	String getCurrentNamespace() {
		return "monitoring-prd";
	}

	// /var/run/secrets/kubernetes.io/serviceaccount/namespace
}
