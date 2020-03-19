package org.freeplane.core.ui.menubuilders.generic;

import static java.lang.Boolean.TRUE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.freeplane.core.ui.menubuilders.generic.BuilderDestroyerPair.VisitorType;

public class RecursiveMenuStructureProcessor{

	final private Map<String, BuilderDestroyerPair> visitors;
	final private Map<String, String> subtreeDefaultVisitors;
	private LinkedList<String> subtreeDefaultVisitorStack;
	private BuilderDestroyerPair defaultBuilder = new BuilderDestroyerPair(EntryVisitor.ILLEGAL,
	    EntryVisitor.ILLEGAL);
	public static final String PROCESS_ON_POPUP = "processOnPopup"; 

	private RecursiveMenuStructureProcessor(Map<String, BuilderDestroyerPair> visitors,
	                                        Map<String, String> subtreeDefaultVisitors,
	                                        BuilderDestroyerPair defaultBuilder) {
		super();
		this.visitors = visitors;
		this.subtreeDefaultVisitors = subtreeDefaultVisitors;
		this.subtreeDefaultVisitorStack = new LinkedList<String>();
		this.defaultBuilder = defaultBuilder;
	}

	public RecursiveMenuStructureProcessor() {
		visitors = new HashMap<String, BuilderDestroyerPair>();
		subtreeDefaultVisitors = new HashMap<String, String>();
		subtreeDefaultVisitorStack = new LinkedList<String>(); 
	}

	public void addBuilderPair(String name, BuilderDestroyerPair pair) {
		visitors.put(name, pair);
	}

	public void addBuilderPair(String name, EntryVisitor builder, EntryVisitor destroyer) {
		addBuilderPair(name, new BuilderDestroyerPair(builder, destroyer));
	}

	public void addBuilder(String name, EntryVisitor builder) {
		addBuilderPair(name, new BuilderDestroyerPair(builder));
	}

	public void build(Entry target) {
		process(target, VisitorType.BUILDER);
	}

	private void process(Entry target, final VisitorType visitorType) {
		final BuilderDestroyerPair builderDestroyerPair = builderDestroyerPair(target);
		process(target, builderDestroyerPair, visitorType);
	}

	private void process(Entry target, final BuilderDestroyerPair builderDestroyerPair, VisitorType visitorType) {
	    final EntryVisitor visitor = builderDestroyerPair.get(visitorType);
		final boolean shouldSkipChildren = visitor.shouldSkipChildren(target);
		visitor.visit(target);
		if (!(shouldSkipChildren || shouldProcessOnEvent(target)))
			processChildren(target, visitorType);
    }

	public void destroy(Entry target) {
		process(target, VisitorType.DESTROYER);
	}

	public static boolean shouldProcessOnEvent(Entry target) {
		return TRUE.equals(target.getAttribute(PROCESS_ON_POPUP));
	}

	private void processChildren(Entry target, VisitorType visitorType) {
		final int originalDefaultBuilderStackSize = subtreeDefaultVisitorStack.size();
		final String visitorToCall = visitorToCall(target);
		if(visitorToCall != null)
			changeDefaultBuilder(visitorToCall);
		final List<Entry> children = target.children();
		for(Entry child:children.toArray(new Entry[children.size()])) {
			process(child, visitorType);
		}
		if(originalDefaultBuilderStackSize < subtreeDefaultVisitorStack.size())
			subtreeDefaultVisitorStack.removeLast();
	}

	private BuilderDestroyerPair builderDestroyerPair(Entry target) {
	    final String builderToCall = visitorToCall(target);
		final BuilderDestroyerPair builder;
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
		String explicitBuilderName = explicitBuilderName(target);
		if (explicitBuilderName != null)
			return explicitBuilderName;
		else if (subtreeDefaultVisitorStack.isEmpty())
			return null;
		else
			return subtreeDefaultVisitorStack.getLast();
	}

	public void setSubtreeDefaultBuilderPair(String builder, String subtreeBuilder) {
		subtreeDefaultVisitors.put(builder, subtreeBuilder);
	}

	public void setDefaultBuilder(EntryVisitor defaultBuilder) {
		setDefaultBuilderPair(new BuilderDestroyerPair(defaultBuilder, EntryVisitor.EMTPY));
	}

	public void setDefaultBuilderPair(EntryVisitor defaultBuilder, EntryVisitor defaultDestroyer) {
		setDefaultBuilderPair(new BuilderDestroyerPair(defaultBuilder, defaultDestroyer));
	}

	private void setDefaultBuilderPair(BuilderDestroyerPair pair) {
		this.defaultBuilder = pair;
	}

	public BuilderDestroyerPair findSubtreeChildrenDefaultBuilder(Entry root, Entry entry) {
		final Entry explicitDefaultBuilderEntry = explicitDefaultBuilderEntry(root, entry);
		if (explicitDefaultBuilderEntry != null) {
			String builderName = explicitBuilderName(explicitDefaultBuilderEntry);
			int count = 1; 
			for (Entry index = entry; index != explicitDefaultBuilderEntry; index = index.getParent()) {
			    count++;
			}
			for (int i = 0; i < count; i++) {
			    final String nextExplicitDefaultBuilderName = subtreeDefaultVisitors.get(builderName);
			    if (nextExplicitDefaultBuilderName != null)
			        builderName = nextExplicitDefaultBuilderName;
			}
			return visitors.get(builderName);
		}
		return defaultBuilder;
	}

	private Entry explicitDefaultBuilderEntry(Entry root, Entry entry) {
		String explicitBuilderName = explicitBuilderName(entry);
		final BuilderDestroyerPair explicitDefaultBuilder = explicitDefaultBuilder(explicitBuilderName);
		if (explicitDefaultBuilder != null)
			return entry;
		else if (root == entry)
			return null;
		else
			return explicitDefaultBuilderEntry(root, entry.getParent());
	}

	private BuilderDestroyerPair explicitDefaultBuilder(String explicitBuilderName) {
	    final String subtreeDefaultBuilder = subtreeDefaultVisitors.get(explicitBuilderName);
		final BuilderDestroyerPair explicitDefaultBuilder = visitors.get(subtreeDefaultBuilder);
	    return explicitDefaultBuilder;
    }

	private String explicitBuilderName(Entry entry) {
		String builderToCall = null;
		if (entry != null) {
		for (String visitorName : entry.builders())
			if (visitors.containsKey(visitorName)) {
				builderToCall = visitorName;
				break;
			}
		}
	    return builderToCall;
    }

	public RecursiveMenuStructureProcessor forChildren(Entry root, Entry subtreeRoot) {
		return new RecursiveMenuStructureProcessor(visitors, subtreeDefaultVisitors, findSubtreeChildrenDefaultBuilder(root, subtreeRoot));
	}

	public boolean containsOneOf(Collection<String> builders) {
		for(String builder:builders)
			if(visitors.containsKey(builder))
				return true;
		return false;
	}

}
