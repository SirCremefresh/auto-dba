package dev.sircremefresh.autodba.controller;

import dev.sircremefresh.autodba.controller.crd.Database;
import dev.sircremefresh.autodba.controller.crd.DatabaseList;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class OnStartServer implements ApplicationListener<ContextRefreshedEvent> {
	final KubernetesClient client;
	@Value("${spring.datasource.url}")
	private String datasourceUrl;

	@Autowired
	public OnStartServer(KubernetesClient client) {
		this.client = client;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
		System.out.println("asldkfjaölskdfö. datasourceUrl: " + datasourceUrl);

//		client.customResources(Database.class).withName("").edit((Database database) -> {
//			database.setStatus(
//					DatabaseStatus.builder()
//							.lastTransitionTime(LocalDateTime.now())
//							.status("True")
//							.type("ad")
//							.build()
//			);
//		});

		val databaseClient = client.customResources(Database.class, DatabaseList.class);

		val databaseResource = databaseClient.withName("test-database");
		System.out.println(databaseResource);

//		items.forEach(database -> {
//			database.setStatus(
//					DatabaseStatus.builder()
//							.lastTransitionTime(LocalDateTime.now())
//							.status("True")
//							.type("ad")
//							.build()
//			);
//
//			System.out.println("Found resource. " + database + " ---------- " +
//					"name: " + database.getMetadata().getName() +
//					", version: " + database.getMetadata().getResourceVersion());
//		});
//
//		List<Pod> list = client.pods().list().getItems();
//
//		list.forEach(pod -> {
//			System.out.println("Found resource. " +
//					"name: " + pod.getMetadata().getName() +
//					", version: " + pod.getMetadata().getResourceVersion());
//		});
//
//		client.pods().watch(new Watcher<>() {
//			@Override
//			public void eventReceived(Action action, Pod resource) {
//				System.out.println("Found resource. " +
//						"name: " + resource.getMetadata().getName() +
//						", version: " + resource.getMetadata().getResourceVersion());
//			}
//
//			@Override
//			public void onClose(WatcherException cause) {
//				if (cause != null) {
//					cause.printStackTrace();
//					System.exit(-1);
//				}
//			}
//		});
	}
}
