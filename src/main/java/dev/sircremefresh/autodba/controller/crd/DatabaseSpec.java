package dev.sircremefresh.autodba.controller.crd;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class DatabaseSpec {
	private String databaseName;
	private String secretName;
}
