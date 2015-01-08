package org.freeplane.core.ui.menubuilders;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RecursiveMenuStructureBuilderForPaths {

	private Map<String, LinkedList<Builder> > builders = new HashMap<String, LinkedList<Builder>>();

	public void build(Entry entry) {
		callBuilders(entry);
		for(Entry child : entry.children())
			build(child);
	}

	private void callBuilders(Entry entry) {
		final List<Builder> buildersForPath = availableBuildersForPath(entry);
		for(Builder builder : buildersForPath)
			builder.build(entry);
	}

	private List<Builder> availableBuildersForPath(Entry childEntry) {
		final List<Builder> buildersForPath = builders.get(childEntry.getPath());
		return buildersForPath != null ? buildersForPath : Collections.emptyList();
	}

	public void addBuilder(String path, Builder builder) {
		LinkedList<Builder> buildersForPath = buildersForPath(path);
		buildersForPath.addLast(builder);
	}

	private LinkedList<Builder> buildersForPath(String path) {
		LinkedList<Builder> buildersForPath = builders.get(path);
		if (buildersForPath == null){
			buildersForPath = new LinkedList<Builder>();
			builders.put(path, buildersForPath);
		}
		return buildersForPath;
	}

}
