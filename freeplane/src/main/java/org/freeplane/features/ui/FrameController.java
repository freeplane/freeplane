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
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ComboBoxEditor;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.RootPaneContainer;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicEditorPaneUI;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.ui.FixedBasicComboBoxEditor;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.components.ContainerComboBoxEditor;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.components.resizer.UIComponentVisibilityDispatcher;
import org.freeplane.core.ui.flatlaf.NonSelectingFlatEditorPaneUI;
import org.freeplane.core.util.ClassLoaderFactory;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.Hyperlink;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.format.FormattedDate;
import org.freeplane.features.format.FormattedObject;
import org.freeplane.features.format.ScannerController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.StyleString;
import org.freeplane.features.styles.StyleTranslatedObject;
import org.freeplane.features.time.TimeComboBoxEditor;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLaf;

/**
 * @author Dimitry Polivaev
 */
abstract public class FrameController implements ViewController {
	public static final String VAQUA_LAF_NAME = "VAqua";
	public static final String VAQUA_LAF_CLASS_NAME = "org.violetlib.aqua.AquaLookAndFeel";
    private static final String DARCULA_LAF_CLASS_NAME = "com.bulenkov.darcula.DarculaLaf";
    private static final String MOTIF_LAF__CLASS_NAME = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
	private static final double DEFAULT_SCALING_FACTOR = 0.8;
	private static final String MENU_ITEM_FONT_SIZE_PROPERTY = "menuItemFontSize";


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
			final Dimension oldPreferredSize = getPreferredSize();
			final Dimension preferredSize;
			int maxHeight = 0;
			for (Component component : getComponents()) {
				if (component.isVisible())
					maxHeight = Math.max(maxHeight, component.getY() + component.getHeight());
			}
			if (maxHeight > 0) {
				preferredSize = new Dimension(getWidth(), maxHeight);
			}
			else {
				preferredSize = new Dimension(0, 0);
			}
			if (oldPreferredSize.height != preferredSize.height) {
				setPreferredSize(preferredSize);
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						getParent().invalidate();
						getMainContentPane().revalidate();
					}
				});
			}
		}
	}

	final protected Controller controller;
	final private JLabel status;
	final private Map<String, Component> statusInfos;
	final private JPanel statusPanel;
	final private JComponent toolbarPanel[];
	final private String propertyKeyPrefix;
	private static boolean uiResourcesInitialized = false;
	private static Icon textIcon;
	private static Icon numberIcon;
	private static Icon dateIcon;
	private static Icon dateTimeIcon;
	private static Icon linkIcon;
	private static Icon unknownTypeIcon;
	static private void initializeUiResources(){
		if(uiResourcesInitialized == false) {
			uiResourcesInitialized = true;
			final ResourceController resourceController = ResourceController.getResourceController();
			textIcon = resourceController.getIcon("text_icon");
			numberIcon = resourceController.getIcon("number_icon");
			dateIcon = resourceController.getIcon("date_icon");
			dateTimeIcon = resourceController.getIcon("date_time_icon");
			linkIcon = resourceController.getIcon("link_icon");
			unknownTypeIcon = resourceController.getIcon("unknown_type_icon");
		}
	}
	private final IMapViewManager mapViewManager;
	public FrameController(Controller controller, final IMapViewManager mapViewManager,
	                       final String propertyKeyPrefix) {
		super();
		this.controller = controller;
		this.mapViewManager = mapViewManager;
		this.propertyKeyPrefix = propertyKeyPrefix;
		statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
		UIComponentVisibilityDispatcher.install(statusPanel, propertyKeyPrefix + "statusVisible");
		status = new JLabel();
		status.setBorder(BorderFactory.createEtchedBorder());
		statusPanel.add(status);
		statusInfos = new HashMap<String, Component>();
		statusInfos.put(STANDARD_STATUS_INFO_KEY, status);
		statusTextCleaner = new Timer(10000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				status.setVisible(false);
				status.setText(null);
			}
		});
		statusTextCleaner.setRepeats(false);
		//		this.controller = controller;
		controller.setViewController(this);
		controller.addAction(new ToggleFullScreenAction(this));
		controller.addAction(new ToggleMenubarAction(this));
		controller.addAction(new ToggleScrollbarsAction(this));
		controller.addAction(new ToggleMapOverviewAction(this));
		controller.addAction(new ToggleToolbarAction("ToggleToolbarAction", "/main_toolbar"));
		controller.addAction(new ToggleToolbarAction("ToggleStatusAction", "/status"));
		addStatusInfo(ResourceController.OBJECT_TYPE, null, null);
		toolbarPanel = new JComponent[4];
		toolbarPanel[TOP] = new HorizontalToolbarPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		toolbarPanel[BOTTOM] = Box.createVerticalBox();
		toolbarPanel[LEFT] = Box.createHorizontalBox();
		toolbarPanel[RIGHT] = Box.createVerticalBox();
	}

	@Override
	public void changeNoteWindowLocation() {
	}

	@Override
	public void err(final String msg) {
		status.setText(msg);
	}

	protected Controller getController() {
		return Controller.getCurrentController();
	}

	@Override
	abstract public FreeplaneMenuBar getFreeplaneMenuBar();

	/**
	 */
	@Override
	public JComponent getStatusBar() {
		return statusPanel;
	}

	@Override
	public void init(Controller controller) {
		initializeUiResources();
		final JComponent mainContentPane = getMainContentPane();
		mainContentPane.add(toolbarPanel[TOP], BorderLayout.NORTH);
		mainContentPane.add(toolbarPanel[LEFT], BorderLayout.WEST);
		mainContentPane.add(toolbarPanel[RIGHT], BorderLayout.EAST);
		mainContentPane.add(toolbarPanel[BOTTOM], BorderLayout.SOUTH);
		status.setText("");
		status.setVisible(false);
		mainContentPane.getRootPane().putClientProperty(Controller.class, controller);
	}

	private JComponent getMainContentPane() {
		return (JComponent) ((RootPaneContainer) getMenuComponent()).getContentPane();
	}

	@Override
	abstract public void insertComponentIntoSplitPane(JComponent noteViewerComponent);

	@Override
	public boolean isMenubarVisible() {
		return isComponentVisible("menubar");
	}

	@Override
	public boolean isMapOverviewVisible() {
		return isComponentVisible("mapOverview");
	}

	@Override
	public boolean areScrollbarsVisible() {
		return isComponentVisible("scrollbars");
	}

	private boolean isComponentVisible(String component) {
		final String property;
		if (isMenuComponentInFullScreenMode()) {
			property = component + "Visible.fullscreen";
		}
		else {
			property = component + "Visible";
		}
		final boolean booleanProperty = ResourceController.getResourceController().getBooleanProperty(
		    propertyKeyPrefix + property);
		return booleanProperty;
	}

	protected boolean isMenuComponentInFullScreenMode() {
		return isFullScreenEnabled(getMenuComponent());
	}

	@Override
	public boolean isFullScreenEnabled() {
		return isFullScreenEnabled(getCurrentRootComponent());
	}

	boolean isFullScreenEnabled(final Component component) {
		if(component instanceof JFrame) {
			JRootPane rootPane = ((JFrame)component).getRootPane();
			return rootPane != null && Boolean.TRUE.equals(rootPane.getClientProperty(ViewController.FULLSCREEN_ENABLED_PROPERTY));
		}
		return false;

	}

	@Override
	abstract public void openDocument(Hyperlink link) throws IOException;

	@Override
	abstract public void openDocument(URL fileToUrl) throws Exception;

	final private Timer statusTextCleaner;

	@Override
	public void out(final String msg) {
		status.setText(msg);
		status.setVisible(msg != null && !msg.isEmpty());
		statusTextCleaner.restart();
	}

	@Override
	public void addStatusInfo(final String key, final String info) {
		addStatusInfo(key, info, null, null);
	}

	@Override
	public void addStatusInfo(final String key, Icon icon) {
		addStatusInfo(key, null, icon, null);
	}

	@Override
	public void addStatusInfo(final String key, final String info, Icon icon) {
		addStatusInfo(key, info, icon, null);
	}

	@Override
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
		label.setVisible(info != null && !info.isEmpty() || icon != null);
	}

	@Override
	public void addStatusComponent(final String key, Component component) {
		Component oldComponent = statusInfos.put(key, component);
		if (oldComponent == null) {
			statusPanel.add(component, statusPanel.getComponentCount() - 1);
		}
		else {
			final int index = UITools.getComponentIndex(oldComponent);
			transferFocusFrom(oldComponent);
			statusPanel.remove(index);
			statusPanel.add(component, index);
		}
		statusPanel.revalidate();
		statusPanel.repaint();
	}

	private void transferFocusFrom(Component oldComponent) {
		if(oldComponent.hasFocus()) {
			Component selectedComponent = mapViewManager.getSelectedComponent();
			if(selectedComponent != null)
				selectedComponent.requestFocusInWindow();
		}
	}

	@Override
	public void removeStatus(final String key) {
		final Component oldComponent = statusInfos.remove(key);
		if (oldComponent == null) {
			return;
		}
		transferFocusFrom(oldComponent);
		statusPanel.remove(oldComponent);
		statusPanel.revalidate();
		statusPanel.repaint();
	}

	/**
	 *
	 */
	@Override
	abstract public void removeSplitPane();

	@Override
	public void saveProperties() {
	}

	@Override
	public void selectMode(final ModeController oldModeController, final ModeController newModeController) {
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
					UIComponentVisibilityDispatcher dispatcher = UIComponentVisibilityDispatcher.of(toolBar);
					if (dispatcher != null) {
						dispatcher.resetVisible();
					}
					toolbarPanel[j].add(toolBar, i++);
				}
				toolbarPanel[j].revalidate();
				toolbarPanel[j].repaint();
			}
		}
		setFreeplaneMenuBar(newUserInputListenerFactory.getMenuBar());
		setUIComponentsVisible(newModeController.getController().getMapViewManager(), isMenubarVisible());
	}

	private void setUIComponentsVisible(IMapViewManager iMapViewManager, boolean visible) {
		setMenubarVisible(visible);
	}

	abstract protected void setFreeplaneMenuBar(FreeplaneMenuBar menuBar);

	@Override
	public void setMenubarVisible(final boolean visible) {
		setComponentVisibleProperty("menubar", visible);
		final Component freeplaneMenuBar = getFreeplaneMenuBar();
		freeplaneMenuBar.setVisible(visible);
	}

	@Override
	public void setMapOverviewVisible(final boolean visible) {
		setComponentVisibleProperty("mapOverview", visible);
	}

	@Override
	public void setScrollbarsVisible(final boolean visible) {
		setComponentVisibleProperty("scrollbars", visible);
	}

	private void setComponentVisibleProperty(final String componentName, final boolean visible) {
		final String property;
		if (isMenuComponentInFullScreenMode()) {
			property = componentName + "Visible.fullscreen";
		}
		else {
			property = componentName + "Visible";
		}
		ResourceController.getResourceController().setProperty(propertyKeyPrefix + property, visible);
	}

	/**
	 * Set the Frame title with mode and file if exist
	 */
	@Override
	abstract public void setTitle(String frameTitle);

	/**
	 * @param b
	 */
	@Override
	abstract public void setWaitingCursor(boolean b);

	@Override
	public void viewNumberChanged(final int number) {
	}

	static class FrameState {
		final Rectangle bounds;
		final int winState;

		public FrameState(Rectangle bounds, int winState) {
			super();
			this.bounds = bounds;
			this.winState = winState;
		}
	}

	public void setFullScreen(final boolean fullScreen) {
		if (fullScreen == isFullScreenEnabled()) {
			return;
		}
		final JFrame frame = (JFrame) getCurrentRootComponent();
		if(Compat.isMacOsX())
			setFullScreenOnMac(fullScreen, frame);
		else
			setFullScreenOnNonMac(frame, fullScreen);
		ToolTipManager.sharedInstance().setEnabled(true);
	}

	private void setFullScreenOnMac(final boolean fullScreen, final JFrame frame) {
		Compat.setFullScreenOnMac(frame, fullScreen);
		if (Boolean.valueOf(fullScreen).equals(frame.getRootPane().getClientProperty(FULLSCREEN_ENABLED_PROPERTY))) {
			ResourceController.getResourceController().firePropertyChanged(FULLSCREEN_ENABLED_PROPERTY,
				    Boolean.toString(fullScreen), Boolean.toString(!fullScreen));
			final Controller controller = getController();
			for (int j = 0; j < 4; j++) {
				final Iterable<JComponent> toolBars = controller.getModeController().getUserInputListenerFactory()
				    .getToolBars(j);
				for (final JComponent toolBar : toolBars) {
					UIComponentVisibilityDispatcher.of(toolBar).resetVisible();
				}
			}
		}
	}

	private void setFullScreenOnNonMac(JFrame frame, final boolean fullScreen) {
		frame.getRootPane().putClientProperty(FULLSCREEN_ENABLED_PROPERTY, fullScreen);
		ResourceController.getResourceController().firePropertyChanged(FULLSCREEN_ENABLED_PROPERTY,
			    Boolean.toString(!fullScreen), Boolean.toString(fullScreen));
		final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		ToolTipManager.sharedInstance().setEnabled(false);
		final Controller controller = getController();
		Iterable<Window> visibleFrames = collectVisibleFrames(frame);
		if (fullScreen) {
			final GraphicsConfiguration graphicsConfiguration = frame.getGraphicsConfiguration();
			final Rectangle bounds = graphicsConfiguration.getBounds();
			frame.getRootPane().putClientProperty(FrameState.class,
			    new FrameState(frame.getBounds(), frame.getExtendedState()));
			frame.getExtendedState();
			frame.dispose();
			frame.setExtendedState(Frame.MAXIMIZED_BOTH);
			frame.setBounds(bounds);
			frame.setUndecorated(true);
			if(Compat.isWindowsOS())
			    frame.setResizable(false);
			setUIComponentsVisible(controller.getMapViewManager(), isMenubarVisible());
			for (int j = 0; j < 4; j++) {
				final Iterable<JComponent> toolBars = controller.getModeController().getUserInputListenerFactory()
				    .getToolBars(j);
				for (final JComponent toolBar : toolBars) {
					UIComponentVisibilityDispatcher.of(toolBar).resetVisible();
				}
			}
			showWindows(visibleFrames);
		}
		else {
			frame.dispose();
			frame.setUndecorated(false);
            if(Compat.isWindowsOS())
                frame.setResizable(false);
			frame.setResizable(true);
			FrameState frameState = (FrameState) frame.getRootPane().getClientProperty(FrameState.class);
			frame.setBounds(frameState.bounds);
			frame.setExtendedState(frameState.winState);
			setUIComponentsVisible(controller.getMapViewManager(), isMenubarVisible());
			for (int j = 0; j < 4; j++) {
				final Iterable<JComponent> toolBars = controller.getModeController().getUserInputListenerFactory()
				    .getToolBars(j);
				for (final JComponent toolBar : toolBars) {
					UIComponentVisibilityDispatcher.of(toolBar).resetVisible();
				}
			}
			showWindows(visibleFrames);
		}
		if (focusOwner != null)
			focusOwner.requestFocus();
	}

	private Collection<Window> collectVisibleFrames(Window window) {
		if (!window.isVisible())
			return Collections.emptyList();
		Window[] ownedWindows = window.getOwnedWindows();
		ArrayList<Window> visibleWindows = new ArrayList<>(ownedWindows.length + 1);
		visibleWindows.add(window);
		for (Window child : ownedWindows) {
			visibleWindows.addAll(collectVisibleFrames(child));
		}
		return visibleWindows;
	}

	protected void showWindows(final Iterable<Window> windows) {
		for (Window child : windows)
			child.setVisible(true);
	}

	public static void setLookAndFeel(final String lookAndFeel) {
		if(DARCULA_LAF_CLASS_NAME.equals(lookAndFeel)) {
			setLookAndFeel(FlatDarculaLaf.class.getName());
			return;
		}
		try {
            if (Compat.isMacOsX()) {
                try {
                    FrameController.class.getClassLoader().loadClass(VAQUA_LAF_CLASS_NAME);
                    UIManager.installLookAndFeel(VAQUA_LAF_NAME, VAQUA_LAF_CLASS_NAME);
                } catch (Exception e) {
                }
            }
            if (lookAndFeel.equals("default")) {
            	String lookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
            	UIManager.setLookAndFeel(lookAndFeelClassName);
            }
            else {
            	LookAndFeelInfo[] lafInfos = UIManager.getInstalledLookAndFeels();
				boolean lookAndFeelSet = false;
				for (LookAndFeelInfo lafInfo : lafInfos) {
					if (lafInfo.getName().equalsIgnoreCase(lookAndFeel)
							|| lafInfo.getClassName().equalsIgnoreCase(lookAndFeel)) {
						String lookAndFeelClassName = lafInfo.getClassName();
						lookAndFeelSet = tryToSetLookAndFeel(lookAndFeelClassName);
						break;
					}
				}
				if (!lookAndFeelSet) {
					final URLClassLoader userLibClassLoader = ClassLoaderFactory.getClassLoaderForUserLib();
					try {
						final Class<?> lookAndFeelClass = userLibClassLoader.loadClass(lookAndFeel);
						final ClassLoader uiClassLoader = lookAndFeelClass.getClassLoader();
						UIManager.getDefaults().put("ClassLoader", uiClassLoader);
						UIManager.setLookAndFeel((LookAndFeel) lookAndFeelClass.newInstance());
						if (userLibClassLoader != uiClassLoader)
							userLibClassLoader.close();
					}
					catch (ClassNotFoundException | ClassCastException | InstantiationException e) {
						LogUtils.warn("Error while setting Look&Feel" + lookAndFeel + ", reverted to default");
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
						Controller.getCurrentController().getResourceController().setProperty("lookandfeel", "default");
					}
				}
			}
		}
		catch (final Exception ex) {
			ex.printStackTrace();
			LogUtils.warn("Error while setting Look&Feel" + lookAndFeel);
		}
	}
	static {
	    UIManager.addPropertyChangeListener(new PropertyChangeListener() {
	        @Override
			public void propertyChange(PropertyChangeEvent event) {
	          if (event.getPropertyName().equals("lookAndFeel")) {
	        	  fixLookAndFeelUI();
	          }
	        }
	      });

	}
    private static void fixLookAndFeelUI(){
    	addHotKeysToMotifInputMaps();
    	configureFlatLookAndFeel();
		UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
		UIManager.put("ComboBox.squareButton", Boolean.FALSE);
		final ResourceController resourceController = ResourceController.getResourceController();
		if (!resourceController.getBooleanProperty("hugeFontsFixed2", false)) {
			resourceController.getProperties().remove("hugeFontsFixed");
			if ("100".equals(resourceController.getProperties().get(MENU_ITEM_FONT_SIZE_PROPERTY))) {
				resourceController.getProperties().remove(MENU_ITEM_FONT_SIZE_PROPERTY);
			}
			resourceController.setProperty("hugeFontsFixed2", true);
			resourceController.saveProperties();
		}
		int lookAndFeelDefaultMenuItemFontSize = obtainLookAndFeelDefaultMenuItemFontSize();
		final int defaultMenuItemSize = (int) Math.round(lookAndFeelDefaultMenuItemFontSize * DEFAULT_SCALING_FACTOR);
		resourceController.setDefaultProperty(MENU_ITEM_FONT_SIZE_PROPERTY, Long.toString(defaultMenuItemSize));
		final int userDefinedMenuItemFontSize = resourceController.getIntProperty(MENU_ITEM_FONT_SIZE_PROPERTY, defaultMenuItemSize);
		final double scalingFactor = ((double) userDefinedMenuItemFontSize) / lookAndFeelDefaultMenuItemFontSize;
		scaleDefaultUIFonts(scalingFactor);
		Object checkIcon =  UIManager.getDefaults().get("CheckBoxMenuItem.checkIcon");
		if(checkIcon instanceof Icon) {
			int checkIconHeight = new Quantity<>(userDefinedMenuItemFontSize * 2 / 3, LengthUnit.pt).toBaseUnitsRounded();
			UIManager.getDefaults().remove("CheckBoxMenuItem.checkIcon");
			UIManager.put("CheckBoxMenuItem.checkIcon", new CheckIconWithBorder((Icon)checkIcon, checkIconHeight, (int) (0.5 + 1.5 * UITools.FONT_SCALE_FACTOR)));
		}
		// Workaround for https://bugs.openjdk.java.net/browse/JDK-8134828
		// Scrollbar thumb disappears with Nimbus L&F
		// http://stackoverflow.com/questions/32857372/jscrollbar-dont-show-thumb-in-nimbus-lf
		final Dimension minimumThumbSize = new Dimension(30, 30);
		UIManager.getDefaults().put("ScrollBar.minimumThumbSize", minimumThumbSize);
		UIManager.put("ScrollBar.minimumThumbSize", minimumThumbSize);
		// Workaround for https://bugs.openjdk.java.net/browse/JDK-8179014
		UIManager.put("FileChooser.useSystemExtensionHiding", false);
		final Color color = UIManager.getColor("control");
		if (color != null && color.getAlpha() < 255)
			UIManager.getDefaults().put("control", Color.LIGHT_GRAY);
	}

	private static int obtainLookAndFeelDefaultMenuItemFontSize() {
		String[] fontsProperties = {"MenuItem.font", "defaultFont"};
		for(String fontProperty : fontsProperties) {
			UIManager.put(fontProperty, null);
			Font font = UIManager.getDefaults().getFont(fontProperty);
			if (font != null) {
				return font.getSize();
			}
		}
		return 12;
	}

    private static boolean tryToSetLookAndFeel(String lafClassName) {
        try {
            UIManager.setLookAndFeel(lafClassName);
            return true;
        } catch (Exception e) {
        }
        try {
            Class<?> lookAndFeelClass = FrameController.class.getClassLoader().loadClass(lafClassName);
            LookAndFeel lookAndFeelInstance = (LookAndFeel) lookAndFeelClass.getDeclaredConstructor().newInstance();
            UIManager.setLookAndFeel(lookAndFeelInstance);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

	private static void configureFlatLookAndFeel() {
		if(Compat.isApplet())
			return;
        if(UIManager.getLookAndFeel() instanceof FlatLaf) {
        	UIManager.put("TableHeader.height", 2);
        	UIManager.put("Table.showHorizontalLines", true);
        	UIManager.put("Table.showVerticalLines", true);
        	UIManager.put("ComboBox.minimumWidth", 2);
        	UIManager.put("TabbedPane.tabsOverlapBorder", false);
        	UIManager.put("EditorPaneUI", NonSelectingFlatEditorPaneUI.class.getName());
         }
        else if(NonSelectingFlatEditorPaneUI.class.getName().equals(UIManager.get("EditorPaneUI"))){
        	UIManager.put("EditorPaneUI", BasicEditorPaneUI.class.getName());
        }
	}

	private static void addHotKeysToMotifInputMaps() {
        if(UIManager.getLookAndFeel().getClass().getName().equals(MOTIF_LAF__CLASS_NAME)) {
            UIDefaults uiDefaults = UIManager.getDefaults();
            uiDefaults.replaceAll((k, v) -> replaceMotifLazyInputMaps(k, v));
         }
    }

	private static Map<String, KeyStroke> keystrokes = new HashMap<>();

    private static Object replaceMotifLazyInputMaps(Object k, Object v) {
        if(!(v instanceof UIDefaults.LazyInputMap))
            return v;
        return new UIDefaults.LazyValue() {
            @Override
            public Object createValue(UIDefaults table) {
                 Object value = ((UIDefaults.LazyInputMap) v).createValue(table);
                 if (! (value instanceof InputMap))
                     return value;
                 InputMap inputMap = (InputMap) value;
                 KeyStroke keyStrokeControlC = keystrokes.computeIfAbsent("control C", KeyStroke::getKeyStroke);
                 if(inputMap.get(keyStrokeControlC) != null)
                     return value;
                 KeyStroke keyStrokeCopy = keystrokes.computeIfAbsent("COPY", KeyStroke::getKeyStroke);
                 Object copyValue = inputMap.get(keyStrokeCopy);
                 if(copyValue == null)
                     return value;
                 inputMap.put(keyStrokeControlC, copyValue);
                 KeyStroke keyStrokePaste = keystrokes.computeIfAbsent("PASTE", KeyStroke::getKeyStroke);
                 KeyStroke keyStrokeControlV = keystrokes.computeIfAbsent("control V", KeyStroke::getKeyStroke);
                 inputMap.put(keyStrokeControlV, inputMap.get(keyStrokePaste));
                 KeyStroke keyStrokeCut = keystrokes.computeIfAbsent("CUT", KeyStroke::getKeyStroke);
                 KeyStroke keyStrokeControlX = keystrokes.computeIfAbsent("control X", KeyStroke::getKeyStroke);
                 inputMap.put(keyStrokeControlX, inputMap.get(keyStrokeCut));
                 return inputMap;
           }
        };
    }

	private static void scaleDefaultUIFonts(double scalingFactor) {
		final UIDefaults uiDefaults = UIManager.getDefaults();
		UIDefaults lookAndFeelDefaults = UIManager.getLookAndFeel().getDefaults();
		boolean shouldUseWorkaroundForJava11 = Compat.isJavaVersionLessThan(Compat.JAVA_VERSION_15);
		Set<Object> keySet = (shouldUseWorkaroundForJava11 ?lookAndFeelDefaults : uiDefaults).keySet();
		Map<Font, Void> scaledFonts = new IdentityHashMap<>();
		Object[] keys = keySet.toArray(new Object[keySet.size()]);
		for (Object key : keys) {
			if (isFontKey(key)) {
				UIManager.put(key, null);
				Font font = uiDefaults.getFont(key);
				if (font != null && ! scaledFonts.containsKey(font)) {
					font = UITools.scaleFontInt(font, scalingFactor);
					lookAndFeelDefaults.put(key, font);
					uiDefaults.put(key, font);
					UIManager.put(key, font);
					scaledFonts.put(font, null);
				}
			}
		}
	}

	private static boolean isFontKey(Object key) {
		return key != null && key.toString().toLowerCase().endsWith("font");
	}

	@Override
	public void addObjectTypeInfo(Object value) {
		if (value instanceof FormattedObject) {
			value = ((FormattedObject) value).getObject();
		}
		if (value instanceof String || value instanceof StyleTranslatedObject || value instanceof StyleString) {
			addStatusInfo(ResourceController.OBJECT_TYPE, null, textIcon);
		}
		else if (value instanceof FormattedDate) {
			final FormattedDate fd = (FormattedDate) value;
			if (fd.containsTime()) {
				addStatusInfo(ResourceController.OBJECT_TYPE, null, dateTimeIcon);
			}
			else {
				addStatusInfo(ResourceController.OBJECT_TYPE, null, dateIcon);
			}
		}
		else if (value instanceof Number) {
			addStatusInfo(ResourceController.OBJECT_TYPE, null, numberIcon);
		}
		else if (value instanceof Hyperlink || value instanceof URI) {
			addStatusInfo(ResourceController.OBJECT_TYPE, null, linkIcon);
		}
		else {
			addStatusInfo(ResourceController.OBJECT_TYPE, null, unknownTypeIcon);
		}
	}

	public static ComboBoxEditor getTextDateTimeEditor() {
		final ContainerComboBoxEditor editor = new ContainerComboBoxEditor();
		final TranslatedObject keyText = new TranslatedObject("text", "1Ab");
		final BasicComboBoxEditor textEditor = new FixedBasicComboBoxEditor() {
			private Object oldItem;

			@Override
			public void setItem(Object object) {
				oldItem = object;
				if (object instanceof FormattedDate)
					super.setItem("");
				else
					super.setItem(object);
			}

			@Override
			public Object getItem() {
				final Object item = super.getItem();
				final Object oldItem = this.oldItem;
				this.oldItem = null;
				if (item != null && oldItem != null && item.toString().equals(oldItem.toString()))
					return oldItem;
				if (ResourceController.getResourceController().getBooleanProperty("parse_data")
				        && item instanceof String) {
					final Object scannedObject = ScannerController.getController().parse((String) item);
					return scannedObject;
				}
				return item;
			}
		};
		editor.put(keyText, textEditor);
		final TranslatedObject keyDate = new TranslatedObject("date", "");
		keyDate.setIcon(dateIcon);
		final TimeComboBoxEditor dateComboBoxEditor = new TimeComboBoxEditor(false) {
			@Override
			public void setItem(Object object) {
				if (object instanceof FormattedDate && !((FormattedDate) object).containsTime())
					super.setItem(object);
				else
					super.setItem(null);
			}
		};
		dateComboBoxEditor.setItem();
		editor.put(keyDate, dateComboBoxEditor);
		final TranslatedObject keyDateTime = new TranslatedObject("date_time", "");
		keyDateTime.setIcon(dateTimeIcon);
		final TimeComboBoxEditor dateTimeComboBoxEditor = new TimeComboBoxEditor(true) {
			@Override
			public void setItem(Object object) {
				if (object instanceof FormattedDate && ((FormattedDate) object).containsTime())
					super.setItem(object);
				else
					super.setItem(null);
			}
		};
		dateTimeComboBoxEditor.setItem();
		editor.put(keyDateTime, dateTimeComboBoxEditor);
		return editor;
	}

	@Override
	public boolean quit() {
	    final JComponent mapViewComponent = mapViewManager.getMapViewComponent();
		final boolean allMapsSaved = mapViewManager.saveAllModifiedMaps();
		if (allMapsSaved)
		    mapViewManager.onQuitApplication();
		mapViewManager.changeToMapView(mapViewComponent);
		return allMapsSaved;
	}

	@Override
	public boolean isDispatchThread() {
		return EventQueue.isDispatchThread();
	}

	@Override
	public ExecutorService getMainThreadExecutorService() {
		return EventQueueExecutorServiceAdapter.INSTANCE;
	}

	@Override
	public void invokeLater(Runnable runnable) {
		EventQueue.invokeLater(runnable);
	}

	@Override
	public void invokeAndWait(Runnable runnable) throws InterruptedException, InvocationTargetException {
		StaticInvoker.invokeAndWait(runnable);
	}

	static private class StaticInvoker {
		private static void invokeAndWait(Runnable runnable) throws InterruptedException, InvocationTargetException {
			if(EventQueue.isDispatchThread())
				runnable.run();
			else
				EventQueue.invokeAndWait(runnable);
		}
	}

	@Override
	public List<? extends Component> getMapViewVector() {
		return mapViewManager.getMapViewVector();
	}
}
