package org.freeplane.plugin.openmaps;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.icon.IStateIconProvider;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.openmaps.mapelements.OpenMapsDialog;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.freeplane.plugin.openmaps.LocationChoosenListener;

/**
 * @author Blair Archibald
 */
@NodeHookDescriptor(hookName = "plugins/openmaps/OpenMapsNodeHook.propterties", onceForMap = false)
public class OpenMapsNodeHook extends PersistentNodeHook implements LocationChoosenListener {
	
	static final String ICON_NAME = "internet";

	private OpenMapsDialog map;

	public OpenMapsNodeHook() {
		super();
		registerStateIconProvider();
	}
	
	public void chooseLocation() {
		map = new OpenMapsDialog();
		map.getController().addLocationChoosenListener(this);
	}
	
	public void removeLocationFromCurrentlySelectedNode() {
		final NodeModel node = getCurrentlySelectedNode();
		OpenMapsExtension openMapsExtension = (OpenMapsExtension) node.getExtension(OpenMapsExtension.class);
		
		if (openMapsExtension != null) {
			super.undoableToggleHook(node, openMapsExtension);
			refreshNode(node);
		}
		
		final MapModel map = Controller.getCurrentModeController().getController().getMap();
		Controller.getCurrentModeController().getMapController().setSaved(map, false);
	}
	

	//Called when a location is chosen in the OpenMapsDialog
	public void locationChoosenAction(ICoordinate locationChoosen, int zoom) {
		addChoosenLocationToSelectedNode(locationChoosen, zoom); 
//		map.getController().removeLocationChoosenListener(this);
	}
	
	public void viewCurrentlySelectedLocation(final NodeModel targetNode) {
		OpenMapsExtension openMapsExtension;
		if (targetNode == null)
			openMapsExtension = (OpenMapsExtension) getCurrentlySelectedNode().getExtension(OpenMapsExtension.class);
		else
			openMapsExtension = (OpenMapsExtension) targetNode.getExtension(OpenMapsExtension.class);
		
		if (openMapsExtension != null) {
			map = new OpenMapsDialog();
			map.showZoomToLocation(openMapsExtension.getLocation(), openMapsExtension.getZoom());
			map.getController().addLocationChoosenListener(this);
		}
		
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
		element.setAttribute("ZOOM", Integer.toString(openMapsExtension.getZoom()));
		super.saveExtension(extension, element);
	}

	private void loadLocationFromXML(final XMLElement element, final OpenMapsExtension extension) {
		if (element != null) {
			final double location_x = Double.parseDouble(element.getAttribute("LAT", null));
			final double location_y = Double.parseDouble(element.getAttribute("LON", null));
			final int zoom = Integer.parseInt(element.getAttribute("ZOOM",null));
			extension.updateLocation(location_x, location_y);
			extension.updateZoom(zoom);
		}
	}
	
	private void addChoosenLocationToSelectedNode(ICoordinate locationChoosen, int zoom) {
		final NodeModel node = getCurrentlySelectedNode();
		OpenMapsExtension openMapsExtension = (OpenMapsExtension) node.getExtension(OpenMapsExtension.class);
		
		if (openMapsExtension == null) {
			openMapsExtension = new OpenMapsExtension();
                        undoableActivateHook(node, openMapsExtension);
		}
		setLocationChoiceUndoable(openMapsExtension, locationChoosen, zoom);
		refreshNode(node);
	}
	
	private void setLocationChoiceUndoable(final OpenMapsExtension extension, final ICoordinate locationChoosen, final int zoomChoosen) {
		final Coordinate currentLocation = extension.getLocation();
		final int currentZoom = extension.getZoom();

		if (!currentLocation.equals(locationChoosen)) {
			final IActor actor = createUndoActor(extension, locationChoosen,
					currentLocation, zoomChoosen, currentZoom);
			
			Controller.getCurrentModeController().execute(actor,
					Controller.getCurrentModeController().getController()
							.getMap());
		}
	}

	private IActor createUndoActor(final OpenMapsExtension extension, final ICoordinate newlyChoosenLocation, 
			final ICoordinate currentlyStoredLocation, final int newlyChoosenZoom , final int currentlyStoredZoom) {
		
		return new IActor() {
			private final ICoordinate oldLocation = currentlyStoredLocation;
			private final int oldZoom = currentlyStoredZoom;

			public void act() {
				extension.updateLocation(newlyChoosenLocation);
				extension.updateZoom(newlyChoosenZoom);
				final MapModel map = Controller.getCurrentModeController()
						.getController().getMap();
				Controller.getCurrentModeController().getMapController()
						.setSaved(map, false);
			}

			public String getDescription() {
				return "setOpenMapsLocationChoiceUndoable";
			}

			public void undo() {
                            if (oldLocation.getLat() == 500 && oldLocation.getLon() == 500)
                            {
                                removeLocationFromCurrentlySelectedNode();
                            }
                            else
                            {
                                extension.updateLocation(oldLocation);
                                extension.updateZoom(oldZoom);
                            }
                            refreshNode(getCurrentlySelectedNode());
			}

		};
	}
	
	private void refreshNode(NodeModel node) {
		Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.UNKNOWN_PROPERTY, null, null);	
	}

	private NodeModel getCurrentlySelectedNode() {
		return Controller.getCurrentModeController().getMapController().getSelectedNode();
	}
	
	private void registerStateIconProvider() {
		Controller.getCurrentModeController().getExtension(IconController.class).addStateIconProvider
		(new IStateIconProvider () {
			public UIIcon getStateIcon(NodeModel node) {
				if (node.getExtension(OpenMapsExtension.class) != null)
					return IconStoreFactory.ICON_STORE.getUIIcon(ICON_NAME);
				else 
					return null;
			}	
		});
	}
}
