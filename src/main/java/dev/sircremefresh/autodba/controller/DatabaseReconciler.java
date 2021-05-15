package dev.sircremefresh.autodba.controller;

import dev.sircremefresh.autodba.controller.crd.clusterdatabaseserver.ClusterDatabaseServer;
import dev.sircremefresh.autodba.controller.crd.database.Database;
import dev.sircremefresh.autodba.controller.crd.database.DatabaseList;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.informers.cache.Cache;
import lombok.NonNull;
import lombok.val;
import org.apache.commons.codec.digest.DigestUtils;
import org.postgresql.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

@Repository
public class DatabaseReconciler {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseReconciler.class.getName());
	private static final String USERNAME_KEY = "username";
	private static final String PASSWORD_KEY = "password";
	private static final int MAX_DATABASE_KEY_LENGTH = 63;
	private static final int DATABASE_KEY_HASH_LENGTH = 11;
	private static final int DATABASE_KEY_IDENTIFIER_LENGTH = 50;

	final KubernetesClient client;
	final MixedOperation<Database, DatabaseList, Resource<Database>> databaseClient;

	@Autowired
	public DatabaseReconciler(KubernetesClient client) {
		this.client = client;

		try {
			if (!Driver.isRegistered()) {
				Driver.register();
			}
		} catch (SQLException err) {
			val msg = "Could not register postgresql driver.";
			logger.error(msg, err);
			throw new FatalBeanException(msg, err);
		}

		databaseClient = client.customResources(Database.class, DatabaseList.class);
	}


	private Optional<JdbcTemplate> getConnection(@NonNull ClusterDatabaseServer databaseServer, @NonNull Secret secret) {
		val data = secret.getStringData();
		if (!data.containsKey(USERNAME_KEY) || !data.containsKey(PASSWORD_KEY)) {
			logger.warn("Secret {} for databaseServer {} does not contain username or password", Cache.metaNamespaceKeyFunc(secret), Cache.metaNamespaceKeyFunc(databaseServer));
			return Optional.empty();
		}
		String jdbcUrl = "jdbc:postgresql://" + databaseServer.getSpec().getHost() + ":" + databaseServer.getSpec().getPort();
		val manager = new DriverManagerDataSource(jdbcUrl, data.get(USERNAME_KEY), data.get(PASSWORD_KEY));
		try {
			Objects.requireNonNull(manager.getConnection());
		} catch (SQLException err) {
			logger.error("Could not connect to databaseServer {}", Cache.metaNamespaceKeyFunc(databaseServer), err);
			return Optional.empty();
		}
		return Optional.of(new JdbcTemplate(manager));
	}

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
						jdbcTemplate -> handleDatabase(database, databaseServer, jdbcTemplate),
						() -> logger.error("Could not get connection to databaseServer {} for database {}", Cache.metaNamespaceKeyFunc(databaseServer), Cache.metaNamespaceKeyFunc(database))
				);
	}


	private void handleDatabase(@NonNull Database database, @NonNull ClusterDatabaseServer databaseServer, @NonNull JdbcTemplate jdbcTemplate) {
		logger.info("handleDatabase({})", Cache.metaNamespaceKeyFunc(database));

		val databaseKey = generateDatabaseKey(database.getMetadata().getNamespace(), database.getMetadata().getName());

		val secretName = database.getSpec().getSecretName();
		val secretResource = client.secrets().inNamespace(database.getMetadata().getNamespace()).withName(secretName);
		if (secretResource.get() != null) {
			logger.info("Handling Database {} secret already created skipping", Cache.metaNamespaceKeyFunc(database));
			return;
		}

		val password = genPassword();

		try {
			logger.info("Creating database/user for Database resource {}", Cache.metaNamespaceKeyFunc(database));
			createDatabaseAndUser(databaseKey, password, jdbcTemplate);
		} catch (DataAccessException err) {
			logger.error("Could not create database/user for Database resource {}", Cache.metaNamespaceKeyFunc(database), err);
			return;
		}

		val secret = new SecretBuilder()
				.withNewMetadata()
				.withName(secretName)

				.addNewOwnerReference()
				.withApiVersion(database.getApiVersion())
				.withName(database.getMetadata().getName())
				.withKind(database.getKind())
				.withBlockOwnerDeletion(true)
				.withController(true)
				.withNewUid(database.getMetadata().getUid())
				.endOwnerReference()

				.endMetadata()
				.addToStringData("database", databaseKey)
				.addToStringData("username", databaseKey)
				.addToStringData("password", password)
				.addToStringData("host", databaseServer.getSpec().getHost())
				.addToStringData("port", databaseServer.getSpec().getPort())
				.build();

		client.secrets().createOrReplace(secret);
		logger.info("Created secret for Database {}", Cache.metaNamespaceKeyFunc(database));
	}

	private String generateDatabaseKey(@NonNull String namespace, @NonNull String name) {
		if (namespace.length() + name.length() + 1 <= MAX_DATABASE_KEY_LENGTH) {
			return namespace + "-" + name;
		}
		val hash = createDatabaseKeyHash(namespace, name);
		val identifierFieldLength = DATABASE_KEY_IDENTIFIER_LENGTH / 2;
		if (name.length() <= identifierFieldLength) {
			return namespace.substring(0, DATABASE_KEY_IDENTIFIER_LENGTH - name.length()) + "-" + name + "-" + hash;
		}
		if (namespace.length() <= identifierFieldLength) {
			return namespace + "-" + name.substring(0, DATABASE_KEY_IDENTIFIER_LENGTH - namespace.length()) + "-" + hash;
		}
		return namespace.substring(0, identifierFieldLength) + "-" + name.substring(0, identifierFieldLength) + "-" + hash;
	}

	private void createDatabaseAndUser(@NonNull String user, @NonNull String password, @NonNull JdbcTemplate jdbcTemplate) {
		val reg = "^[a-z0-9_]{3,59}$";
		if (!Pattern.matches(reg, user) || !Pattern.matches(reg, password)) {
			throw new IllegalStateException("Username or Password has illegal characters" + user);
		}

		jdbcTemplate.execute("create user " + user + " with encrypted password '" + password + "';");
		jdbcTemplate.execute("create database " + user + " OWNER " + user + ";");
	}

	private boolean doesUserExist(@NonNull String user, @NonNull JdbcTemplate jdbcTemplate) {
		val rs = jdbcTemplate.queryForRowSet(
				"SELECT count(*) > 0 as exists FROM pg_database WHERE datname=?",
				user);
		return rs.getBoolean("exists");
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

	private String createDatabaseKeyHash(@NonNull String namespace, @NonNull String name) {
		return DigestUtils.sha1Hex(namespace + "-" + name).substring(0, DATABASE_KEY_HASH_LENGTH);
	}
}
