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

	@Override
	public Processor forChildren(Entry root, Entry entry) {
		RecursiveMenuStructureProcessor[] subtreeProcessors = new RecursiveMenuStructureProcessor[processors.length];
		int i = 0;
		for (RecursiveMenuStructureProcessor processor : processors)
			subtreeProcessors[i++] = processor.forChildren(root, entry);
		return new PhaseProcessor(subtreeProcessors);
	}
}
