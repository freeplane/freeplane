package org.freeplane.features.explorer;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

public class GlobalNodes implements IExtension, Iterable<NodeModel>{
	private static final GlobalNodes EMPTY = new GlobalNodes(Collections.<NodeModel, Void>emptyMap());
	private final Map<NodeModel, Void> nodes;

	GlobalNodes(Map<NodeModel, Void> nodes) {
		super();
		this.nodes = nodes;
	}



	boolean contains(NodeModel key) {
		return nodes.containsKey(key);
	}

	public static GlobalNodes writeableOf(MapModel map) {
		GlobalNodes globalNodes = map.getExtension(GlobalNodes.class);
		if(globalNodes == null) {
			globalNodes = new GlobalNodes(new WeakHashMap<NodeModel, Void>());
			map.addExtension(globalNodes);
		}
		return globalNodes;
	}

	static GlobalNodes readableOf(MapModel map) {
		GlobalNodes globalNodes = map.getExtension(GlobalNodes.class);
		return globalNodes == null ? EMPTY : globalNodes;
	}

	static void add(NodeModel node) {
		writeableOf(node.getMap()).makeGlobal(node);
	}

	public void makeGlobal(NodeModel node, boolean isGlobal) {
		if(isGlobal)
			makeGlobal(node);
		else
			makeNotGlobal(node);
	}

	void makeGlobal(NodeModel node) {
		nodes.put(node, null);
	}

	void makeNotGlobal(NodeModel node) {
		nodes.remove(node);
	}

	public static boolean isGlobal(NodeModel node) {
		return readableOf(node.getMap()).contains(node);
	}



	@Override
	public Iterator<NodeModel> iterator() {
		return nodes.keySet().iterator();
	}
}
