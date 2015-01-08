package org.freeplane.core.ui.menubuilders;

import java.util.HashMap;
import java.util.LinkedList;

public class RecursiveMenuStructureBuilder implements Builder{

	final private HashMap<String, Builder> builders;
	final private HashMap<String, Builder> subtreeDefaultBuilders;
	private LinkedList<Builder> subtreeDefaultBuilderStack; 

	public RecursiveMenuStructureBuilder() {
		builders = new HashMap<String, Builder>();
		subtreeDefaultBuilders = new HashMap<String, Builder>();
		subtreeDefaultBuilderStack = new LinkedList<>(); 
		subtreeDefaultBuilderStack.addLast(ILLEGAL_BUILDER);
	}

	public void addBuilder(String name, Builder builder) {
		builders.put(name, builder);
	}

	@Override
	public void build(Entry target) {
		callBuilder(target);
		boolean defaultBuilderChanged = changeDefaultBuilder(target);
		for(Entry child:target.children())
			build(child);
		if(defaultBuilderChanged)
			subtreeDefaultBuilderStack.removeLast();
	}

	private boolean changeDefaultBuilder(Entry target) {
		boolean defaultBuilderChanged = false;
		for(String builderName :  target.builders()) {
			final Builder defaultBuilder = subtreeDefaultBuilders.get(builderName);
			defaultBuilderChanged = defaultBuilder != null;
			if(defaultBuilderChanged){
				subtreeDefaultBuilderStack.addLast(defaultBuilder);
				break;
			}
		}
		return defaultBuilderChanged;
	}

	public void callBuilder(Entry target) {
		for(String builderName :  target.builders()) {
			final Builder explicitBuilder = builders.get(builderName);
			if(explicitBuilder != null){
				explicitBuilder.build(target);
				return;
			}
		}
		subtreeDefaultBuilderStack.getLast().build(target);
	}

	public void addSubtreeDefaultBuilder(String name, Builder builder) {
		subtreeDefaultBuilders.put(name, builder);
	}

}
