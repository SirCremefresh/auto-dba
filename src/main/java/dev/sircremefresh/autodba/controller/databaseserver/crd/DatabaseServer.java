package dev.sircremefresh.autodba.controller.databaseserver.crd;

import dev.sircremefresh.autodba.controller.database.crd.DatabaseSpec;
import dev.sircremefresh.autodba.controller.database.crd.DatabaseStatus;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Plural;
import io.fabric8.kubernetes.model.annotation.Version;
import io.sundr.builder.annotations.Buildable;

@Version("v1alpha1")
@Group("autodba.sircremefresh.dev")
@Kind("DatabaseServer")
@Plural("DatabaseServers")
@Buildable
public class DatabaseServer extends CustomResource<DatabaseSpec, DatabaseStatus> implements Namespaced {
}
