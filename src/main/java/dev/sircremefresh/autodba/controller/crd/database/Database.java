package dev.sircremefresh.autodba.controller.crd.database;

import io.fabric8.kubernetes.api.model.Namespaced;
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
@Kind("Database")
@Plural("databases")
@Singular("database")
public class Database extends CustomResource<DatabaseSpec, DatabaseStatus> implements Namespaced {
}
