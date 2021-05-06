package dev.sircremefresh.autodba.controller.database;

import dev.sircremefresh.autodba.controller.database.crd.Database;
import dev.sircremefresh.autodba.controller.database.crd.DatabaseList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

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
		val databaseName = newDatabase.getSpec().getDatabaseName();
		if (!doesUserExist(databaseName)) {
			System.out.println("created user: " + databaseName);
			createUser(databaseName);
		} else {
			System.out.println("user already exists: " + databaseName);
		}
	}

	private void createUser(String user) {
		jdbcTemplate.execute("create user " + user + " with encrypted password 'mypass';");
		jdbcTemplate.execute("create database " + user + " OWNER " + user + ";");
	}

	private boolean doesUserExist(String user) {
		val res = jdbcTemplate.query("SELECT count(*) as count FROM pg_database WHERE datname=?", new CountRowMapper(), user);
		return res.get(0);
	}


	public class CountRowMapper implements RowMapper<Boolean> {
		@Override
		public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
			return rs.getLong("count") > 0;
		}
	}
}
