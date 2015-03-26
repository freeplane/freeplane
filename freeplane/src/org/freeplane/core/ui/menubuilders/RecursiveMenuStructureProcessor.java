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

	private RecursiveMenuStructureProcessor(Map<String, EntryVisitor> visitors,
	                                        Map<String, String> subtreeDefaultVisitors,
	                                        EntryVisitor defaultBuilder) {
		super();
		this.visitors = visitors;
		this.subtreeDefaultVisitors = subtreeDefaultVisitors;
		this.subtreeDefaultVisitorStack = new LinkedList<String>();
		this.defaultBuilder = defaultBuilder;
	}

	public RecursiveMenuStructureProcessor() {
		visitors = new HashMap<String, EntryVisitor>();
		subtreeDefaultVisitors = new HashMap<String, String>();
		subtreeDefaultVisitorStack = new LinkedList<>(); 
	}

	public void addBuilder(String name, EntryVisitor builder) {
		visitors.put(name, builder);
	}

	public void process(Entry target) {
		final EntryVisitor builder = builder(target);
		process(builder, target);
	}

	private void process(final EntryVisitor visitor, Entry target) {
		visitor.visit(target);
		if(! visitor.shouldSkipChildren(target))
			processChildren(target);
	}

	private void processChildren(Entry target) {
		final int originalDefaultBuilderStackSize = subtreeDefaultVisitorStack.size();
		final String visitorToCall = visitorToCall(target);
		if(visitorToCall != null)
			changeDefaultBuilder(visitorToCall);
		for(Entry child:target.children()) {
				process(child);
		}
		if(originalDefaultBuilderStackSize < subtreeDefaultVisitorStack.size())
			subtreeDefaultVisitorStack.removeLast();
	}

	private EntryVisitor builder(Entry target) {
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
		String explicitBuilderName = explicitBuilderName(target);
		if (explicitBuilderName != null)
			return explicitBuilderName;
		else if (subtreeDefaultVisitorStack.isEmpty())
			return null;
		else
			return subtreeDefaultVisitorStack.getLast();
	}

	public void addSubtreeDefaultBuilder(String builder, String subtreeBuilder) {
		subtreeDefaultVisitors.put(builder, subtreeBuilder);
	}

	public void setDefaultBuilder(EntryVisitor defaultBuilder) {
		this.defaultBuilder = defaultBuilder;
	}

	public EntryVisitor findSubtreeChildrenDefaultBuilder(Entry root, Entry entry) {
		final Entry explicitDefaultBuilderEntry = explicitDefaultBuilderEntry(root, entry);
		if (explicitDefaultBuilderEntry != null) {
			String builderName = explicitBuilderName(explicitDefaultBuilderEntry);
			if (entry == explicitDefaultBuilderEntry) {
				builderName = subtreeDefaultVisitors.get(builderName);
			}
			else
				for (Entry index = entry; index != explicitDefaultBuilderEntry; index = index.getParent()) {
					if (explicitBuilderName(index) == null) {
					final String nextExplicitDefaultBuilderName = subtreeDefaultVisitors.get(builderName);
					if (nextExplicitDefaultBuilderName != null)
						builderName = nextExplicitDefaultBuilderName;
					}
			}
			return visitors.get(builderName);
		}
		return defaultBuilder;
	}

	private Entry explicitDefaultBuilderEntry(Entry root, Entry entry) {
		String explicitBuilderName = explicitBuilderName(entry);
		final EntryVisitor explicitDefaultBuilder = explicitDefaultBuilder(explicitBuilderName);
		if (explicitDefaultBuilder != null)
			return entry;
		else if (root == entry)
			return null;
		else
			return explicitDefaultBuilderEntry(root, entry.getParent());
	}

	private EntryVisitor explicitDefaultBuilder(String explicitBuilderName) {
	    final String subtreeDefaultBuilder = subtreeDefaultVisitors.get(explicitBuilderName);
		final EntryVisitor explicitDefaultBuilder = visitors.get(subtreeDefaultBuilder);
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
}
