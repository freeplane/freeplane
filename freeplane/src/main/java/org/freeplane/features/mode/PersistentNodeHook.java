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
package org.freeplane.features.mode;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IElementHandler;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public abstract class PersistentNodeHook {

	public abstract class HookAction extends AFreeplaneAction {
		private static final long serialVersionUID = 1L;

		public HookAction(final String key) {
			super(key);
		}

		public void actionPerformed(final ActionEvent e) {
			undoableSetHookForSelection(!isActiveForSelection());
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


    @SelectableAction(checkOnNodeChange = true)
    protected class SelectableEnumAction extends HookAction {
        private static final long serialVersionUID = 1L;
        final Enum<?> value;
        public SelectableEnumAction(String key, final Enum<?> value) {
            super(key + "." + String.valueOf(value));
            this.value = value;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            undoableSetHookForSelection(false);
            if(value != null)
                undoableSetHookForSelection((IExtension)value);
            setSelected(true);
        }

        @Override
        public void setSelected() {
            setSelected(isActiveForSelection(value));
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public PersistentNodeHook() {
		super();
		final Class<? extends IExtension> extensionClass = getExtensionClass();
        if (getHookAnnotation().onceForMap()) {
			MapExtensions.registerMapExtension(extensionClass);
		}
		//		this.modeController = modeController;
		//		controller = modeController.getController();
		final ModeController modeController = Controller.getCurrentModeController();
		if (modeController.supportsHookActions())
			registerActions();
		final MapController mapController = modeController.getMapController();
		mapController.getReadManager().addElementHandler("hook", createXmlReader());
		final IExtensionElementWriter xmlWriter = createXmlWriter();
		if (xmlWriter != null) {
			mapController.getWriteManager().addExtensionElementWriter(extensionClass, xmlWriter);
		}
		if (this instanceof IExtension) {
			// do not use getExtensionClass() here since in several subclasses getExtensionClass() returns a
			// different class than getClass()
			modeController.addExtension((Class<? extends IExtension>) getClass(), (IExtension) this);
		}
	}

	protected void registerActions() {
		final Class<? extends IExtension> extensionClass = getExtensionClass();
		if(extensionClass.isEnum()){
		    Class<Enum> enumClass = (Class<Enum>) (Class<?>)extensionClass;
		    EnumSet<? extends Enum<?>> all= EnumSet.allOf(enumClass);
		    for(Enum e : all){
		        registerAction(new SelectableEnumAction(getClass().getSimpleName() + "Action", e));
		    }
		    registerAction(new SelectableEnumAction(getClass().getSimpleName() + "Action", null));
		}
		else{
		    final HookAction selectableHookAction = createHookAction();
		    if (selectableHookAction != null) {
		        registerAction(selectableHookAction);
		    }
		}
	}

	protected void add(final NodeModel node, final IExtension extension) {
		assert (getExtensionClass().equals(extension.getClass()));
		node.addExtension(extension);
	}

	protected IExtension createExtension(final NodeModel node) {
		return createExtension(node, null);
	}

	protected IExtension createExtension(final NodeModel node, final XMLElement element){
        try {
	    final Class<? extends IExtension> extensionClass = getExtensionClass();
	    if(extensionClass.isEnum()){
	            final String value = element.getAttribute("VALUE");
                final Method factory = extensionClass.getMethod("valueOf", String.class);
                return (IExtension)factory.invoke(null, value);

	    }
	    return extensionClass.newInstance();
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
        }
        return null;
	}

	protected HookAction createHookAction() {
		return new SelectableHookAction(getClass().getSimpleName() + "Action");
	}

	protected IElementHandler createXmlReader() {
		return new XmlReader();
	}

	protected IExtensionElementWriter createXmlWriter() {
		return new XmlWriter();
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

	public IExtension getMapHook(final MapModel map) {
		final NodeModel rootNode = map.getRootNode();
		return rootNode.getExtension(getExtensionClass());
	}

	protected NodeModel[] getNodesForSelection() {
		if (getHookAnnotation().onceForMap()) {
			return getRootNodeForSelection();
		}
		return getSelectedNodes();
	}

	protected NodeModel[] getRootNodeForSelection() {
		final NodeModel[] nodes = new NodeModel[1];
		nodes[0] = Controller.getCurrentController().getMap().getRootNode();
		return nodes;
	}

	protected NodeModel[] getSelectedNodes() {
		final IMapSelection mapSelection = Controller.getCurrentController().getSelection();
		if(mapSelection != null) {
			final Collection<NodeModel> selection = mapSelection.getSelection();
			final int size = selection.size();
			final NodeModel[] nodes= new NodeModel[size];
			final Iterator<NodeModel> iterator = selection.iterator();
			int i = 0;
			while (iterator.hasNext()) {
				nodes[i++] = iterator.next();
			}
			return nodes;
		}
		else
		return new NodeModel[]{};
	}

	public boolean isActive(final NodeModel nodeModel) {
		if (!nodeModel.isRoot() && getHookAnnotation().onceForMap()) {
			return isActive(nodeModel.getMap().getRootNode());
		}
		return nodeModel.containsExtension(getExtensionClass());
	}

	public IExtension getExtension(final NodeModel nodeModel) {
		if (!nodeModel.isRoot() && getHookAnnotation().onceForMap()) {
			return getExtension(nodeModel.getMap().getRootNode());
		}
		return nodeModel.getExtension(getExtensionClass());
	}

	protected boolean isActiveForSelection() {
		final NodeModel[] nodes = getNodesForSelection();
		for (int i = 0; i < nodes.length; i++) {
			final NodeModel nodeModel = nodes[i];
			if (nodeModel.containsExtension(getExtensionClass())) {
				return true;
			}
		}
		return false;
	}


    private boolean isActiveForSelection(Enum<?> value) {
        final NodeModel[] nodes = getNodesForSelection();
        for (int i = 0; i < nodes.length; i++) {
            final NodeModel nodeModel = nodes[i];
            final IExtension nodeValue = nodeModel.getExtension(getExtensionClass());
            if (value == null && nodeValue != null || value != null && !value.equals(nodeValue)) {
                return false;
            }
        }
        return true;
    }
	protected void registerAction(final AFreeplaneAction action) {
		Controller.getCurrentModeController().addAction(action);
	}

	protected void remove(final NodeModel node, final IExtension extension) {
		node.removeExtension(extension);
	}

	protected void saveExtension(final IExtension extension, final XMLElement element) {
		element.setAttribute("NAME", getHookName());
		if(extension instanceof Enum){
		    element.setAttribute("VALUE", extension.toString());
		}
	}

	public void undoableActivateHook(final NodeModel node, final IExtension extension) {
		if (! node.containsExtension(extension.getClass())) {
			undoableToggleHook(node, extension);
		}
	}

	public void undoableDeactivateHook(final NodeModel node) {
		final IExtension extension = node.getExtension(getExtensionClass());
		if (extension != null) {
			undoableToggleHook(node, extension);
		}
	}

	public void undoableSetHookForSelection(final boolean enable) {
		final NodeModel[] nodes = getNodesForSelection();
		for (int i = 0; i < nodes.length; i++) {
			final NodeModel node = nodes[i];
			if (node.containsExtension(getExtensionClass()) != enable) {
				undoableToggleHook(node);
			}
		}
	}

	public void undoableSetHookForSelection(final IExtension extension) {
		final NodeModel[] nodes = getNodesForSelection();
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
		final IExtension before;
		final IExtension after;
		if (extension != null && node.containsExtension(extension.getClass())) {
			before = extension;
			after = null;
			remove(node, extension);
		}
		else {
			if (extension == null) {
				extension = createExtension(node);
			}
			if (extension != null) {
				add(node, extension);
			}
			before = null;
			after = extension;
		}
		Controller.getCurrentModeController().getMapController()
		    .nodeChanged(node, getExtensionClass(), before, after);
		return extension;
	}

	public static void removeMapExtensions(NodeModel node) {
		final IExtension[] extensionArray = node.getSharedExtensions().values().toArray(new IExtension[]{});
		for(IExtension extension : extensionArray){
			if(MapExtensions.isMapExtension(extension.getClass())){
				node.removeExtension(extension);
			}
		}
    }

	public void moveExtension(ModeController modeController, MapModel sourceMap, MapModel targetMap) {
		final NodeModel sourceNode = sourceMap.getRootNode();
		final Class<? extends IExtension> extensionClass = getExtensionClass();
		final IExtension sourceExtension = sourceNode.getExtension(extensionClass);
		final NodeModel targetNode = targetMap.getRootNode();
		final IExtension targetExtension = targetNode.getExtension(extensionClass);
		if(sourceExtension == targetExtension)
			return;
		IActor actor = new IActor() {
			@Override
			public void act() {
				if(targetExtension != null)
					targetNode.removeExtension(targetExtension);
				if(sourceExtension != null)
					targetNode.addExtension(sourceExtension);
			}
			
			@Override
			public void undo() {
				if(sourceExtension != null)
					targetNode.removeExtension(sourceExtension);
				if(targetExtension != null)
					targetNode.addExtension(targetExtension);
			}
			
			@Override
			public String getDescription() {
				return "move extension " + extensionClass.getName();
			}
			
		};
		modeController.execute(actor, targetMap);
		
	}
}
