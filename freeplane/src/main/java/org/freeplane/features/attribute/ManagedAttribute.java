/*
 * Created on 1 Feb 2024
 *
 * author dimitry
 */
package org.freeplane.features.attribute;

public class ManagedAttribute extends Attribute{

    public ManagedAttribute(Attribute pAttribute) {
        super(pAttribute);
    }

    public ManagedAttribute(String name, Object value) {
        super(name, value);
    }

    public ManagedAttribute(String name) {
        super(name);
    }

    @Override
    public boolean isManaged() {
        return true;
    }
}
