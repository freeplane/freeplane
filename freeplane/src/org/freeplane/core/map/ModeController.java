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
package org.freeplane.core.map;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.ExtensionHashMap;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.ActionController;
import org.freeplane.core.ui.ActionDescriptor;
import org.freeplane.core.ui.FreeplaneAction;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.IMouseWheelEventHandler;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.undo.IUndoableActor;
import org.freeplane.core.url.UrlManager;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

/**
 * Derive from this class to implement the Controller for your mode. Overload
 * the methods you need for your data model, or use the defaults. There are some
 * default Actions you may want to use for easy editing of your model. Take
 * MindMapController as a sample.
 */
public class ModeController {
	public void setUserInputListenerFactory(IUserInputListenerFactory userInputListenerFactory) {
    	this.userInputListenerFactory = userInputListenerFactory;
    }

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

		public void onDeselect(final NodeView node) {
		}

		public void onSelect(final NodeView node) {
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

		public void onDeselect(final NodeView node) {
		}

		public void onSelect(final NodeView node) {
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

		public void onDeselect(final NodeView node) {
		}

		public void onSelect(final NodeView node) {
			action.setSelected();
		}
	}

	private interface IActionOnChange {
		Action getAction();
	}

	public static final String NODESEPARATOR = "<nodeseparator>";
	private final ActionController actionController;
	final private ExtensionHashMap extensions;
	private boolean isBlocked = false;
	private MapController mapController;
	final private LinkedList<IMenuContributor> menuContributors;
	final private HashSet mRegisteredMouseWheelEventHandler = new HashSet();
	final private LinkedList<INodeChangeListener> nodeChangeListeners;
	final private LinkedList<INodeSelectionListener> nodeSelectionListeners;
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
		nodeSelectionListeners = new LinkedList();
		nodeChangeListeners = new LinkedList();
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
			addNodeSelectionListener(listener);
			addNodeChangeListener(listener);
		}
		if (FreeplaneAction.checkSelectionOnChange(action)) {
			final ActionSelectorOnChange listener = new ActionSelectorOnChange(
			    (FreeplaneAction) action);
			addNodeSelectionListener(listener);
			addNodeChangeListener(listener);
		}
		if (FreeplaneAction.checkVisibilityOnChange(action)) {
			final ActionDisplayerOnChange listener = new ActionDisplayerOnChange(
			    (FreeplaneAction) action);
			addNodeSelectionListener(listener);
			addNodeChangeListener(listener);
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

	public void addMouseWheelEventHandler(final IMouseWheelEventHandler handler) {
		mRegisteredMouseWheelEventHandler.add(handler);
	}

	public void addNodeChangeListener(final INodeChangeListener listener) {
		nodeChangeListeners.add(listener);
	}

	public void addNodeSelectionListener(final INodeSelectionListener listener) {
		nodeSelectionListeners.add(listener);
	}

	public void centerNode(final NodeModel node) {
		NodeView view = null;
		if (node != null) {
			view = Controller.getController().getMapView().getNodeView(node);
		}
		if (view == null) {
			getMapController().displayNode(node);
			view = Controller.getController().getMapView().getNodeView(node);
		}
		centerNode(view);
	}

	private void centerNode(final NodeView node) {
		getMapView().centerNode(node);
		getMapView().selectAsTheOnlyOneSelected(node);
		Controller.getController().getViewController().obtainFocusForSelected();
	}

	public boolean containsExtension(final Class clazz) {
		return extensions.containsExtension(clazz);
	}

	public void doubleClick(final MouseEvent e) {
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

	public void getFilteredXml(final MapModel map, final Writer fileout) throws IOException {
		getMapController().writeMapAsXml(map, fileout, false);
	}

	/**
	 * @return
	 */
	public MapController getMapController() {
		return mapController;
	}

	public MapView getMapView() {
		return Controller.getController().getMapView();
	}

	public String getModeName() {
		return null;
	}

	public Set getMouseWheelEventHandlers() {
		return Collections.unmodifiableSet(mRegisteredMouseWheelEventHandler);
	}

	public NodeView getNodeView(final NodeModel node) {
		return getMapView().getNodeView(node);
	}

	private NodeView getNodeView(final Object object) {
		if (object instanceof NodeView) {
			return (NodeView) object;
		}
		if (object instanceof NodeModel) {
			return getMapView().getNodeView((NodeModel) object);
		}
		throw new ClassCastException();
	}


	public NodeModel getSelectedNode() {
		final NodeView selectedView = getSelectedView();
		if (selectedView != null) {
			return selectedView.getModel();
		}
		return null;
	}

	/**
	 * fc, 24.1.2004: having two methods getSelecteds with different return
	 * values (linkedlists of models resp. views) is asking for trouble. @see
	 * MapView
	 *
	 * @return returns a list of MindMapNode s.
	 */
	public List getSelectedNodes() {
		final MapView view = getMapView();
		if (view == null) {
			return Collections.EMPTY_LIST;
		}
		final LinkedList selecteds = new LinkedList();
		final Iterator it = view.getSelection().iterator();
		if (it != null) {
			while (it.hasNext()) {
				final NodeView selected = (NodeView) it.next();
				selecteds.add(selected.getModel());
			}
		}
		return selecteds;
	}

	public NodeView getSelectedView() {
		if (getMapView() != null) {
			return getMapView().getSelected();
		}
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

	public void onDeselect(final NodeView node) {
		try {
			final HashSet copy = new HashSet(nodeSelectionListeners);
			for (final Iterator iter = copy.iterator(); iter.hasNext();) {
				final INodeSelectionListener listener = (INodeSelectionListener) iter.next();
				listener.onDeselect(node);
			}
		}
		catch (final RuntimeException e) {
			Logger.global.log(Level.SEVERE, "Error in node selection listeners", e);
		}
	}

	public void onSelect(final NodeView node) {
		for (final Iterator iter = nodeSelectionListeners.iterator(); iter.hasNext();) {
			final INodeSelectionListener listener = (INodeSelectionListener) iter.next();
			listener.onSelect(node);
		}
	}

	/**
	 * @param isUpdate
	 * @param node
	 */
	public void onUpdate(final NodeModel node, final Object property, final Object oldValue,
	                     final Object newValue) {
		final Iterator<INodeChangeListener> iterator = nodeChangeListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().nodeChanged(new NodeChangeEvent(node, property, oldValue, newValue));
		}
	}

	public void onViewCreated(final NodeView node) {
		for (final Iterator i = nodeViewListeners.iterator(); i.hasNext();) {
			final INodeViewLifeCycleListener hook = (INodeViewLifeCycleListener) i.next();
			hook.onViewCreated(node);
		}
	}

	public void onViewRemoved(final NodeView node) {
		for (final Iterator i = nodeViewListeners.iterator(); i.hasNext();) {
			final INodeViewLifeCycleListener hook = (INodeViewLifeCycleListener) i.next();
			hook.onViewRemoved(node);
		}
	}

	public void plainClick(final MouseEvent e) {
	}

	public Action removeAction(final String key) {
		final Action action = actionController.removeAction(key);
		if (FreeplaneAction.checkEnabledOnChange(action)) {
			removeNodeSelectionListener(ActionEnablerOnChange.class, action);
			removeNodeChangeListener(ActionEnablerOnChange.class, action);
		}
		if (FreeplaneAction.checkSelectionOnChange(action)) {
			removeNodeSelectionListener(ActionSelectorOnChange.class, action);
			removeNodeChangeListener(ActionSelectorOnChange.class, action);
		}
		if (FreeplaneAction.checkVisibilityOnChange(action)) {
			removeNodeSelectionListener(ActionDisplayerOnChange.class, action);
			removeNodeChangeListener(ActionDisplayerOnChange.class, action);
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

	public void removeMouseWheelEventHandler(final IMouseWheelEventHandler handler) {
		mRegisteredMouseWheelEventHandler.remove(handler);
	}

	private void removeNodeChangeListener(final Class<? extends IActionOnChange> clazz,
	                                      final Action action) {
		final Iterator<INodeChangeListener> iterator = nodeChangeListeners.iterator();
		while (iterator.hasNext()) {
			final INodeChangeListener next = iterator.next();
			if (next instanceof IActionOnChange && ((IActionOnChange) next).getAction() == action) {
				iterator.remove();
				return;
			}
		}
	}

	public void removeNodeChangeListener(final INodeChangeListener listener) {
		nodeChangeListeners.remove(listener);
	}

	private void removeNodeSelectionListener(final Class<? extends IActionOnChange> clazz,
	                                         final Action action) {
		final Iterator<INodeSelectionListener> iterator = nodeSelectionListeners.iterator();
		while (iterator.hasNext()) {
			final INodeSelectionListener next = iterator.next();
			if (next instanceof IActionOnChange && ((IActionOnChange) next).getAction() == action) {
				iterator.remove();
				return;
			}
		}
	}

	public void removeNodeSelectionListener(final INodeSelectionListener listener) {
		nodeSelectionListeners.remove(listener);
	}

	public void select(final NodeView node) {
		getMapView().scrollNodeToVisible(node);
		getMapView().selectAsTheOnlyOneSelected(node);
		getMapView().setSiblingMaxLevel(node.getModel().getNodeLevel());
	}

	public void selectBranch(final NodeView selected, final boolean extend) {
		getMapController().displayNode(selected.getModel());
		getMapView().selectBranch(selected, extend);
	}

	public void selectMultipleNodes(final NodeModel focussed, final Collection selecteds) {
		selectMultipleNodesImpl(focussed, selecteds);
	}

	public void selectMultipleNodes(final NodeView focussed, final Collection selecteds) {
		selectMultipleNodesImpl(focussed, selecteds);
	}

	private void selectMultipleNodesImpl(final Object focussed, final Collection selecteds) {
		for (final Iterator i = selecteds.iterator(); i.hasNext();) {
			final NodeModel node = (NodeModel) (i.next());
			getMapController().displayNode(node);
		}
		select(getNodeView(focussed));
		for (final Iterator i = selecteds.iterator(); i.hasNext();) {
			final NodeView node = getNodeView(i.next());
			getMapView().makeTheSelected(node);
		}
		Controller.getController().getViewController().obtainFocusForSelected();
	}

	public void setBlocked(final boolean isBlocked) {
		this.isBlocked = isBlocked;
	}

	public void setMapController(final MapController mapController) {
		this.mapController = mapController;
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.modes.ModeController#setVisible(boolean)
	 */
	public void setVisible(final boolean visible) {
		if (visible) {
			final NodeView node = getSelectedView();
			onSelect(node);
		}
		else {
			final NodeView node = getSelectedView();
			if (node != null) {
				onDeselect(node);
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

	protected void updateMenus() {
		final IUserInputListenerFactory userInputListenerFactory = getUserInputListenerFactory();
		final MenuBuilder menuBuilder = userInputListenerFactory.getMenuBuilder();
		final Iterator<IMenuContributor> iterator = menuContributors.iterator();
		while (iterator.hasNext()) {
			iterator.next().updateMenus(menuBuilder);
		}
	}
}
