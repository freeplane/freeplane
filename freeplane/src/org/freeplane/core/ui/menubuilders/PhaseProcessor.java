package org.freeplane.core.ui.menubuilders;

public class PhaseProcessor {

	final private RecursiveMenuStructureProcessor[] processors;

	public PhaseProcessor(RecursiveMenuStructureProcessor... processors) {
		this.processors = processors;
	}

	public void process(Entry entry) {
		for(RecursiveMenuStructureProcessor processor:processors)
			processor.process(entry);
	}

}
