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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ComboBoxEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.metal.MetalFileChooserUI;

import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.FixedBasicComboBoxEditor;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.components.ContainerComboBoxEditor;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.format.FormattedDate;
import org.freeplane.features.format.FormattedObject;
import org.freeplane.features.format.ScannerController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.StyleNamedObject;
import org.freeplane.features.time.TimeComboBoxEditor;

/**
 * @author Dimitry Polivaev
 */
abstract public class FrameController implements ViewController {

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

	// // 	final private Controller controller;
	final private JLabel status;
	final private Map<String, Component> statusInfos;
	final private JPanel statusPanel;
	final private JComponent toolbarPanel[];
	private Rectangle frameSize;

	public Rectangle getFrameSize() {
		return frameSize;
	}

	public void setFrameSize(final Rectangle frameSize) {
		this.frameSize = frameSize;
	}

	private int winState;
	final private String propertyKeyPrefix;
	public static Icon textIcon;
	public static Icon numberIcon;
	public static Icon dateIcon;
	public static Icon dateTimeIcon;
	public static Icon linkIcon;
	public static Icon localLinkIcon;

	public FrameController(Controller controller,  final IMapViewManager mapViewManager,
	                      final String propertyKeyPrefix) {
		super();
		final ResourceController resourceController = ResourceController.getResourceController();
		if(textIcon == null){
			FrameController.textIcon = new ImageIcon(resourceController.getResource("/images/text.png"));
			FrameController.numberIcon = new ImageIcon(resourceController.getResource("/images/number.png"));
			FrameController.dateIcon = new ImageIcon(resourceController.getResource("/images/calendar_red.png"));
			FrameController.dateTimeIcon = new ImageIcon(resourceController.getResource("/images/calendar_clock_red.png"));
			FrameController.linkIcon = new ImageIcon(resourceController.getResource("/images/" + resourceController.getProperty("link_icon")));
			FrameController.localLinkIcon = new ImageIcon(resourceController.getResource("/images/" + resourceController.getProperty("link_local_icon")));
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
		controller.addAction(new ToggleFullScreenAction(this));
		controller.addAction(new CloseAction());
		
		controller.addAction(new ToggleMenubarAction(this));
		controller.addAction(new ToggleScrollbarsAction(this));
		controller.addAction(new ToggleToolbarAction("ToggleToolbarAction", "/main_toolbar"));
		controller.addAction(new ToggleToolbarAction("ToggleStatusAction", "/status"));
		toolbarPanel = new JComponent[4];

		toolbarPanel[TOP] = new HorizontalToolbarPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		toolbarPanel[BOTTOM] = Box.createHorizontalBox();
		toolbarPanel[LEFT] = Box.createHorizontalBox();
		toolbarPanel[RIGHT] = Box.createVerticalBox();
	}

	public void changeNoteWindowLocation() {
	}

	public void err(final String msg) {
		status.setText(msg);
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

	public Frame getFrame() {
		return JOptionPane.getFrameForComponent(getContentPane());
	}

	abstract public FreeplaneMenuBar getFreeplaneMenuBar();

	/**
	 * @return
	 */
	abstract public JFrame getJFrame();

	/**
	 */
	public JComponent getStatusBar() {
		return statusPanel;
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

	/**
	 * 
	 */
	abstract public void removeSplitPane();

	public void saveProperties() {
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
		setUIComponentsVisible(newModeController.getController().getMapViewManager());
	}

	private void setUIComponentsVisible(IMapViewManager iMapViewManager) {
	    getFreeplaneMenuBar().setVisible(isMenubarVisible());
    }

	abstract protected void setFreeplaneMenuBar(FreeplaneMenuBar menuBar);

	public void setMenubarVisible(final boolean visible) {
		final FreeplaneMenuBar freeplaneMenuBar = getFreeplaneMenuBar();
		setComponentVisibleProperty("menubar", visible);
		freeplaneMenuBar.setVisible(visible);
	}

	public void setScrollbarsVisible(final boolean visible) {
		setComponentVisibleProperty("scrollbars", visible);
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

	/**
	 * Set the Frame title with mode and file if exist
	 */

	abstract public void setTitle(String frameTitle);

	/**
	 * @param b
	 */
	abstract public void setWaitingCursor(boolean b);

	public void viewNumberChanged(final int number) {
	}

	void setFullScreen(final boolean fullScreen) {
		final Frame frame = getFrame();
		final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		if (fullScreen == isFullScreenEnabled()) {
			return;
		}
		final Controller controller = getController();
		ResourceController.getResourceController().firePropertyChanged(FULLSCREEN_ENABLED_PROPERTY, Boolean.toString(!fullScreen),Boolean.toString(fullScreen));
		Iterable<Window> visibleFrames = collectVisibleFrames(frame);
		if (fullScreen) {
			winState = frame.getExtendedState();
			frame.dispose();
			frame.setExtendedState(Frame.MAXIMIZED_BOTH);
			final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setBounds(0, 0, screenSize.width, screenSize.height);
			frame.setUndecorated(true);
			frame.setResizable(false);
			setUIComponentsVisible(controller.getMapViewManager());
			for (int j = 0; j < 4; j++) {
				final Iterable<JComponent> toolBars = controller.getModeController().getUserInputListenerFactory()
				    .getToolBars(j);
				for (final JComponent toolBar : toolBars) {
					toolBar.setVisible(isToolbarVisible(toolBar));
				}
			}
			showWindows(visibleFrames);
		}
		else {
			frame.dispose();
			frame.setUndecorated(false);
			frame.setResizable(true);
			frame.setBounds(frameSize);
			frame.setExtendedState(winState);
			setUIComponentsVisible(controller.getMapViewManager());
			for (int j = 0; j < 4; j++) {
				final Iterable<JComponent> toolBars = controller.getModeController().getUserInputListenerFactory()
				    .getToolBars(j);
				for (final JComponent toolBar : toolBars) {
					toolBar.setVisible(isToolbarVisible(toolBar));
				}
			}
			showWindows(visibleFrames);
		}
		if(focusOwner != null)
		    focusOwner.requestFocus();
	}

	private Collection<Window> collectVisibleFrames(Window window) {
		if(! window.isVisible())
			return Collections.emptyList();
		Window[] ownedWindows = window.getOwnedWindows();
		ArrayList<Window> visibleWindows = new ArrayList(ownedWindows.length+ 1); 
		visibleWindows.add(window);
		for(Window child : ownedWindows){
			visibleWindows.addAll(collectVisibleFrames(child));
		}
		return visibleWindows;
    }

	protected void showWindows(final Iterable<Window> windows) {
	    for(Window child : windows)
	    	child.setVisible(true);
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

	public boolean isFullScreenEnabled() {
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
				LookAndFeelInfo[] lafInfos = UIManager.getInstalledLookAndFeels();
				boolean setLnF = false;
				for(LookAndFeelInfo lafInfo : lafInfos){
					if(lafInfo.getName().equalsIgnoreCase(lookAndFeel)){										
						UIManager.setLookAndFeel(lafInfo.getClassName());						
						Controller.getCurrentController().getResourceController().setProperty("lookandfeel", lafInfo.getClassName());
						setLnF = true;
						break;										
					}
					if(lafInfo.getClassName().equals(lookAndFeel)){
						UIManager.setLookAndFeel(lafInfo.getClassName());
						setLnF = true;
						break;
					}
				}
				if(!setLnF){
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					Controller.getCurrentController().getResourceController().setProperty("lookandfeel", "default");
				}
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
			addStatusInfo(ResourceController.OBJECT_TYPE, null, FrameController.textIcon);
		}
		else if (value instanceof FormattedDate) {
			final FormattedDate fd = (FormattedDate) value;
			if (fd.containsTime()) {
				addStatusInfo(ResourceController.OBJECT_TYPE, null, FrameController.dateTimeIcon);
			}
			else {
				addStatusInfo(ResourceController.OBJECT_TYPE, null, FrameController.dateIcon);
			}
		}
		else if (value instanceof Number) {
			addStatusInfo(ResourceController.OBJECT_TYPE, null, FrameController.numberIcon);
		}
		else if (value instanceof URI) {
			addStatusInfo(ResourceController.OBJECT_TYPE, null, FrameController.linkIcon);
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

	public boolean quit() {
	    return getController().getMapViewManager().closeAllMaps();
    }

	public boolean isDispatchThread() {
	    return EventQueue.isDispatchThread();
    }

	public void invokeLater(Runnable runnable) {
	   EventQueue.invokeLater(runnable);
    }

	public void invokeAndWait(Runnable runnable) throws InterruptedException, InvocationTargetException {
		EventQueue.invokeAndWait(runnable);
    }
	
	public boolean isHeadless() {
	    return false;
    }
	
}
