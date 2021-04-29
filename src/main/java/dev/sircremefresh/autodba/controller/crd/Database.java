package dev.sircremefresh.autodba.controller.crd;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1alpha1")
@Group("autodba.sircremefresh.dev")
public class Database extends CustomResource<DatabaseSpec, DatabaseStatus> implements Namespaced {
}
