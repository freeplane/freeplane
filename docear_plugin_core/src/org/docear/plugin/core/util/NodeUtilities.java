package org.docear.plugin.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.apache.commons.lang.NullArgumentException;
import org.docear.plugin.core.features.DocearNodeModelExtension.DocearExtensionKey;
import org.docear.plugin.core.features.DocearNodeModelExtensionController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.INodeView;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.attribute.AttributeView;

public class NodeUtilities {

	public static boolean isMapCurrentlyOpened(MapModel map) {
		if (map == null) {
			throw new NullArgumentException("map");
		}
		Map<String, MapModel> maps = Controller.getCurrentController().getMapViewManager().getMaps();
		for (Entry<String, MapModel> entry : maps.entrySet()) {
			if (entry.getValue().getFile() == null) {
				if (entry.getValue().equals(map)) {
					return true;
				}
			}
			else if (entry.getValue().getFile().equals(map.getFile())) {
				return true;
			}
		}
		return false;
	}

	public static boolean saveMap(MapModel map) {
		try {
			Controller.getCurrentController().selectMode(MModeController.MODENAME);
			MMapIO mapIO = (MMapIO) MModeController.getMModeController().getExtension(MapIO.class);
			mapIO.writeToFile(map, map.getFile());
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static List<MapModel> getMapsFromUris(Collection<URI> mindmaps) {
		List<MapModel> maps = new ArrayList<MapModel>();
		for (URI uri : mindmaps) {
			MapModel map = getMapFromUri(uri);
			if (map != null) {
				maps.add(map);
			}
		}
		return maps;
	}

	public static MapModel getMapFromUri(URI uri) {
		ModeController current = Controller.getCurrentModeController();
		Controller.getCurrentController().selectMode(MModeController.MODENAME);
		Map<String, MapModel> maps = Controller.getCurrentController().getMapViewManager().getMaps();
		try {
			for (Entry<String, MapModel> entry : maps.entrySet()) {
				if (entry.getValue().getFile() != null && entry.getValue().getFile().toURI().equals(uri)) {
					return entry.getValue();
				}
			}
			try {
				MapModel map = new MMapModel();
				AttributeRegistry.getRegistry(map);
				URL url = Tools.getFilefromUri(uri).toURI().toURL();
				final MapIO mapIO = (MapIO) Controller.getCurrentModeController().getExtension(MapIO.class);
				mapIO.load(url, map);
				return map;
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		finally {
			Controller.getCurrentController().selectMode(current);
		}
	}

	public static NodeModel setLinkFrom(URI file, NodeModel node) {
		((MLinkController) LinkController.getController()).setLinkTypeDependantLink(node, file);

		return node;
	}

	public static NodeModel insertChildNodeFrom(NodeModel node, boolean isLeft, NodeModel target) {
		((MMapController) Controller.getCurrentModeController().getMapController()).insertNode(node, target, false, isLeft, isLeft);

		return node;
	}

	public static NodeModel createFolderStructurePath(NodeModel target, Stack<File> pathStack) {
		if (pathStack.isEmpty()) {
			return target;
		}
		File parent = pathStack.pop();
		NodeModel pathNode = null;
		for (NodeModel child : target.getChildren()) {
			if (child.getText().equals(parent.getName()) && DocearNodeModelExtensionController.containsKey(child, DocearExtensionKey.MONITOR_PATH)) {
				pathNode = child;
				break;
			}
		}
		if (pathNode != null) {
			return createFolderStructurePath(pathNode, pathStack);
		}
		else {
			pathNode = ((MMapController) Controller.getCurrentModeController().getMapController()).newNode(parent.getName(), target.getMap());
			DocearNodeModelExtensionController.setEntry(pathNode, DocearExtensionKey.MONITOR_PATH, null);
			setLinkFrom(WorkspaceUtils.getURI(parent), pathNode);
			insertChildNodeFrom(pathNode, target.isLeft(), target);
			return createFolderStructurePath(pathNode, pathStack);
		}
	}

	public static boolean setAttributeValue(NodeModel target, String attributeKey, Object value) {
		try {
			if (target == null || attributeKey == null || value == null) return false;
			for (INodeView nodeView : target.getViewers()) {
				if (nodeView instanceof NodeView) {
					NodeAttributeTableModel attributes = ((NodeView) nodeView).getAttributeView().getAttributes();
					if (attributes != null) {
						if (attributes.getAttributeKeyList().contains(attributeKey)) {
							// int pos =
							// attributes.getAttributePosition(attributeKey);
							AttributeController.getController(MModeController.getMModeController()).performSetValueAt(attributes, value,
									attributes.getAttributePosition(attributeKey), 1);
							// attributes.setValue(pos,value);
							// attributes.fireTableRowsUpdated(pos, pos);
						}
						else {
							AttributeController.getController(MModeController.getMModeController()).performInsertRow(attributes, attributes.getRowCount(),
									attributeKey, value);
							// attributes.addRowNoUndo(new
							// Attribute(attributeKey, value));
						}

						AttributeView attributeView = (((MapView) Controller.getCurrentController().getViewController().getMapView()).getSelected())
								.getAttributeView();
						attributeView.getContainer().invalidate();
						attributeView.update();
						return true;
					}
				}
			}
		}
		catch (Exception e) {
			LogUtils.warn("org.docear.plugin.pdfutilities.util.NodeUtils.setAttributeValue(1): " + e.getMessage());
		}
		return false;
	}

	public static void removeAttribute(NodeModel target, String attributeKey) {
		if (target == null || attributeKey == null) {
			return;
		}
		for (INodeView nodeView : target.getViewers()) {
			if (nodeView instanceof NodeView) {
				NodeAttributeTableModel attributes = ((NodeView) nodeView).getAttributeView().getAttributes();
				if (attributes != null && attributes.getAttributeKeyList().contains(attributeKey)) {
					AttributeController.getController(MModeController.getMModeController()).performRemoveRow(attributes,
							attributes.getAttributePosition(attributeKey));
				}
				if (attributes.getRowCount() <= 0) {
					((NodeView) nodeView).getAttributeView().viewRemoved();
				}
			}
		}
		// NodeAttributeTableModel attributes =
		// AttributeController.getController(MModeController.getMModeController()).createAttributeTableModel(target);
		// if(attributes != null &&
		// attributes.getAttributeKeyList().contains(attributeKey)) {
		// AttributeController.getController(MModeController.getMModeController()).performRemoveRow(attributes,
		// attributes.getAttributePosition(attributeKey));
		// }
	}

	public static void removeAttributes(NodeModel target) {
		if (target == null) {
			return;
		}
		for (INodeView nodeView : target.getViewers()) {
			if (nodeView instanceof NodeView) {
				NodeAttributeTableModel attributes = ((NodeView) nodeView).getAttributeView().getAttributes();
				for (String attributeKey : attributes.getAttributeKeyList()) {
					AttributeController.getController(MModeController.getMModeController()).performRemoveRow(attributes,
							attributes.getAttributePosition(attributeKey));
				}
				if (attributes.getRowCount() <= 0) {
					((NodeView) nodeView).getAttributeView().viewRemoved();
				}
			}
		}
	}

	public static Object getAttributeValue(NodeModel target, String attributeKey) {
		if (target == null || attributeKey == null) return null;
		NodeAttributeTableModel attributes = AttributeController.getController(MModeController.getMModeController()).createAttributeTableModel(target);
		if (attributes != null) {
			if (attributes.getAttributeKeyList().contains(attributeKey)) {
				return attributes.getAttribute(attributes.getAttributePosition(attributeKey)).getValue();
			}
		}
		return null;
	}

	public static int getAttributeIntValue(NodeModel target, String attributeKey) {
		Object o = getAttributeValue(target, attributeKey);
		Integer value = 0;
		if (o == null) {
			return value;
		}
		if (o instanceof Integer) {
			value = (Integer) o;
		}
		else {
			try {
				value = Integer.parseInt(o.toString());
			}
			catch (NumberFormatException e) {
				LogUtils.severe("Could not read Attribute Key: " + attributeKey + " . Number expected.", e);
			}
		}
		return value;
	}

}
