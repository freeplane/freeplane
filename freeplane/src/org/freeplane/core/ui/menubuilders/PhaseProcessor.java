package org.freeplane.core.ui.menubuilders;

import java.util.Arrays;
import java.util.ListIterator;

public class PhaseProcessor implements Processor{

	final private RecursiveMenuStructureProcessor[] processors;

	public PhaseProcessor(RecursiveMenuStructureProcessor... processors) {
		this.processors = processors;
	}

	@Override
	public void build(Entry entry) {
		for(RecursiveMenuStructureProcessor processor:processors)
			processor.build(entry);
	}

	@Override
	public Processor forChildren(Entry root, Entry entry) {
		RecursiveMenuStructureProcessor[] subtreeProcessors = new RecursiveMenuStructureProcessor[processors.length];
		int i = 0;
		for (RecursiveMenuStructureProcessor processor : processors)
			subtreeProcessors[i++] = processor.forChildren(root, entry);
		return new PhaseProcessor(subtreeProcessors);
	}

	@Override
	public void destroy(Entry entry) {
		final ListIterator<RecursiveMenuStructureProcessor> processorIterator = Arrays.asList(processors).listIterator(
		    processors.length);
		while (processorIterator.hasPrevious())
			processorIterator.previous().destroy(entry);
	}
}
