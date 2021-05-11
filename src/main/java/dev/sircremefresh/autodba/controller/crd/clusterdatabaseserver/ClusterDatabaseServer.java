package dev.sircremefresh.autodba.controller.crd.clusterdatabaseserver;

import dev.sircremefresh.autodba.controller.crd.database.DatabaseSpec;
import dev.sircremefresh.autodba.controller.crd.database.DatabaseStatus;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.*;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;
import lombok.ToString;

@Buildable(
		editableEnabled = false,
		generateBuilderPackage = false,
		refs = {@BuildableReference(ObjectMeta.class)}
)
@ToString(callSuper = true)
@Version("v1alpha1")
@Group("autodba.sircremefresh.dev")
@Kind("ClusterDatabaseServer")
@Singular("clusterdatabaseserver")
@Plural("clusterdatabaseservers")
public class ClusterDatabaseServer extends CustomResource<ClusterDatabaseServerSpec, ClusterDatabaseServerStatus> {
}
