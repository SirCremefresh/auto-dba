package dev.sircremefresh.autodba.controller;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OnStartServer implements ApplicationListener<ContextRefreshedEvent> {
	final KubernetesClient client;

	@Autowired
	public OnStartServer(KubernetesClient client) {
		this.client = client;
	}

	@Value( "${spring.datasource.url}" )
	private String datasourceUrl;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
		System.out.println("asldkfjaölskdfö. datasourceUrl: " + datasourceUrl);

		List<Pod> list = client.pods().list().getItems();

		list.forEach(pod -> {
			System.out.println("Found resource. " +
					"name: " + pod.getMetadata().getName() +
					", version: " + pod.getMetadata().getResourceVersion());
		});

		client.pods().watch(new Watcher<>() {
			@Override
			public void eventReceived(Action action, Pod resource) {
				System.out.println("Found resource. " +
						"name: " + resource.getMetadata().getName() +
						", version: " + resource.getMetadata().getResourceVersion());
			}

			@Override
			public void onClose(WatcherException cause) {
				if (cause != null) {
					cause.printStackTrace();
					System.exit(-1);
				}
			}
		});
	}
}
