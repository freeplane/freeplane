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
package org.freeplane.core.addins;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IElementHandler;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public abstract class PersistentNodeHook {
	static private Set<Class<? extends IExtension>> mapExtensionClasses = new HashSet<Class<? extends IExtension>>();

	public static boolean isMapExtension(final Class<? extends IExtension> clazz) {
		return mapExtensionClasses.contains(clazz);
	}

	public abstract class HookAction extends AFreeplaneAction {
		private static final long serialVersionUID = 1L;

		public HookAction(final String key) {
			super(key);
		}

		public void actionPerformed(final ActionEvent e) {
			undoableSetHook(!isActiveForSelection());
		}
	}

	@SelectableAction(checkOnNodeChange = true)
	protected class SelectableHookAction extends HookAction {
		private static final long serialVersionUID = 1L;

		public SelectableHookAction(final String key) {
			super(key);
			//			System.out.println("SelectableHookAction " + key);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			setSelected(!isActiveForSelection());
			super.actionPerformed(e);
			setSelected(isActiveForSelection());
		}

		@Override
		public void setSelected() {
			setSelected(isActiveForSelection());
		}
	}

	private final class ToggleHookActor implements IActor {
		IExtension extension;
		private final NodeModel node;

		private ToggleHookActor(final NodeModel node, final IExtension extension) {
			this.node = node;
			this.extension = extension != null ? extension : node.getExtension(getExtensionClass());
		}

		public void act() {
			extension = toggle(node, extension);
		}

		public String getDescription() {
			return getHookName();
		}

		public void undo() {
			act();
		}
	}

	protected class XmlReader implements IElementDOMHandler {
		public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
			if (attributes == null) {
				return null;
			}
			if (!getHookName().equals(attributes.getAttribute("NAME", null))) {
				return null;
			}
			return parent;
		}

		public void endElement(final Object parent, final String tag, final Object userObject,
		                       final XMLElement lastBuiltElement) {
			if (getHookAnnotation().onceForMap()) {
				final XMLElement parentNodeElement = lastBuiltElement.getParent().getParent();
				if (parentNodeElement == null || !parentNodeElement.getName().equals("map")) {
					return;
				}
			}
			final NodeModel node = (NodeModel) userObject;
			if (node.getExtension(getExtensionClass()) != null) {
				return;
			}
			final IExtension extension = createExtension(node, lastBuiltElement);
			if (extension == null) {
				return;
			}
			add(node, extension);
			if (selectableHookAction != null) {
				selectableHookAction.setEnabled(true);
			}
		}
	}

	protected class XmlWriter implements IExtensionElementWriter {
		public void writeContent(final ITreeWriter writer, final Object object, final IExtension extension)
		        throws IOException {
			final XMLElement element = new XMLElement("hook");
			try {
				saveExtension(extension, element);
				writer.addElement(null, element);
			}
			catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	// // 	final private Controller controller;
	// 	private final ModeController modeController;
	private final HookAction selectableHookAction;

	@SuppressWarnings("unchecked")
	public PersistentNodeHook() {
		super();
		if (getHookAnnotation().onceForMap()) {
			mapExtensionClasses.add(getExtensionClass());
		}
		//		this.modeController = modeController;
		//		controller = modeController.getController();
		final ModeController modeController = Controller.getCurrentModeController();
		if (modeController.getModeName().equals("MindMap")) {
			final ActionLocationDescriptor actionAnnotation = getActionAnnotation();
			if (actionAnnotation != null) {
				selectableHookAction = createHookAction();
				if (selectableHookAction != null) {
					registerAction(selectableHookAction, actionAnnotation);
				}
			}
			else {
				selectableHookAction = null;
			}
		}
		else {
			selectableHookAction = null;
		}
		final MapController mapController = modeController.getMapController();
		mapController.getReadManager().addElementHandler("hook", createXmlReader());
		final IExtensionElementWriter xmlWriter = createXmlWriter();
		if (xmlWriter != null) {
			mapController.getWriteManager().addExtensionElementWriter(getExtensionClass(), xmlWriter);
		}
		if (this instanceof IExtension) {
			// do not use getExtensionClass() here since in several subclasses getExtensionClass() returns a
			// different class than getClass()
			modeController.addExtension((Class<? extends IExtension>) getClass(), (IExtension) this);
		}
	}

	protected void add(final NodeModel node, final IExtension extension) {
		assert (getExtensionClass().equals(extension.getClass()));
		node.addExtension(extension);
	}

	protected IExtension createExtension(final NodeModel node) {
		return createExtension(node, null);
	}

	abstract protected IExtension createExtension(final NodeModel node, final XMLElement element);

	protected HookAction createHookAction() {
		return new SelectableHookAction(getClass().getSimpleName() + "Action");
	}

	protected IElementHandler createXmlReader() {
		return new XmlReader();
	}

	protected IExtensionElementWriter createXmlWriter() {
		return new XmlWriter();
	}

	protected ActionLocationDescriptor getActionAnnotation() {
		final ActionLocationDescriptor annotation = getClass().getAnnotation(ActionLocationDescriptor.class);
		return annotation;
	}

	@SuppressWarnings("unchecked")
	protected Class<? extends IExtension> getExtensionClass() {
		return (Class<? extends IExtension>) getClass();
	}

	private NodeHookDescriptor getHookAnnotation() {
		final NodeHookDescriptor annotation = getClass().getAnnotation(NodeHookDescriptor.class);
		return annotation;
	}

	protected String getHookName() {
		return getHookAnnotation().hookName();
	}

	public IExtension getMapHook() {
		final NodeModel rootNode = Controller.getCurrentController().getMap().getRootNode();
		return rootNode.getExtension(getExtensionClass());
	}

	protected NodeModel[] getNodes() {
		if (getHookAnnotation().onceForMap()) {
			return getRootNode();
		}
		return getSelectedNodes();
	}

	protected NodeModel[] getRootNode() {
		final NodeModel[] nodes = new NodeModel[1];
		nodes[0] = Controller.getCurrentController().getMap().getRootNode();
		return nodes;
	}

	protected NodeModel[] getSelectedNodes() {
		final List<NodeModel> selection = Controller.getCurrentController().getSelection().getSelection();
		final int size = selection.size();
		final NodeModel[] nodes = new NodeModel[size];
		final Iterator<NodeModel> iterator = selection.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			nodes[i++] = iterator.next();
		}
		return nodes;
	}

	public boolean isActive(final NodeModel nodeModel) {
		if (!nodeModel.isRoot() && getHookAnnotation().onceForMap()) {
			return isActive(nodeModel.getMap().getRootNode());
		}
		return nodeModel.containsExtension(getExtensionClass());
	}

	protected boolean isActiveForSelection() {
		final NodeModel[] nodes = getNodes();
		for (int i = 0; i < nodes.length; i++) {
			final NodeModel nodeModel = nodes[i];
			if (nodeModel.containsExtension(getExtensionClass())) {
				return true;
			}
		}
		return false;
	}

	protected void registerAction(final AFreeplaneAction action) {
		registerAction(action, action.getClass().getAnnotation(ActionLocationDescriptor.class));
	}

	protected void registerAction(final AFreeplaneAction action, final ActionLocationDescriptor actionAnnotation) {
		Controller.getCurrentModeController().addAction(action);
		Controller.getCurrentModeController().getUserInputListenerFactory().getMenuBuilder()
		    .addAction(action, actionAnnotation);
	}

	protected void remove(final NodeModel node, final IExtension extension) {
		node.removeExtension(extension);
	}

	protected void saveExtension(final IExtension extension, final XMLElement element) {
		element.setAttribute("NAME", getHookName());
	}

	public void undoableActivateHook(final NodeModel node, final IExtension extension) {
		undoableToggleHook(node, extension);
	}

	public void undoableDeactivateHook(final NodeModel node) {
		final IExtension extension = node.getExtension(getExtensionClass());
		if (extension != null) {
			undoableToggleHook(node, extension);
		}
	}

	public void undoableSetHook(final boolean enable) {
		final NodeModel[] nodes = getNodes();
		for (int i = 0; i < nodes.length; i++) {
			final NodeModel node = nodes[i];
			if (node.containsExtension(getExtensionClass()) != enable) {
				undoableToggleHook(node);
			}
		}
	}

	public void undoableSetHook(final IExtension extension) {
		final NodeModel[] nodes = getNodes();
		for (int i = 0; i < nodes.length; i++) {
			final NodeModel node = nodes[i];
			if (extension != null || node.containsExtension(getExtensionClass())) {
				undoableToggleHook(node, extension);
			}
		}
	}

	public void undoableToggleHook(final NodeModel node) {
		undoableToggleHook(node, node.getExtension(getExtensionClass()));
	}

	public void undoableToggleHook(final NodeModel node, final IExtension extension) {
		final IActor actor = new ToggleHookActor(node, extension);
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}

	protected IExtension toggle(final NodeModel node, IExtension extension) {
		if (extension != null && node.containsExtension(extension.getClass())) {
			remove(node, extension);
		}
		else {
			if (extension == null) {
				extension = createExtension(node);
			}
			if (extension != null) {
				add(node, extension);
			}
		}
		Controller.getCurrentModeController().getMapController()
		    .nodeChanged(node, NodeModel.UNKNOWN_PROPERTY, null, null);
		return extension;
	}
}
