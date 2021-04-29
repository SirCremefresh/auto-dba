package dev.sircremefresh.autodba.asdf;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Repository;

@Repository
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
