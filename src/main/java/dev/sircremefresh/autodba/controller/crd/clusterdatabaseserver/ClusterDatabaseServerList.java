package dev.sircremefresh.autodba.controller.crd.clusterdatabaseserver;

import io.fabric8.kubernetes.client.CustomResourceList;
import io.sundr.builder.annotations.Buildable;
import lombok.ToString;

@Buildable(
		editableEnabled = false,
		generateBuilderPackage = false
)
@ToString(callSuper = true)
public class ClusterDatabaseServerList extends CustomResourceList<ClusterDatabaseServer> {
}
