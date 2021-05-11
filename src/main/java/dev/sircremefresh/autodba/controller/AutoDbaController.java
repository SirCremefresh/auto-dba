package dev.sircremefresh.autodba.controller;

import dev.sircremefresh.autodba.controller.database.DatabaseReconciler;
import dev.sircremefresh.autodba.controller.database.crd.Database;
import dev.sircremefresh.autodba.controller.databaseserver.crd.DatabaseServer;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.cache.Cache;
import io.fabric8.kubernetes.client.informers.cache.Lister;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class AutoDbaController {
	private static final Logger logger = LoggerFactory.getLogger(OnStartServer.class.getName());

	private final BlockingQueue<String> workQueue = new ArrayBlockingQueue<>(1024);
	private final SharedIndexInformer<Database> databaseInformer;
	private final SharedIndexInformer<DatabaseServer> databaseServerInformer;
	private final DatabaseReconciler databaseReconciler;
	private final Lister<Database> databaseLister;

	public AutoDbaController(
			SharedIndexInformer<Database> databaseInformer,
			SharedIndexInformer<DatabaseServer> databaseServerInformer,
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
			public void onAdd(DatabaseServer database) {
				System.out.printf("%s database added\n", database.getMetadata().getName());
			}

			@Override
			public void onUpdate(DatabaseServer oldDatabaseServer, DatabaseServer newDatabaseServer) {
				if (oldDatabaseServer.getMetadata().getResourceVersion().equals(newDatabaseServer.getMetadata().getResourceVersion())) {
					return;
				}
			}

			@Override
			public void onDelete(DatabaseServer database, boolean deletedFinalStateUnknown) {
				System.out.printf("%s database deleted \n", database.getMetadata().getName());
			}
		});
	}

	private void enqueueDatabase(@NonNull Database database) {
		logger.info("enqueueDatabase({}/{})", database.getMetadata().getNamespace(), database.getMetadata().getName());
		String key = Cache.metaNamespaceKeyFunc(database);
		logger.info("Going to enqueue key {}", key);
		workQueue.add(key);
	}

	public void run() {
		logger.info("Starting {} controller", AutoDbaController.class.getSimpleName());
		logger.info("Waiting for informer caches to sync");
		while (!databaseInformer.hasSynced() || !databaseServerInformer.hasSynced()) {
			// Wait till Informer syncs
		}
		logger.info("Informer caches are synced");


		while (!Thread.currentThread().isInterrupted()) {
			try {
				logger.info("trying to fetch item from workQueue...");
				if (workQueue.isEmpty()) {
					logger.info("Work Queue is empty");
				}
				String key = workQueue.take();
				Objects.requireNonNull(key, "key can't be null");
				logger.info("Got {}", key);
				if (key.isEmpty() || (!key.contains("/"))) {
					logger.warn("invalid resource key: {}", key);
				}

				String namespace = key.split("/")[0];
				String name = key.split("/")[1];
				Database database = databaseLister
						.namespace(namespace)
						.get(name);
				if (database == null) {
					logger.error("Database {}/{} in workQueue no longer exists", namespace, name);
					return;
				}

				databaseReconciler.reconcile(database);
			} catch (InterruptedException interruptedException) {
				Thread.currentThread().interrupt();
				logger.error("controller interrupted..");
			}
		}
	}
}
