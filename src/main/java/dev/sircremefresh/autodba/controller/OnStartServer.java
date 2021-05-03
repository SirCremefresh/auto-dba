package dev.sircremefresh.autodba.controller;

import dev.sircremefresh.autodba.controller.crd.Database;
import dev.sircremefresh.autodba.controller.crd.DatabaseList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
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
		KubernetesDeserializer.registerCustomKind("v1alpha1", "Database", Database.class);
		val databaseClient = client.customResources(Database.class, DatabaseList.class);


//		Database d = new Database();
//		d.setMetadata(
//				new ObjectMetaBuilder()
//						.withName("aaaaa")
//						.build()
//		);
//		d.setSpec(DatabaseSpec.builder()
//				.databaseName("asdfasd")
//				.secretName("asdfasdfasd").build());
//
//		databaseClient.inNamespace("auto-dba-dev").create(d);

		databaseClient.inAnyNamespace().watch(new Watcher<>() {
			@Override
			public void eventReceived(Action action, Database resource) {
				databaseClient.inNamespace(resource.getMetadata().getNamespace()).withName(resource.getMetadata().getName()).edit(database -> {
					database.getSpec().setDatabaseName("alsdf");
					return database;
				});
//				resource.setStatus(
//						DatabaseStatus.builder()
//								.type("True")
//								.status("True")
//								.lastTransitionTime(LocalDateTime.now())
//								.build()
//				);
//				resource.setSpec(
//						resource.getSpec().toBuilder()
//								.databaseName("laskjdflaskd")
//								.build()
//				);
//				resource.getMetadata().setName("asdfasdddddd");
//				resource.getSpec().setDatabaseName("alskdfj");
//				databaseClient.replace(resource);
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

//		val databaseResource = databaseClient.inAnyNamespace().list();
//		System.out.println(databaseResource.getItems());
	}
}
