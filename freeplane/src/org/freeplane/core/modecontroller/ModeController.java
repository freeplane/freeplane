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
package org.freeplane.core.modecontroller;

import java.awt.Container;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.freeplane.core.controller.AController;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.ExtensionContainer;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.model.NodeModel.NodeChangeType;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.url.UrlManager;

/**
 * Derive from this class to implement the Controller for your mode. Overload
 * the methods you need for your data model, or use the defaults. There are some
 * default Actions you may want to use for easy editing of your model. Take
 * MindMapController as a sample.
 */
public class ModeController extends AController {
	private static class ActionEnablerOnChange implements INodeChangeListener, INodeSelectionListener, IActionOnChange {
		final AFreeplaneAction action;

		public ActionEnablerOnChange(final AFreeplaneAction action) {
			super();
			this.action = action;
		}

		public AFreeplaneAction getAction() {
			return action;
		}

		public void nodeChanged(final NodeChangeEvent event) {
			action.setEnabled();
		}

		public void onDeselect(final NodeModel node) {
		}

		public void onSelect(final NodeModel node) {
			action.setEnabled();
		}
	}

	private static class ActionSelectorOnChange implements INodeChangeListener, INodeSelectionListener, IActionOnChange {
		final AFreeplaneAction action;

		public ActionSelectorOnChange(final AFreeplaneAction action) {
			super();
			this.action = action;
		}

		public AFreeplaneAction getAction() {
			return action;
		}

		public void nodeChanged(final NodeChangeEvent event) {
			if (!NodeChangeType.REFRESH.equals(event.getProperty())) {
				action.setSelected();
			}
		}

		public void onDeselect(final NodeModel node) {
		}

		public void onSelect(final NodeModel node) {
			action.setSelected();
		}
	}

	final private Controller controller;
	private final ExtensionContainer extensionContainer;
	private boolean isBlocked = false;
	private MapController mapController;
	final private List<IMenuContributor> menuContributors = new LinkedList<IMenuContributor>();
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

	/**
	 * Instantiation order: first me and then the model.
	 */
	public ModeController(final Controller controller) {
		this.controller = controller;
		extensionContainer = new ExtensionContainer(new HashMap<Class<? extends IExtension>, IExtension>());
	}

	@Override
	public void addAction(final AFreeplaneAction action) {
		super.addAction(action);
		if (AFreeplaneAction.checkEnabledOnChange(action)) {
			final ActionEnablerOnChange listener = new ActionEnablerOnChange(action);
			mapController.addNodeSelectionListener(listener);
			mapController.addNodeChangeListener(listener);
		}
		if (AFreeplaneAction.checkSelectionOnChange(action)) {
			final ActionSelectorOnChange listener = new ActionSelectorOnChange(action);
			mapController.addNodeSelectionListener(listener);
			mapController.addNodeChangeListener(listener);
		}
	}

	public void addExtension(final Class<? extends IExtension> clazz, final IExtension extension) {
		extensionContainer.addExtension(clazz, extension);
	}

	public void addINodeViewLifeCycleListener(final INodeViewLifeCycleListener listener) {
		nodeViewListeners.add(listener);
	}

	public void addMenuContributor(final IMenuContributor contributor) {
		menuContributors.add(contributor);
	}

	public void commit() {
	}
	
	public boolean isUndoAction() {
		return false;
	}

	public void execute(final IActor actor, MapModel map) {
		actor.act();
	}

	@Override
	public AFreeplaneAction getAction(final String key) {
		final AFreeplaneAction action = super.getAction(key);
		if (action != null) {
			return action;
		}
		return controller.getAction(key);
	}

	public Controller getController() {
		return controller;
	}

	public IExtension getExtension(final Class<? extends IExtension> clazz) {
		return extensionContainer.getExtension(clazz);
	}

	/**
	 * @return
	 */
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
		for (final ListIterator<NodeModel> i = getMapController().childrenUnfolded(parent); i.hasNext();) {
			if ((i.next()).isVisible()) {
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
		for(INodeViewLifeCycleListener hook : nodeViewListeners) {
			hook.onViewCreated(node);
		}
	}

	public void onViewRemoved(final Container node) {
		for(INodeViewLifeCycleListener hook : nodeViewListeners) {
			hook.onViewRemoved(node);
		}
	}

	@Override
	public AFreeplaneAction removeAction(final String key) {
		final AFreeplaneAction action = super.removeAction(key);
		if (AFreeplaneAction.checkEnabledOnChange(action)) {
			mapController.removeNodeSelectionListener(ActionEnablerOnChange.class, action);
			mapController.removeNodeChangeListener(ActionEnablerOnChange.class, action);
		}
		if (AFreeplaneAction.checkSelectionOnChange(action)) {
			mapController.removeNodeSelectionListener(ActionSelectorOnChange.class, action);
			mapController.removeNodeChangeListener(ActionSelectorOnChange.class, action);
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
	}

	public void setUserInputListenerFactory(final IUserInputListenerFactory userInputListenerFactory) {
		this.userInputListenerFactory = userInputListenerFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.modes.ModeController#setVisible(boolean)
	 */
	public void setVisible(final boolean visible) {
		final NodeModel node = controller.getSelection().getSelected();
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

	/**
	 * This method is called after and before a change of the map mapView. Use
	 * it to perform the actions that cannot be performed at creation time.
	 */
	public void startup() {
		UrlManager.getController(this).startup();
	}

	public void updateMenus() {
		final IUserInputListenerFactory userInputListenerFactory = getUserInputListenerFactory();
		final MenuBuilder menuBuilder = userInputListenerFactory.getMenuBuilder();
		final Iterator<IMenuContributor> iterator = menuContributors.iterator();
		while (iterator.hasNext()) {
			iterator.next().updateMenus(menuBuilder);
		}
	}
}
