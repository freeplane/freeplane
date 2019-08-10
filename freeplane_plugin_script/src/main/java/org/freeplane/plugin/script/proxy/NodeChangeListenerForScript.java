package org.freeplane.plugin.script.proxy;

import java.util.function.Predicate;

import org.freeplane.api.NodeChangeListener;
import org.freeplane.api.NodeChanged;
import org.freeplane.api.NodeChanged.ChangedElement;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ScriptContext;

class NodeChangeListenerForScript {
	static Predicate<? super NodeChangeListenerForScript> contains(NodeChangeListener listener) {
		return e -> e.scriptListener.equals(listener);
	}
	
	private final NodeChangeListener scriptListener;
	private final ScriptContext context;
	public NodeChangeListenerForScript(NodeChangeListener scriptListener, ScriptContext context) {
		super();
		this.scriptListener = scriptListener;
		this.context = context;
	}
	void fire(NodeModel node, ChangedElement element) {
		scriptListener.nodeChanged(new NodeChanged(new NodeProxy(node, context), element));
	}
	
}
