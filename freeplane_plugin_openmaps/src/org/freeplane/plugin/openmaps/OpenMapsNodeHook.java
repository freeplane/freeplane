package org.freeplane.plugin.openmaps;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.openmaps.mapelements.OpenMapsDialog;
import org.openstreetmap.gui.jmapviewer.Coordinate;

@NodeHookDescriptor(hookName = "plugins/openmaps/OpenMapsNodeHook.propterties", onceForMap = false)
public class OpenMapsNodeHook extends PersistentNodeHook implements LocationChoosenListener {
	
	public OpenMapsNodeHook() {
		super();
	}
	
	public void chooseLocation() {
		final OpenMapsDialog map = new OpenMapsDialog();
		map.getController().addLocationChoosenListener(this);
	}
	
	@Override
    protected HookAction createHookAction() {
	    return null;
    }
	
	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		final OpenMapsExtension extension = new OpenMapsExtension();
		loadLocationFromXML(element, extension);
		return (IExtension) extension;
	}
	
	@Override
	protected Class<OpenMapsExtension> getExtensionClass() {
		return OpenMapsExtension.class;
	}
	
	@Override
	protected void saveExtension(final IExtension extension, final XMLElement element) {
		final OpenMapsExtension openMapsExtension = (OpenMapsExtension) extension;
		element.setAttribute("LAT", Double.toString(openMapsExtension.getLocation().getLat()));
		element.setAttribute("LON", Double.toString(openMapsExtension.getLocation().getLon()));
		super.saveExtension(extension, element);
	}

	private void loadLocationFromXML(final XMLElement element, final OpenMapsExtension extension) {
		if (element != null) {
			final double location_x = Double.parseDouble(element.getAttribute("LAT", null));
			final double location_y = Double.parseDouble(element.getAttribute("LON", null));
			extension.updateLocation(location_x, location_y);
		}
	}
	
	private void addChoosenLocationToSelectedNode(Coordinate locationChoosen) {
		final NodeModel node = getCurrentlySelectedNode();
		OpenMapsExtension openMapsExtension = (OpenMapsExtension) node.getExtension(OpenMapsExtension.class);
		
		if (openMapsExtension == null) {
			openMapsExtension = new OpenMapsExtension();
			openMapsExtension.updateLocation(locationChoosen);
			add (node, openMapsExtension);
			node.addIcon(new MindIcon("internet"));
			refreshNode(node);
		}
		
		setLocationChoiceUndoable(openMapsExtension, locationChoosen);
	}
	
	private void setLocationChoiceUndoable(final OpenMapsExtension extension, final Coordinate locationChoosen) {
		final Coordinate currentLocation = extension.getLocation();
	
		if (!currentLocation.equals(locationChoosen)) {
			
		final IActor actor = new IActor() {
			private final Coordinate oldLocation = currentLocation;

			public void act() {
				extension.updateLocation(locationChoosen);
				final MapModel map = Controller.getCurrentModeController().getController().getMap();
				Controller.getCurrentModeController().getMapController().setSaved(map, false);
			}

			public String getDescription() {
				return "setOpenMapsLocationChoiceUndoable";
			}

			public void undo() {
				extension.updateLocation(oldLocation);
				//FIXME Remove the icon?
			}
		};
		
		Controller.getCurrentModeController().execute(actor,
		    Controller.getCurrentModeController().getController().getMap());
		}
	}

	private void refreshNode(NodeModel node) {
		Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.UNKNOWN_PROPERTY, null, null);	
	}

	private NodeModel getCurrentlySelectedNode() {
		return Controller.getCurrentModeController().getMapController().getSelectedNode();
	}

	@Override
	public void locationChoosenAction(Coordinate locationChoosen) {
		addChoosenLocationToSelectedNode(locationChoosen); 	
	}

}
