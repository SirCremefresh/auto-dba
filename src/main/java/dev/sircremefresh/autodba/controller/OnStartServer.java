package dev.sircremefresh.autodba.controller;

import dev.sircremefresh.autodba.controller.database.DatabaseReconciler;
import dev.sircremefresh.autodba.controller.database.crd.Database;
import dev.sircremefresh.autodba.controller.database.crd.DatabaseList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class OnStartServer implements ApplicationListener<ContextRefreshedEvent> {
	private final KubernetesClient client;
	private final DatabaseReconciler databaseReconciler;

	@Value("${spring.datasource.url}")
	private String datasourceUrl;

	@Autowired
	public OnStartServer(KubernetesClient client, DatabaseReconciler databaseReconciler) {
		this.client = client;
		this.databaseReconciler = databaseReconciler;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
		System.out.println("datasourceUrl: " + datasourceUrl);
		KubernetesDeserializer.registerCustomKind("v1alpha1", "Database", Database.class);

		SharedInformerFactory sharedInformerFactory = client.informers();

		SharedIndexInformer<Database> databaseInformer = sharedInformerFactory.sharedIndexInformerForCustomResource(Database.class, DatabaseList.class, 15 * 1000L);

		databaseInformer.addEventHandler(new ResourceEventHandler<>() {
			@Override
			public void onAdd(Database database) {
				System.out.printf("%s database added\n", database.getMetadata().getName());
			}

			@Override
			public void onUpdate(Database oldDatabase, Database newDatabase) {
				System.out.printf("%s database updated\n", oldDatabase.getMetadata().getName());
				System.out.printf("version1: %s, version: %s\n", oldDatabase.getMetadata().getResourceVersion(), newDatabase.getMetadata().getResourceVersion());
				databaseReconciler.reconcile(oldDatabase, newDatabase);
			}

			@Override
			public void onDelete(Database database, boolean deletedFinalStateUnknown) {
				System.out.printf("%s database deleted \n", database.getMetadata().getName());
			}
		});

		databaseInformer.run();

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

//		databaseClient.inAnyNamespace().watch(new Watcher<>() {
//			@Override
//			public void eventReceived(Action action, Database resource) {
//				databaseClient.inNamespace(resource.getMetadata().getNamespace()).withName(resource.getMetadata().getName()).edit(database -> {
//					database.getSpec().setDatabaseName("alsdf");
//					return database;
//				});
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
