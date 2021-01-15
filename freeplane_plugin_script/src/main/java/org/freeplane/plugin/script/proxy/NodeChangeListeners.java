package org.freeplane.plugin.script.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.freeplane.api.NodeChangeListener;
import org.freeplane.api.NodeChanged.ChangedElement;
import org.freeplane.core.extension.IExtension;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.text.DetailModel;
import org.freeplane.plugin.script.FormulaCache;
import org.freeplane.plugin.script.ScriptContext;

class NodeChangeListeners implements IExtension{
	private static class NodeChangeListenersListener implements IExtension, INodeChangeListener { 
		static void installInto(ModeController controller) {
			NodeChangeListenersListener listeners = controller.getExtension(NodeChangeListenersListener.class);
			if(listeners == null) {
				listeners = new NodeChangeListenersListener();
				controller.addExtension(NodeChangeListenersListener.class, listeners);
				controller.getMapController().addNodeChangeListener(listeners);
			}
		}
		
		
		@Override
		public void nodeChanged(NodeChangeEvent event) {
			NodeChangeListeners listeners = event.getNode().getMap().getExtension(NodeChangeListeners.class);
			if(listeners != null)
				listeners.fire(event);
		}
	}
	
	private static Map<Object, ChangedElement> elements = new HashMap<Object, ChangedElement>(){
		private static final long serialVersionUID = 1L;
		{
			put(NodeModel.NODE_TEXT, ChangedElement.TEXT);
			put(DetailModel.class, ChangedElement.DETAILS);
			put(NodeModel.NOTE_TEXT, ChangedElement.NOTE);
			put(NodeAttributeTableModel.class, ChangedElement.ATTRIBUTE);
			put(NodeModel.NODE_ICON, ChangedElement.ICON);
			put(FormulaCache.class, ChangedElement.FORMULA_RESULT);
		}
	};
	
	
	static NodeChangeListeners of(ModeController controller, MapModel map) {
		NodeChangeListenersListener.installInto(controller);
		return NodeChangeListeners.of(map);
	}
	
	private final ArrayList<NodeChangeListenerForScript> listeners = new ArrayList<>();
	private final MapModel mindmap;
	
	public NodeChangeListeners(MapModel mindmap) {
		this.mindmap = mindmap;
	}
	private static NodeChangeListeners of(MapModel map) {
		NodeChangeListeners listeners = map.getExtension(NodeChangeListeners.class);
		if(listeners == null) {
			listeners = new NodeChangeListeners(map);
			map.addExtension(NodeChangeListeners.class, listeners);
		}
		return listeners;
	}

	public void add(ScriptContext context, NodeChangeListener listener) {
		if(mindmap.getExtension(NodeChangeListeners.class) != this)
			mindmap.addExtension(NodeChangeListeners.class, this);
		listeners.add(new NodeChangeListenerForScript(listener, context));
	}

	public void remove(NodeChangeListener listener) {
		listeners.removeIf(NodeChangeListenerForScript.contains(listener));
		if(listeners.isEmpty())
			mindmap.removeExtension(this);
	}
	
	public void fire(NodeChangeEvent event) {
		if (listeners.isEmpty())
			return;
		ChangedElement element = elements.getOrDefault(event.getProperty(), ChangedElement.UNKNOWN);
		listeners.forEach(l -> l.fire(event.getNode(), element));
	}
}
