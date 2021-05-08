package dev.sircremefresh.autodba.controller.database;

import dev.sircremefresh.autodba.controller.database.crd.Database;
import dev.sircremefresh.autodba.controller.database.crd.DatabaseList;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Random;
import java.util.regex.Pattern;

@Repository
public class DatabaseReconciler {

	final JdbcTemplate jdbcTemplate = new JdbcTemplate();

	final KubernetesClient client;
	final MixedOperation<Database, DatabaseList, Resource<Database>> databaseClient;

	@Autowired
	public DatabaseReconciler(KubernetesClient client) throws SQLException {
		this.client = client;
		val conn = DriverManager.getConnection("");

		databaseClient = client.customResources(Database.class, DatabaseList.class);
	}

	@Bean
	public DataSource dataSource(){
		DriverManagerDataSource ds = new DriverManagerDataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUrl("jdbc:mysql://localhost:3306/gene");
		ds.setUsername("");
		ds.setPassword("");
		return ds;
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
							.withNewMetadata()
							.withName(databaseName)

							.addNewOwnerReference()
							.withApiVersion("autodba.sircremefresh.dev/v1alpha1")
							.withName(newDatabase.getMetadata().getName())
							.withKind(newDatabase.getKind())
							.withBlockOwnerDeletion(true)
							.withController(true)
							.withNewUid(newDatabase.getMetadata().getUid())
							.endOwnerReference()

							.endMetadata()
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

		jdbcTemplate.execute("create user " + user + " with encrypted password '" + password + "';");
		jdbcTemplate.execute("create database " + user + " OWNER " + user + ";");
	}

	private boolean doesUserExist(String user) {
		val res = jdbcTemplate.query(
				"SELECT count(*) as count FROM pg_database WHERE datname=?",
				(rs, rowNum) -> rs.getLong("count") > 0,
				user);
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
