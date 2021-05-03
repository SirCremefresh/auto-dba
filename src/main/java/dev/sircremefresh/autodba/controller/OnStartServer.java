package dev.sircremefresh.autodba.controller;

import dev.sircremefresh.autodba.controller.crd.Database;
import dev.sircremefresh.autodba.controller.crd.DatabaseList;
import dev.sircremefresh.autodba.controller.crd.DatabaseSpec;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
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
		System.out.println("datasourceUrl: " + datasourceUrl);

		val databaseClient = client.customResources(Database.class, DatabaseList.class);


		Database d = new Database();
		d.setMetadata(
				new ObjectMetaBuilder()
						.withName("aaaaa")
						.build()
		);
		d.setSpec(DatabaseSpec.builder()
				.databaseName("asdfasd")
				.secretName("asdfasdfasd").build());

		databaseClient.inNamespace("auto-dba-dev").create(d);

//		databaseClient.watch(new Watcher<>() {
//			@Override
//			public void eventReceived(Action action, Database resource) {
////				resource.setStatus(
////						DatabaseStatus.builder()
////								.type("True")
////								.status("True")
////								.lastTransitionTime(LocalDateTime.now())
////								.build()
////				);
////				resource.setSpec(
////						resource.getSpec().toBuilder()
////								.databaseName("laskjdflaskd")
////								.build()
////				);
//				resource.getSpec().setDatabaseName("alskdfj");
//				databaseClient.replace(resource);
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

//		val databaseResource = databaseClient.inAnyNamespace().list();
//		System.out.println(databaseResource.getItems());
	}
}
