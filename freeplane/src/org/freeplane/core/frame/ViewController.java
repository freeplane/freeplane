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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.freeplane.core.controller.Controller;
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
import org.freeplane.core.util.LogTool;

/**
 * @author Dimitry Polivaev
 */
abstract public class ViewController implements IMapViewChangeListener, IFreeplanePropertyListener {
	public static final String VISIBLE_PROPERTY_KEY = "VISIBLE_PROPERTY_KEY";
	public static final String RESOURCE_ANTIALIAS = "antialias";
	private static final String[] zooms = { "25%", "50%", "75%", "100%", "150%", "200%", "300%", "400%" };
	private boolean antialiasAll = false;
	private boolean antialiasEdges = false;
	final private Controller controller;
	final private JPanel leftToolbarPanel;
	private final IMapViewManager mapViewManager;
	final private JScrollPane scrollPane;
	final private JLabel status;
	final private Map<String, JLabel> statusInfos;
	final private JPanel statusPanel;
	final private JPanel toolbarPanel;
	final private String userDefinedZoom;
	final private ZoomInAction zoomIn;
	private final DefaultComboBoxModel zoomModel;
	final private ZoomOutAction zoomOut;
	private Rectangle frameSize;
	public Rectangle getFrameSize() {
    	return frameSize;
    }

	public void setFrameSize(Rectangle frameSize) {
    	this.frameSize = frameSize;
    }

	private int winState;

	public ViewController(final Controller controller, final IMapViewManager mapViewManager) {
		super();
		statusInfos = new HashMap<String, JLabel>();
		statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
		status = new JLabel();
		statusPanel.add(status);
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
		controller.addAction(new ToggleFullScreenAction(this));
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
			LogTool.severe(e);
		}
		controller.addAction(new ToggleMenubarAction(controller, this));
		controller.addAction(new ToggleToolbarAction(controller, "ToggleToolbarAction", "/main_toolbar", "toolbarVisible"));
		controller.addAction(new ToggleLeftToolbarAction(controller, this));
		toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0))
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void validateTree() {
				if(! isValid()){
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
					EventQueue.invokeLater(new Runnable(){
						public void run() {
							getParent().invalidate();
							((JComponent) getContentPane()).revalidate();
                        }});
				}
			}

			
		};
		leftToolbarPanel = new JPanel(new BorderLayout());
		scrollPane = new MapViewScrollPane();
		resourceController.addPropertyChangeListener(this);
		final String antialiasProperty = resourceController.getProperty(ViewController.RESOURCE_ANTIALIAS);
		changeAntialias(antialiasProperty);
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
		if(pNewMap != null){
			newModeController.setVisible(true);
		}
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

	public Component getStatusBar() {
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

	public void init() {
		getContentPane().add(toolbarPanel, BorderLayout.NORTH);
		getContentPane().add(leftToolbarPanel, BorderLayout.WEST);
		if (!ResourceController.getResourceController().getBooleanProperty("leftToolbarVisible")) {
			controller.getViewController().setLeftToolbarVisible(false);
		}
		final Frame frame = getFrame();
		frame.addComponentListener(new ComponentAdapter(){

			@Override
            public void componentResized(ComponentEvent e) {
				final Frame frame = (Frame) e.getComponent();
				if(frame.getExtendedState() != Frame.NORMAL || isFullScreenEnabled()){
					return;
				}
				frameSize = frame.getBounds();
            }

			@Override
            public void componentMoved(ComponentEvent e) {
				componentResized(e);
            }
			
			
			
		});
	}

	abstract public JSplitPane insertComponentIntoSplitPane(JComponent noteViewerComponent);

	abstract public boolean isApplet();

	boolean isLeftToolbarVisible() {
		final String property ;
		if(isFullScreenEnabled()){
			property = "leftToolbarVisible.fullscreen";
		}
		else{
			property = "leftToolbarVisible";
		}
		return ResourceController.getResourceController().getBooleanProperty(property);
	}

	public boolean isMenubarVisible() {
		final String property ;
		if(isFullScreenEnabled()){
			property = "menubarVisible.fullscreen";
		}
		else{
			property = "menubarVisible";
		}
		final boolean booleanProperty = ResourceController.getResourceController().getBooleanProperty(property);
		return booleanProperty;
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

	abstract public void openDocument(URI uri) throws IOException;

	abstract public void openDocument(URL fileToUrl) throws Exception;

	public void out(final String msg) {
		status.setText(msg);
	}
	
	public void addStatusImage(String key, Icon image){
		final JLabel oldLabel = statusInfos.get(key);
		if(oldLabel == null){
			JLabel imageLabel = new JLabel(image);
			imageLabel.setBorder(BorderFactory.createEtchedBorder());
			statusInfos.put(key, imageLabel);
			statusPanel.add(imageLabel, 0);
		}
		else{
			oldLabel.setText(null);
			oldLabel.setIcon(image);
			oldLabel.revalidate();
			oldLabel.repaint();
		}
	}

	public void addStatusInfo(String key, String info){
		JLabel label = statusInfos.get(key);
		if(label == null){
			label = new JLabel(info);
			label.setBorder(BorderFactory.createEtchedBorder());
			statusInfos.put(key, label);
			statusPanel.add(label);
		}
		else{
			label.setText(info);
			label.setIcon(null);
			label.revalidate();
			label.repaint();
		}
		label.setVisible(info != null);
	}
	
	public void removeStatus(String key){
		final JLabel oldLabel = statusInfos.remove(key);
		if(oldLabel == null){
			return;
		}
		statusPanel.remove(oldLabel);
		
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

	public void selectMode(final ModeController oldModeController, final ModeController newModeController) {
		if (oldModeController == newModeController) {
			return;
		}
		if (oldModeController != null) {
			final IUserInputListenerFactory userInputListenerFactory = oldModeController.getUserInputListenerFactory();
			final Iterable<JComponent> modeToolBars = userInputListenerFactory.getToolBars();
			if (modeToolBars != null) {
				for (final Component toolBar : modeToolBars) {
					toolbarPanel.remove(toolBar);
				}
				toolbarPanel.revalidate();
			}
			final Component leftToolBar = userInputListenerFactory.getLeftToolBar();
			if (leftToolBar != null) {
				leftToolbarPanel.remove(leftToolBar);
			}
		}
		final IUserInputListenerFactory newUserInputListenerFactory = newModeController.getUserInputListenerFactory();
		final JComponent mainToolBar = newUserInputListenerFactory.getToolBar("/main_toolbar");
		mainToolBar.putClientProperty(VISIBLE_PROPERTY_KEY, "toolbarVisible");
		mainToolBar.setVisible(isToolbarVisible(mainToolBar));
		final Iterable<JComponent> newToolBars = newUserInputListenerFactory.getToolBars();
		if (newToolBars != null) {
			int i = 0;
			for (final Component toolBar : newToolBars) {
				toolbarPanel.add(toolBar, i++);
			}
			toolbarPanel.revalidate();
			toolbarPanel.repaint();
		}
		/* new left toolbar. */
		final Component newLeftToolBar = newUserInputListenerFactory.getLeftToolBar();
		if (newLeftToolBar != null) {
			leftToolbarPanel.add(newLeftToolBar, BorderLayout.WEST);
		}
		setFreeplaneMenuBar(newUserInputListenerFactory.getMenuBar());
		getFreeplaneMenuBar().setVisible(isMenubarVisible());
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
		final String property ;
		if(isFullScreenEnabled()){
			property = "leftToolbarVisible.fullscreen";
		}
		else{
			property = "leftToolbarVisible";
		}
		ResourceController.getResourceController().setProperty(property, visible);
		leftToolbarPanel.setVisible(visible);
	}

	public void setMenubarVisible(final boolean visible) {
		final String property ;
		if(isFullScreenEnabled()){
			property = "menubarVisible.fullscreen";
		}
		else{
			property = "menubarVisible";
		}
		ResourceController.getResourceController().setProperty(property, visible);
		getFreeplaneMenuBar().setVisible(visible);
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
	
	void setFullScreen(boolean fullScreen) {
		final Frame frame = getFrame();
		if (fullScreen == isFullScreenEnabled()) {
			return;
		}
		if (fullScreen) {
			winState = frame.getExtendedState();
			frame.dispose();
			frame.setExtendedState(Frame.MAXIMIZED_BOTH);
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setBounds(0, 0, screenSize.width, screenSize.height);
			frame.setUndecorated(true);
			frame.setResizable(false);
			getFreeplaneMenuBar().setVisible(isMenubarVisible());
			leftToolbarPanel.setVisible(isLeftToolbarVisible());
			final Iterable<JComponent> toolBars = getController().getModeController().getUserInputListenerFactory()
			    .getToolBars();
			for (final JComponent toolBar : toolBars) {
				toolBar.setVisible(isToolbarVisible(toolBar));
			}
			frame.setVisible(true);
		}
		else {
			frame.dispose();
			frame.setUndecorated(false);
			frame.setResizable(true);
			frame.setBounds(frameSize);
			frame.setExtendedState(winState);
			getFreeplaneMenuBar().setVisible(isMenubarVisible());
			leftToolbarPanel.setVisible(isLeftToolbarVisible());
			final Iterable<JComponent> toolBars = getController().getModeController().getUserInputListenerFactory()
			    .getToolBars();
			for (final JComponent toolBar : toolBars) {
				toolBar.setVisible(isToolbarVisible(toolBar));
			}
			frame.setVisible(true);
		}
	}

	boolean isToolbarVisible(JComponent toolBar) {
		final String completeKeyString = completeVisiblePropertyKey(toolBar);
		if (completeKeyString == null) {
			return true;
		}
		return ! "false".equals(ResourceController.getResourceController().getProperty(completeKeyString, "true"));
    }

	public String completeVisiblePropertyKey(JComponent toolBar) {
		final Object key = toolBar.getClientProperty(VISIBLE_PROPERTY_KEY);
		if(key == null){
			return null;
		}
		final String keyString = key.toString();
	    final String completeKeyString;
		if(isFullScreenEnabled()){
			completeKeyString = keyString + ".fullscreen";
		}
		else{
			completeKeyString = keyString;
		}
	    return completeKeyString;
    }

	protected boolean isFullScreenEnabled() {
	    return ! getFrame().isResizable();
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
			LogTool.warn("Error while setting Look&Feel" + lookAndFeel, ex);
		}
	}
}
