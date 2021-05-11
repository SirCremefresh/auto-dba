package dev.sircremefresh.autodba.controller.crd.database;

import io.sundr.builder.annotations.Buildable;
import lombok.*;

@Buildable(
		editableEnabled = false,
		generateBuilderPackage = false
)
@ToString
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseServerRef {
	@NonNull
	private String name;
}
