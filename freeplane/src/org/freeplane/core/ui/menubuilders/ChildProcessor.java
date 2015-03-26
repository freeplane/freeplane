package org.freeplane.core.ui.menubuilders;

import static java.lang.Boolean.TRUE;
import static org.freeplane.core.ui.menubuilders.RecursiveMenuStructureProcessor.PROCESS_ON_POPUP;

class ChildProcessor implements
EntryPopupListener {
	private Processor processor;

	@Override
	public void childEntriesWillBecomeVisible(Entry entry) {
		if (TRUE.equals(entry.getAttribute(PROCESS_ON_POPUP))) {
			final Processor subtreeProcessor = processor.forChildren(entry.getRoot(), entry);
			for (Entry child : entry.children()) {
				subtreeProcessor.build(child);
			}
		}
	}

	public void setProcessor(Processor processor) {
		this.processor = processor;
		
	}

	@Override
	public void childEntriesWillBecomeInvisible(Entry entry) {
	}
}