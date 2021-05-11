package dev.sircremefresh.autodba.controller.database.crd;

import io.sundr.builder.annotations.Buildable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Buildable(
		editableEnabled = false,
		generateBuilderPackage = false
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseStatus {
	private String status;
}
