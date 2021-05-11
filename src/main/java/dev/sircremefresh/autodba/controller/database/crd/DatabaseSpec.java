package dev.sircremefresh.autodba.controller.database.crd;

import io.sundr.builder.annotations.Buildable;
import lombok.*;

@Buildable(
		editableEnabled = false,
		generateBuilderPackage = false
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseSpec {
	@NonNull
	private String databaseName;
	@NonNull
	private DatabaseServerRef serverRef;
}
