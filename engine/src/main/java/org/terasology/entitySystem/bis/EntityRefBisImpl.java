package org.terasology.entitySystem.bis;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;

public class EntityRefBisImpl implements EntityRefBis {
    private static final Object NULL_VALUE = new Object();
    private Set<Class<? extends ComponentBis>> components = Sets.newHashSet();
    private Table<Class<? extends ComponentBis>, String, Object> storedValues = HashBasedTable.create();

    @Override
    public <T extends ComponentBis> T getComponent(Class<T> clazz) {
        if (!components.contains(clazz)) {
            return null;
        }
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ComponentView(clazz));
    }

    @Override
    public <T extends ComponentBis> T addComponent(Class<T> clazz) {
        if (components.contains(clazz)) {
            throw new IllegalStateException("This entity already contains a component of that type");
        }
        components.add(clazz);
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ComponentView(clazz));
    }

    @Override
    public <T extends ComponentBis> void removeComponent(Class<T> clazz) {
        if (!components.contains(clazz)) {
            throw new IllegalStateException("This entity does not contain a component of that type");
        }
        components.remove(clazz);
        storedValues.rowKeySet().remove(clazz);
    }

    @Override
    public <T extends ComponentBis> void saveComponent(Class<T> clazz, T component) {
        if (!components.contains(clazz)) {
            throw new IllegalStateException("This entity does not contain a component of that type");
        }
        final ComponentView componentView = (ComponentView) Proxy.getInvocationHandler(component);
        final Map<String, Object> row = storedValues.row(clazz);
        for (Map.Entry<String, Object> change : componentView.changes.entrySet()){
            final Object newValue = change.getValue();
            if (newValue == NULL_VALUE) {
                row.remove(change.getKey());
            } else {
                row.put(change.getKey(), newValue);
            }
        }

    }

    private class ComponentView implements InvocationHandler {
        private Class<? extends ComponentBis> component;
        private Map<String, Object> changes = Maps.newHashMap();

        private ComponentView(Class<? extends ComponentBis> component) {
            this.component = component;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            final GetProperty get = method.getAnnotation(GetProperty.class);
            if (get != null) {
                return handleGet(get);
            }
            final SetProperty set = method.getAnnotation(SetProperty.class);
            if (set != null) {
                return handleSet(args[0], set);
            }
            throw new IllegalStateException("Component method invoked without property defined");
        }

        private Object handleSet(Object arg, SetProperty set) {
            if (arg == null) {
                arg = NULL_VALUE;
            }
            changes.put(set.value(), arg);
            
            return null;
        }

        private Object handleGet(GetProperty get) {
            final String propertyName = get.value();
            final Object changedValue = changes.get(propertyName);
            if (changedValue != null) {
                if (changedValue == NULL_VALUE) {
                    return null;
                } else {
                    return changedValue;
                }
            } else {
                final Object storedValue = storedValues.get(component, propertyName);
                if (storedValue == NULL_VALUE) {
                    return null;
                } else {
                    return storedValue;
                }
            }
        }
    }
}
