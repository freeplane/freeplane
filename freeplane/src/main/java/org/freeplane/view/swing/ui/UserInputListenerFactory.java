/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.view.swing.ui;

import java.awt.Component;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionAcceleratorManager;
import org.freeplane.core.ui.IKeyStrokeProcessor;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.IMouseWheelEventHandler;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.FreeplaneResourceAccessor;
import org.freeplane.core.ui.menubuilders.XmlEntryStructureBuilder;
import org.freeplane.core.ui.menubuilders.action.EntriesForAction;
import org.freeplane.core.ui.menubuilders.generic.BuildPhaseListener;
import org.freeplane.core.ui.menubuilders.generic.BuildProcessFactory;
import org.freeplane.core.ui.menubuilders.generic.BuilderDestroyerPair;
import org.freeplane.core.ui.menubuilders.generic.ChildActionEntryRemover;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase;
import org.freeplane.core.ui.menubuilders.generic.RecursiveMenuStructureProcessor;
import org.freeplane.core.ui.menubuilders.generic.SubtreeProcessor;
import org.freeplane.core.ui.menubuilders.generic.UserRole;
import org.freeplane.core.ui.menubuilders.menu.MenuAcceleratorChangeListener;
import org.freeplane.core.ui.menubuilders.menu.MenuBuildProcessFactory;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.ui.ViewController;
import org.freeplane.view.swing.map.MapView;

public class UserInputListenerFactory implements IUserInputListenerFactory {
	private final class ActionEnabler implements IMapSelectionListener, IFreeplanePropertyListener {
		private final ModeController modeController;
		private UserRole userRole = null;

		private ActionEnabler(ModeController modeController) {
			this.modeController = modeController;
		}

		@Override
		public void afterMapChange(final MapModel oldMap, final MapModel newMap) {
			if (modeController.equals(Controller.getCurrentModeController())) {
				updateActions(newMap);
			}
		}


		@Override
		public void propertyChanged(String propertyName, String newValue, String oldValue) {
			if(ModeController.USER_INTERFACE_PROPERTIES.contains(propertyName))
				updateActions(modeController.getController().getMap());
		}

		private void updateActions(final MapModel map) {
			UserRole newUserRole = modeController.userRole(map);
			if(newUserRole != userRole) {
				userRole = newUserRole;
				final RecursiveMenuStructureProcessor recursiveMenuStructureProcessor = new RecursiveMenuStructureProcessor();
				recursiveMenuStructureProcessor.setDefaultBuilder(new EntryVisitor() {
					EntryAccessor entryAccessor = new EntryAccessor();

					@Override
					public void visit(Entry entry) {
						final AFreeplaneAction action = entryAccessor.getAction(entry);
						if (action != null) {
							action.afterMapChange( userRole, map != null);
						}
                        entry.removeAttribute("allowed");
                        entry.setAttribute("allowed", entry.isAllowed(userRole));
					}

					@Override
					public boolean shouldSkipChildren(Entry entry) {
						return false;
					}
				});
				recursiveMenuStructureProcessor.build(genericMenuStructure);
			}
		}
	}

	public static final String NODE_POPUP = "/node_popup";
// // 	final private Controller controller;
	private IMouseListener mapMouseListener;
	private MouseWheelListener mapMouseWheelListener;
	private JPopupMenu mapsPopupMenu;
	private FreeplaneMenuBar menuBar;
	final private HashSet<IMouseWheelEventHandler> mRegisteredMouseWheelEventHandler = new HashSet<IMouseWheelEventHandler>();
	private DragGestureListener nodeDragListener;
	private DropTargetListener nodeDropTargetListener;
	private KeyListener nodeKeyListener;
	private IMouseListener nodeMotionListener;
	private IMouseListener nodeMouseMotionListener;
	private MouseWheelListener nodeMouseWheelListener;
	private JPopupMenu nodePopupMenu;
	private final Map<String, JComponent> toolBars;
	private final List<JComponent>[] toolbarLists;
	final private List<Map<String, BuilderDestroyerPair>> customBuilders;
	final private List<BuildPhaseListener> buildPhaseListeners;
	private Entry genericMenuStructure;
	private SubtreeProcessor subtreeBuilder;
	final private ModeController modeController;
	final static private IKeyStrokeProcessor DEFAULT_PROCESSOR = new IKeyStrokeProcessor() {
		@Override
		public boolean processKeyBinding(KeyStroke ks, KeyEvent e) {
			return ResourceController.getResourceController().getAcceleratorManager().processKeyBinding(ks, e);
		}
	};
	final private IKeyStrokeProcessor delegateProcessor = new IKeyStrokeProcessor() {
		@Override
		public boolean processKeyBinding(KeyStroke ks, KeyEvent e) {
			return keyEventProcessor.processKeyBinding(ks, e);
		}
	};
	private IKeyStrokeProcessor keyEventProcessor;

	public UserInputListenerFactory(final ModeController modeController) {
		this.modeController = modeController;
		customBuilders = new ArrayList<Map<String, BuilderDestroyerPair>>(Phase.values().length);
		keyEventProcessor = DEFAULT_PROCESSOR;
		buildPhaseListeners = new ArrayList<BuildPhaseListener>();
		for (@SuppressWarnings("unused")
		Phase phase : Phase.values()) {
			customBuilders.add(new HashMap<String, BuilderDestroyerPair>());
		}
		Controller controller = Controller.getCurrentController();
		ActionEnabler actionEnabler = new ActionEnabler(modeController);
		controller.getMapViewManager().addMapSelectionListener(actionEnabler);
		ResourceController.getResourceController().addPropertyChangeListener(actionEnabler);

		addUiBuilder(Phase.ACTIONS, "navigate_maps", new BuilderDestroyerPair(new EntryVisitor() {
			@Override
			public void visit(Entry target) {
				createMapActions(target);
			}

			@Override
			public boolean shouldSkipChildren(Entry entry) {
				return true;
			}
		}, new ChildActionEntryRemover(modeController)));

		addUiBuilder(Phase.ACTIONS, "navigate_modes", new BuilderDestroyerPair(new EntryVisitor() {
			@Override
			public void visit(Entry target) {
				createModeActions(target);
			}

			@Override
			public boolean shouldSkipChildren(Entry entry) {
				return true;
			}
		}, new ChildActionEntryRemover(modeController)));

		toolBars = new LinkedHashMap<String, JComponent>();
		toolbarLists = newListArray();
		for (int j = 0; j < 4; j++) {
			toolbarLists[j] = new LinkedList<JComponent>();
		}
	}

	public <T> T getMenu(Class<T> clazz) {
		return null;
	}


	public void setKeyEventProcessor(IKeyStrokeProcessor keyEventProcessor) {
		this.keyEventProcessor = keyEventProcessor;
	}

	// isolate unchecked stuff in this method
	@SuppressWarnings("unchecked")
	private List<JComponent>[] newListArray() {
		return new List[4];
	}

	@Override
	public void addToolBar(final String name, final int position, final JComponent toolBar) {
		toolBars.put(name, toolBar);
		toolbarLists[position].add(toolBar);
	}

	@Override
	public void addMouseWheelEventHandler(final IMouseWheelEventHandler handler) {
		mRegisteredMouseWheelEventHandler.add(handler);
	}

	@Override
	public IMouseListener getMapMouseListener() {
		if (mapMouseListener == null) {
			mapMouseListener = new DefaultMapMouseListener();
		}
		return mapMouseListener;
	}

	@Override
	public MouseWheelListener getMapMouseWheelListener() {
		if (mapMouseWheelListener == null) {
			mapMouseWheelListener = new DefaultMouseWheelListener();
		}
		return mapMouseWheelListener;
	}

	@Override
	public JPopupMenu getMapPopup() {
		return mapsPopupMenu;
	}

	@Override
	public FreeplaneMenuBar getMenuBar() {
		if (menuBar == null) {
			menuBar = new FreeplaneMenuBar(delegateProcessor);
		}
		return menuBar;
	}

	@Override
	public Set<IMouseWheelEventHandler> getMouseWheelEventHandlers() {
		return Collections.unmodifiableSet(mRegisteredMouseWheelEventHandler);
	}

	@Override
	public DragGestureListener getNodeDragListener() {
		return nodeDragListener;
	}

	@Override
	public DropTargetListener getNodeDropTargetListener() {
		return nodeDropTargetListener;
	}

	@Override
	public KeyListener getNodeKeyListener() {
		if (nodeKeyListener == null) {
			nodeKeyListener = new DefaultNodeKeyListener(null);
		}
		return nodeKeyListener;
	}

	public IMouseListener getNodeMotionListener() {
		return nodeMotionListener;
	}

	@Override
	public IMouseListener getNodeMouseMotionListener() {
		if (nodeMouseMotionListener == null) {
			nodeMouseMotionListener = new DefaultNodeMouseMotionListener();
		}
		return nodeMouseMotionListener;
	}

	@Override
	public MouseWheelListener getNodeMouseWheelListener() {
		if (nodeMouseWheelListener == null) {
			nodeMouseWheelListener = new DefaultNodeMouseWheelListener(getMapMouseWheelListener());
		}
		return nodeMouseWheelListener;
	}

	@Override
	public JPopupMenu getNodePopupMenu() {
		return nodePopupMenu;
	}

	@Override
	public JComponent getToolBar(final String name) {
		return toolBars.get(name);
	}

	@Override
	public Iterable<JComponent> getToolBars(final int position) {
		return toolbarLists[position];
	}

	@Override
	public void removeMouseWheelEventHandler(final IMouseWheelEventHandler handler) {
		mRegisteredMouseWheelEventHandler.remove(handler);
	}

	public void setMapMouseListener(final IMouseListener mapMouseMotionListener) {
		if (mapMouseListener != null) {
			throw new RuntimeException("already set");
		}
		mapMouseListener = mapMouseMotionListener;
	}

	public void setMapMouseWheelListener(final MouseWheelListener mouseWheelListener) {
		if (mapMouseWheelListener != null) {
			throw new RuntimeException("already set");
		}
		mapMouseWheelListener = mouseWheelListener;
	}

	public void setMenuBar(final FreeplaneMenuBar menuBar) {
		if (mapMouseWheelListener != null) {
			throw new RuntimeException("already set");
		}
		this.menuBar = menuBar;
	}


	public void setNodeDragListener(DragGestureListener nodeDragListener) {
		if (this.nodeDragListener != null) {
			throw new RuntimeException("already set");
		}
    	this.nodeDragListener = nodeDragListener;
    }

	public void setNodeDropTargetListener(final DropTargetListener nodeDropTargetListener) {
		if (this.nodeDropTargetListener != null) {
			throw new RuntimeException("already set");
		}
		this.nodeDropTargetListener = nodeDropTargetListener;
	}

	public void setNodeKeyListener(final KeyListener nodeKeyListener) {
		if (this.nodeKeyListener != null) {
			throw new RuntimeException("already set");
		}
		this.nodeKeyListener = nodeKeyListener;
	}

	public void setNodeMotionListener(final IMouseListener nodeMotionListener) {
		if (this.nodeMotionListener != null) {
			throw new RuntimeException("already set");
		}
		this.nodeMotionListener = nodeMotionListener;
	}

	public void setNodeMouseMotionListener(final IMouseListener nodeMouseMotionListener) {
		if (this.nodeMouseMotionListener != null) {
			throw new RuntimeException("already set");
		}
		this.nodeMouseMotionListener = nodeMouseMotionListener;
	}

	public void setNodeMouseWheelListener(MouseWheelListener nodeMouseWheelListener) {
		if (this.nodeMouseWheelListener != null) {
			throw new RuntimeException("already set");
		}
		this.nodeMouseWheelListener = nodeMouseWheelListener;
	}

	public void setNodePopupMenu(final JPopupMenu nodePopupMenu) {
		if (this.nodePopupMenu != null) {
			throw new RuntimeException("already set");
		}
		this.nodePopupMenu = nodePopupMenu;
	}

	@Override
	public void updateMapList() {
		for (Entry entry : mapMenuEntries.keySet())
			rebuildMenu(entry);
	}

	final private Map<Entry, Entry> mapMenuEntries = new IdentityHashMap<Entry, Entry>();

	private void createModeActions(final Entry modesMenuEntry) {
		rebuildMenuOnMapChange(modesMenuEntry);
		Controller controller = Controller.getCurrentController();
		EntryAccessor entryAccessor = new EntryAccessor();
		for (final String key : new LinkedList<String>(controller.getModes())) {
			final AFreeplaneAction modesMenuAction = new ModesMenuAction(key, controller);
			modeController.addActionIfNotAlreadySet(modesMenuAction);
			Entry actionEntry = new Entry();
			entryAccessor.setAction(actionEntry, modesMenuAction);
			actionEntry.setName(modesMenuAction.getKey());
			final ModeController modeController = controller.getModeController();
			if (modeController != null && modeController.getModeName().equals(key)) {
				actionEntry.setAttribute("selected", true);
			}
			modesMenuEntry.addChild(actionEntry);
			ResourceController.getResourceController().getProperty(("keystroke_mode_" + key));
		}
	}

	private void createMapActions(final Entry mapsMenuEntry) {
		final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
		final ViewController viewController = Controller.getCurrentController().getViewController();
		final List<? extends Component> mapViewVector = viewController.getMapViewVector();
		if (mapViewVector == null) {
			return;
		}
		EntryAccessor entryAccessor = new EntryAccessor();
		for (final Component mapView : mapViewVector) {
			final String displayName = mapView.getName();
			Entry actionEntry = new Entry();
			final MapsMenuAction action = new MapsMenuAction(displayName);
			actionEntry.setName(action.getKey());
			modeController.addActionIfNotAlreadySet(action);
			entryAccessor.setAction(actionEntry, action);
			final MapView currentMapView = (MapView) mapViewManager.getMapViewComponent();
			if (currentMapView != null) {
				if (mapView == currentMapView) {
					actionEntry.setAttribute("selected", true);
				}
			}
			mapsMenuEntry.addChild(actionEntry);
		}
	}

	private void rebuildMenuOnMapChange(final Entry entry) {
	    Entry menuEntry;
		for (menuEntry = entry.getParent(); //
		menuEntry.getName().isEmpty(); //
		menuEntry = menuEntry.getParent());
		mapMenuEntries.put(menuEntry, null);
    }

	@Override
	public void rebuildMenu(Entry entry){
		if (subtreeBuilder != null)
			subtreeBuilder.rebuildChildren(entry);
	}

	@Override
	public void rebuildMenus(String name) {
		if(genericMenuStructure != null) {
    		final List<Entry> entries = genericMenuStructure.findEntries(name);
    		for (Entry entry : entries)
    			rebuildMenu(entry);
		}
	}

	@Override
	public void updateMenus(String menuStructureResource, Set<String> plugins) {
		mapsPopupMenu = new JPopupMenu();
		mapsPopupMenu.setName(TextUtils.getText("mindmaps"));

		final URL genericStructure = ResourceController.getResourceController().getResource(
		    menuStructureResource);
		try {
			final FreeplaneResourceAccessor resourceAccessor = new FreeplaneResourceAccessor();
			final EntriesForAction entries = new EntriesForAction();
			final ActionAcceleratorManager acceleratorManager = ResourceController.getResourceController().getAcceleratorManager();
			final BuildProcessFactory buildProcessFactory = new MenuBuildProcessFactory(this, modeController,
			    resourceAccessor, acceleratorManager, entries, buildPhaseListeners);
			final PhaseProcessor buildProcessor = buildProcessFactory.getBuildProcessor();
			subtreeBuilder = buildProcessFactory.getChildProcessor();
			acceleratorManager.addAcceleratorChangeListener(modeController, new MenuAcceleratorChangeListener(entries));
			for (final Phase phase : Phase.values())
				for (java.util.Map.Entry<String, BuilderDestroyerPair> entry : customBuilders.get(phase.ordinal())
				    .entrySet())
					buildProcessor.phase(phase).addBuilderPair(entry.getKey(), entry.getValue());
			final InputStream resource = genericStructure.openStream();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8));
			genericMenuStructure = XmlEntryStructureBuilder.buildMenuStructure(reader);
			filterPlugins(genericMenuStructure, plugins);
			buildProcessor.build(genericMenuStructure);
			if(Boolean.getBoolean("org.freeplane.outputUnusedActions"))
				outputUnusedActions();
		}
		catch (Exception e) {
			final boolean isUserDefined = isUserDefined(genericStructure);
			if (isUserDefined) {
				LogUtils.warn(e);
				String myMessage = TextUtils.format("menu_error", genericStructure.getPath(), e.getMessage());
				UITools.backOtherWindows();
				JOptionPane.showMessageDialog(UITools.getMenuComponent(), myMessage, "Freeplane", JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
			throw new RuntimeException("Error in menu resource " + menuStructureResource, e);
		}

	}

	private boolean isUserDefined(final URL location){
		try {
			if(location == null || ! location.getProtocol().equalsIgnoreCase("file"))
				return false;
			String freeplaneUserDirectory = ResourceController.getResourceController().getFreeplaneUserDirectory();
			if(freeplaneUserDirectory == null)
				return false;
			String userResourcesUrl = Compat.fileToUrl(new File(freeplaneUserDirectory, "resources")).getPath();
			return location.getPath().startsWith(userResourcesUrl);
		} catch (MalformedURLException e) {
			return false;
		}
	}

	private void filterPlugins(Entry entry, Set<String> plugins) {
		final Iterator<Entry> iterator = entry.children().iterator();
		while(iterator.hasNext()){
			final Entry child = iterator.next();
			final Object plugin = child.getAttribute("plugin");
			if(plugin != null && ! plugins.contains(plugin))
				iterator.remove();
			else
				filterPlugins(child, plugins);
		}
	}

	private void outputUnusedActions() {
		StringBuilder sb = new StringBuilder();
		sb.append("Unused actions for mode ").append(modeController.getModeName()).append('\n');
		TreeSet<String> actionKeys = new TreeSet<String>();
		actionKeys.addAll(modeController.getActionKeys());
		actionKeys.addAll(modeController.getController().getActionKeys());
		KEYS: for(String key : actionKeys){
			final List<Entry> entries = genericMenuStructure.findEntries(key);
			for(Entry entry : entries)
				if(new EntryAccessor().getComponent(entry) != null)
					continue KEYS;
			sb.append(key).append('\n');
		}
		LogUtils.info(sb.toString());

	}

	@Override
	public void addUiBuilder(Phase phase, String name, BuilderDestroyerPair builderDestroyerPair) {
		customBuilders.get(phase.ordinal()).put(name, builderDestroyerPair);
	}

	@Override
	public void addBuildPhaseListener(BuildPhaseListener listener) {
		buildPhaseListeners.add(listener);
	}

	@Override
	public Entry getGenericMenuStructure() {
		return genericMenuStructure;
	}
}
