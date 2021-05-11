package dev.sircremefresh.autodba.controller;

import dev.sircremefresh.autodba.controller.crd.clusterdatabaseserver.ClusterDatabaseServer;
import dev.sircremefresh.autodba.controller.crd.clusterdatabaseserver.ClusterDatabaseServerList;
import dev.sircremefresh.autodba.controller.crd.database.Database;
import dev.sircremefresh.autodba.controller.crd.database.DatabaseList;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class ClientProducer {
	private static final Logger logger = LoggerFactory.getLogger(ClientProducer.class.getName());

	@Bean
	KubernetesClient makeDefaultKubernetesClient(@Name("namespace") String namespace) {
		return new DefaultKubernetesClient().inNamespace(namespace);
	}

	@Bean(name = "namespace")
	String getDefaultNamespace() {
		// TODO: use namespace of pod
		return "auto-dba-dev";
	}

	@Bean
	MixedOperation<Database, DatabaseList, Resource<Database>> makeDatabaseClient(KubernetesClient client) {
		logger.info("Registering Database CustomResource {}/{}", HasMetadata.getKind(Database.class), HasMetadata.getVersion(Database.class));
		KubernetesDeserializer.registerCustomKind(HasMetadata.getVersion(Database.class), HasMetadata.getKind(Database.class), Database.class);
		return client.customResources(Database.class, DatabaseList.class);
	}

	@Bean
	MixedOperation<ClusterDatabaseServer, ClusterDatabaseServerList, Resource<ClusterDatabaseServer>> makeDatabaseServerClient(KubernetesClient client) {
		logger.info("Registering ClusterDatabaseServer CustomResource {}/{}", HasMetadata.getKind(ClusterDatabaseServer.class), HasMetadata.getVersion(ClusterDatabaseServer.class));
		KubernetesDeserializer.registerCustomKind(HasMetadata.getVersion(ClusterDatabaseServer.class), HasMetadata.getKind(ClusterDatabaseServer.class), ClusterDatabaseServer.class);
		return client.customResources(ClusterDatabaseServer.class, ClusterDatabaseServerList.class);
	}
}
