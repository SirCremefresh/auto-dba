package dev.sircremefresh.autodba.controller.databaseserver.crd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterDatabaseServerSpec {
	private String secretName;
	private String type;
}
