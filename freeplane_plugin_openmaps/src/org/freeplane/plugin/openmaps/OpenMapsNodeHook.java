package org.freeplane.plugin.openmaps;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.openmaps.mapElements.MapViewer;

@NodeHookDescriptor(hookName = "plugins/openmaps/OpenMapsNodeHook.propterties", onceForMap = false)
public class OpenMapsNodeHook extends PersistentNodeHook {
	
	public OpenMapsNodeHook() {
		super();
	}
	
	public void chooseLocation() {
		final MapViewer map = new MapViewer();
		OpenMapsLocation locationChoosen = null;
		//While loop needs to be replaced with a listener of some kind - This breaks things
		while (locationChoosen == null) {
			locationChoosen = map.getController().getSelectedLocation();
		}
		addChoosenLocationToSelectedNode(locationChoosen); 
	}
	
	@Override
    protected HookAction createHookAction() {
	    return null;
    }
	
	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		final OpenMapsExtension extension = new OpenMapsExtension();
		loadLocationFromXML(element, extension);
		refreshNode(node);
		return (IExtension) extension;
	}
	
	@Override
	protected Class<OpenMapsExtension> getExtensionClass() {
		return OpenMapsExtension.class;
	}
	
	@Override
	protected void saveExtension(final IExtension extension, final XMLElement element) {
		final OpenMapsExtension openMapsExtension = (OpenMapsExtension) extension;
		element.setAttribute("LOCATION_X", Float.toString(openMapsExtension.getLocation().getXLocation()));
		element.setAttribute("LOCATION_Y", Float.toString(openMapsExtension.getLocation().getYLocation()));
		super.saveExtension(extension, element);
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
		OpenMapsExtension openMapsExtension = (OpenMapsExtension) node.getExtension(OpenMapsExtension.class);
		
		if (openMapsExtension == null) {
			openMapsExtension  = new OpenMapsExtension();
			add (node, openMapsExtension);
			node.addIcon(new MindIcon("internet"));
			refreshNode(node);
		}
		
	}
	

	private void refreshNode(NodeModel node) {
		Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.UNKNOWN_PROPERTY, null, null);	
	}

	private NodeModel getCurrentlySelectedNode() {
		return Controller.getCurrentModeController().getMapController().getSelectedNode();
	}

}
