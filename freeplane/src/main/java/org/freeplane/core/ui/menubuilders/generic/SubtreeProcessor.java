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
		final Processor subtreeProcessor = forChildren(entry);
		for (Entry child : entry.children()) {
			subtreeProcessor.build(child);
		}
	}

	public void rebuildEntry(Entry entry) {
		final Entry parent = entry.getParent();
		final Processor subtreeProcessor = parent != null ? forChildren(parent) : processor;
		subtreeProcessor.destroy(entry);
		subtreeProcessor.build(entry);
	}

	private Processor forChildren(Entry entry) {
	    final Entry root = entry.getRoot();
		final Processor subtreeProcessor = processor.forChildren(root, entry);
	    return subtreeProcessor;
    }

	@Override
	public void childEntriesWillBecomeInvisible(Entry entry) {
		if (RecursiveMenuStructureProcessor.shouldProcessOnEvent(entry)) {
			destroyChildren(entry);
		}
	}

	public void destroyChildren(Entry entry) {
		final Processor subtreeProcessor = forChildren(entry);
		for (Entry child : entry.children()) {
			subtreeProcessor.destroy(child);
		}
	}
	
	public void rebuildChildren(Entry entry){
		destroyChildren(entry);
		buildChildren(entry);
	}
}