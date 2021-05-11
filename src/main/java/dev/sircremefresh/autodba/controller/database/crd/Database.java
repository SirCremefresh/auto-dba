package dev.sircremefresh.autodba.controller.database.crd;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Plural;
import io.fabric8.kubernetes.model.annotation.Version;
import io.sundr.builder.annotations.Buildable;

@Version("v1alpha1")
@Group("autodba.sircremefresh.dev")
@Kind("Database")
@Plural("Databases")
@Buildable
public class Database extends CustomResource<DatabaseSpec, DatabaseStatus> implements Namespaced {
}
