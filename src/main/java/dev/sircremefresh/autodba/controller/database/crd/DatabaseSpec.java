package dev.sircremefresh.autodba.controller.database.crd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseSpec {
	private String databaseName;
	private String secretName;
}
