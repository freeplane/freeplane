package org.freeplane.core.ui.menubuilders;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class RecursiveMenuStructureBuilder implements EntryVisitor{

	final private Map<String, EntryVisitor> builders;
	final private Map<String, String> subtreeDefaultBuilders;
	private LinkedList<String> subtreeDefaultBuilderStack;
	private EntryVisitor defaultBuilder = EntryVisitor.ILLEGAL_VISITOR; 
	final private Map<Integer, EntryPopupListener> entryPopupListeners;
	private static final String DELAYED_BUILD_ATTRIBUTE = "delayedBuild";

	public RecursiveMenuStructureBuilder() {
		builders = new HashMap<String, EntryVisitor>();
		subtreeDefaultBuilders = new HashMap<String, String>();
		subtreeDefaultBuilderStack = new LinkedList<>(); 
		entryPopupListeners = new HashMap<>();
	}

	public void addBuilder(String name, EntryVisitor builder) {
		builders.put(name, builder);
	}

	@Override
	public void visit(Entry target) {
		final EntryVisitor builder = builder(target);
		builder.visit(target);
//		if(builder.shouldProcessChildren())
			buildChildren(target);
	}

	private void buildChildren(Entry target) {
		processChildren(target);
	}

	private void processChildren(Entry target) {
		final int originalDefaultBuilderStackSize = subtreeDefaultBuilderStack.size();
		final String builderToCall = builderToCall(target);
		if(builderToCall != null)
			changeDefaultBuilder(builderToCall);
		for(Entry child:target.children()) {
				visit(child);
		}
		if(originalDefaultBuilderStackSize < subtreeDefaultBuilderStack.size())
			subtreeDefaultBuilderStack.removeLast();
	}

	private EntryVisitor builder(Entry target) {
		final String builderToCall = builderToCall(target);
		final EntryVisitor builder;
		if(builderToCall != null)
			builder = builders.get(builderToCall);
		else
			builder = defaultBuilder;
		return builder;
	}

	private void changeDefaultBuilder(String calledBuilder) {
		final String defaultBuilder = subtreeDefaultBuilders.get(calledBuilder);
		if (defaultBuilder != null && (subtreeDefaultBuilderStack.isEmpty() || ! subtreeDefaultBuilderStack.getLast().equals(defaultBuilder)))
			subtreeDefaultBuilderStack.addLast(defaultBuilder);
	}

	private String builderToCall(Entry target) {
		for(String builderName :  target.builders()) 
			if(builders.containsKey(builderName))
				return builderName;
		if (subtreeDefaultBuilderStack.isEmpty())
			return null;
		return subtreeDefaultBuilderStack.getLast();
	}

	public void addSubtreeDefaultBuilder(String builder, String subtreeBuilder) {
		subtreeDefaultBuilders.put(builder, subtreeBuilder);
	}

	public void setDefaultBuilder(EntryVisitor defaultBuilder) {
		this.defaultBuilder = defaultBuilder;
	}

	@Override
	public boolean shouldSkipChildren() {
		// TODO Auto-generated method stub
		return false;
	}
}
