package dev.sircremefresh.autodba;

public class TestAutoDbaApplication {
	public static void main(String[] args) {
		var application = AutoDbaApplication.createSpringApplication();

//		application.addInitializers(new AbstractIntegrationTest.Initializer());

		application.run(args);
	}
}
