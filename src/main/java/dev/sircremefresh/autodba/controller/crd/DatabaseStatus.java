package dev.sircremefresh.autodba.controller.crd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseStatus {
	private LocalDateTime lastTransitionTime;
	private String status;
	private String type;
}
