package org.terasology.entitySystem.bis;

public interface EntityRefBis {
    public <T extends ComponentBis> T addComponent(Class<T> clazz);
    public <T extends ComponentBis> T getComponent(Class<T> clazz);
    public <T extends ComponentBis> void saveComponent(Class<T> clazz, T component);
    public <T extends ComponentBis> void removeComponent(Class<T> clazz);
}
