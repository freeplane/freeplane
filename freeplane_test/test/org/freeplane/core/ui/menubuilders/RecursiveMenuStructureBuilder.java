package org.freeplane.core.ui.menubuilders;

import java.util.HashMap;
import java.util.LinkedList;

public class RecursiveMenuStructureBuilder implements Builder{

	final private HashMap<String, Builder> builders;
	final private HashMap<String, String> subtreeDefaultBuilders;
	private LinkedList<String> subtreeDefaultBuilderStack; 

	public RecursiveMenuStructureBuilder() {
		builders = new HashMap<String, Builder>();
		subtreeDefaultBuilders = new HashMap<String, String>();
		subtreeDefaultBuilderStack = new LinkedList<>(); 
	}

	public void addBuilder(String name, Builder builder) {
		builders.put(name, builder);
	}

	@Override
	public void build(Entry target) {
		final String builderToCall = builderToCall(target);
		final int originalDefaultBuilderStackSize = subtreeDefaultBuilderStack.size();
		changeDefaultBuilder(builderToCall);
		builders.get(builderToCall).build(target);
		for(Entry child:target.children())
			build(child);
		if(originalDefaultBuilderStackSize < subtreeDefaultBuilderStack.size())
			subtreeDefaultBuilderStack.removeLast();
	}

	private void changeDefaultBuilder(String calledBuilder) {
		final String defaultBuilder = subtreeDefaultBuilders.get(calledBuilder);
		if (defaultBuilder != null && (subtreeDefaultBuilderStack.isEmpty() || ! subtreeDefaultBuilderStack.getLast().equals(defaultBuilder)))
			subtreeDefaultBuilderStack.addLast(defaultBuilder);
	}

	public String builderToCall(Entry target) {
		for(String builderName :  target.builders()) 
			if(builders.containsKey(builderName))
				return builderName;
		if (subtreeDefaultBuilderStack.isEmpty())
			throw new IllegalStateException("no builder found");
		return subtreeDefaultBuilderStack.getLast();
	}

	public void addSubtreeDefaultBuilder(String name, String string) {
		subtreeDefaultBuilders.put(name, string);
	}

}
