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
package org.freeplane.core.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.filter.FilterController;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.FreeplaneMenuBar;

/**
 * @author Dimitry Polivaev
 */
abstract public class ViewController implements IMapViewChangeListener {
	public static final String RESOURCE_ANTIALIAS = "antialias";
	private static final String[] zooms = { "25%", "50%", "75%", "100%", "150%", "200%", "300%", "400%" };
	private boolean antialiasAll = false;
	private boolean antialiasEdges = false;
	final private Controller controller;
	final private JPanel leftToolbarPanel;
	private boolean leftToolbarVisible;
	private final IMapViewManager mapViewManager;
	private boolean menubarVisible;
	final private HashSet mMapTitleChangeListenerSet = new HashSet();
	final private OptionAntialiasAction optionAntialiasAction;
	final private JScrollPane scrollPane;
	final private JLabel status;
	final private JPanel toolbarPanel;
	private boolean toolbarVisible;
	final private String userDefinedZoom;
	final private JComboBox zoom;
	final private ZoomInAction zoomIn;
	final private ZoomOutAction zoomOut;

	public ViewController(final Controller controller, final IMapViewManager mapViewManager) {
		super();
		this.controller = controller;
		controller.setViewController(this);
		this.mapViewManager = mapViewManager;
		mapViewManager.addMapViewChangeListener(this);
		controller.putAction(new CloseAction(controller));
		controller.putAction(new MoveToRootAction(controller));
		zoomIn = new ZoomInAction(this);
		controller.putAction(zoomIn);
		zoomOut = new ZoomOutAction(this);
		controller.putAction(zoomOut);
		optionAntialiasAction = new OptionAntialiasAction(controller);
		controller.putAction(optionAntialiasAction);
		userDefinedZoom = ResourceController.getResourceController().getText("user_defined_zoom");
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
		controller.putAction(new ToggleMenubarAction(controller, this));
		controller.putAction(new ToggleToolbarAction(controller, this));
		controller.putAction(new ToggleLeftToolbarAction(controller, this));
		toolbarVisible = true;
		leftToolbarVisible = true;
		menubarVisible = true;
		toolbarPanel = new JPanel(new BorderLayout());
		leftToolbarPanel = new JPanel(new BorderLayout());
		status = new JLabel("!");
		scrollPane = new MapViewScrollPane();
	}

	public void addMapTitleChangeListener(final IMapTitleChangeListener pMapTitleChangeListener) {
		mMapTitleChangeListenerSet.add(pMapTitleChangeListener);
	}

	public void afterMapClose(final MapModel pOldMapView) {
	}

	public void afterViewChange(final Component oldMap, final Component pNewMap) {
		final ModeController oldModeController = controller.getModeController();
		ModeController newModeController = oldModeController;
		if (pNewMap != null) {
			setViewportView(pNewMap);
			final IMapSelection mapSelection = mapViewManager.getMapSelection();
			if (mapSelection.getSelected() == null) {
				mapSelection.selectRoot();
			}
			setZoomComboBox(mapViewManager.getZoom());
			obtainFocusForSelected();
			newModeController = mapViewManager.getModeController(pNewMap);
			if (newModeController != oldModeController) {
				controller.selectMode(newModeController);
			}
			newModeController.setVisible(true);
		}
		else {
			setViewportView(null);
		}
		setTitle();
		viewNumberChanged(mapViewManager.getViewNumber());
		newModeController.getUserInputListenerFactory().updateMapList();
	}

	public void afterViewClose(final Component oldView) {
	}

	public void afterViewCreated(final Component mapView) {
	}

	public void beforeViewChange(final Component oldMap, final Component newMap) {
		final ModeController modeController = controller.getModeController();
		if (oldMap != null) {
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

	private boolean getAntialiasAll() {
		return antialiasAll;
	}

	private boolean getAntialiasEdges() {
		return antialiasEdges;
	}

	public Color getBackgroundColor(final NodeModel node) {
		return mapViewManager.getBackgroundColor(node);
	}

	public Component getComponent(final NodeModel node) {
		return mapViewManager.getComponent(node);
	}

	/**
	 * @return
	 */
	abstract public Container getContentPane();

	protected Controller getController() {
		return controller;
	}

	private float getCurrentZoomIndex() {
		final int selectedIndex = zoom.getSelectedIndex();
		final int itemCount = zoom.getItemCount();
		if (selectedIndex != itemCount - 1) {
			return selectedIndex;
		}
		final float userZoom = mapViewManager.getZoom();
		for (int i = 0; i < itemCount - 1; i++) {
			if (userZoom < getZoomValue(zoom.getItemAt(i))) {
				return i - 0.5f;
			}
		}
		return itemCount - 0.5f;
	}

	public Font getFont(final NodeModel node) {
		return mapViewManager.getFont(node);
	}

	public Frame getFrame() {
		return JOptionPane.getFrameForComponent(getContentPane());
	}

	abstract public FreeplaneMenuBar getFreeplaneMenuBar();

	public String getItemForZoom(final float f) {
		return (int) (f * 100F) + "%";
	}

	/**
	 * @return
	 */
	abstract public JFrame getJFrame();

	/**
	 */
	public MapModel getMap() {
		return mapViewManager.getModel();
	}

	public Component getMapView() {
		return getMapViewManager().getMapViewComponent();
	}

	public IMapViewManager getMapViewManager() {
		return mapViewManager;
	}

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public Component getSelectedComponent() {
		return mapViewManager.getSelectedComponent();
	}

	public IMapSelection getSelection() {
		return mapViewManager.getMapSelection();
	}

	public JLabel getStatusLabel() {
		return status;
	}

	public Color getTextColor(final NodeModel node) {
		return mapViewManager.getTextColor(node);
	}

	public Container getViewport() {
		return scrollPane.getViewport();
	}

	public float getZoom() {
		return mapViewManager.getZoom();
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
		final JToolBar filterToolbar = FilterController.getController(controller).getFilterToolbar();
		getContentPane().add(toolbarPanel, BorderLayout.NORTH);
		getContentPane().add(leftToolbarPanel, BorderLayout.WEST);
		toolbarPanel.add(filterToolbar, BorderLayout.SOUTH);
		status.setPreferredSize(status.getPreferredSize());
		status.setText("");
	}

	abstract public JSplitPane insertComponentIntoSplitPane(JComponent noteViewerComponent);

	abstract public boolean isApplet();

	boolean isLeftToolbarVisible() {
		return leftToolbarVisible;
	}

	boolean isMenubarVisible() {
		return menubarVisible;
	}

	boolean isToolbarVisible() {
		return toolbarVisible;
	}

	public void obtainFocusForSelected() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (getMapView() != null) {
					getSelectedComponent().requestFocus();
				}
				else {
					getFreeplaneMenuBar().requestFocus();
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
				final boolean closingNotCancelled = getMapViewManager().close(false);
				if (!closingNotCancelled) {
					return false;
				}
			}
			else {
				getMapViewManager().nextMapView();
			}
		}
		ResourceController.getResourceController().setProperty("antialiasEdges", (antialiasEdges ? "true" : "false"));
		ResourceController.getResourceController().setProperty("antialiasAll", (antialiasAll ? "true" : "false"));
		ResourceController.getResourceController().setProperty("toolbarVisible", (toolbarVisible ? "true" : "false"));
		ResourceController.getResourceController().setProperty("leftToolbarVisible", (leftToolbarVisible ? "true" : "false"));
		return true;
	}

	public void removeMapTitleChangeListener(final IMapTitleChangeListener pMapTitleChangeListener) {
		mMapTitleChangeListenerSet.remove(pMapTitleChangeListener);
	}

	/**
	 * 
	 */
	abstract public void removeSplitPane();

	public void scrollNodeToVisible(final NodeModel node) {
		mapViewManager.scrollNodeToVisible(node);
	}

	public void selectMode(final ModeController oldModeController, final ModeController newModeController) {
		if (oldModeController == newModeController) {
			return;
		}
		if (oldModeController != null) {
			final IUserInputListenerFactory userInputListenerFactory = oldModeController.getUserInputListenerFactory();
			final JToolBar modeToolBar = userInputListenerFactory.getMainToolBar();
			if (modeToolBar != null) {
				toolbarPanel.remove(modeToolBar);
			}
			final Component leftToolBar = userInputListenerFactory.getLeftToolBar();
			if (leftToolBar != null) {
				leftToolbarPanel.remove(leftToolBar);
			}
		}
		final JToolBar newToolBar = newModeController.getUserInputListenerFactory().getMainToolBar();
		if (newToolBar != null) {
			toolbarPanel.add(newToolBar, BorderLayout.NORTH);
			newToolBar.repaint();
		}
		/* new left toolbar. */
		final Component newLeftToolBar = newModeController.getUserInputListenerFactory().getLeftToolBar();
		if (newLeftToolBar != null) {
			leftToolbarPanel.add(newLeftToolBar, BorderLayout.WEST);
		}
		setFreeplaneMenuBar(newModeController.getUserInputListenerFactory().getMenuBar());
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
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		else {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
	}

	abstract protected void setFreeplaneMenuBar(FreeplaneMenuBar menuBar);

	public void setLeftToolbarVisible(final boolean visible) {
		leftToolbarVisible = visible;
		leftToolbarPanel.setVisible(visible);
	}

	public void setMenubarVisible(final boolean visible) {
		menubarVisible = visible;
		getFreeplaneMenuBar().setVisible(menubarVisible);
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
	 * Set the Frame title with mode and file if exist
	 */
	public void setTitle() {
		final ModeController modeController = controller.getModeController();
		if (modeController == null) {
			setTitle("");
			return;
		}
		final Object[] messageArguments = { ResourceController.getText(("mode_" + modeController.getModeName())) };
		final MessageFormat formatter = new MessageFormat(ResourceController.getText("mode_title"));
		String title = formatter.format(messageArguments);
		String rawTitle = "";
		final MapModel model = mapViewManager.getModel();
		if (model != null) {
			rawTitle = mapViewManager.getMapViewComponent().getName();
			title = rawTitle + (model.isSaved() ? "" : "*") + " - " + title
			        + (model.isReadOnly() ? " (" + ResourceController.getText("read_only") + ")" : "");
			File file = model.getFile();
            if (file != null) {
                title += " " + file.getAbsolutePath();
            }
		}
		setTitle(title);
		for (final Iterator iterator = mMapTitleChangeListenerSet.iterator(); iterator.hasNext();) {
			final IMapTitleChangeListener listener = (IMapTitleChangeListener) iterator.next();
			listener.setMapTitle(model, rawTitle);
		}
	}

	abstract public void setTitle(String title);

	public void setToolbarVisible(final boolean visible) {
		toolbarVisible = visible;
		toolbarPanel.setVisible(toolbarVisible);
	}

	private void setViewportView(final Component view) {
		scrollPane.setViewportView(view);
	}

	/**
	 * @param b
	 */
	abstract public void setWaitingCursor(boolean b);

	public void setZoom(final float zoom) {
		mapViewManager.setZoom(zoom);
		setZoomComboBox(zoom);
		final Object[] messageArguments = { String.valueOf(zoom * 100f) };
		final String stringResult = ResourceController.getResourceController().format("user_defined_zoom_status_bar",
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

	abstract public void stop();

	public void updateMenus(final MenuBuilder menuBuilder) {
		if (menuBuilder.contains("/main_toolbar/zoom")) {
			menuBuilder.addComponent("/main_toolbar/zoom", getZoomComboBox(), zoomIn, MenuBuilder.AS_CHILD);
		}
	}

	public void updateView() {
		mapViewManager.updateMapView();
	}

	protected void viewNumberChanged(final int number) {
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

	public void changeNoteWindowLocation(boolean b) {
    }
}
