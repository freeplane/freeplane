package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;

public class JRibbonTaskbarBuilder implements EntryVisitor {
	final private EntryAccessor entryAccessor;
	private TaskbarDelegator delegator;
	
	public JRibbonTaskbarBuilder() {
		entryAccessor = new EntryAccessor();
	}

	@Override
	public void visit(Entry entry) {		
		entryAccessor.setComponent(entry, getDelegatorComponent(entry));
	}

	private TaskbarDelegator getDelegatorComponent(Entry entry) {
		if(delegator == null) {
			delegator = new TaskbarDelegator((JRibbon)entryAccessor.getAncestorComponent(entry));
		}
		return delegator;
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}

}

class TaskbarDelegator extends JRibbonContainer {
	final private JRibbon ribbon;

	public TaskbarDelegator(JRibbon ribbon) {
		this.ribbon = ribbon;
	}

	@Override
	public void add(Component component, Object constraints, int index) {
		this.ribbon.addTaskbarComponent(component);
	}

}

