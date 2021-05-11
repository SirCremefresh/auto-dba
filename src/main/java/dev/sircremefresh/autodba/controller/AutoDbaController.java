package dev.sircremefresh.autodba.controller;

import dev.sircremefresh.autodba.controller.crd.clusterdatabaseserver.ClusterDatabaseServer;
import dev.sircremefresh.autodba.controller.crd.database.Database;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.cache.Cache;
import io.fabric8.kubernetes.client.informers.cache.Lister;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class AutoDbaController {
	private static final Logger logger = LoggerFactory.getLogger(AutoDbaControllerStarter.class.getName());

	private final BlockingQueue<String> workQueue = new ArrayBlockingQueue<>(1024);
	private final SharedIndexInformer<Database> databaseInformer;
	private final SharedIndexInformer<ClusterDatabaseServer> databaseServerInformer;
	private final DatabaseReconciler databaseReconciler;
	private final Lister<Database> databaseLister;

	public AutoDbaController(
			SharedIndexInformer<Database> databaseInformer,
			SharedIndexInformer<ClusterDatabaseServer> databaseServerInformer,
			DatabaseReconciler databaseReconciler) {
		this.databaseInformer = databaseInformer;
		this.databaseServerInformer = databaseServerInformer;
		this.databaseReconciler = databaseReconciler;
		this.databaseLister = new Lister<>(databaseInformer.getIndexer());
		addListeners();
	}

	private void addListeners() {
		databaseInformer.addEventHandler(new ResourceEventHandler<>() {
			@Override
			public void onAdd(Database database) {
				logger.info("Database {} added", Cache.metaNamespaceKeyFunc(database));
				enqueueDatabase(database);
			}

			@Override
			public void onUpdate(Database oldDatabase, Database newDatabase) {
				logger.info("Database {} updated", Cache.metaNamespaceKeyFunc(newDatabase));

				enqueueDatabase(newDatabase);
			}

			@Override
			public void onDelete(Database database, boolean deletedFinalStateUnknown) {
				logger.info("Database {} deleted", Cache.metaNamespaceKeyFunc(database));
				enqueueDatabase(database);
			}
		});

		databaseServerInformer.addEventHandler(new ResourceEventHandler<>() {
			@Override
			public void onAdd(ClusterDatabaseServer clusterDatabaseServer) {
				logger.info("ClusterDatabaseServer {} added", Cache.metaNamespaceKeyFunc(clusterDatabaseServer));
			}

			@Override
			public void onUpdate(ClusterDatabaseServer oldClusterDatabaseServer, ClusterDatabaseServer newClusterDatabaseServer) {
				if (oldClusterDatabaseServer.getMetadata().getResourceVersion().equals(newClusterDatabaseServer.getMetadata().getResourceVersion())) {
					return;
				}

				logger.info("ClusterDatabaseServer {} updated", Cache.metaNamespaceKeyFunc(newClusterDatabaseServer));
				handleDatabaseServer(newClusterDatabaseServer);
			}

			@Override
			public void onDelete(ClusterDatabaseServer database, boolean deletedFinalStateUnknown) {
				logger.info("ClusterDatabaseServer {} deleted", Cache.metaNamespaceKeyFunc(database));
			}
		});
	}

	private void handleDatabaseServer(@NonNull ClusterDatabaseServer newClusterDatabaseServer) {
		logger.info("handleDatabaseServer({})", Cache.metaNamespaceKeyFunc(newClusterDatabaseServer));
		databaseLister
				.list()
				.stream()
				.filter(database -> newClusterDatabaseServer.getMetadata().getName().equals(database.getSpec().getServerRef().getName()))
				.forEach(this::enqueueDatabase);
	}

	private void enqueueDatabase(@NonNull Database database) {
		logger.info("enqueueDatabase({})", Cache.metaNamespaceKeyFunc(database));
		String key = Cache.metaNamespaceKeyFunc(database);
		logger.info("Going to enqueue key {}", key);
		workQueue.add(key);
	}

	public void run() {
		logger.info("Starting {} controller", AutoDbaController.class.getSimpleName());
		logger.info("Waiting for informer caches to sync");
		//noinspection StatementWithEmptyBody
		while (!databaseInformer.hasSynced() || !databaseServerInformer.hasSynced()) {
			// Wait till Informer syncs
		}
		logger.info("Informer caches are synced");


		while (!Thread.currentThread().isInterrupted()) {
			try {
				logger.info("Fetching next item from workQueue");
				if (workQueue.isEmpty()) {
					logger.info("WorkQueue is empty");
				}
				String key = workQueue.take();
				logger.info("Got key {} from workQueue", key);
				if (key.isEmpty() || (!key.contains("/"))) {
					logger.error("Key {} is not a valid resource", key);
				}

				String namespace = key.split("/")[0];
				String name = key.split("/")[1];
				Database database = databaseLister
						.namespace(namespace)
						.get(name);
				if (database == null) {
					logger.error("Database {} in workQueue no longer exists", key);
					return;
				}

				databaseReconciler.reconcile(database);
			} catch (InterruptedException interruptedException) {
				Thread.currentThread().interrupt();
				logger.error("Controller interrupted");
			}
		}
	}
}
