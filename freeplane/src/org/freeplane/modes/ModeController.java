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
package org.freeplane.modes;

import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.net.URL;
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
import javax.swing.JPopupMenu;

import org.freeplane.controller.ActionController;
import org.freeplane.controller.Controller;
import org.freeplane.io.url.UrlManager;
import org.freeplane.map.attribute.IAttributeController;
import org.freeplane.map.clipboard.ClipboardController;
import org.freeplane.map.cloud.CloudController;
import org.freeplane.map.edge.EdgeController;
import org.freeplane.map.icon.IconController;
import org.freeplane.map.link.LinkController;
import org.freeplane.map.nodelocation.LocationController;
import org.freeplane.map.nodestyle.NodeStyleController;
import org.freeplane.map.note.NoteController;
import org.freeplane.map.text.TextController;
import org.freeplane.map.tree.MapController;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.map.tree.view.NodeView;
import org.freeplane.ui.IMouseListener;
import org.freeplane.ui.MenuBuilder;
import org.freeplane.undo.IUndoableActor;

import deprecated.freemind.common.ITextTranslator;
import deprecated.freemind.extensions.IHookFactory;

/**
 * Derive from this class to implement the Controller for your mode. Overload
 * the methods you need for your data model, or use the defaults. There are some
 * default Actions you may want to use for easy editing of your model. Take
 * MindMapController as a sample.
 */
public class ModeController {
	public static final String NODESEPARATOR = "<nodeseparator>";
	private final ActionController actionController;
	private ClipboardController clipboardController;
	private CloudController cloudController;
	private EdgeController edgeController;
	private IconController iconController;
	private boolean isBlocked = false;
	private LinkController linkController;
	private MapController mapController;
	final private LinkedList<IMenuContributor> menuContributors;
	final private LinkedList<INodeChangeListener> nodeChangeListeners;
	final private LinkedList<INodeSelectionListener> nodeSelectionListeners;
	/**
	 * The model, this controller belongs to. It may be null, if it is the
	 * default controller that does not show a map.
	 */
	private NodeStyleController nodeStyleController;
	final private LinkedList<INodeViewLifeCycleListener> nodeViewListeners;
	private NoteController noteController;
	final private ControllerPopupMenuListener popupListener = new ControllerPopupMenuListener(
	    this);
	/**
	 * Take care! This listener is also used for modelpopups (as for graphical
	 * links).
	 */
	LocationController positionController;
	private TextController textController;
	private UrlManager urlManager;
	final private UserInputListenerFactory userInputListenerFactory;

	/**
	 * Instantiation order: first me and then the model.
	 */
	public ModeController() {
		userInputListenerFactory = new UserInputListenerFactory(this);
		nodeSelectionListeners = new LinkedList();
		nodeChangeListeners = new LinkedList();
		menuContributors = new LinkedList();
		nodeViewListeners = new LinkedList();
		actionController = new ActionController();
	}

	public void addAction(final Object key, final Action value) {
		actionController.addAction(key, value);
	}

	public void addMenuContributor(final IMenuContributor contributor) {
		menuContributors.add(contributor);
	}

	public void addNodeChangeListener(final INodeChangeListener listener) {
		nodeChangeListeners.add(listener);
	}

	public void addNodeSelectionListener(final INodeSelectionListener listener) {
		nodeSelectionListeners.add(listener);
	}

	public void addNodeViewLifeCycleListener(
	                                         final INodeViewLifeCycleListener listener) {
		nodeViewListeners.add(listener);
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

	public void doubleClick(final MouseEvent e) {
	}

	public void enableActions(final boolean enabled) {
		Controller.getController().enableActions(enabled);
		actionController.enableActions(enabled);
	}

	public void execute(final IUndoableActor actor) {
		actor.act();
	}

	public Action getAction(final String key) {
		final Action action = actionController.getAction(key);
		if (action != null) {
			return action;
		}
		return Controller.getController().getAction(key);
	}

	public IAttributeController getAttributeController() {
		return null;
	}

	public ClipboardController getClipboardController() {
		return clipboardController;
	}

	/**
	 * @return
	 */
	public CloudController getCloudController() {
		return cloudController;
	}

	/**
	 * @return
	 */
	public EdgeController getEdgeController() {
		return edgeController;
	}

	public IHookFactory getHookFactory() {
		return null;
	}

	public IconController getIconController() {
		return iconController;
	}

	/**
	 *
	 */
	public LinkController getLinkController() {
		return linkController;
	}

	public LocationController getLocationController() {
		return positionController;
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

	/**
	 * @return
	 */
	public NodeStyleController getNodeStyleController() {
		return nodeStyleController;
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

	public NoteController getNoteController() {
		return noteController;
	}

	public JPopupMenu getPopupForModel(final java.lang.Object obj) {
		final JPopupMenu popupForModel = getLinkController().getPopupForModel(
		    obj);
		if (popupForModel != null) {
			popupForModel.addPopupMenuListener(popupListener);
			return popupForModel;
		}
		return getUserInputListenerFactory().getMapPopup();
	}

	public Set getRegisteredMouseWheelEventHandler() {
		return Collections.EMPTY_SET;
	}

	public URL getResource(final String name) {
		return Controller.getResourceController().getResource(name);
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

	public TextController getTextController() {
		return textController;
	}

	public ITextTranslator getTextTranslator() {
		return new ITextTranslator() {
			public String getText(final String key) {
				return Controller.getText(key);
			}
		};
	}

	public UrlManager getUrlManager() {
		return urlManager;
	}

	public UserInputListenerFactory getUserInputListenerFactory() {
		return userInputListenerFactory;
	}

	public boolean hasOneVisibleChild(final NodeModel parent) {
		int count = 0;
		for (final ListIterator i = getMapController().childrenUnfolded(parent); i
		    .hasNext();) {
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
				final INodeSelectionListener listener = (INodeSelectionListener) iter
				    .next();
				listener.onDeselect(node);
			}
		}
		catch (final RuntimeException e) {
			Logger.global.log(Level.SEVERE,
			    "Error in node selection listeners", e);
		}
	}

	public void onSelect(final NodeView node) {
		for (final Iterator iter = nodeSelectionListeners.iterator(); iter
		    .hasNext();) {
			final INodeSelectionListener listener = (INodeSelectionListener) iter
			    .next();
			listener.onSelect(node);
		}
	}

	/**
	 * @param isUpdate
	 * @param node
	 */
	public void onUpdate(final NodeModel node, final Object property,
	                     final Object oldValue, final Object newValue) {
		final Iterator<INodeChangeListener> iterator = nodeChangeListeners
		    .iterator();
		while (iterator.hasNext()) {
			iterator.next().nodeChanged(
			    new NodeChangeEvent(node, property, oldValue, newValue));
		}
	}

	public void onViewCreated(final NodeView node) {
		for (final Iterator i = nodeViewListeners.iterator(); i.hasNext();) {
			final INodeViewLifeCycleListener hook = (INodeViewLifeCycleListener) i
			    .next();
			hook.onViewCreated(node);
		}
	}

	public void onViewRemoved(final NodeView node) {
		for (final Iterator i = nodeViewListeners.iterator(); i.hasNext();) {
			final INodeViewLifeCycleListener hook = (INodeViewLifeCycleListener) i
			    .next();
			hook.onViewRemoved(node);
		}
	}

	public void plainClick(final MouseEvent e) {
	}

	public Action removeAction(final String key) {
		return actionController.removeAction(key);
	}

	public void removeNodeChangeListener(final INodeChangeListener listener) {
		nodeChangeListeners.remove(listener);
	}

	public void removeNodeSelectionListener(
	                                        final INodeSelectionListener listener) {
		nodeSelectionListeners.remove(listener);
	}

	public void removeNodeViewLifeCycleListener(
	                                            final INodeViewLifeCycleListener listener) {
		nodeViewListeners.remove(listener);
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

	public void selectMultipleNodes(final NodeModel focussed,
	                                final Collection selecteds) {
		selectMultipleNodesImpl(focussed, selecteds);
	}

	public void selectMultipleNodes(final NodeView focussed,
	                                final Collection selecteds) {
		selectMultipleNodesImpl(focussed, selecteds);
	}

	private void selectMultipleNodesImpl(final Object focussed,
	                                     final Collection selecteds) {
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

	public void setClipboardController(
	                                   final ClipboardController clipboardController) {
		this.clipboardController = clipboardController;
	}

	public void setCloudController(final CloudController cloudController) {
		this.cloudController = cloudController;
	}

	public void setEdgeController(final EdgeController edgeController) {
		this.edgeController = edgeController;
	}

	public void setIconController(final IconController iconController) {
		this.iconController = iconController;
	}

	public void setLinkController(final LinkController linkController) {
		this.linkController = linkController;
	}

	public void setLocationController(
	                                  final LocationController positionController) {
		this.positionController = positionController;
	}

	public void setMapController(final MapController mapController) {
		this.mapController = mapController;
	}

	public void setMapMouseMotionListener(
	                                      final IMouseListener mapMouseMotionListener) {
		userInputListenerFactory.setMapMouseListener(mapMouseMotionListener);
	}

	public void setNodeDropTargetListener(
	                                      final DropTargetListener nodeDropTargetListener) {
		userInputListenerFactory
		    .setNodeDropTargetListener(nodeDropTargetListener);
	}

	public void setNodeKeyListener(final KeyListener nodeKeyListener) {
		userInputListenerFactory.setNodeKeyListener(nodeKeyListener);
	}

	public void setNodeMotionListener(final IMouseListener nodeMotionListener) {
		userInputListenerFactory.setNodeMotionListener(nodeMotionListener);
	}

	public void setNodeStyleController(
	                                   final NodeStyleController textStyleController) {
		nodeStyleController = textStyleController;
	}

	public void setNoteController(final NoteController noteController) {
		this.noteController = noteController;
	}

	public void setTextController(final TextController textController) {
		this.textController = textController;
	}

	public void setUrlManager(final UrlManager urlManager) {
		this.urlManager = urlManager;
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
		getUrlManager().startup();
	}

	protected void updateMenus(final MenuBuilder builder) {
	}

	protected void updateMenus(final String resource) {
		final UserInputListenerFactory userInputListenerFactory = getUserInputListenerFactory();
		userInputListenerFactory.setMenuStructure(resource);
		userInputListenerFactory.updateMenus(this);
		final MenuBuilder menuBuilder = userInputListenerFactory
		    .getMenuBuilder();
		updateMenus(menuBuilder);
		final Iterator<IMenuContributor> iterator = menuContributors.iterator();
		while (iterator.hasNext()) {
			iterator.next().updateMenus(menuBuilder);
		}
	}
}
