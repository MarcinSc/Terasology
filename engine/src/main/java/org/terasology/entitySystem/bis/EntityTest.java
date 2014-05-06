package org.terasology.entitySystem.bis;

import javax.vecmath.Vector3f;

public class EntityTest {
    public static void main(String[] args) {
        EntityRefBisImpl entity = new EntityRefBisImpl();

        final SampleComponent original = entity.addComponent(SampleComponent.class);
        if (original.getLocation() != null) {
            throw new RuntimeException("fail");
        }
        original.setLocation(new Vector3f(1, 1, 1));
        if (original.getLocation() == null) {
            throw new RuntimeException("fail");
        }

        final SampleComponent copy = entity.getComponent(SampleComponent.class);
        if (copy.getLocation() != null) {
            // Shouldn't see unsaved changes
            throw new RuntimeException("fail");
        }

        entity.saveComponent(SampleComponent.class, original);

        if (copy.getLocation() == null) {
            // Changes immediately visible
            throw new RuntimeException("fail");
        }
    }
}
