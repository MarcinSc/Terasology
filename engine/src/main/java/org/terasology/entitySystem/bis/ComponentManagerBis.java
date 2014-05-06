package org.terasology.entitySystem.bis;

public interface ComponentManagerBis {
    public <T extends ComponentBis> T createComponent(Class<T> clazz);
    public <T extends ComponentBis> T copyComponent(Class<T> clazz, T component);
}
