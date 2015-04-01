package org.freeplane.core.ui.menubuilders.action;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.util.Compat;

public class AcceleratorBuilder implements EntryVisitor{

	private final IAcceleratorMap map;
	private final IEntriesForAction entries;

	public AcceleratorBuilder(IAcceleratorMap map, IEntriesForAction entries) {
		this.map = map;
		this.entries = entries;
	}

	public void visit(Entry entry) {
		final AFreeplaneAction action = new EntryAccessor().getAction(entry);
		if (action != null) {
			final EntryAccessor entryAccessor = new EntryAccessor();
			String accelerator = entryAccessor.getAccelerator(entry);
			final String key = entry.getName();
			if(accelerator != null) {
				if (isMacOsX()) {
			        accelerator = accelerator.replaceFirst("CONTROL", "META").replaceFirst("control", "meta");
			    }
				map.setDefaultAccelerator(key, accelerator);
			}
			entries.registerEntry(action, entry);
		}
	}

	protected boolean isMacOsX() {
		return Compat.isMacOsX();
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}

}
