package dev.sircremefresh.autodba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AutoDbaApplication {


	public static void main(String[] args) {
		createSpringApplication().run(args);
	}

	public static SpringApplication createSpringApplication() {
		return new SpringApplication(AutoDbaApplication.class);
	}

}
