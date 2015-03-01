package org.freeplane.core.ui.menubuilders;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class RecursiveMenuStructureProcessor{

	final private Map<String, EntryVisitor> visitors;
	final private Map<String, String> subtreeDefaultVisitors;
	private LinkedList<String> subtreeDefaultVisitorStack;
	private EntryVisitor defaultBuilder = EntryVisitor.ILLEGAL_VISITOR;
	public static final String PROCESS_ON_POPUP = "processOnPopup"; 

	public RecursiveMenuStructureProcessor() {
		visitors = new HashMap<String, EntryVisitor>();
		subtreeDefaultVisitors = new HashMap<String, String>();
		subtreeDefaultVisitorStack = new LinkedList<>(); 
	}

	public void addBuilder(String name, EntryVisitor builder) {
		visitors.put(name, builder);
	}

	public void process(Entry target) {
		final EntryVisitor builder = visitor(target);
		builder.visit(target);
		if(! builder.shouldSkipChildren(target))
			processChildren(target);
	}

	private void processChildren(Entry target) {
		final int originalDefaultBuilderStackSize = subtreeDefaultVisitorStack.size();
		final String builderToCall = visitorToCall(target);
		if(builderToCall != null)
			changeDefaultBuilder(builderToCall);
		for(Entry child:target.children()) {
				process(child);
		}
		if(originalDefaultBuilderStackSize < subtreeDefaultVisitorStack.size())
			subtreeDefaultVisitorStack.removeLast();
	}

	private EntryVisitor visitor(Entry target) {
		final String builderToCall = visitorToCall(target);
		final EntryVisitor builder;
		if(builderToCall != null)
			builder = visitors.get(builderToCall);
		else
			builder = defaultBuilder;
		return builder;
	}

	private void changeDefaultBuilder(String calledBuilder) {
		final String defaultBuilder = subtreeDefaultVisitors.get(calledBuilder);
		if (defaultBuilder != null && (subtreeDefaultVisitorStack.isEmpty() || ! subtreeDefaultVisitorStack.getLast().equals(defaultBuilder)))
			subtreeDefaultVisitorStack.addLast(defaultBuilder);
	}

	private String visitorToCall(Entry target) {
		for(String visitorName :  target.builders()) 
			if(visitors.containsKey(visitorName))
				return visitorName;
		if (subtreeDefaultVisitorStack.isEmpty())
			return null;
		return subtreeDefaultVisitorStack.getLast();
	}

	public void addSubtreeDefaultBuilder(String builder, String subtreeBuilder) {
		subtreeDefaultVisitors.put(builder, subtreeBuilder);
	}

	public void setDefaultBuilder(EntryVisitor defaultBuilder) {
		this.defaultBuilder = defaultBuilder;
	}
}
