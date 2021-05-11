package dev.sircremefresh.autodba.controller;

import dev.sircremefresh.autodba.controller.database.DatabaseReconciler;
import dev.sircremefresh.autodba.controller.database.crd.Database;
import dev.sircremefresh.autodba.controller.database.crd.DatabaseList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class OnStartServer implements ApplicationListener<ContextRefreshedEvent> {
	private final KubernetesClient client;
	private final DatabaseReconciler databaseReconciler;

	@Autowired
	public OnStartServer(KubernetesClient client, DatabaseReconciler databaseReconciler) {
		this.client = client;
		this.databaseReconciler = databaseReconciler;
	}

	@Override
	public void onApplicationEvent(@NonNull ContextRefreshedEvent contextRefreshedEvent) {
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
	}
}
