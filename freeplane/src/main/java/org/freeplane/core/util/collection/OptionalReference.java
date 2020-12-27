package org.freeplane.core.util.collection;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

public class OptionalReference<T> {
    private final WeakReference<T> reference;

    public OptionalReference(T content) {
        super();
        this.reference = new WeakReference<>(content);
    }
    
    public T get() {
        T object = reference.get();
        if(object != null && ! reference.isEnqueued())
            return object;
        else
            return null;
    }
    
    public void ifPresent(Consumer<T> consumer) {
       T object = get();
       if(object != null)
           consumer.accept(object);
    }

    public boolean isPresent() {
        return reference.get() != null && !reference.isEnqueued();
    }

}
