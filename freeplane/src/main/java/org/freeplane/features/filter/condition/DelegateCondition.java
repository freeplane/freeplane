package org.freeplane.features.filter.condition;

import org.freeplane.features.map.NodeModel;

public class DelegateCondition extends ASelectableCondition {
    private final ICondition delegate;
    private final String name;
    
    public DelegateCondition(ICondition delegate, String name) {
        super();
        this.delegate = delegate;
        this.name = name;
    }

    @Override
    public boolean checkNode(final NodeModel node) {
        return delegate.checkNode(node);
    }

    @Override
    protected String createDescription() {
        return "<" + name + ">";
    }

    @Override
    protected String getName() {
        return name;
    }

    @Override
    public boolean canBePersisted() {
        return false;
    }
    
    

}
