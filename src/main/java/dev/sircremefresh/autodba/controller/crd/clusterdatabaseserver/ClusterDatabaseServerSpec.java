package dev.sircremefresh.autodba.controller.crd.clusterdatabaseserver;

import io.sundr.builder.annotations.Buildable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Buildable(
		editableEnabled = false,
		generateBuilderPackage = false
)
@ToString
@Getter
@AllArgsConstructor
public class ClusterDatabaseServerSpec {
	@NonNull
	private AuthSecretRef authSecretRef;
	@NonNull
	private String jdbcUrl;
	@NonNull
	private String databaseType;
}
