package dev.sircremefresh.autodba.controller;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatabaseReconciler {

	final KubernetesClient client;

	@Autowired
	public DatabaseReconciler(KubernetesClient client) {
		this.client = client;
	}

	public void reconcile() {

	}
}
