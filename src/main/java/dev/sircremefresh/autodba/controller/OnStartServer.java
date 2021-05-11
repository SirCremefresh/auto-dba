package dev.sircremefresh.autodba.controller;

import dev.sircremefresh.autodba.controller.database.DatabaseReconciler;
import dev.sircremefresh.autodba.controller.database.crd.Database;
import dev.sircremefresh.autodba.controller.database.crd.DatabaseList;
import dev.sircremefresh.autodba.controller.databaseserver.crd.DatabaseServer;
import dev.sircremefresh.autodba.controller.databaseserver.crd.DatabaseServerList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import lombok.NonNull;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class OnStartServer implements ApplicationListener<ContextRefreshedEvent> {
	private static final Logger logger = LoggerFactory.getLogger(OnStartServer.class.getName());
	private static final long RESYNC_PERIOD_MILLIS = 10 * 60 * 1000;

	private final KubernetesClient client;
	private final DatabaseReconciler databaseReconciler;

	@Autowired
	public OnStartServer(KubernetesClient client, DatabaseReconciler databaseReconciler) {
		this.client = client;
		this.databaseReconciler = databaseReconciler;
	}

	@Override
	public void onApplicationEvent(@NonNull ContextRefreshedEvent contextRefreshedEvent) {
		try (client) {
			logger.info("Starting");

			SharedInformerFactory informerFactory = client.informers();

			SharedIndexInformer<Database> databaseInformer = informerFactory.sharedIndexInformerForCustomResource(Database.class, DatabaseList.class, RESYNC_PERIOD_MILLIS);
			SharedIndexInformer<DatabaseServer> databaseServerInformer = informerFactory.sharedIndexInformerForCustomResource(DatabaseServer.class, DatabaseServerList.class, RESYNC_PERIOD_MILLIS);

			val controller = new AutoDbaController(databaseInformer, databaseServerInformer, databaseReconciler);

			informerFactory.startAllRegisteredInformers();
			informerFactory.addSharedInformerEventListener(exception -> logger.error("Exception occurred, but caught", exception));

			logger.info("Starting {} Controller", AutoDbaController.class.getSimpleName());
			controller.run();
		} catch (KubernetesClientException exception) {
			logger.error("Kubernetes Client Exception : ", exception);
		}
	}
}
