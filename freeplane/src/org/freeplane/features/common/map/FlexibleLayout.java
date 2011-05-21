package org.freeplane.features.common.map;

import org.freeplane.core.addins.NodeHookDescriptor;
import org.freeplane.core.addins.PersistentNodeHook;
import org.freeplane.core.extension.IExtension;
import org.freeplane.n3.nanoxml.XMLElement;

@NodeHookDescriptor(hookName = "FlexibleLayout")
public class FlexibleLayout extends PersistentNodeHook implements IExtension {
    static public enum Type implements IExtension{
        SIBLINGS, CHILDREN 
    }

    @Override
    protected Class<? extends IExtension> getExtensionClass() {
        return Type.class;
    }

    public static Type getType(MapModel map) {
        return (Type) map.getRootNode().getExtension(Type.class);
    };
    
    
}
