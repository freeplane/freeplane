package org.freeplane.core.ui.menubuilders;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class RecursiveMenuStructureBuilder implements Builder{

	final private Map<String, Builder> builders;
	final private Map<String, String> subtreeDefaultBuilders;
	private LinkedList<String> subtreeDefaultBuilderStack;
	private Builder defaultBuilder = Builder.ILLEGAL_BUILDER; 
	final private Map<Integer, EntryPopupListener> entryPopupListeners;
	private boolean delayChildBuild;
	private static final String DELAYED_BUILD_ATTRIBUTE = "delayedBuild";

	public RecursiveMenuStructureBuilder() {
		builders = new HashMap<String, Builder>();
		subtreeDefaultBuilders = new HashMap<String, String>();
		subtreeDefaultBuilderStack = new LinkedList<>(); 
		entryPopupListeners = new HashMap<>();
		delayChildBuild = true;
	}

	public void addBuilder(String name, Builder builder) {
		builders.put(name, builder);
	}

	@Override
	public void build(Entry target) {
		final Builder builder = builder(target);
		builder.build(target);
		if(shouldDelayChildBuild(target))
			startProcessingChildrenWhenTheirVisibilityChanges(target);
		else
			buildChildren(target);
	}

	private boolean shouldDelayChildBuild(Entry target) {
		return delayChildBuild && Boolean.TRUE.equals(target.getAttribute(RecursiveMenuStructureBuilder.DELAYED_BUILD_ATTRIBUTE));
	}

	private void startProcessingChildrenWhenTheirVisibilityChanges(Entry target) {
		final EntryPopupListener entryPopupListener = new EntryPopupListener() {
			@Override
			public void childEntriesWillBecomeVisible(Entry entry) {
				buildChildren(entry);
			}
			
			@Override
			public void childEntriesWillBecomeInvisible(Entry entry) {
				destroyChildren(entry);
			}
		};
		entryPopupListeners.put(System.identityHashCode(target), entryPopupListener);
		new EntryPopupListenerAccessor(target).addEntryPopupListener(entryPopupListener);
	}

	@Override
	public void destroy(Entry target) {
		if(shouldDelayChildBuild(target))
			cancelProcessingChildrenWhenTheirVisibilityChanges(target);
		else
			destroyChildren(target);
		final Builder builder = builder(target);
		builder.destroy(target);
	}

	private void cancelProcessingChildrenWhenTheirVisibilityChanges(Entry target) {
		EntryPopupListener entryPopupListener = entryPopupListeners.remove(System.identityHashCode(target));
		new EntryPopupListenerAccessor(target).removeEntryPopupListener(entryPopupListener);
	}

	private void buildChildren(Entry target) {
		processChildren(target, true);
	}

	private void destroyChildren(Entry target) {
		processChildren(target, false);
	}
	
	private void processChildren(Entry target, boolean build) {
		final int originalDefaultBuilderStackSize = subtreeDefaultBuilderStack.size();
		final String builderToCall = builderToCall(target);
		if(builderToCall != null)
			changeDefaultBuilder(builderToCall);
		for(Entry child:target.children()) {
			if(build)
				build(child);
			else
				destroy(child);
		}
		if(originalDefaultBuilderStackSize < subtreeDefaultBuilderStack.size())
			subtreeDefaultBuilderStack.removeLast();
	}

	private Builder builder(Entry target) {
		final String builderToCall = builderToCall(target);
		final Builder builder;
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

	public void setDefaultBuilder(Builder defaultBuilder) {
		this.defaultBuilder = defaultBuilder;
	}

	public void noDelay() {
		delayChildBuild = false;
	}
}
