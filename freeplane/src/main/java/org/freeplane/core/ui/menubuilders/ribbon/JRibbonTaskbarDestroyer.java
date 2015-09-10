package org.freeplane.core.ui.menubuilders.ribbon;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;

public class JRibbonTaskbarDestroyer implements EntryVisitor {
	final private EntryAccessor entryAccessor;
	
	public JRibbonTaskbarDestroyer() {
		entryAccessor = new EntryAccessor();
	}

	@Override
	public void visit(Entry entry) {		
		((JRibbon)entryAccessor.getAncestorComponent(entry)).removeAllTaskbarComponents();
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}

}
