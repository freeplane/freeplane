package org.freeplane.core.ui.menubuilders;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RecursiveMenuStructureBuilderForPaths {

	private Map<String, LinkedList<EntryVisitor> > builders = new HashMap<String, LinkedList<EntryVisitor>>();

	public void build(Entry entry) {
		callBuilders(entry);
		for(Entry child : entry.children())
			build(child);
	}

	private void callBuilders(Entry entry) {
		final List<EntryVisitor> buildersForPath = availableBuildersForPath(entry);
		for(EntryVisitor builder : buildersForPath)
			builder.visit(entry);
	}

	private List<EntryVisitor> availableBuildersForPath(Entry childEntry) {
		final List<EntryVisitor> buildersForPath = builders.get(childEntry.getPath());
		return buildersForPath != null ? buildersForPath : Collections.<EntryVisitor>emptyList();
	}

	public void addBuilder(String path, EntryVisitor builder) {
		LinkedList<EntryVisitor> buildersForPath = buildersForPath(path);
		buildersForPath.addLast(builder);
	}

	private LinkedList<EntryVisitor> buildersForPath(String path) {
		LinkedList<EntryVisitor> buildersForPath = builders.get(path);
		if (buildersForPath == null){
			buildersForPath = new LinkedList<EntryVisitor>();
			builders.put(path, buildersForPath);
		}
		return buildersForPath;
	}

}
