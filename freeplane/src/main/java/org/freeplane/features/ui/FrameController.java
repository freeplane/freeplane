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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ComboBoxEditor;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.RootPaneContainer;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.ui.FixedBasicComboBoxEditor;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.components.ContainerComboBoxEditor;
import org.freeplane.core.ui.components.FixDarculaToggleButtonUI;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.components.resizer.UIComponentVisibilityDispatcher;
import org.freeplane.core.util.ClassLoaderFactory;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.format.FormattedDate;
import org.freeplane.features.format.FormattedObject;
import org.freeplane.features.format.ScannerController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.styles.StyleTranslatedObject;
import org.freeplane.features.time.TimeComboBoxEditor;

/**
 * @author Dimitry Polivaev
 */
abstract public class FrameController implements ViewController {
	private static final String AQUA_LAF_NAME = "org.violetlib.aqua.AquaLookAndFeel.AquaLookAndFeel";
    private static final String DARCULA_LAF_NAME = "com.bulenkov.darcula.DarculaLaf";
	private static final double DEFAULT_SCALING_FACTOR = 0.8;

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
	static private void initializeUiResources(){
		if(uiResourcesInitialized == false) {
			uiResourcesInitialized = true;
			final ResourceController resourceController = ResourceController.getResourceController();
			textIcon = resourceController.getIcon("text_icon");
			numberIcon = resourceController.getIcon("number_icon");
			dateIcon = resourceController.getIcon("date_icon");
			dateTimeIcon = resourceController.getIcon("date_time_icon");
			linkIcon = resourceController.getIcon("link_icon");
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
		UIComponentVisibilityDispatcher.install(statusPanel, propertyKeyPrefix + "toolbarVisible");
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
	abstract public boolean isApplet();

	@Override
	public boolean isMenubarVisible() {
		return isComponentVisible("menubar");
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

	boolean isFullScreenEnabled() {
		return isFullScreenEnabled(getCurrentRootComponent());
	}

	boolean isFullScreenEnabled(final Component currentRootComponent) {
		return currentRootComponent instanceof Frame && !((Frame) currentRootComponent).isResizable();
	}

	@Override
	abstract public void openDocument(URI uri) throws IOException;

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
			final int index = UITools.getComponentIndex(component);
			statusPanel.remove(index);
			statusPanel.add(component, index);
		}
	}

	@Override
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
						toolbarPanel[j].add(toolBar, i++);
					}
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
		final JFrame frame = (JFrame) getCurrentRootComponent();
		final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		if (fullScreen == isFullScreenEnabled()) {
			return;
		}
		ToolTipManager.sharedInstance().setEnabled(false);
		final Controller controller = getController();
		ResourceController.getResourceController().firePropertyChanged(FULLSCREEN_ENABLED_PROPERTY,
		    Boolean.toString(!fullScreen), Boolean.toString(fullScreen));
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
		ToolTipManager.sharedInstance().setEnabled(true);
		if (focusOwner != null)
			focusOwner.requestFocus();
	}

	private Collection<Window> collectVisibleFrames(Window window) {
		if (!window.isVisible())
			return Collections.emptyList();
		Window[] ownedWindows = window.getOwnedWindows();
		ArrayList<Window> visibleWindows = new ArrayList(ownedWindows.length + 1);
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

	public static void setLookAndFeel(final String lookAndFeel, boolean supportHidpi) {
		try {
			if (lookAndFeel.equals("default")) {
			    boolean lookAndFeelSet = false;
			    if (Compat.isMacOsX()) {
			        try {
                        Class<?> lookAndFeelClass = FrameController.class.getClassLoader().loadClass(AQUA_LAF_NAME);
                        UIManager.setLookAndFeel((LookAndFeel) lookAndFeelClass.getDeclaredConstructor().newInstance());
                        lookAndFeelSet = true;
                    } catch (Exception e) {
                    }
			    }
			    if(! lookAndFeelSet) {
			        String lookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
			        fixDarculaNPE(lookAndFeelClassName);
			        UIManager.setLookAndFeel(lookAndFeelClassName);
			        fixDarculaButtonUI();
			    }
			}
			else {
				LookAndFeelInfo[] lafInfos = UIManager.getInstalledLookAndFeels();
				boolean setLnF = false;
				for (LookAndFeelInfo lafInfo : lafInfos) {
					if (lafInfo.getName().equalsIgnoreCase(lookAndFeel)) {
						String lookAndFeelClassName = lafInfo.getClassName();
						fixDarculaNPE(lookAndFeelClassName);
						UIManager.setLookAndFeel(lookAndFeelClassName);
						fixDarculaButtonUI();
						setLnF = true;
						break;
					}
				}
				if (!setLnF) {
					final URLClassLoader userLibClassLoader = ClassLoaderFactory.getClassLoaderForUserLib();
					try {
						final Class<?> lookAndFeelClass = userLibClassLoader.loadClass(lookAndFeel);
						fixDarculaNPE(lookAndFeelClass.getName());
						UIManager.setLookAndFeel((LookAndFeel) lookAndFeelClass.newInstance());
						fixDarculaButtonUI();
						final ClassLoader uiClassLoader = lookAndFeelClass.getClassLoader();
						if (userLibClassLoader != uiClassLoader)
							userLibClassLoader.close();
						UIManager.getDefaults().put("ClassLoader", uiClassLoader);
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
		UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
		UIManager.put("ComboBox.squareButton", Boolean.FALSE);
		final ResourceController resourceController = ResourceController.getResourceController();
		if (!resourceController.getBooleanProperty("hugeFontsFixed", false)) {
			if ("100".equals(resourceController.getProperties().get(UITools.MENU_ITEM_FONT_SIZE_PROPERTY))) {
				resourceController.getProperties().remove(UITools.MENU_ITEM_FONT_SIZE_PROPERTY);
			}
			resourceController.setProperty("hugeFontsFixed", true);
		}
		int lookAndFeelDefaultMenuItemFontSize = getLookAndFeelDefaultMenuItemFontSize();
		final long defaultMenuItemSize = Math.round(lookAndFeelDefaultMenuItemFontSize * DEFAULT_SCALING_FACTOR);
		resourceController.setDefaultProperty(UITools.MENU_ITEM_FONT_SIZE_PROPERTY, Long.toString(defaultMenuItemSize));
		if (supportHidpi) {
			double scalingFactor = calculateFontSizeScalingFactor(lookAndFeelDefaultMenuItemFontSize);
			scaleDefaultUIFonts(scalingFactor);
		}
		// Workaround for https://bugs.openjdk.java.net/browse/JDK-8134828
		// Scrollbar thumb disappears with Nimbus L&F
		// http://stackoverflow.com/questions/32857372/jscrollbar-dont-show-thumb-in-nimbus-lf
		final Dimension minimumThumbSize = new Dimension(30, 30);
		UIManager.getLookAndFeelDefaults().put("ScrollBar.minimumThumbSize", minimumThumbSize);
		UIManager.put("ScrollBar.minimumThumbSize", minimumThumbSize);
		// Workaround for https://bugs.openjdk.java.net/browse/JDK-8179014
		UIManager.put("FileChooser.useSystemExtensionHiding", false);
		final Color color = UIManager.getColor("control");
		if (color != null && color.getAlpha() < 255)
			UIManager.getDefaults().put("control", Color.LIGHT_GRAY);
	}

	private static void fixDarculaNPE(String lookAndFeelClassName) throws UnsupportedLookAndFeelException {
		if(lookAndFeelClassName.equals(DARCULA_LAF_NAME))
			UIManager.setLookAndFeel(new MetalLookAndFeel());
	}

	private static void fixDarculaButtonUI(){
		if(UIManager.getLookAndFeel().getClass().getName().equals(DARCULA_LAF_NAME)) {
			UIManager.put("ToggleButtonUI", FixDarculaToggleButtonUI.class.getName());
			UIManager.put("Button.darcula.selection.color1", ColorUtils.rgbStringToColor("#687f88"));
			UIManager.put("Button.darcula.selection.color2", ColorUtils.rgbStringToColor("#436188"));
		}
	}

	private static int getLookAndFeelDefaultMenuItemFontSize() {
		int lookAndFeelDefaultMenuItemFontSize = 10;
		Font uiDefaultMenuItemFont = UIManager.getDefaults().getFont("MenuItem.font");
		if (uiDefaultMenuItemFont != null) {
			lookAndFeelDefaultMenuItemFontSize = uiDefaultMenuItemFont.getSize();
		}
		return lookAndFeelDefaultMenuItemFontSize;
	}

	final private static int UNKNOWN = -1;

	private static double calculateFontSizeScalingFactor(int lookAndFeelDefaultMenuItemFontSize) {
		final ResourceController resourceController = ResourceController.getResourceController();
		final int userDefinedMenuItemFontSize = resourceController.getIntProperty(UITools.MENU_ITEM_FONT_SIZE_PROPERTY,
		    UNKNOWN);
		final double scalingFactor;
		if (userDefinedMenuItemFontSize == UNKNOWN) {
			scalingFactor = DEFAULT_SCALING_FACTOR;
		}
		else {
			scalingFactor = ((double) userDefinedMenuItemFontSize) / lookAndFeelDefaultMenuItemFontSize;
		}
		return scalingFactor;
	}

	private static void scaleDefaultUIFonts(double scalingFactor) {
		Set<Object> keySet = UIManager.getLookAndFeelDefaults().keySet();
		Object[] keys = keySet.toArray(new Object[keySet.size()]);
		final UIDefaults uiDefaults = UIManager.getDefaults();
		final UIDefaults lookAndFeelDefaults = UIManager.getLookAndFeel().getDefaults();
		for (Object key : keys) {
			if (isFontKey(key)) {
				Font font = uiDefaults.getFont(key);
				if (font != null) {
					font = UITools.scaleFontInt(font, scalingFactor);
					UIManager.put(key, font);
					lookAndFeelDefaults.put(key, font);
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
		if (value instanceof String || value instanceof StyleTranslatedObject) {
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
		else if (value instanceof URI) {
			addStatusInfo(ResourceController.OBJECT_TYPE, null, linkIcon);
		}
		else {
			addStatusInfo(ResourceController.OBJECT_TYPE, null, null);
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
		final Controller controller = Controller.getCurrentController();
		controller.selectMode(MModeController.MODENAME);
		final boolean allMapsClosed = controller.closeAllMaps();
		if (allMapsClosed)
			getController().getMapViewManager().onQuitApplication();
		return allMapsClosed;
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
		if(isDispatchThread())
			runnable.run();
		else
			EventQueue.invokeAndWait(runnable);
	}

	@Override
	public List<? extends Component> getMapViewVector() {
		return mapViewManager.getMapViewVector();
	}
}
