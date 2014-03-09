/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.view.swing.map;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.ui.ViewController;

/**
 * Manages the list of MapViews. As this task is very complex, I exported it
 * from Controller to this class to keep Controller simple. The information
 * exchange between controller and this class is managed by observer pattern
 * (the controller observes changes to the map mapViews here).
 */
public class MapViewController implements IMapViewManager , IMapViewChangeListener, IFreeplanePropertyListener {
	private String lastModeName;
	/** reference to the current mapmapView; null is allowed, too. */
	private MapView selectedMapView;
	MapViewChangeObserverCompound mapViewChangeListeners = new MapViewChangeObserverCompound();
	/**
	 * A vector of MapView instances. They are ordered according to their screen
	 * order.
	 */
	final private Vector<MapView> mapViewVector = new Vector<MapView>();
	private float zoom;
	private boolean setZoomComboBoxRun;
	private final Controller controller;

	/**
	 * Reference to the current mode as the mapView may be null.
	 */
	public MapViewController(Controller controller){
		this.controller =controller;
		controller.setMapViewManager(this);
		addMapViewChangeListener(this);
		zoomIn = new ZoomInAction(this);
		controller.addAction(zoomIn);
		zoomOut = new ZoomOutAction(this);
		controller.addAction(zoomOut);
		userDefinedZoom = TextUtils.getText("user_defined_zoom");
		zoomModel = new DefaultComboBoxModel(getZooms());
		zoomModel.addElement(userDefinedZoom);
		ResourceController resourceController = ResourceController.getResourceController();
		resourceController.addPropertyChangeListener(this);
		zoomModel.setSelectedItem("100%");
		zoomModel.addListDataListener(new  ListDataListener() {
			public void intervalRemoved(ListDataEvent e) {
			}

			public void intervalAdded(ListDataEvent e) {
			}

			public void contentsChanged(ListDataEvent e) {
				if (!setZoomComboBoxRun && e.getIndex0() == -1) {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							setZoomByItem(zoomModel.getSelectedItem());
						}
					});
				}
			}
		}) ;
		final String antialiasProperty = resourceController.getProperty(ViewController.RESOURCE_ANTIALIAS);
		changeAntialias(antialiasProperty);
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#addMapChangeListener(org.freeplane.core.frame.IMapChangeListener)
	 */
	public void addMapSelectionListener(final IMapSelectionListener pListener) {
		mapViewChangeListeners.addListener(pListener);
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#addMapViewChangeListener(org.freeplane.core.frame.IMapViewChangeListener)
	 */
	public void addMapViewChangeListener(final IMapViewChangeListener pListener) {
		mapViewChangeListeners.addListener(pListener);
	}

	private void addToOrChangeInMapViews(final String key, final MapView newOrChangedMapView) {
		String extension = "";
		int count = 1;
		final List<String> mapKeys = getMapKeys();
		while (mapKeys.contains(key + extension)) {
			extension = "<" + (++count) + ">";
		}
		newOrChangedMapView.setName((key + extension));
		newOrChangedMapView.setName((key + extension));
		if (!mapViewVector.contains(newOrChangedMapView)) {
			mapViewVector.add(newOrChangedMapView);
		}
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#changeToMapView(org.freeplane.view.swing.map.MapView)
	 */
	public boolean changeToMapView(final Component newMapViewComponent) {
		final MapView newMapView = (MapView) newMapViewComponent;
		final MapView oldMapView = selectedMapView;
		if (newMapView == oldMapView) {
			return true;
		}
		mapViewChangeListeners.beforeMapViewChange(oldMapView, newMapView);
		selectedMapView = newMapView;
		if (selectedMapView != null) {
			selectedMapView.revalidateSelecteds();
			final ModeController modeController = selectedMapView.getModeController();
			lastModeName = modeController.getModeName();
			final float mapViewZoom = selectedMapView.getZoom();
			if (zoom != mapViewZoom) {
				setZoom(mapViewZoom);
			}
			modeController.getController().selectMode(modeController);
		}
		mapViewChangeListeners.afterMapViewChange(oldMapView, newMapView);
		return true;
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#changeToMapView(java.lang.String)
	 */
	public boolean changeToMapView(final String mapViewDisplayName) {
		MapView mapViewCandidate = null;
		for (final MapView mapView : mapViewVector) {
			final String mapViewName = mapView.getName();
			if (mapViewDisplayName == mapViewName || mapViewDisplayName != null && mapViewDisplayName.equals(mapViewName)) {
				mapViewCandidate = mapView;
				break;
			}
		}
		if (mapViewCandidate == null) {
			throw new IllegalArgumentException("Map mapView " + mapViewDisplayName + " not found.");
		}
		return changeToMapView(mapViewCandidate);
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#changeToMode(java.lang.String)
	 */
	public boolean changeToMode(final String modeName) {
		if (modeName.equals(lastModeName)) {
			return true;
		}
		MapView mapViewCandidate = null;
		for (final MapView mapView : mapViewVector) {
			if (modeName.equals(mapView.getModeController().getModeName())) {
				mapViewCandidate = mapView;
				break;
			}
		}
		final MapView oldMapView = selectedMapView;
		final boolean changed = changeToMapView(mapViewCandidate);
		if (changed) {
			lastModeName = modeName;
			if (oldMapView == selectedMapView) {
				// if the same map remains selected post event for menu updates.
				mapViewChangeListeners.afterMapViewChange(oldMapView, selectedMapView);
			}
		}
		return changed;
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#checkIfFileIsAlreadyOpened(java.net.URL)
	 */
	public String checkIfFileIsAlreadyOpened(final URL urlToCheck) throws MalformedURLException {
		for (final MapView mapView : mapViewVector) {
			if (getModel(mapView) != null) {
				final URL mapViewUrl = getModel(mapView).getURL();
				if (sameFile(urlToCheck, mapViewUrl)) {
					return mapView.getName();
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#close(boolean)
	 */
	public boolean close(final boolean force) {
		final MapView mapView = getMapView();
		return close(mapView, force);
	}

	public boolean close(final Component mapViewComponent, final boolean force) {
	    if (mapViewComponent == null) {
			return false;
		}
		MapView mapView = (MapView) mapViewComponent;
		final MapController mapController = mapView.getModeController().getMapController();
		final boolean closingNotCancelled = mapController.close(mapView.getModel(), force);
		if (!closingNotCancelled) {
			return false;
		}
		int index = mapViewVector.indexOf(mapView);
		mapController.removeMapChangeListener(mapView);
		ResourceController.getResourceController().removePropertyChangeListener(mapView);
		mapViewVector.remove(mapView);
		if (mapViewVector.isEmpty()) {
			/* Keep the current running mode */
			changeToMapView((MapView) null);
		}
		else if(mapView == selectedMapView){
			if (index >= mapViewVector.size() || index < 0) {
				index = mapViewVector.size() - 1;
			}
			changeToMapView((mapViewVector.get(index)));
		}
		mapViewChangeListeners.afterMapViewClose(mapView);
		mapView.getRoot().remove();
		return true;
    }

	public String createHtmlMap() {
		final MapModel model = getModel();
		final ClickableImageCreator creator = new ClickableImageCreator(model.getRootNode(), getMapView()
		    .getModeController(), "FM$1FM");
		return creator.generateHtml();
	}

	public RenderedImage createImage(int dpi) {
		final MapView view = getMapView();
		if (view == null) {
			return null;
		}
		view.preparePrinting();
		final Rectangle innerBounds = view.getInnerBounds();
		final int BOUND = 1;
		innerBounds.x -= BOUND;
		innerBounds.y -= BOUND;
		innerBounds.width += 2 * BOUND;
		innerBounds.height += 2 * BOUND;

		double scaleFactor = (double) dpi / (double) UITools.getScreenResolution();

		int imageWidth = (int) Math.ceil(innerBounds.width * scaleFactor);
		int imageHeight = (int) Math.ceil(innerBounds.height * scaleFactor);

		final BufferedImage myImage = (BufferedImage) view.createImage(imageWidth, imageHeight);
		final Graphics2D g = (Graphics2D) myImage.getGraphics();
		g.scale(scaleFactor, scaleFactor);
		g.translate(-innerBounds.x, -innerBounds.y);
		view.print(g);
		view.endPrinting();
		return myImage;
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#getBackgroundColor(org.freeplane.core.model.NodeModel)
	 */
	public Color getBackgroundColor(final NodeModel node) {
		final MapView mapView = getMapView();
		if (mapView == null) {
			return null;
		}
		final NodeView nodeView = mapView.getNodeView(node);
		if (nodeView == null) {
			return null;
		}
		return nodeView.getTextBackground();
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#getComponent(org.freeplane.core.model.NodeModel)
	 */
	public Component getComponent(final NodeModel node) {
		if(selectedMapView == null)
			return null;
		final NodeView nodeView = selectedMapView.getNodeView(node);
		if(nodeView == null)
			return null;
		return nodeView.getMainView();
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#getFont(org.freeplane.core.model.NodeModel)
	 */
	public Font getFont(final NodeModel node) {
		final MapView mapView = getMapView();
		if (mapView == null) {
			return null;
		}
		final NodeView nodeView = mapView.getNodeView(node);
		if (nodeView == null) {
			return null;
		}
		return nodeView.getMainView().getFont();
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#getMapKeys()
	 */
	public List<String> getMapKeys() {
		final LinkedList<String> returnValue = new LinkedList<String>();
		for (final MapView mapView : mapViewVector) {
			returnValue.add(mapView.getName());
		}
		return Collections.unmodifiableList(returnValue);
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#getMaps()
	 */
	public Map<String, MapModel> getMaps() {
		final HashMap<String, MapModel> returnValue = new HashMap<String, MapModel>(mapViewVector.size());
		for (final MapView mapView : mapViewVector) {
			returnValue.put(mapView.getName(), getModel(mapView));
		}
		return Collections.unmodifiableMap(returnValue);
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#getMapSelection()
	 */
	public IMapSelection getMapSelection() {
		final MapView mapView = getMapView();
		return mapView == null ? null : mapView.getMapSelection();
	}

	public MapView getMapView() {
		return selectedMapView;
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#getMapViewComponent()
	 */
	public Component getMapViewComponent() {
		return getMapView();
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#getMapViewVector()
	 */
	public List<MapView> getMapViewVector() {
		return Collections.unmodifiableList(mapViewVector);
	}

	public ModeController getModeController(final Component mapView) {
		return ((MapView) mapView).getModeController();
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#getModel()
	 */
	public MapModel getModel() {
		final MapView mapView = getMapView();
		return mapView == null ? null : getModel(mapView);
	}

	public MapModel getModel(final Component mapView) {
		return ((MapView) mapView).getModel();
	}

	private MapModel getModel(final MapView mapView) {
		return mapView == null ? null : mapView.getModel();
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#getSelectedComponent()
	 */
	public Component getSelectedComponent() {
		final MapView mapView = getMapView();
		return mapView == null ? null : mapView.getSelected();
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#getTextColor(org.freeplane.core.model.NodeModel)
	 */
	public Color getTextColor(final NodeModel node) {
		final MapView mapView = getMapView();
		if (mapView == null) {
			return null;
		}
		final NodeView nodeView = mapView.getNodeView(node);
		if (nodeView == null) {
			return null;
		}
		return nodeView.getTextColor();
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#getViewNumber()
	 */
	public int getViewNumber() {
		return mapViewVector.size();
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#getZoom()
	 */
	public float getZoom() {
		return zoom;
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#newMapView(org.freeplane.core.model.MapModel, org.freeplane.core.modecontroller.ModeController)
	 */
	public void newMapView(final MapModel map, final ModeController modeController) {
		final MapView mapView = new MapView(map, modeController);
		addToOrChangeInMapViews(mapView.getName(), mapView);
		modeController.getMapController().addMapChangeListener(mapView);
		ResourceController.getResourceController().addPropertyChangeListener(mapView);
		mapViewChangeListeners.mapViewCreated(mapView);
		changeToMapView(mapView);
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#nextMapView()
	 */
	public void nextMapView() {
		int index;
		final int size = mapViewVector.size();
		if (getMapView() != null) {
			index = mapViewVector.indexOf(getMapView());
		}
		else {
			index = size - 1;
		}
		if (index + 1 < size && index >= 0) {
			changeToMapView((mapViewVector.get(index + 1)));
		}
		else if (size > 0) {
			changeToMapView((mapViewVector.get(0)));
		}
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#previousMapView()
	 */
	public void previousMapView() {
		int index;
		final int size = mapViewVector.size();
		if (getMapView() != null) {
			index = mapViewVector.indexOf(getMapView());
		}
		else {
			index = 0;
		}
		if (index > 0) {
			changeToMapView((mapViewVector.get(index - 1)));
		}
		else {
			if (size > 0) {
				changeToMapView((mapViewVector.get(size - 1)));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#removeIMapViewChangeListener(org.freeplane.core.frame.IMapChangeListener)
	 */
	public void removeMapSelectionListener(final IMapSelectionListener pListener) {
		mapViewChangeListeners.removeListener(pListener);
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#removeMapViewChangeListener(org.freeplane.core.frame.IMapViewChangeListener)
	 */
	public void removeMapViewChangeListener(final IMapViewChangeListener pListener) {
		mapViewChangeListeners.removeListener(pListener);
	}

	private boolean sameFile(final URL urlToCheck, final URL mapViewUrl) {
		if (mapViewUrl == null) {
			return false;
		}
		if (urlToCheck.getProtocol().equals("file") && mapViewUrl.getProtocol().equals("file")) {
			return (new File(urlToCheck.getFile())).equals(new File(mapViewUrl.getFile()));
		}
		return urlToCheck.sameFile(mapViewUrl);
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#scrollNodeToVisible(org.freeplane.core.model.NodeModel)
	 */
	public void scrollNodeToVisible(final NodeModel node) {
		final NodeView nodeView = selectedMapView.getNodeView(node);
		if (nodeView != null) {
			selectedMapView.scrollNodeToVisible(nodeView);
		}
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#setZoom(float)
	 */
	public void setZoom(final float zoom) {
		this.zoom = zoom;
		final MapView mapView = getMapView();
		if (mapView == null) {
			return;
		}
		final MapModel map = mapView.getModel();
		final MapStyle mapStyle = mapView.getModeController().getExtension(MapStyle.class);
		if(mapView.getZoom() == zoom){
			return;
		}
		mapStyle.setZoom(map, zoom);
		mapView.setZoom(zoom);
		setZoomComboBox(zoom);
		final Object[] messageArguments = { String.valueOf(zoom * 100f) };
		final String stringResult = TextUtils.format("user_defined_zoom_status_bar", messageArguments);
		controller.getViewController().out(stringResult);
	}


	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#tryToChangeToMapView(java.lang.String)
	 */
	public boolean tryToChangeToMapView(final String mapView) {
		if (mapView != null && getMapKeys().contains(mapView)) {
			changeToMapView(mapView);
			return true;
		}
		else {
			return false;
		}
	}

	public boolean tryToChangeToMapView(URL url) throws MalformedURLException {
		final String mapExtensionKey = checkIfFileIsAlreadyOpened(url);
		if (mapExtensionKey != null) {
			tryToChangeToMapView(mapExtensionKey);
			return true;
		}
		return false;
    }

	/* (non-Javadoc)
	 * @see org.freeplane.core.frame.IMapViewController#updateMapViewName()
	 */
	public void updateMapViewName() {
		final MapView r = getMapView();
		final String name = r.getModel().getTitle();
		addToOrChangeInMapViews(name, getMapView());
		changeToMapView(getMapView());
	}

	public boolean isLeftTreeSupported(final Component mapViewComponent) {
		return ((MapView) mapViewComponent).getLayoutType() != MapViewLayout.OUTLINE;
	}

	public Map<String, MapModel> getMaps(final String modename) {
		final HashMap<String, MapModel> returnValue = new HashMap<String, MapModel>(mapViewVector.size());
		for (final MapView mapView : mapViewVector) {
			if (mapView.getModeController().getModeName().equals(modename)) {
				returnValue.put(mapView.getName(), getModel(mapView));
			}
		}
		return Collections.unmodifiableMap(returnValue);
	}

	public List<Component> getViews(final MapModel map) {
		final LinkedList<Component> list = new LinkedList<Component>();
		for (final MapView view : mapViewVector) {
			if (view.getModel().equals(map)) {
				list.add(view);
			}
		}
		return list;
	}
	public void afterViewChange(final Component oldMap, final Component pNewMap) {
		Controller controller = Controller.getCurrentController();
		final ModeController oldModeController = controller.getModeController();
		ModeController newModeController = oldModeController;
		if (pNewMap != null) {
			final IMapSelection mapSelection = getMapSelection();
			final NodeModel selected = mapSelection.getSelected();
			mapSelection.scrollNodeToVisible(selected);
			setZoomComboBox(getZoom());
			obtainFocusForSelected();
			newModeController = getModeController(pNewMap);
			if (newModeController != oldModeController) {
				controller.selectMode(newModeController);
			}
		}
		setTitle();
		controller.getViewController().viewNumberChanged(getViewNumber());
		newModeController.getUserInputListenerFactory().updateMapList();
		if (pNewMap != null) {
			newModeController.setVisible(true);
		}
	}

	public void afterViewClose(final Component oldView) {
		ModeController newModeController = getModeController(oldView);
		newModeController.getUserInputListenerFactory().updateMapList();
	}

	public void afterViewCreated(final Component mapView) {
	}

	public void beforeViewChange(final Component oldMap, final Component newMap) {
		Controller controller = Controller.getCurrentController();
		final ModeController modeController = controller.getModeController();
		if (oldMap != null) {
			modeController.setVisible(false);
		}
	}

	private void setZoomByItem(final Object item) {
		final float zoomValue;
		if (((String) item).equals(userDefinedZoom)) {
			final float zoom =getZoom();
			final int zoomInt = Math.round(100 * zoom);
			final SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(zoomInt, 1, 3200, 1);
			JSpinner spinner = new JSpinner(spinnerNumberModel);
			final int option = JOptionPane.showConfirmDialog(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner(), spinner, TextUtils.getText("enter_zoom"), JOptionPane.OK_CANCEL_OPTION);
			if(option == JOptionPane.OK_OPTION)
				zoomValue = spinnerNumberModel.getNumber().floatValue() / 100;
			else
				zoomValue = zoom;
		}
		else
			zoomValue = getZoomValue(item);
		setZoom(zoomValue);
	}

	final private String userDefinedZoom;
	final private ZoomInAction zoomIn;
	private final DefaultComboBoxModel zoomModel;
	final private ZoomOutAction zoomOut;

	private float getCurrentZoomIndex() {
		final int selectedIndex = zoomModel.getIndexOf(zoomModel.getSelectedItem());
		final int itemCount = zoomModel.getSize();
		if (selectedIndex != - 1) {
			return selectedIndex;
		}
		final float userZoom = getZoom();
		for (int i = 0; i < itemCount - 1; i++) {
			if (userZoom < getZoomValue(zoomModel.getElementAt(i))) {
				return i - 0.5f;
			}
		}
		return itemCount  - 1.5f;
	}

	public String getItemForZoom(final float f) {
		return (int) (f * 100F) + "%";
	}

	private void setZoomComboBox(final float f) {
		setZoomComboBoxRun = true;
		try {
			final String toBeFound = getItemForZoom(f);
			zoomModel.setSelectedItem(toBeFound);
		}
		finally {
			setZoomComboBoxRun = false;
		}
	}

	public void zoomIn() {
		final float currentZoomIndex = getCurrentZoomIndex();
		if (currentZoomIndex < zoomModel.getSize() - 2) {
			setZoomByItem(zoomModel.getElementAt((int) (currentZoomIndex + 1f)));
		}
	}

	public void zoomOut() {
		final float currentZoomIndex = getCurrentZoomIndex();
		if (currentZoomIndex > 0) {
			setZoomByItem(zoomModel.getElementAt((int) (currentZoomIndex - 0.5f)));
		}
	}

	public void updateMenus(final MenuBuilder menuBuilder) {
		if (menuBuilder.contains("main_toolbar_zoom")) {
			final JComboBox zoomBox = createZoomBox();
			menuBuilder.addElement("main_toolbar_zoom", zoomBox, MenuBuilder.AS_CHILD);
			// FELIXHACK
			//zoomBox.setRenderer(new ComboBoxRendererWithTooltip(zoomBox));
		}
	}

	public JComboBox createZoomBox() {
		if(zoomBox == null) {
			zoomBox = new JComboBox(zoomModel);
		}
		return zoomBox;
	}

	public String[] getZooms() {
		return zooms;
	}

	private float getZoomValue(final Object item) {
		final String dirty = (String) item;
		final String cleaned = dirty.substring(0, dirty.length() - 1);
		final float zoomValue = Integer.parseInt(cleaned, 10) / 100F;
		return zoomValue;
	}

	private static final String[] zooms = { "25%", "50%", "75%", "100%", "150%", "200%", "300%", "400%" };
	public void obtainFocusForSelected() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (getMapView() != null) {
					final Component selectedComponent = getSelectedComponent();
					if(selectedComponent != null){
						selectedComponent.requestFocus();
					}
				}
			}
		});
	}

	public boolean closeAllMaps() {
		while (getMapViewVector().size() > 0) {
			if (getMapView() != null) {
				final boolean closingNotCancelled = close(false);
				if (!closingNotCancelled) {
					return false;
				}
			}
			else {
				nextMapView();
			}
		}
		ResourceController.getResourceController().setProperty("antialiasEdges", (antialiasEdges ? "true" : "false"));
		ResourceController.getResourceController().setProperty("antialiasAll", (antialiasAll ? "true" : "false"));
		return true;
	}
	private boolean antialiasAll = false;
	private boolean antialiasEdges = false;
	private JComboBox zoomBox;
	private boolean getAntialiasAll() {
		return antialiasAll;
	}

	private boolean getAntialiasEdges() {
		return antialiasEdges;
	}

	public void setAntialiasAll(final boolean antialiasAll) {
		this.antialiasAll = antialiasAll;
	}

	public void setAntialiasEdges(final boolean antialiasEdges) {
		this.antialiasEdges = antialiasEdges;
	}

	public Object setEdgesRenderingHint(final Graphics2D g) {
		final Object renderingHint = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		if (getAntialiasEdges()) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		else {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
		return renderingHint;
	}


	public void setTextRenderingHint(final Graphics2D g) {
		if (getAntialiasAll()) {
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
		else {
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		}
	}
	/**
	 */
	private void changeAntialias(final String command) {
		if (command == null) {
			return;
		}
		if (command.equals("antialias_none")) {
			setAntialiasEdges(false);
			setAntialiasAll(false);
		}
		if (command.equals("antialias_edges")) {
			setAntialiasEdges(true);
			setAntialiasAll(false);
		}
		if (command.equals("antialias_all")) {
			setAntialiasEdges(true);
			setAntialiasAll(true);
		}
		final Component mapView = getMapViewComponent();
		if (mapView != null) {
			mapView.repaint();
		}
	}


	public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
		if (propertyName.equals(ViewController.RESOURCE_ANTIALIAS)) {
			changeAntialias(newValue);
		}
	}
	public void setTitle() {
		final ModeController modeController = Controller.getCurrentModeController();
		if (modeController == null) {
			controller.getViewController().setTitle("");
			return;
		}
		final Object[] messageArguments = { TextUtils.getText(("mode_" + modeController.getModeName())) };
		final MessageFormat formatter = new MessageFormat(TextUtils.getText("mode_title"));
		String frameTitle = formatter.format(messageArguments);
		String viewName = "";
		final MapModel model = getModel();
		if (model != null) {
			viewName = getMapViewComponent().getName();
			frameTitle = viewName + (model.isSaved() ? "" : "*") + " - " + frameTitle
			        + (model.isReadOnly() ? " (" + TextUtils.getText("read_only") + ")" : "");
			final File file = model.getFile();
			if (file != null) {
				frameTitle += " " + file.getAbsolutePath();
			}
		}
		controller.getViewController().setTitle(frameTitle);
		modeController.getUserInputListenerFactory().updateMapList();
	}
}
