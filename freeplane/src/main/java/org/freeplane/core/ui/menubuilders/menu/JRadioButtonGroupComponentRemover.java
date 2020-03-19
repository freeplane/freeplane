package org.freeplane.core.ui.menubuilders.menu;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;

public class JRadioButtonGroupComponentRemover implements EntryVisitor{
    private final JComponentRemover remover = new JComponentRemover();
    
    
	@Override
    public void visit(Entry target) {
        target.children().forEach(remover::visit);
        remover.visit(target);
    }


    @Override
	public boolean shouldSkipChildren(Entry entry) {
		return true;
	}
	
}