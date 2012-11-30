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
package org.freeplane.features.ui;

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
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.RootPaneContainer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.metal.MetalFileChooserUI;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.FixedBasicComboBoxEditor;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.ContainerComboBoxEditor;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.format.FormattedDate;
import org.freeplane.features.format.FormattedObject;
import org.freeplane.features.format.ScannerController;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.StyleNamedObject;
import org.freeplane.features.time.TimeComboBoxEditor;

/**
 * @author Dimitry Polivaev
 */
abstract public class ViewController implements IMapViewChangeListener, IFreeplanePropertyListener {
	public static final String STANDARD_STATUS_INFO_KEY = "standard";

	private final class HorizontalToolbarPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private HorizontalToolbarPanel(final LayoutManager layout) {
			super(layout);
		}

		@Override
		public void validateTree() {
			if (!isValid()) {
				super.validateTree();
				resizeToolbarPane();
			}
		}

		private void resizeToolbarPane() {
			if (getWidth() == 0) {
				return;
			}
			int lastComponent = getComponentCount() - 1;
			while (lastComponent >= 0 && !getComponent(lastComponent).isVisible()) {
				lastComponent--;
			}
			final Dimension oldPreferredSize = getPreferredSize();
			final Dimension preferredSize;
			if (lastComponent >= 0) {
				final Component component = getComponent(lastComponent);
				preferredSize = new Dimension(getWidth(), component.getY() + component.getHeight());
			}
			else {
				preferredSize = new Dimension(0, 0);
			}
			if (oldPreferredSize.height != preferredSize.height) {
				setPreferredSize(preferredSize);
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						getParent().invalidate();
						((JComponent) getContentPane()).revalidate();
					}
				});
			}
		}
	}

	public static final String VISIBLE_PROPERTY_KEY = "VISIBLE_PROPERTY_KEY";
	public static final int TOP = 0, LEFT = 1, RIGHT = 2, BOTTOM = 3;
	public static final String RESOURCE_ANTIALIAS = "antialias";
	private static final String[] zooms = { "25%", "50%", "75%", "100%", "150%", "200%", "300%", "400%" };
	private boolean antialiasAll = false;
	private boolean antialiasEdges = false;
// // 	final private Controller controller;
	private final IMapViewManager mapViewManager;
	final private JScrollPane scrollPane;
	final private JLabel status;
	final private Map<String, Component> statusInfos;
	final private JPanel statusPanel;
	final private JComponent toolbarPanel[];
	final private String userDefinedZoom;
	final private ZoomInAction zoomIn;
	private final DefaultComboBoxModel zoomModel;
	final private ZoomOutAction zoomOut;
	private Rectangle frameSize;

	public Rectangle getFrameSize() {
		return frameSize;
	}

	public void setFrameSize(final Rectangle frameSize) {
		this.frameSize = frameSize;
	}

	private int winState;
	final private String propertyKeyPrefix;
	private boolean setZoomComboBoxRun;
	public static final String SLOW_SCROLLING = "slowScrolling";
	public static Icon textIcon;
	public static Icon numberIcon;
	public static Icon dateIcon;
	public static Icon dateTimeIcon;
	public static Icon linkIcon;
	public static Icon localLinkIcon;

	public ViewController(Controller controller,  final IMapViewManager mapViewManager,
	                      final String propertyKeyPrefix) {
		super();
		final ResourceController resourceController = ResourceController.getResourceController();
		if(textIcon == null){
			ViewController.textIcon = new ImageIcon(resourceController.getResource("/images/text.png"));
			ViewController.numberIcon = new ImageIcon(resourceController.getResource("/images/number.png"));
			ViewController.dateIcon = new ImageIcon(resourceController.getResource("/images/calendar_red.png"));
			ViewController.dateTimeIcon = new ImageIcon(resourceController.getResource("/images/calendar_clock_red.png"));
			ViewController.linkIcon = new ImageIcon(resourceController.getResource("/images/" + resourceController.getProperty("link_icon")));
			ViewController.localLinkIcon = new ImageIcon(resourceController.getResource("/images/" + resourceController.getProperty("link_local_icon")));
		}
		this.propertyKeyPrefix = propertyKeyPrefix;
		statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
		statusPanel.putClientProperty(VISIBLE_PROPERTY_KEY, "status_visible");
		status = new JLabel();
		status.setBorder(BorderFactory.createEtchedBorder());
		statusPanel.add(status);
		statusInfos = new HashMap<String, Component>();
		statusInfos.put(STANDARD_STATUS_INFO_KEY, status);
//		this.controller = controller;
		controller.setViewController(this);
		this.mapViewManager = mapViewManager;
		mapViewManager.addMapViewChangeListener(this);
		controller.addAction(new CloseAction());
		zoomIn = new ZoomInAction(this);
		controller.addAction(zoomIn);
		zoomOut = new ZoomOutAction(this);
		controller.addAction(zoomOut);
		controller.addAction(new ToggleFullScreenAction(this));
		userDefinedZoom = TextUtils.getText("user_defined_zoom");
		zoomModel = new DefaultComboBoxModel(getZooms());
		zoomModel.addElement(userDefinedZoom);
		final String mapViewZoom = resourceController.getProperty(getPropertyKeyPrefix() + "map_view_zoom", "1.0");
		try {
			setZoom(Float.parseFloat(mapViewZoom));
		}
		catch (final Exception e) {
			zoomModel.setSelectedItem("100%");
			LogUtils.severe(e);
		}
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
		
		controller.addAction(new ToggleMenubarAction(this));
		controller.addAction(new ToggleScrollbarsAction(this));
		controller.addAction(new ToggleToolbarAction("ToggleToolbarAction", "/main_toolbar"));
		controller.addAction(new ToggleToolbarAction("ToggleStatusAction", "/status"));
		toolbarPanel = new JComponent[4];

		toolbarPanel[TOP] = new HorizontalToolbarPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		toolbarPanel[BOTTOM] = Box.createHorizontalBox();
		toolbarPanel[LEFT] = Box.createVerticalBox();
		toolbarPanel[RIGHT] = Box.createVerticalBox();
		scrollPane = new MapViewScrollPane();
		resourceController.addPropertyChangeListener(this);
		final String antialiasProperty = resourceController.getProperty(ViewController.RESOURCE_ANTIALIAS);
		changeAntialias(antialiasProperty);
	}

	public void afterMapClose(final MapModel pOldMapView) {
	}

	public void afterViewChange(final Component oldMap, final Component pNewMap) {
		Controller controller = Controller.getCurrentController();
		final ModeController oldModeController = controller.getModeController();
		ModeController newModeController = oldModeController;
		if (pNewMap != null) {
			setViewportView(pNewMap);
			final IMapSelection mapSelection = mapViewManager.getMapSelection();
			final NodeModel selected = mapSelection.getSelected();
			mapSelection.scrollNodeToVisible(selected);
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
		if (pNewMap != null) {
			newModeController.setVisible(true);
		}
	}

	public void afterViewClose(final Component oldView) {
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

	public void changeNoteWindowLocation() {
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
	abstract public RootPaneContainer getRootPaneContainer();
	
	public Container getContentPane(){
		return getRootPaneContainer().getContentPane();
	}

	protected Controller getController() {
		return Controller.getCurrentController();
	}

	private float getCurrentZoomIndex() {
		final int selectedIndex = zoomModel.getIndexOf(zoomModel.getSelectedItem());
		final int itemCount = zoomModel.getSize();
		if (selectedIndex != - 1) {
			return selectedIndex;
		}
		final float userZoom = mapViewManager.getZoom();
		for (int i = 0; i < itemCount - 1; i++) {
			if (userZoom < getZoomValue(zoomModel.getElementAt(i))) {
				return i - 0.5f;
			}
		}
		return itemCount  - 1.5f;
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

	public JComponent getStatusBar() {
		return statusPanel;
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

	public void init(Controller controller) {
		getContentPane().add(toolbarPanel[TOP], BorderLayout.NORTH);
		getContentPane().add(toolbarPanel[LEFT], BorderLayout.WEST);
		getContentPane().add(toolbarPanel[RIGHT], BorderLayout.EAST);
		getContentPane().add(toolbarPanel[BOTTOM], BorderLayout.SOUTH);
//		status.setPreferredSize(status.getPreferredSize());
		status.setText("");
		getRootPaneContainer().getRootPane().putClientProperty(Controller.class, controller);
		final Frame frame = getFrame();
		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				final Frame frame = (Frame) e.getComponent();
				if (frame.getExtendedState() != Frame.NORMAL || isFullScreenEnabled()) {
					return;
				}
				frameSize = frame.getBounds();
			}

			@Override
			public void componentMoved(final ComponentEvent e) {
				componentResized(e);
			}
		});
	}

	abstract public void insertComponentIntoSplitPane(JComponent noteViewerComponent);

	abstract public boolean isApplet();

	public boolean isMenubarVisible() {
		return isComponentVisible("menubar");
	}

	public boolean areScrollbarsVisible() {
		return isComponentVisible("scrollbars");
	}

	private boolean isComponentVisible(String component) {
	    final String property;
		if (isFullScreenEnabled()) {
			property = component+"Visible.fullscreen";
		}
		else {
			property = component +"Visible";
		}
		final boolean booleanProperty = ResourceController.getResourceController().getBooleanProperty(
		    getPropertyKeyPrefix() + property);
		return booleanProperty;
    }

	public void obtainFocusForSelected() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (getMapView() != null) {
					final Component selectedComponent = getSelectedComponent();
					if(selectedComponent != null){
						selectedComponent.requestFocus();
					}
				}
				else {
					getFreeplaneMenuBar().requestFocus();
				}
			}
		});
	}

	abstract public void openDocument(URI uri) throws IOException;

	abstract public void openDocument(URL fileToUrl) throws Exception;

	public void out(final String msg) {
		status.setText(msg);
	}

	public void addStatusInfo(final String key, final String info) {
		addStatusInfo(key, info, null, null);
	}
	
	public void addStatusInfo(final String key, Icon icon) {
		addStatusInfo(key, null, icon, null);
	}
	
	public void addStatusInfo(final String key, final String info, Icon icon) {
		addStatusInfo(key, info, icon, null);
	}
	
	public void addStatusInfo(final String key, final String info, Icon icon, final String tooltip) {
		JLabel label = (JLabel) statusInfos.get(key);
		if (label == null) {
			label = new JLabel(info);
			label.setBorder(BorderFactory.createEtchedBorder());
			statusInfos.put(key, label);
			statusPanel.add(label, statusPanel.getComponentCount() - 1);
		}
		else {
			label.setText(info);
			label.revalidate();
			label.repaint();
		}
		label.setIcon(icon);
		label.setToolTipText(tooltip);
		label.setVisible(info != null || icon != null);
	}

	public void addStatusComponent(final String key, Component component) {
		Component oldComponent = statusInfos.put(key, component);
		if (oldComponent == null) {
			statusPanel.add(component, statusPanel.getComponentCount() - 1);
		}
		else {
			final int index = UITools.getComponentIndex(component);
			statusPanel.remove(index);
			statusPanel.add(component, index);
		}
	}

	public void removeStatus(final String key) {
		final Component oldComponent = statusInfos.remove(key);
		if (oldComponent == null) {
			return;
		}
		statusPanel.remove(oldComponent);
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
		return true;
	}

	/**
	 * 
	 */
	abstract public void removeSplitPane();

	public void saveProperties() {
	}

	public void scrollNodeToVisible(final NodeModel node) {
		mapViewManager.scrollNodeToVisible(node);
	}

	public void selectMode( final ModeController oldModeController,  final ModeController newModeController) {
		if (oldModeController == newModeController) {
			return;
		}
		if (oldModeController != null) {
			final IUserInputListenerFactory userInputListenerFactory = oldModeController.getUserInputListenerFactory();
			for (int j = 0; j < 4; j++) {
				final Iterable<JComponent> modeToolBars = userInputListenerFactory.getToolBars(j);
				if (modeToolBars != null) {
					for (final Component toolBar : modeToolBars) {
						toolbarPanel[j].remove(toolBar);
					}
					toolbarPanel[j].revalidate();
				}
			}
		}
		final IUserInputListenerFactory newUserInputListenerFactory = newModeController.getUserInputListenerFactory();
		for (int j = 0; j < 4; j++) {
			final Iterable<JComponent> newToolBars = newUserInputListenerFactory.getToolBars(j);
			if (newToolBars != null) {
				int i = 0;
				for (final JComponent toolBar : newToolBars) {
					toolBar.setVisible(isToolbarVisible(toolBar));
					toolbarPanel[j].add(toolBar, i++);
				}
				toolbarPanel[j].revalidate();
				toolbarPanel[j].repaint();
			}
		}
		setFreeplaneMenuBar(newUserInputListenerFactory.getMenuBar());
		setUIComponentsVisible();
	}

	private void setUIComponentsVisible() {
	    getFreeplaneMenuBar().setVisible(isMenubarVisible());
		final boolean areScrollbarsVisible = areScrollbarsVisible();
		scrollPane.setHorizontalScrollBarPolicy(areScrollbarsVisible ? JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS : JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(areScrollbarsVisible ? JScrollPane.VERTICAL_SCROLLBAR_ALWAYS : JScrollPane.VERTICAL_SCROLLBAR_NEVER);
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

	public void setMenubarVisible(final boolean visible) {
		final FreeplaneMenuBar freeplaneMenuBar = getFreeplaneMenuBar();
		setComponentVisibleProperty("menubar", visible);
		freeplaneMenuBar.setVisible(visible);
	}

	public void setScrollbarsVisible(final boolean visible) {
		setComponentVisibleProperty("scrollbars", visible);
		scrollPane.setHorizontalScrollBarPolicy(visible ? JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS : JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(visible ? JScrollPane.VERTICAL_SCROLLBAR_ALWAYS : JScrollPane.VERTICAL_SCROLLBAR_NEVER);
	}

	private void setComponentVisibleProperty(final String componentName, final boolean visible) {
	    final String property;
		if (isFullScreenEnabled()) {
			property = componentName+"Visible.fullscreen";
		}
		else {
			property = componentName+"Visible";
		}
		ResourceController.getResourceController().setProperty(getPropertyKeyPrefix() + property, visible);		
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
		final ModeController modeController = Controller.getCurrentModeController();
		if (modeController == null) {
			setTitle("");
			return;
		}
		final Object[] messageArguments = { TextUtils.getText(("mode_" + modeController.getModeName())) };
		final MessageFormat formatter = new MessageFormat(TextUtils.getText("mode_title"));
		String title = formatter.format(messageArguments);
		String rawTitle = "";
		final MapModel model = mapViewManager.getModel();
		if (model != null) {
			rawTitle = mapViewManager.getMapViewComponent().getName();
			title = rawTitle + (model.isSaved() ? "" : "*") + " - " + title
			        + (model.isReadOnly() ? " (" + TextUtils.getText("read_only") + ")" : "");
			final File file = model.getFile();
			if (file != null) {
				title += " " + file.getAbsolutePath();
			}
		}
		setTitle(title);
		modeController.getUserInputListenerFactory().updateMapList();
	}

	abstract public void setTitle(String title);

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
		final String stringResult = TextUtils.format("user_defined_zoom_status_bar", messageArguments);
		out(stringResult);
	}

	private void setZoomByItem(final Object item) {
		final float zoomValue;
		if (((String) item).equals(userDefinedZoom)) {
			final float zoom = mapViewManager.getZoom();
			final int zoomInt = Math.round(100 * zoom);
			final SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(zoomInt, 1, 3200, 1);
			JSpinner spinner = new JSpinner(spinnerNumberModel);
			final int option = JOptionPane.showConfirmDialog(scrollPane, spinner, TextUtils.getText("enter_zoom"), JOptionPane.OK_CANCEL_OPTION);
			if(option == JOptionPane.OK_OPTION)
				zoomValue = spinnerNumberModel.getNumber().floatValue() / 100;
			else
				zoomValue = zoom;
		}
		else
			zoomValue = getZoomValue(item);
		setZoom(zoomValue);
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

	public void updateMenus(final MenuBuilder menuBuilder) {
		if (menuBuilder.contains("main_toolbar_zoom")) {
			final JComboBox zoomBox = new JComboBox(zoomModel);
			menuBuilder.addElement("main_toolbar_zoom", zoomBox, MenuBuilder.AS_CHILD);
			// FELIXHACK
			//zoomBox.setRenderer(new ComboBoxRendererWithTooltip(zoomBox));
		}
	}

	protected void viewNumberChanged(final int number) {
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

	void setFullScreen(final boolean fullScreen) {
		final Frame frame = getFrame();
		final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		if (fullScreen == isFullScreenEnabled()) {
			return;
		}
		if (fullScreen) {
			winState = frame.getExtendedState();
			frame.dispose();
			frame.setExtendedState(Frame.MAXIMIZED_BOTH);
			final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setBounds(0, 0, screenSize.width, screenSize.height);
			frame.setUndecorated(true);
			frame.setResizable(false);
			setUIComponentsVisible();
			for (int j = 0; j < 4; j++) {
				final Iterable<JComponent> toolBars = getController().getModeController().getUserInputListenerFactory()
				    .getToolBars(j);
				for (final JComponent toolBar : toolBars) {
					toolBar.setVisible(isToolbarVisible(toolBar));
				}
			}
			frame.setVisible(true);
		}
		else {
			frame.dispose();
			frame.setUndecorated(false);
			frame.setResizable(true);
			frame.setBounds(frameSize);
			frame.setExtendedState(winState);
			setUIComponentsVisible();
			for (int j = 0; j < 4; j++) {
				final Iterable<JComponent> toolBars = getController().getModeController().getUserInputListenerFactory()
				    .getToolBars(j);
				for (final JComponent toolBar : toolBars) {
					toolBar.setVisible(isToolbarVisible(toolBar));
				}
			}
			frame.setVisible(true);
		}
		if(focusOwner != null)
		    focusOwner.requestFocus();
	}

	boolean isToolbarVisible(final JComponent toolBar) {
		final String completeKeyString = completeVisiblePropertyKey(toolBar);
		if (completeKeyString == null) {
			return true;
		}
		return !"false".equals(ResourceController.getResourceController().getProperty(completeKeyString, "true"));
	}

	public String completeVisiblePropertyKey(final JComponent toolBar) {
		final Object key = toolBar.getClientProperty(VISIBLE_PROPERTY_KEY);
		if (key == null) {
			return null;
		}
		final String keyString = key.toString();
		final String completeKeyString;
		if (isFullScreenEnabled()) {
			completeKeyString = keyString + ".fullscreen";
		}
		else {
			completeKeyString = keyString;
		}
		return getPropertyKeyPrefix() + completeKeyString;
	}

	protected boolean isFullScreenEnabled() {
		return !getFrame().isResizable();
	}

	protected String getPropertyKeyPrefix() {
		return propertyKeyPrefix;
	}

	public static void setLookAndFeel(final String lookAndFeel) {
		try {
			if (lookAndFeel.equals("default")) {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			else {
				UIManager.setLookAndFeel(lookAndFeel);
			}
		}
		catch (final Exception ex) {
			LogUtils.warn("Error while setting Look&Feel" + lookAndFeel);
		}
		
		UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
		
		// Workaround for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7077418
		// NullPointerException in WindowsFileChooserUI when system icons missing/invalid
		// set FileChooserUI to MetalFileChooserUI if no JFileChooser can be created
		try{
			new JFileChooser();
		}
		catch (Throwable t){
			try{
				UIManager.getLookAndFeelDefaults().put("FileChooserUI", MetalFileChooserUI.class.getName());
			}
			catch (Throwable t1){
			}
		}
	}

	public void addObjectTypeInfo(Object value) {
		if (value instanceof FormattedObject) {
			value = ((FormattedObject) value).getObject();
		}
		if (value instanceof String || value instanceof StyleNamedObject) {
			addStatusInfo(ResourceController.OBJECT_TYPE, null, ViewController.textIcon);
		}
		else if (value instanceof FormattedDate) {
			final FormattedDate fd = (FormattedDate) value;
			if (fd.containsTime()) {
				addStatusInfo(ResourceController.OBJECT_TYPE, null, ViewController.dateTimeIcon);
			}
			else {
				addStatusInfo(ResourceController.OBJECT_TYPE, null, ViewController.dateIcon);
			}
		}
		else if (value instanceof Number) {
			addStatusInfo(ResourceController.OBJECT_TYPE, null, ViewController.numberIcon);
		}
		else if (value instanceof URI) {
			addStatusInfo(ResourceController.OBJECT_TYPE, null, ViewController.linkIcon);
		}
		else {
			addStatusInfo(ResourceController.OBJECT_TYPE, null, null);
		}
	}

	public static ComboBoxEditor getTextDateTimeEditor() {
	    final ContainerComboBoxEditor editor = new ContainerComboBoxEditor();
		final NamedObject keyText = new NamedObject("text", "1Ab");
		final BasicComboBoxEditor textEditor = new FixedBasicComboBoxEditor(){
			private Object oldItem;
	
			@Override
	        public void setItem(Object object) {
				oldItem = object;
				if(object instanceof FormattedDate)
					super.setItem("");
				else
					super.setItem(object);
	        }
	
			@Override
	        public Object getItem() {
	            final Object item = super.getItem();
				final Object oldItem = this.oldItem;
				this.oldItem = null;
	            if(item != null && oldItem != null && item.toString().equals(oldItem.toString()))
	            	return oldItem;
	            if(ResourceController.getResourceController().getBooleanProperty("parse_data") 
	            		&& item instanceof String){
	                final Object scannedObject = ScannerController.getController().parse((String)item);
	                return scannedObject;
	            }
				return item;
	        }
			
		};
		editor.put(keyText, textEditor);
		
		final NamedObject keyDate = new NamedObject("date", ""); 
		keyDate.setIcon(dateIcon);
		final TimeComboBoxEditor dateComboBoxEditor = new TimeComboBoxEditor(false){
			@Override
	        public void setItem(Object object) {
				if(object instanceof FormattedDate && !((FormattedDate)object).containsTime())
					super.setItem(object);
				else
					super.setItem(null);
	        }
		};
		
		dateComboBoxEditor.setItem();
		editor.put(keyDate, dateComboBoxEditor);
	
		final NamedObject keyDateTime = new NamedObject("date_time", ""); 
		keyDateTime.setIcon(dateTimeIcon);
		final TimeComboBoxEditor dateTimeComboBoxEditor = new TimeComboBoxEditor(true){
			@Override
	        public void setItem(Object object) {
				if(object instanceof FormattedDate && ((FormattedDate)object).containsTime())
					super.setItem(object);
				else
					super.setItem(null);
	        }
		};
		dateTimeComboBoxEditor.setItem();
		editor.put(keyDateTime, dateTimeComboBoxEditor);
	
		return editor;
	}
}
