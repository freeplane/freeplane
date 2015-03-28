package org.freeplane.core.ui.menubuilders.generic;

public class ChildProcessor implements
EntryPopupListener {
	public ChildProcessor() {
		super();
	}

	private Processor processor;

	public void setProcessor(Processor processor) {
		this.processor = processor;
	}

	@Override
	public void childEntriesWillBecomeVisible(Entry entry) {
		if (RecursiveMenuStructureProcessor.shouldProcessOnEvent(entry)) {
			final Processor subtreeProcessor = processor.forChildren(entry.getRoot(), entry);
			for (Entry child : entry.children()) {
				subtreeProcessor.build(child);
			}
		}
	}

	@Override
	public void childEntriesWillBecomeInvisible(Entry entry) {
		if (RecursiveMenuStructureProcessor.shouldProcessOnEvent(entry)) {
			final Processor subtreeProcessor = processor.forChildren(entry.getRoot(), entry);
			for (Entry child : entry.children()) {
				subtreeProcessor.destroy(child);
			}
		}
	}
}