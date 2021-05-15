package dev.sircremefresh.autodba.controller;

import dev.sircremefresh.autodba.controller.crd.clusterdatabaseserver.ClusterDatabaseServer;
import dev.sircremefresh.autodba.controller.crd.database.Database;
import dev.sircremefresh.autodba.controller.crd.database.DatabaseList;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.informers.cache.Cache;
import lombok.NonNull;
import lombok.val;
import org.postgresql.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

@Repository
public class DatabaseReconciler {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseReconciler.class.getName());
	private static final String USERNAME_KEY = "username";
	private static final String PASSWORD_KEY = "password";

	final JdbcTemplate jdbcTemplate = new JdbcTemplate();

	final KubernetesClient client;
	final MixedOperation<Database, DatabaseList, Resource<Database>> databaseClient;

	@Autowired
	public DatabaseReconciler(KubernetesClient client) {
		this.client = client;

		try {
			Driver.register();
		} catch (SQLException err) {
			val msg = "Could not register postgresql driver.";
			logger.error(msg, err);
			throw new FatalBeanException(msg, err);
		}

//		val conn = DriverManager.getConnection("");

		databaseClient = client.customResources(Database.class, DatabaseList.class);
	}

	public DataSource createDataSource(@NonNull String url, @NonNull String username, @NonNull String password) {
		DriverManagerDataSource ds = new DriverManagerDataSource();
		ds.setUrl(url);
		ds.setUsername(username);
		ds.setPassword(password);
		return ds;
	}

	private Optional<Connection> getConnection(@NonNull ClusterDatabaseServer databaseServer, @NonNull Secret secret) {
		val data = secret.getStringData();
		if (!data.containsKey(USERNAME_KEY) || !data.containsKey(PASSWORD_KEY)) {
			logger.warn("Secret {} for databaseServer {} does not contain username or password", Cache.metaNamespaceKeyFunc(secret), Cache.metaNamespaceKeyFunc(databaseServer));
			return Optional.empty();
		}
		try {
			val manager = new DriverManagerDataSource(databaseServer.getSpec().getJdbcUrl(), data.get(USERNAME_KEY), data.get(PASSWORD_KEY));
			return Optional.of(manager.getConnection());
		} catch (SQLException err) {
			logger.error("Could not connect to databaseServer {}", Cache.metaNamespaceKeyFunc(databaseServer), err);
			return Optional.empty();
		}
	}

//	private loadSecretFor

	public void reconcile(Database database, @Nullable ClusterDatabaseServer databaseServer, @Nullable Secret secret) {
		logger.info("Reconcile database {}", database.getMetadata().getName());
		if (databaseServer == null) {
			logger.warn("Could not find databaseServer {} referenced in {}", database.getSpec().getServerRef().getName(), Cache.metaNamespaceKeyFunc(database));
			return;
		}
		if (secret == null) {
			logger.warn("Could not find secret {} in databaseServer {} for database {}", databaseServer.getSpec().getAuthSecretRef().getName(), Cache.metaNamespaceKeyFunc(databaseServer), Cache.metaNamespaceKeyFunc(database));
			return;
		}

		getConnection(databaseServer, secret)
				.ifPresentOrElse(
						connection -> {
							try (connection) {
								handleDatabase(database, connection);
							} catch (SQLException ex) {
								logger.error("Error occurred in database connection for database {} in databaseServer {}", Cache.metaNamespaceKeyFunc(database), Cache.metaNamespaceKeyFunc(databaseServer));
							}
						},
						() -> logger.error("Could not connect to databaseServer {} for database {}", Cache.metaNamespaceKeyFunc(databaseServer), Cache.metaNamespaceKeyFunc(database))
				);

//		System.out.println(jdbcTemplate.getQueryTimeout());
//		val databaseName = database.getSpec().getDatabaseName();
//		val namespace = database.getMetadata().getNamespace();
//		if (!doesUserExist(databaseName)) {
//			val password = genPassword();
//			createUser(databaseName, password);
//			client.secrets().inNamespace(namespace).create(
//					new SecretBuilder()
//							.withNewMetadata()
//							.withName(databaseName)
//
//							.addNewOwnerReference()
//							.withApiVersion("autodba.sircremefresh.dev/v1alpha1")
//							.withName(database.getMetadata().getName())
//							.withKind(database.getKind())
//							.withBlockOwnerDeletion(true)
//							.withController(true)
//							.withNewUid(database.getMetadata().getUid())
//							.endOwnerReference()
//
//							.endMetadata()
//							.addToStringData("username", databaseName)
//							.addToStringData("password", password)
//							.build()
//			);
//			System.out.println("created user: " + databaseName);
//		} else {
//			System.out.println("user already exists: " + databaseName);
//		}
	}

	private void handleDatabase(@NonNull Database database, @NonNull Connection connection) {

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
