package org.freeplane.plugin.openmaps;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.n3.nanoxml.XMLElement;

@NodeHookDescriptor(hookName = "plugins/openmaps/OpenMapsNodeHook.propterties", onceForMap = false)
public class OpenMapsNodeHook extends PersistentNodeHook {
	
	public OpenMapsNodeHook() {
		super();
	}
	
	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		final OpenMapsExtension extension = new OpenMapsExtension();
		loadLocationFromXML(element, extension);
		refreshNode(node);
		return (IExtension) extension;
	}

	private void loadLocationFromXML(final XMLElement element, final OpenMapsExtension extension) {
		if (element != null) {
			final float location_x = Float.parseFloat(element.getAttribute("LOCATION_X", null));
			final float location_y = Float.parseFloat(element.getAttribute("LOCATION_Y", null));
			extension.updateLocation(location_x, location_y);
		}
	}
	
	private void addChoosenLocationToSelectedNode(OpenMapsLocation locationChoosen) {
		final NodeModel node = getCurrentlySelectedNode();
		//Check for extension 
		node.addIcon(new MindIcon("internet"));
		refreshNode(node);
	}

	private void refreshNode(NodeModel node) {
		Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.UNKNOWN_PROPERTY, null, null);	
	}

	private NodeModel getCurrentlySelectedNode() {
		return Controller.getCurrentModeController().getMapController().getSelectedNode();
	}

}
