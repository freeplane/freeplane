package org.freeplane.core.ui.menubuilders.generic;

public class SubtreeProcessor implements EntryPopupListener {
	public SubtreeProcessor() {
		super();
	}

	private Processor processor;

	public void setProcessor(Processor processor) {
		this.processor = processor;
	}

	@Override
	public void childEntriesWillBecomeVisible(Entry entry) {
		if (RecursiveMenuStructureProcessor.shouldProcessOnEvent(entry)) {
			buildChildren(entry);
		}
	}

	public void buildChildren(Entry entry) {
		final Processor subtreeProcessor = processor.forChildren(entry.getRoot(), entry);
		for (Entry child : entry.children()) {
			subtreeProcessor.build(child);
		}
	}

	@Override
	public void childEntriesWillBecomeInvisible(Entry entry) {
		if (RecursiveMenuStructureProcessor.shouldProcessOnEvent(entry)) {
			destroyChildren(entry);
		}
	}

	public void destroyChildren(Entry entry) {
		final Processor subtreeProcessor = processor.forChildren(entry.getRoot(), entry);
		for (Entry child : entry.children()) {
			subtreeProcessor.destroy(child);
		}
	}
	
	public void rebuildChildren(Entry entry){
		destroyChildren(entry);
		buildChildren(entry);
	}
}