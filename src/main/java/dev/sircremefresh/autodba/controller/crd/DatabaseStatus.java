package dev.sircremefresh.autodba.controller.crd;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class DatabaseStatus {
	private LocalDateTime lastTransitionTime;
	private String status;
	private String type;
}
