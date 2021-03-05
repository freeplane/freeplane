package org.freeplane.core.ui.menubuilders.menu;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;

public class JRadioButtonGroupComponentRemover implements EntryVisitor{
	public static final JRadioButtonGroupComponentRemover INSTANCE = new JRadioButtonGroupComponentRemover();

	private final JComponentRemover remover = JComponentRemover.INSTANCE;
    
	private JRadioButtonGroupComponentRemover() {/**/}


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