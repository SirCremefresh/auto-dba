package dev.sircremefresh.autodba.controller.database.crd;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Plural;
import io.fabric8.kubernetes.model.annotation.Version;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;

@Buildable(
		editableEnabled = false,
		generateBuilderPackage = false,
		refs = {@BuildableReference(ObjectMeta.class)}
)
@Version("v1alpha1")
@Group("autodba.sircremefresh.dev")
@Kind("Database")
@Plural("Databases")
public class Database extends CustomResource<DatabaseSpec, DatabaseStatus> implements Namespaced {
}
