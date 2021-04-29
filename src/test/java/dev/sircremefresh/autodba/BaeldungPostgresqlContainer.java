package dev.sircremefresh.autodba;

import org.testcontainers.containers.PostgreSQLContainer;

public class BaeldungPostgresqlContainer extends PostgreSQLContainer<BaeldungPostgresqlContainer> {
	private static final String IMAGE_TAG = "postgres:1.13";
	private static BaeldungPostgresqlContainer container;

	private BaeldungPostgresqlContainer() {
		super(IMAGE_TAG);
	}

	public static BaeldungPostgresqlContainer getInstance() {
		if (container == null) {
			container = new BaeldungPostgresqlContainer();
		}
		return container;
	}

	@Override
	public void start() {
		super.start();
		System.setProperty("DB_URL", container.getJdbcUrl());
		System.setProperty("DB_USERNAME", container.getUsername());
		System.setProperty("DB_PASSWORD", container.getPassword());
	}

	@Override
	public void stop() {
		//do nothing, JVM handles shut down
	}
}
