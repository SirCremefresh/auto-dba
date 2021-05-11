package dev.sircremefresh.autodba.controller.crd.database;

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
public class DatabaseServerRef {
	@NonNull
	private final String name;
}
