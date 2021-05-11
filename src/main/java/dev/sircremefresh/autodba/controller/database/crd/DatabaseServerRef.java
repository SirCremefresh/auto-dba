package dev.sircremefresh.autodba.controller.database.crd;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseServerRef {
	@NonNull
	private String type;
	@NonNull
	private String name;
}
