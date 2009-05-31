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
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.DefaultComboBoxModel;
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
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.FreeplaneMenuBar;

/**
 * @author Dimitry Polivaev
 */
abstract public class ViewController implements IMapViewChangeListener, IFreeplanePropertyListener {
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
	final private JScrollPane scrollPane;
	final private JLabel status;
	final private JPanel toolbarPanel;
	private boolean toolbarVisible;
	final private String userDefinedZoom;
	final private ZoomInAction zoomIn;
	private final DefaultComboBoxModel zoomModel;
	final private ZoomOutAction zoomOut;

	public ViewController(final Controller controller, final IMapViewManager mapViewManager) {
		super();
		status = new JLabel("!");
		this.controller = controller;
		controller.setViewController(this);
		this.mapViewManager = mapViewManager;
		mapViewManager.addMapViewChangeListener(this);
		controller.addAction(new CloseAction(controller));
		controller.addAction(new MoveToRootAction(controller));
		zoomIn = new ZoomInAction(this);
		controller.addAction(zoomIn);
		zoomOut = new ZoomOutAction(this);
		controller.addAction(zoomOut);
		userDefinedZoom = ResourceBundles.getText("user_defined_zoom");
		zoomModel = new DefaultComboBoxModel(getZooms());
		zoomModel.addElement(userDefinedZoom);
		final ResourceController resourceController = ResourceController.getResourceController();
		final String mapViewZoom = resourceController.getProperty("map_view_zoom", "1.0");
		try {
			setZoom(Float.parseFloat(mapViewZoom));
		}
		catch (final Exception e) {
			zoomModel.setSelectedItem("100%");
			e.printStackTrace();
		}
		controller.addAction(new ToggleMenubarAction(controller, this));
		controller.addAction(new ToggleToolbarAction(controller, this));
		controller.addAction(new ToggleLeftToolbarAction(controller, this));
		toolbarVisible = true;
		leftToolbarVisible = true;
		menubarVisible = true;
		toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		leftToolbarPanel = new JPanel(new BorderLayout());
		scrollPane = new MapViewScrollPane();
		resourceController.addPropertyChangeListener(this);
		final String antialiasProperty = resourceController.getProperty(ViewController.RESOURCE_ANTIALIAS);
		changeAntialias(antialiasProperty);
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
			final NodeModel selected = mapSelection.getSelected();
			if (selected == null) {
				mapSelection.selectRoot();
			}
			else {
				mapSelection.scrollNodeToVisible(selected);
			}
			setZoomComboBox(mapViewManager.getZoom());
			obtainFocusForSelected();
			newModeController = mapViewManager.getModeController(pNewMap);
			if (newModeController != oldModeController) {
				controller.selectMode(newModeController);
			}
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
	 */
	private void changeAntialias(final String command) {
		if (command == null) {
			return;
		}
		final Controller controller = getController();
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
		final Component mapView = controller.getViewController().getMapView();
		if (mapView != null) {
			mapView.repaint();
		}
	}

	public void changeNoteWindowLocation(final boolean b) {
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
		final int selectedIndex = zoomModel.getIndexOf(zoomModel.getSelectedItem());
		final int itemCount = zoomModel.getSize();
		if (selectedIndex != itemCount - 1) {
			return selectedIndex;
		}
		final float userZoom = mapViewManager.getZoom();
		for (int i = 0; i < itemCount - 1; i++) {
			if (userZoom < getZoomValue(zoomModel.getElementAt(i))) {
				return i - 0.5f;
			}
		}
		return selectedIndex - 0.5f;
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
		toolbarPanel.add(filterToolbar);
		filterToolbar.addComponentListener(new ComponentListener() {
			public void componentHidden(final ComponentEvent e) {
				resizeToolbarPane();
			}

			public void componentMoved(final ComponentEvent e) {
				resizeToolbarPane();
			}

			public void componentResized(final ComponentEvent e) {
			}

			public void componentShown(final ComponentEvent e) {
				resizeToolbarPane();
			}
		});
		toolbarPanel.addComponentListener(new ComponentListener() {
			public void componentHidden(final ComponentEvent e) {
			}

			public void componentMoved(final ComponentEvent e) {
			}

			public void componentResized(final ComponentEvent e) {
				resizeToolbarPane();
			}

			public void componentShown(final ComponentEvent e) {
			}
		});
		toolbarPanel.addContainerListener(new ContainerListener() {
			public void componentAdded(final ContainerEvent e) {
				resizeToolbarPane();
			}

			public void componentRemoved(final ContainerEvent e) {
				resizeToolbarPane();
			}
		});
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

	public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
		if (propertyName.equals(ViewController.RESOURCE_ANTIALIAS)) {
			changeAntialias(newValue);
		}
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
		ResourceController.getResourceController().setProperty("leftToolbarVisible",
		    (leftToolbarVisible ? "true" : "false"));
		return true;
	}

	public void removeMapTitleChangeListener(final IMapTitleChangeListener pMapTitleChangeListener) {
		mMapTitleChangeListenerSet.remove(pMapTitleChangeListener);
	}

	/**
	 * 
	 */
	abstract public void removeSplitPane();

	private void resizeToolbarPane() {
		if (!toolbarPanel.isValid()) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					resizeToolbarPane();
				}
			});
			return;
		}
		int lastComponent = toolbarPanel.getComponentCount() - 1;
		while (lastComponent >= 0 && !toolbarPanel.getComponent(lastComponent).isVisible()) {
			lastComponent--;
		}
		if (lastComponent >= 0) {
			final Component component = toolbarPanel.getComponent(lastComponent);
			final Dimension oldPreferredSize = toolbarPanel.getPreferredSize();
			final Dimension preferredSize = new Dimension(toolbarPanel.getWidth(), component.getY()
			        + component.getHeight());
			if (oldPreferredSize.height != preferredSize.height) {
				toolbarPanel.setPreferredSize(preferredSize);
				toolbarPanel.getParent().invalidate();
				((JComponent) getContentPane()).revalidate();
			}
		}
	}

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
				toolbarPanel.revalidate();
			}
			final Component leftToolBar = userInputListenerFactory.getLeftToolBar();
			if (leftToolBar != null) {
				leftToolbarPanel.remove(leftToolBar);
			}
		}
		final JToolBar newToolBar = newModeController.getUserInputListenerFactory().getMainToolBar();
		if (newToolBar != null) {
			toolbarPanel.add(newToolBar, 0);
			toolbarPanel.revalidate();
			newToolBar.repaint();
		}
		/* new left toolbar. */
		final Component newLeftToolBar = newModeController.getUserInputListenerFactory().getLeftToolBar();
		if (newLeftToolBar != null) {
			leftToolbarPanel.add(newLeftToolBar, BorderLayout.WEST);
		}
		setFreeplaneMenuBar(newModeController.getUserInputListenerFactory().getMenuBar());
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
		final Object[] messageArguments = { ResourceBundles.getText(("mode_" + modeController.getModeName())) };
		final MessageFormat formatter = new MessageFormat(ResourceBundles.getText("mode_title"));
		String title = formatter.format(messageArguments);
		String rawTitle = "";
		final MapModel model = mapViewManager.getModel();
		if (model != null) {
			rawTitle = mapViewManager.getMapViewComponent().getName();
			title = rawTitle + (model.isSaved() ? "" : "*") + " - " + title
			        + (model.isReadOnly() ? " (" + ResourceBundles.getText("read_only") + ")" : "");
			final File file = model.getFile();
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
		final String stringResult = FpStringUtils.format("user_defined_zoom_status_bar", messageArguments);
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
		for (int i = 0; i < zoomModel.getSize(); ++i) {
			if (toBeFound.equals(zoomModel.getElementAt(i))) {
				zoomModel.setSelectedItem(toBeFound);
				return;
			}
		}
		zoomModel.setSelectedItem(userDefinedZoom);
	}

	public void updateMenus(final MenuBuilder menuBuilder) {
		if (menuBuilder.contains("/main_toolbar/zoom")) {
			final JComboBox zoomBox = new JComboBox(zoomModel);
			zoomBox.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						setZoomByItem(e.getItem());
					}
				}
			});
			menuBuilder.addElement("/main_toolbar/zoom", zoomBox, MenuBuilder.AS_CHILD);
		}
	}

	protected void viewNumberChanged(final int number) {
	}

	public void zoomIn() {
		final float currentZoomIndex = getCurrentZoomIndex();
		if (currentZoomIndex < zoomModel.getSize() - 1) {
			setZoomByItem(zoomModel.getElementAt((int) (currentZoomIndex + 1f)));
		}
	}

	public void zoomOut() {
		final float currentZoomIndex = getCurrentZoomIndex();
		if (currentZoomIndex > 0) {
			setZoomByItem(zoomModel.getElementAt((int) (currentZoomIndex - 0.5f)));
		}
	}
}
