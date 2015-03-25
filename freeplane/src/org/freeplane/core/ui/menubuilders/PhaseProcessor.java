package org.freeplane.core.ui.menubuilders;

public class PhaseProcessor implements Processor{

	final private RecursiveMenuStructureProcessor[] processors;

	public PhaseProcessor(RecursiveMenuStructureProcessor... processors) {
		this.processors = processors;
	}

	@Override
	public void build(Entry entry) {
		for(RecursiveMenuStructureProcessor processor:processors)
			processor.process(entry);
	}

}
