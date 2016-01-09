package org.freeplane.core.ui.menubuilders.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;

public class PhaseProcessor{
	public static enum Phase {
		ACTIONS, ACCELERATORS, UI
	}

	final private LinkedHashMap<Phase, RecursiveMenuStructureProcessor> processors;
	final private Collection<BuildPhaseListener> buildPhaseListeners;
	

	public PhaseProcessor(Collection<BuildPhaseListener> buildPhaseListeners) {
		this.processors = new LinkedHashMap<Phase, RecursiveMenuStructureProcessor>();
		this.buildPhaseListeners = buildPhaseListeners;
	}
	
	public PhaseProcessor(){
		this(Collections.<BuildPhaseListener>emptyList());
	}

	
	public void build(Entry entry) {
		for ( Map.Entry<Phase, RecursiveMenuStructureProcessor> processEntry : processors.entrySet()){
			processEntry.getValue().build(entry);
			for(BuildPhaseListener listener : buildPhaseListeners)
				listener.buildPhaseFinished(processEntry.getKey(), entry);
		}
	}

	
	public PhaseProcessor forChildren(Entry root, Entry entry) {
		final PhaseProcessor phaseProcessor = new PhaseProcessor(buildPhaseListeners);
		for (java.util.Map.Entry<Phase, RecursiveMenuStructureProcessor> processor : processors.entrySet())
			phaseProcessor.withPhase(processor.getKey(), processor.getValue().forChildren(root, entry));
		return phaseProcessor;
	}

	public PhaseProcessor withPhase(Phase phaseName, RecursiveMenuStructureProcessor processor) {
		processors.put(phaseName, processor);
		return this;
	}

	public RecursiveMenuStructureProcessor phase(Phase name) {
		return processors.get(name);
	}

	
	public void destroy(Entry entry) {
		final ListIterator<RecursiveMenuStructureProcessor> processorIterator = new ArrayList<RecursiveMenuStructureProcessor>(processors.values())
		    .listIterator(processors.size());
		while (processorIterator.hasPrevious())
			processorIterator.previous().destroy(entry);
	}

	public void buildChildren(Entry entry) {
		for ( Map.Entry<Phase, RecursiveMenuStructureProcessor> processEntry : processors.entrySet()){
			for(Entry child : new ArrayList<Entry>(entry.children()))
				processEntry.getValue().build(child);
			for(BuildPhaseListener listener : buildPhaseListeners)
				listener.buildPhaseFinished(processEntry.getKey(), entry);
		}
	}
}
