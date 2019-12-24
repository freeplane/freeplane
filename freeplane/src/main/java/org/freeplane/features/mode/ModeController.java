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
package org.freeplane.features.mode;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.freeplane.core.extension.ExtensionContainer;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.components.html.CssRuleBuilder;
import org.freeplane.core.ui.menubuilders.generic.BuilderDestroyerPair;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase;
import org.freeplane.core.ui.menubuilders.generic.UserRole;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.features.map.IExtensionCopier;
import org.freeplane.features.map.ITooltipProvider;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.ui.INodeViewLifeCycleListener;

/**
 * Derive from this class to implement the Controller for your mode. Overload
 * the methods you need for your data model, or use the defaults. There are some
 * default Actions you may want to use for easy editing of your model. Take
 * MindMapController as a sample.
 */
public class ModeController extends AController implements FreeplaneActions{
	public static final String VIEW_MODE_PROPERTY = "view_mode";
	public static final String USER_INTERFACE_PROPERTY = "user_interface";
	public static final List<String> USER_INTERFACE_PROPERTIES = Arrays.asList(VIEW_MODE_PROPERTY, USER_INTERFACE_PROPERTY);
	// // 	final private Controller controller;
	private final ExtensionContainer extensionContainer;
	private final Collection<IExtensionCopier> copiers;
	private boolean isBlocked = false;
	private MapController mapController;
	final private Map<Integer, ITooltipProvider> toolTip = new TreeMap<Integer, ITooltipProvider>();
	/**
	 * The model, this controller belongs to. It may be null, if it is the
	 * default controller that does not show a map.
	 */
	final private List<INodeViewLifeCycleListener> nodeViewListeners = new LinkedList<INodeViewLifeCycleListener>();
	/**
	 * Take care! This listener is also used for modelpopups (as for graphical
	 * links).
	 */
	private IUserInputListenerFactory userInputListenerFactory;
	final private Controller controller;

	/**
	 * Instantiation order: first me and then the model.
	 */
	public ModeController(final Controller controller) {
		this.controller = controller;
		extensionContainer = new ExtensionContainer(new HashMap<Class<? extends IExtension>, IExtension>());
		copiers = new LinkedList<IExtensionCopier>();
	}

	@Override
	public void addAction(final AFreeplaneAction action) {
		super.addAction(action);
		if (mapController != null) {
			mapController.addListenerForAction(action);
		}
	}


	public void addExtension(final Class<? extends IExtension> clazz, final IExtension extension) {
		extensionContainer.addExtension(clazz, extension);
	}

	public void registerExtensionCopier(final IExtensionCopier copier) {
		copiers.add(copier);
	}

	public void unregisterExtensionCopier(final IExtensionCopier copier) {
		copiers.remove(copier);
	}

	public void copyExtensions(final Object key, final NodeModel from, final NodeModel to) {
		for (final IExtensionCopier copier : copiers) {
			copier.copy(key, from, to);
		}
	}

	public void undoableCopyExtensions(final Object key, final NodeModel from, final NodeModel to) {
		final MapModel map = to.getMap();
		if (map == null) {
			copyExtensions(key, from, to);
			return;
		}
		final IUndoHandler undoHandler = map.getExtension(IUndoHandler.class);
		if (undoHandler == null) {
			copyExtensions(key, from, to);
			return;
		}
		final NodeModel backup = new NodeModel(null);
		copyExtensions(key, to, backup);
		final IActor actor = new IActor() {
			@Override
			public void undo() {
				removeExtensions(key, to);
				copyExtensions(key, backup, to);
				getMapController().nodeChanged(to);
			}

			@Override
			public String getDescription() {
				return "undoableCopyExtensions";
			}

			@Override
			public void act() {
				copyExtensions(key, from, to);
				getMapController().nodeChanged(to);
			}
		};
		execute(actor, map);
	}

	void removeExtensions(final Object key, final NodeModel from, final NodeModel which) {
		if (from.equals(which)) {
			for (final IExtensionCopier copier : copiers) {
				copier.remove(key, from);
			}
			return;
		}
		for (final IExtensionCopier copier : copiers) {
			copier.remove(key, from, which);
		}
	}

	public void undoableRemoveExtensions(final Object key, final NodeModel from, final NodeModel which) {
		final MapModel map = from.getMap();
		if (map == null) {
			removeExtensions(key, from, which);
			return;
		}
		final IUndoHandler undoHandler = map.getExtension(IUndoHandler.class);
		if (undoHandler == null) {
			removeExtensions(key, from, which);
			return;
		}
		final NodeModel backup = new NodeModel(null);
		copyExtensions(key, from, backup);
		final IActor actor = new IActor() {
			@Override
			public void undo() {
				copyExtensions(key, backup, from);
				getMapController().nodeChanged(from);
			}

			@Override
			public String getDescription() {
				return "undoableCopyExtensions";
			}

			@Override
			public void act() {
				removeExtensions(key, from, which);
				getMapController().nodeChanged(from);
			}
		};
		execute(actor, map);
	}

	public void resolveParentExtensions(final Object key, final NodeModel to) {
		for (final IExtensionCopier copier : copiers) {
			copier.resolveParentExtensions(key, to);
		}
	}

	public void undoableResolveParentExtensions(final Object key,  final NodeModel to) {
		final MapModel map = to.getMap();
		if (map == null) {
			resolveParentExtensions(key, to);
			return;
		}
		final IUndoHandler undoHandler = map.getExtension(IUndoHandler.class);
		if (undoHandler == null) {
			resolveParentExtensions(key, to);
			return;
		}
		final NodeModel backup = new NodeModel(null);
		copyExtensions(key, to, backup);
		final IActor actor = new IActor() {
			@Override
			public void undo() {
				copyExtensions(key, backup, to);
				getMapController().nodeChanged(to);
			}

			@Override
			public String getDescription() {
				return "undoableCopyExtensions";
			}

			@Override
			public void act() {
				resolveParentExtensions(key, to);
				getMapController().nodeChanged(to);
			}
		};
		execute(actor, map);
	}

	void removeExtensions(final Object key, final NodeModel from) {
		for (final IExtensionCopier copier : copiers) {
			copier.remove(key, from, from);
		}
	}

	public void addINodeViewLifeCycleListener(final INodeViewLifeCycleListener listener) {
		nodeViewListeners.add(listener);
	}
	public void addUiBuilder(Phase phase, String name, EntryVisitor builder) {
		addUiBuilder(phase, name, new BuilderDestroyerPair(builder));
	}

	public void addUiBuilder(Phase phase, String name, EntryVisitor builder, EntryVisitor destroyer) {
		addUiBuilder(phase, name, new BuilderDestroyerPair(builder, destroyer));
	}

	public void addUiBuilder(Phase phase, String name, BuilderDestroyerPair builderDestroyerPair) {
		getUserInputListenerFactory().addUiBuilder(phase, name, builderDestroyerPair);
    }


	public void commit() {
	}

	public boolean canEdit(MapModel map) {
		return canEdit() && map != null && ! map.isReadOnly() && ! isEditingLocked();
	}

	public UserRole userRole(MapModel map) {
		return UserRole.ADVANCED_EDITOR;
	}
	
	public boolean isEditingLocked() {
		return ResourceController.getResourceController().getBooleanProperty(VIEW_MODE_PROPERTY);
	}

	public void execute(final IActor actor, final MapModel map) {
		actor.act();
	}

	@Override
	public AFreeplaneAction getAction(final String key) {
		final AFreeplaneAction action = super.getAction(key);
		if (action != null) {
			return action;
		}
		return getController().getAction(key);
	}

	public Controller getController() {
		return controller;
	}

	public <T extends IExtension> T getExtension(final Class<T> clazz) {
		return extensionContainer.getExtension(clazz);
	}

	public boolean containsExtension(final Class<? extends IExtension> clazz) {
		return extensionContainer.containsExtension(clazz);
	}

	public void removeExtension(Class<? extends IExtension> clazz) {
		extensionContainer.removeExtension(clazz);
	}
	public MapController getMapController() {
		return mapController;
	}

	public String getModeName() {
		return null;
	}

	public IUserInputListenerFactory getUserInputListenerFactory() {
		return userInputListenerFactory;
	}

	public boolean hasOneVisibleChild(final NodeModel parent) {
		int count = 0;
		for (final NodeModel child : parent.getChildren()) {
			if (child.hasVisibleContent()) {
				count++;
			}
			if (count == 2) {
				return false;
			}
		}
		return count == 1;
	}

	public boolean isBlocked() {
		return isBlocked;
	}

	public void onViewCreated(final Container node) {
		for (final INodeViewLifeCycleListener hook : nodeViewListeners) {
			hook.onViewCreated(node);
		}
	}

	public void onViewRemoved(final Container node) {
		for (final INodeViewLifeCycleListener hook : nodeViewListeners) {
			hook.onViewRemoved(node);
		}
	}

	@Override
	public AFreeplaneAction removeAction(final String key) {
		final AFreeplaneAction action = super.removeAction(key);
		if (mapController != null) {
			mapController.removeListenerForAction(action);
		}
		return action;
	}

	public void removeINodeViewLifeCycleListener(final INodeViewLifeCycleListener listener) {
		nodeViewListeners.remove(listener);
	}

	public void rollback() {
	}

	public void setBlocked(final boolean isBlocked) {
		this.isBlocked = isBlocked;
	}

	public void setMapController(final MapController mapController) {
		this.mapController = mapController;
		addExtension(MapController.class, mapController);
	}

	public void setUserInputListenerFactory(final IUserInputListenerFactory userInputListenerFactory) {
		this.userInputListenerFactory = userInputListenerFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.modes.ModeController#setVisible(boolean)
	 */
	public void setVisible(final boolean visible) {
		final NodeModel node = getController().getSelection().getSelected();
		if (visible) {
			mapController.onSelect(node);
		}
		else {
			if (node != null) {
				mapController.onDeselect(node);
			}
		}
	}

	public void shutdown() {
	}

	public void startTransaction() {
	}
	public void forceNewTransaction() {
    }

	/**
	 * This method is called after and before a change of the map mapView. Use
	 * it to perform the actions that cannot be performed at creation time.
	 */
	public void startup() {
	}

	public void updateMenus(String menuStructure, final Set<String> plugins) {
		final IUserInputListenerFactory userInputListenerFactory = getUserInputListenerFactory();
		userInputListenerFactory.updateMenus(menuStructure, plugins);
	}

	public boolean canEdit() {
		return false;
	}
	public String createToolTip(final NodeModel node, Component view) {
		final MapModel map = node.getMap();
		// perhaps we should use the solution presented in the 3rd answer at
		// http://stackoverflow.com/questions/3355469/1-pixel-table-border-in-jtextpane-using-html
		// html/css example: http://www.wer-weiss-was.de/theme35/article3555660.html
		final Color background = getExtension(MapStyle.class).getBackground(map);
		final StringBuilder style = new StringBuilder( "<style type='text/css'>")
		        .append(" body { font-size: 10pt;") // FIXME: copy from NoteController.setNoteTooltip() ?
		        .append(new CssRuleBuilder().withBackground(background))
		        .append(" }</style>");
		final StringBuilder text = new StringBuilder("<html><head>"+style+"</head><body>");
		boolean tooltipSet = false;
		for (final ITooltipProvider provider : toolTip.values()) {
			String value = provider.getTooltip(this, node, view);
			if (value == null) {
				continue;
			}
			value = value.replace("<html>", "<div>");
			value = value.replaceAll("\\s*</?(body|head)>", "");
			value = value.replace("</html>", "</div>");
			if (tooltipSet) {
				text.append("<hr>");
			}
			text.append(value);
			tooltipSet = true;
		}
		if (tooltipSet) {
			text.append("</body></html>");
//			System.err.println("tooltip=" + text);
			return text.toString();
		}
		return null;
	}

	/**
	 */
	public void addToolTipProvider(final Integer key, final ITooltipProvider tooltip) {
		if (tooltip == null) {
			if (toolTip.containsKey(key)) {
				toolTip.remove(key);
			}
		}
		else {
			toolTip.put(key, tooltip);
		}
	}

	public boolean canEdit(NodeModel model) {
		return true;
	}

	public boolean supportsHookActions() {
		return false;
	}

	public boolean shouldCenterCompactMaps() {
		return true;
	}
}
