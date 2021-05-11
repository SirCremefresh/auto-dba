package dev.sircremefresh.autodba.controller;

import dev.sircremefresh.autodba.controller.crd.database.Database;
import dev.sircremefresh.autodba.controller.crd.database.DatabaseList;
import dev.sircremefresh.autodba.controller.crd.clusterdatabaseserver.ClusterDatabaseServer;
import dev.sircremefresh.autodba.controller.crd.clusterdatabaseserver.ClusterDatabaseServerList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class ClientProducer {

	@Bean
	KubernetesClient makeDefaultKubernetesClient() {
		return new DefaultKubernetesClient();
	}

	@Bean
	MixedOperation<Database, DatabaseList, Resource<Database>> makeDatabaseClient(KubernetesClient client) {
		KubernetesDeserializer.registerCustomKind("v1alpha1", "Database", Database.class);
		return client.customResources(Database.class, DatabaseList.class);
	}

	@Bean
	MixedOperation<ClusterDatabaseServer, ClusterDatabaseServerList, Resource<ClusterDatabaseServer>> makeDatabaseServerClient(KubernetesClient client) {
		KubernetesDeserializer.registerCustomKind("v1alpha1", "DatabaseServer", ClusterDatabaseServer.class);
		return client.customResources(ClusterDatabaseServer.class, ClusterDatabaseServerList.class);
	}
}
