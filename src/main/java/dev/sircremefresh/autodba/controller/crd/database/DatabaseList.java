package dev.sircremefresh.autodba.controller.crd.database;

import io.fabric8.kubernetes.client.CustomResourceList;
import io.sundr.builder.annotations.Buildable;
import lombok.ToString;

@Buildable(
		editableEnabled = false,
		generateBuilderPackage = false
)
@ToString(callSuper = true)
public class DatabaseList extends CustomResourceList<Database> {
}
