package dev.sircremefresh.autodba.controller.database;

import dev.sircremefresh.autodba.controller.database.crd.Database;
import dev.sircremefresh.autodba.controller.database.crd.DatabaseList;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Repository;

import java.util.Random;
import java.util.regex.Pattern;

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
		val namespace = newDatabase.getMetadata().getNamespace();
		if (!doesUserExist(databaseName)) {
			val password = genPassword();
			createUser(databaseName, password);
			client.secrets().inNamespace(namespace).create(
					new SecretBuilder()
							.withNewMetadata().withName(databaseName).endMetadata()
							.addToStringData("username", databaseName)
							.addToStringData("password", password)
							.build()
			);
			System.out.println("created user: " + databaseName);
		} else {
			System.out.println("user already exists: " + databaseName);
		}
	}

	private void createUser(String user, String password) {
		if (!Pattern.matches("^[a-z0-9_]{3,59}$", user)) {
			throw new IllegalStateException("username has illegal characters" + user);
		}

		jdbcTemplate.execute("create user " + user + " with encrypted password " + password + ";",
				(PreparedStatementCallback<Boolean>) ps -> {
//					ps.setString(1, user);
					ps.setString(1, password);
					return ps.execute();
				});
		jdbcTemplate.execute("create database " + user + " OWNER " + user + ";",
				(PreparedStatementCallback<Boolean>) ps -> {
					ps.setString(1, user);
					ps.setString(2, user);
					return ps.execute();
				});
	}

	private boolean doesUserExist(String user) {
		val res = jdbcTemplate.query("SELECT count(*) as count FROM pg_database WHERE datname=?",
				(rs, rowNum) -> rs.getLong("count") > 0, user);
		return res.get(0);
	}


	private String genPassword() {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder out = new StringBuilder();
		Random rnd = new Random();
		while (out.length() < 32) {
			int index = (int) (rnd.nextFloat() * chars.length());
			out.append(chars.charAt(index));
		}
		return out.toString();
	}
}
