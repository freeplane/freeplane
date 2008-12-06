/*
 * Freeplane - A Program for creating and viewing Mindmaps Copyright =Copyright
 * © 2000-2008 Jörg Müller, Daniel Polansky, Petr Novak, Christian Foltin,
 * Dimitry Polivaev and others See COPYING for Details This program is free
 * software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version. This program
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place
 * - Suite 330, Boston, MA 02111-1307, USA.
 */
package org.freeplane.controller.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.freeplane.controller.Controller;
import org.freeplane.controller.Freeplane;
import org.freeplane.map.tree.MapModel;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.UserInputListenerFactory;
import org.freeplane.ui.FreemindMenuBar;
import org.freeplane.ui.MenuBuilder;
import org.freeplane.ui.UIBuilder;

/**
 * @author Dimitry Polivaev
 */
abstract public class ViewController implements IMapViewChangeListener {
	public static final String RESOURCE_ANTIALIAS = "antialias";
	private static final String[] zooms = { "25%", "50%", "75%", "100%",
	        "150%", "200%", "300%", "400%" };
	private boolean antialiasAll = false;
	private boolean antialiasEdges = false;
	final private LastOpenedList lastOpened;
	final private ActionListener lastOpenedActionListener = new LastOpenedActionListener(
	    this);
	final private JPanel leftToolbarPanel;
	private boolean leftToolbarVisible;
	final private MapViewManager mapViewManager;
	private boolean menubarVisible;
	final private HashSet mMapTitleChangeListenerSet = new HashSet();
	final private Action navigationNextMap;
	final private Action navigationPreviousMap;
	final private OptionAntialiasAction optionAntialiasAction;
	final private JScrollPane scrollPane = new MapView.ScrollPane();
	final private JLabel status;
	final private JPanel toolbarPanel;
	private boolean toolbarVisible;
	final private String userDefinedZoom;
	final private JComboBox zoom;
	final private Action zoomIn;
	final private Action zoomOut;

	public ViewController() {
		super();
		final Controller controller = Freeplane.getController();
		lastOpened = new LastOpenedList(controller.getResourceController()
		    .getProperty("lastOpened"));
		mapViewManager = new MapViewManager();
		mapViewManager.addMapViewChangeListener(this);
		controller.addAction("close", new CloseAction());
		navigationPreviousMap = new NavigationPreviousMapAction();
		controller.addAction("navigationPreviousMap", navigationPreviousMap);
		navigationNextMap = new NavigationNextMapAction();
		controller.addAction("navigationNextMap", navigationNextMap);
		controller.addAction("moveToRoot", new MoveToRootAction());
		zoomIn = new ZoomInAction(this);
		controller.addAction("zoomIn", zoomIn);
		zoomOut = new ZoomOutAction(this);
		controller.addAction("zoomOut", zoomOut);
		optionAntialiasAction = new OptionAntialiasAction();
		controller.addAction("optionAntialiasAction", optionAntialiasAction);
		userDefinedZoom = controller.getResourceController().getText(
		    "user_defined_zoom");
		zoom = new JComboBox(getZooms());
		zoom.setSelectedItem("100%");
		zoom.addItem(userDefinedZoom);
		zoom.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					setZoomByItem(e.getItem());
				}
			}
		});
		controller.addAction("toggleMenubar", new ToggleMenubarAction(this));
		controller.addAction("toggleToolbar", new ToggleToolbarAction(this));
		controller.addAction("toggleLeftToolbar", new ToggleLeftToolbarAction(
		    this));
		toolbarVisible = true;
		leftToolbarVisible = true;
		menubarVisible = true;
		toolbarPanel = new JPanel(new BorderLayout());
		leftToolbarPanel = new JPanel();
		status = new JLabel("!");
	}

	public void addMapTitleChangeListener(
	                                      final IMapTitleChangeListener pMapTitleChangeListener) {
		mMapTitleChangeListenerSet.add(pMapTitleChangeListener);
	}

	public void afterMapClose(final MapView pOldMapView) {
	}

	public void afterMapViewChange(final MapView oldMapView,
	                               final MapView newMapView) {
		final ModeController oldModeController = Freeplane.getController()
		    .getModeController();
		ModeController newModeController = oldModeController;
		if (newMapView != null) {
			setViewportView(newMapView);
			if (getMapView().getSelected() == null) {
				moveToRoot();
			}
			lastOpened.mapOpened(newMapView);
			setZoomComboBox(newMapView.getZoom());
			obtainFocusForSelected();
			newModeController = newMapView.getModeController();
			if (newModeController != oldModeController) {
				Freeplane.getController().selectMode(newModeController);
			}
			else if (oldMapView == null) {
				newModeController.enableActions(true);
			}
			newModeController.setVisible(true);
		}
		else {
			setViewportView(null);
			oldModeController.enableActions(false);
		}
		setTitle();
		viewNumberChanged(mapViewManager.getViewNumber());
		newModeController.getUserInputListenerFactory().updateMapList();
	}

	public void beforeMapViewChange(final MapView oldMapView,
	                                final MapView newMapView) {
		final ModeController modeController = Freeplane.getController()
		    .getModeController();
		if (oldMapView != null) {
			modeController.setVisible(false);
		}
	}

	/**
	 * @param property
	 */
	public void changeAntialias(final String property) {
		optionAntialiasAction.changeAntialias(property);
	}

	public void err(final String msg) {
		status.setText(msg);
	}

	abstract public void exit();

	private boolean getAntialiasAll() {
		return antialiasAll;
	}

	private boolean getAntialiasEdges() {
		return antialiasEdges;
	}

	/**
	 * @return
	 */
	abstract public Container getContentPane();

	private float getCurrentZoomIndex() {
		final int selectedIndex = zoom.getSelectedIndex();
		final int itemCount = zoom.getItemCount();
		if (selectedIndex != itemCount - 1) {
			return selectedIndex;
		}
		final float userZoom = getMapView().getZoom();
		for (int i = 0; i < itemCount - 1; i++) {
			if (userZoom < getZoomValue(zoom.getItemAt(i))) {
				return i - 0.5f;
			}
		}
		return itemCount - 0.5f;
	}

	abstract public FreemindMenuBar getFreeMindMenuBar();

	public String getItemForZoom(final float f) {
		return (int) (f * 100F) + "%";
	}

	/**
	 * @return
	 */
	abstract public JFrame getJFrame();

	public LastOpenedList getLastOpenedList() {
		return lastOpened;
	}

	/**
	 */
	public MapModel getMap() {
		final MapView mapView = getMapView();
		return mapView != null ? mapView.getModel() : null;
	}

	public MapView getMapView() {
		return getMapViewManager().getMapView();
	}

	public MapViewManager getMapViewManager() {
		return mapViewManager;
	}

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public JLabel getStatusLabel() {
		return status;
	}

	public Container getViewport() {
		return scrollPane.getViewport();
	}

	public JComboBox getZoomComboBox() {
		return zoom;
	}

	public String[] getZooms() {
		return ViewController.zooms;
	}

	private float getZoomValue(final Object item) {
		final String dirty = (String) item;
		final String cleaned = dirty.substring(0, dirty.length() - 1);
		final float zoomValue = Integer.parseInt(cleaned, 10) / 100F;
		return zoomValue;
	}

	public void init() {
		final JToolBar filterToolbar = Freeplane.getController()
		    .getFilterController().getFilterToolbar();
		getContentPane().add(toolbarPanel, BorderLayout.NORTH);
		getContentPane().add(leftToolbarPanel, BorderLayout.WEST);
		toolbarPanel.add(filterToolbar, BorderLayout.SOUTH);
		status.setPreferredSize(status.getPreferredSize());
		status.setText("");
	}

	abstract public JSplitPane insertComponentIntoSplitPane(
	                                                        JComponent noteViewerComponent);

	abstract public boolean isApplet();

	boolean isLeftToolbarVisible() {
		return leftToolbarVisible;
	}

	public boolean isMapViewChangeAllowed(final MapView oldMapView,
	                                      final MapView newMapView) {
		return true;
	}

	boolean isMenubarVisible() {
		return menubarVisible;
	}

	boolean isToolbarVisible() {
		return toolbarVisible;
	}

	/**
	 * I don't understand how this works now (it's called twice etc.) but it
	 * _works_ now. So let it alone or fix it to be understandable, if you have
	 * the time ;-)
	 */
	void moveToRoot() {
		if (getMapView() != null) {
			getMapView().moveToRoot();
		}
	}

	public void obtainFocusForSelected() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (getMapView() != null) {
					getMapView().getSelected().requestFocus();
				}
				else {
					getFreeMindMenuBar().requestFocus();
				}
			}
		});
	}

	abstract public void openDocument(URL fileToUrl) throws Exception;

	public void out(final String msg) {
		status.setText(msg);
	}

	public boolean quit() {
		while (getMapViewManager().getMapViewVector().size() > 0) {
			if (getMapView() != null) {
				final boolean closingNotCancelled = getMapViewManager().close(
				    false);
				if (!closingNotCancelled) {
					return false;
				}
			}
			else {
				getMapViewManager().nextMapView();
			}
		}
		Freeplane.getController().getResourceController().setProperty(
		    "antialiasEdges", (antialiasEdges ? "true" : "false"));
		Freeplane.getController().getResourceController().setProperty(
		    "antialiasAll", (antialiasAll ? "true" : "false"));
		final String lastOpenedString = lastOpened.save();
		Freeplane.getController().getResourceController().setProperty(
		    "lastOpened", lastOpenedString);
		Freeplane.getController().getResourceController().setProperty(
		    "toolbarVisible", (toolbarVisible ? "true" : "false"));
		Freeplane.getController().getResourceController().setProperty(
		    "leftToolbarVisible", (leftToolbarVisible ? "true" : "false"));
		return true;
	}

	public void removeMapTitleChangeListener(
	                                         final IMapTitleChangeListener pMapTitleChangeListener) {
		mMapTitleChangeListenerSet.remove(pMapTitleChangeListener);
	}

	/**
	 * 
	 */
	abstract public void removeSplitPane();

	public void selectMode(final ModeController oldModeController,
	                       final ModeController newModeController) {
		if (oldModeController == newModeController) {
			return;
		}
		if (oldModeController != null) {
			oldModeController.enableActions(false);
			final UserInputListenerFactory userInputListenerFactory = oldModeController
			    .getUserInputListenerFactory();
			final JToolBar modeToolBar = userInputListenerFactory
			    .getMainToolBar();
			if (modeToolBar != null) {
				toolbarPanel.remove(modeToolBar);
			}
			final Component leftToolBar = userInputListenerFactory
			    .getLeftToolBar();
			if (leftToolBar != null) {
				leftToolbarPanel.remove(leftToolBar);
			}
		}
		newModeController.enableActions(true);
		final JToolBar newToolBar = newModeController
		    .getUserInputListenerFactory().getMainToolBar();
		if (newToolBar != null) {
			toolbarPanel.add(newToolBar, BorderLayout.NORTH);
			newToolBar.repaint();
		}
		/* new left toolbar. */
		final Component newLeftToolBar = newModeController
		    .getUserInputListenerFactory().getLeftToolBar();
		if (newLeftToolBar != null) {
			leftToolbarPanel.add(newLeftToolBar, BorderLayout.WEST);
		}
		setFreeMindMenuBar(newModeController.getUserInputListenerFactory()
		    .getMenuBar());
		newModeController.getUserInputListenerFactory().updateMapList();
	}

	public void setAntialiasAll(final boolean antialiasAll) {
		this.antialiasAll = antialiasAll;
	}

	public void setAntialiasEdges(final boolean antialiasEdges) {
		this.antialiasEdges = antialiasEdges;
	}

	public void setEdgesRenderingHint(final Graphics2D g) {
		if (getAntialiasEdges()) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			    RenderingHints.VALUE_ANTIALIAS_ON);
		}
		else {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			    RenderingHints.VALUE_ANTIALIAS_OFF);
		}
	}

	abstract void setFreeMindMenuBar(FreemindMenuBar menuBar);

	public void setLeftToolbarVisible(final boolean visible) {
		leftToolbarVisible = visible;
		leftToolbarPanel.setVisible(visible);
	}

	public void setMenubarVisible(final boolean visible) {
		menubarVisible = visible;
		getFreeMindMenuBar().setVisible(menubarVisible);
	}

	public void setTextRenderingHint(final Graphics2D g) {
		if (getAntialiasAll()) {
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
		else {
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		}
	}

	/**
	 * Set the Frame title with mode and file if exist
	 */
	public void setTitle() {
		final ModeController modeController = Freeplane.getController()
		    .getModeController();
		if (modeController == null) {
			setTitle("");
			return;
		}
		final Object[] messageArguments = { Freeplane
		    .getText(("mode_" + modeController.getModeName())) };
		final MessageFormat formatter = new MessageFormat(Freeplane
		    .getText("mode_title"));
		String title = formatter.format(messageArguments);
		String rawTitle = "";
		MapModel model = null;
		final MapView mapView = getMapView();
		if (mapView != null) {
			model = mapView.getModel();
			rawTitle = mapView.getName();
			title = rawTitle
			        + (model.isSaved() ? "" : "*")
			        + " - "
			        + title
			        + (model.isReadOnly() ? " ("
			                + Freeplane.getText("read_only") + ")" : "");
			final String modelTitle = model.getTitle();
			if (modelTitle != null) {
				title += ' ' + modelTitle;
			}
		}
		setTitle(title);
		for (final Iterator iterator = mMapTitleChangeListenerSet.iterator(); iterator
		    .hasNext();) {
			final IMapTitleChangeListener listener = (IMapTitleChangeListener) iterator
			    .next();
			listener.setMapTitle(rawTitle, mapView, model);
		}
	}

	abstract public void setTitle(String title);

	public void setToolbarVisible(final boolean visible) {
		toolbarVisible = visible;
		toolbarPanel.setVisible(toolbarVisible);
	}

	private void setViewportView(final MapView view) {
		scrollPane.setViewportView(view);
	}

	/**
	 * @param b
	 */
	abstract public void setWaitingCursor(boolean b);

	public void setZoom(final float zoom) {
		getMapView().setZoom(zoom);
		setZoomComboBox(zoom);
		final Object[] messageArguments = { String.valueOf(zoom * 100f) };
		final String stringResult = Freeplane.getController()
		    .getResourceController().format("user_defined_zoom_status_bar",
		        messageArguments);
		out(stringResult);
	}

	private void setZoomByItem(final Object item) {
		if (((String) item).equals(userDefinedZoom)) {
			return;
		}
		final float zoomValue = getZoomValue(item);
		setZoom(zoomValue);
	}

	public void setZoomComboBox(final float f) {
		final String toBeFound = getItemForZoom(f);
		for (int i = 0; i < zoom.getItemCount(); ++i) {
			if (toBeFound.equals(zoom.getItemAt(i))) {
				zoom.setSelectedItem(toBeFound);
				return;
			}
		}
		zoom.setSelectedItem(userDefinedZoom);
	}

	public void updateMenus(final MenuBuilder menuBuilder) {
		menuBuilder.removeChildElements(FreemindMenuBar.FILE_MENU + "/last");
		boolean firstElement = true;
		final LastOpenedList lst = getLastOpenedList();
		for (final ListIterator it = lst.listIterator(); it.hasNext();) {
			final String key = (String) it.next();
			final JMenuItem item = new JMenuItem(key);
			if (firstElement) {
				firstElement = false;
				item.setAccelerator(KeyStroke.getKeyStroke(Freeplane
				    .getController().getResourceController()
				    .getAdjustableProperty("keystroke_open_first_in_history")));
			}
			item.addActionListener(lastOpenedActionListener);
			menuBuilder.addMenuItem(FreemindMenuBar.FILE_MENU + "/last", item,
			    UIBuilder.AS_CHILD);
		}
		if (menuBuilder.contains("/main_toolbar/zoom")) {
			menuBuilder.addComponent("/main_toolbar/zoom", getZoomComboBox(),
			    zoomIn, MenuBuilder.AS_CHILD);
		}
	}

	private void viewNumberChanged(final int number) {
		navigationPreviousMap.setEnabled(number > 0);
		navigationNextMap.setEnabled(number > 0);
	}

	public void zoomIn() {
		final float currentZoomIndex = getCurrentZoomIndex();
		if (currentZoomIndex < zoom.getItemCount() - 1) {
			setZoomByItem(zoom.getItemAt((int) (currentZoomIndex + 1f)));
		}
	}

	public void zoomOut() {
		final float currentZoomIndex = getCurrentZoomIndex();
		if (currentZoomIndex > 0) {
			setZoomByItem(zoom.getItemAt((int) (currentZoomIndex - 0.5f)));
		}
	}
}
