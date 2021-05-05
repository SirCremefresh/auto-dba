package dev.sircremefresh.autodba.controller.database;

import dev.sircremefresh.autodba.controller.database.crd.Database;
import dev.sircremefresh.autodba.controller.database.crd.DatabaseList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DatabaseReconciler {

	final JdbcTemplate jdbcTemplate;

	final KubernetesClient client;
	final MixedOperation<Database, DatabaseList, Resource<Database>> databaseClient;

	@Autowired
	public DatabaseReconciler(KubernetesClient client, JdbcTemplate jdbcTemplate) {
		this.client = client;
		databaseClient = client.customResources(Database.class, DatabaseList.class);
		this.jdbcTemplate = jdbcTemplate;
	}

	public void reconcile(Database oldDatabase, Database newDatabase) {
		System.out.println(jdbcTemplate.getQueryTimeout());
//		jdbcTemplate.execute("select 1;");
//		jdbcTemplate.execute("SELECT 1/count(*) FROM pg_database WHERE datname='db_name'");
		jdbcTemplate.execute("create user " + newDatabase.getSpec().getDatabaseName() + " with encrypted password 'mypass';");
		jdbcTemplate.execute("create database " + newDatabase.getSpec().getDatabaseName() + " OWNER " + newDatabase.getSpec().getDatabaseName() + ";");
	}
}
