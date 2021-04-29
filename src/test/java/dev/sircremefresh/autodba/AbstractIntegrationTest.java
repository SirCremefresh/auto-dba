package dev.sircremefresh.autodba;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = AbstractIntegrationTest.Initializer.class)
abstract class AbstractIntegrationTest {

	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		private static final String IMAGE_TAG = "postgres:13-alpine";

		static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(IMAGE_TAG);

		public static Map<String, String> getProperties() {
			postgres.start();

			return Map.of(
					"spring.datasource.url", postgres.getJdbcUrl(),
					"spring.datasource.username", postgres.getUsername(),
					"spring.datasource.password", postgres.getPassword()
			);
		}

		@Override
		public void initialize(ConfigurableApplicationContext context) {
			var env = context.getEnvironment();
			env.getPropertySources().addFirst(new MapPropertySource(
					"testcontainers",
					(Map) getProperties()
			));
		}
	}
}
