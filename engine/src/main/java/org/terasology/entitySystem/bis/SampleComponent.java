package org.terasology.entitySystem.bis;

import javax.vecmath.Vector3f;

public interface SampleComponent extends ComponentBis {
    @GetProperty("location")
    public Vector3f getLocation();

    @SetProperty("location")
    public void setLocation(Vector3f location);
}
