package dev.sircremefresh.autodba.controller.databaseserver.crd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseServerStatus {
	private String status;
}
