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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.Action;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.ExtensionHashMap;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.ActionController;
import org.freeplane.core.ui.ActionDescriptor;
import org.freeplane.core.ui.FreeplaneAction;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.undo.IUndoableActor;
import org.freeplane.core.url.UrlManager;
/**
 * Derive from this class to implement the Controller for your mode. Overload
 * the methods you need for your data model, or use the defaults. There are some
 * default Actions you may want to use for easy editing of your model. Take
 * MindMapController as a sample.
 */
public class ModeController {
	private static class ActionDisplayerOnChange implements INodeChangeListener,
	        INodeSelectionListener, IActionOnChange {
		final FreeplaneAction action;

		public ActionDisplayerOnChange(final FreeplaneAction action) {
			super();
			this.action = action;
		}

		public Action getAction() {
			return action;
		}

		public void nodeChanged(final NodeChangeEvent event) {
			action.setVisible();
		}

		public void onDeselect(final NodeModel node) {
		}

		public void onSelect(final NodeModel node) {
			action.setVisible();
		}
	}

	private static class ActionEnablerOnChange implements INodeChangeListener,
	        INodeSelectionListener, IActionOnChange {
		final FreeplaneAction action;

		public ActionEnablerOnChange(final FreeplaneAction action) {
			super();
			this.action = action;
		}

		public Action getAction() {
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

	private static class ActionSelectorOnChange implements INodeChangeListener,
	        INodeSelectionListener, IActionOnChange {
		final FreeplaneAction action;

		public ActionSelectorOnChange(final FreeplaneAction action) {
			super();
			this.action = action;
		}

		public Action getAction() {
			return action;
		}

		public void nodeChanged(final NodeChangeEvent event) {
			action.setSelected();
		}

		public void onDeselect(final NodeModel node) {
		}

		public void onSelect(final NodeModel node) {
			action.setSelected();
		}
	}

	interface IActionOnChange {
		Action getAction();
	}

	public static final String NODESEPARATOR = "<nodeseparator>";
	private final ActionController actionController;
	final private ExtensionHashMap extensions;
	private boolean isBlocked = false;
	private MapController mapController;
	final private LinkedList<IMenuContributor> menuContributors;
	/**
	 * The model, this controller belongs to. It may be null, if it is the
	 * default controller that does not show a map.
	 */
	final private LinkedList<INodeViewLifeCycleListener> nodeViewListeners;
	/**
	 * Take care! This listener is also used for modelpopups (as for graphical
	 * links).
	 */
	private IUserInputListenerFactory userInputListenerFactory;

	/**
	 * Instantiation order: first me and then the model.
	 */
	public ModeController() {
		menuContributors = new LinkedList();
		nodeViewListeners = new LinkedList();
		actionController = new ActionController();
		extensions = new ExtensionHashMap();
	}

	public void addAction(final Object key, final Action action) {
		actionController.addAction(key, action);
		if (FreeplaneAction.checkEnabledOnChange(action)) {
			final ActionEnablerOnChange listener = new ActionEnablerOnChange(
			    (FreeplaneAction) action);
			mapController.addNodeSelectionListener(listener);
			mapController.addNodeChangeListener(listener);
		}
		if (FreeplaneAction.checkSelectionOnChange(action)) {
			final ActionSelectorOnChange listener = new ActionSelectorOnChange(
			    (FreeplaneAction) action);
			mapController.addNodeSelectionListener(listener);
			mapController.addNodeChangeListener(listener);
		}
		if (FreeplaneAction.checkVisibilityOnChange(action)) {
			final ActionDisplayerOnChange listener = new ActionDisplayerOnChange(
			    (FreeplaneAction) action);
			mapController.addNodeSelectionListener(listener);
			mapController.addNodeChangeListener(listener);
		}
	}

	public void addAnnotatedAction(final Action action) {
		final String name = action.getClass().getAnnotation(ActionDescriptor.class).name();
		addAction(name, action);
	}

	public boolean addExtension(final Class clazz, final IExtension extension) {
		return extensions.addExtension(clazz, extension);
	}

	public boolean addExtension(final IExtension extension) {
		return extensions.addExtension(extension);
	}

	public void addINodeViewLifeCycleListener(final INodeViewLifeCycleListener listener) {
		nodeViewListeners.add(listener);
	}

	public void addMenuContributor(final IMenuContributor contributor) {
		menuContributors.add(contributor);
	}

	public boolean containsExtension(final Class clazz) {
		return extensions.containsExtension(clazz);
	}

	public void execute(final IUndoableActor actor) {
		actor.act();
	}

	public Iterator<IExtension> extensionIterator() {
		return extensions.extensionIterator();
	}

	public Iterator<IExtension> extensionIterator(final Class clazz) {
		return extensions.extensionIterator(clazz);
	}

	public Action getAction(final String key) {
		final Action action = actionController.getAction(key);
		if (action != null) {
			return action;
		}
		return Controller.getController().getAction(key);
	}

	public IExtension getExtension(final Class clazz) {
		return extensions.getExtension(clazz);
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

	public String getText(final String textId) {
		return Controller.getText(textId);
	}

	public IUserInputListenerFactory getUserInputListenerFactory() {
		return userInputListenerFactory;
	}

	public boolean hasOneVisibleChild(final NodeModel parent) {
		int count = 0;
		for (final ListIterator i = getMapController().childrenUnfolded(parent); i.hasNext();) {
			if (((NodeModel) i.next()).isVisible()) {
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
		for (final Iterator i = nodeViewListeners.iterator(); i.hasNext();) {
			final INodeViewLifeCycleListener hook = (INodeViewLifeCycleListener) i.next();
			hook.onViewCreated(node);
		}
	}

	public void onViewRemoved(final Container node) {
		for (final Iterator i = nodeViewListeners.iterator(); i.hasNext();) {
			final INodeViewLifeCycleListener hook = (INodeViewLifeCycleListener) i.next();
			hook.onViewRemoved(node);
		}
	}


	public Action removeAction(final String key) {
		final Action action = actionController.removeAction(key);
		if (FreeplaneAction.checkEnabledOnChange(action)) {
			mapController.removeNodeSelectionListener(ActionEnablerOnChange.class, action);
			mapController.removeNodeChangeListener(ActionEnablerOnChange.class, action);
		}
		if (FreeplaneAction.checkSelectionOnChange(action)) {
			mapController.removeNodeSelectionListener(ActionSelectorOnChange.class, action);
			mapController.removeNodeChangeListener(ActionSelectorOnChange.class, action);
		}
		if (FreeplaneAction.checkVisibilityOnChange(action)) {
			mapController.removeNodeSelectionListener(ActionDisplayerOnChange.class, action);
			mapController.removeNodeChangeListener(ActionDisplayerOnChange.class, action);
		}
		return action;
	}

	public IExtension removeExtension(final Class clazz) {
		return extensions.removeExtension(clazz);
	}

	public boolean removeExtension(final IExtension extension) {
		return extensions.removeExtension(extension);
	}

	public void removeINodeViewLifeCycleListener(final INodeViewLifeCycleListener listener) {
		nodeViewListeners.remove(listener);
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
		final NodeModel node = Controller.getController().getSelection().getSelected();
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
