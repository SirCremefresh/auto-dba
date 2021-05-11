package dev.sircremefresh.autodba.controller.crd.clusterdatabaseserver;

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
public class AuthSecretRef {
	@NonNull
	private String name;
}
