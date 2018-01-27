/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
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
package org.freeplane.features.styles;

import java.awt.Color;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataListener;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.attribute.FontSizeExtension;
import org.freeplane.features.cloud.CloudModel;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapReader;
import org.freeplane.features.map.MapWriter.Hint;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.NodeBuilder;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.nodestyle.NodeSizeModel;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.ShapeConfigurationModel;

/**
 * @author Dimitry Polivaev
 * Mar 12, 2009
 */
public class MapStyleModel implements IExtension {
	public static final String STYLES_PREDEFINED = "styles.predefined";
	public static final String STYLES_USER_DEFINED = "styles.user-defined";
	public static final String STYLES_AUTOMATIC_LAYOUT = "styles.AutomaticLayout";
	public static final IStyle DEFAULT_STYLE = new StyleTranslatedObject("default");
	public static final IStyle DETAILS_STYLE = new StyleTranslatedObject("defaultstyle.details");
	public static final IStyle ATTRIBUTE_STYLE = new StyleTranslatedObject("defaultstyle.attributes");
	public static final IStyle NOTE_STYLE = new StyleTranslatedObject("defaultstyle.note");
	public static final IStyle FLOATING_STYLE = new StyleTranslatedObject("defaultstyle.floating");
	private Map<IStyle, NodeModel> styleNodes;
	private MapModel styleMap;
	private ConditionalStyleModel conditionalStyleModel;
	final private DefaultComboBoxModel stylesComboBoxModel;
	final private Map<String, String> properties;

	Map<String, String> getProperties() {
		return properties;
	}

	public static MapStyleModel getExtension(final MapModel map) {
		return MapStyleModel.getExtension(map.getRootNode());
	}

	public MapModel getStyleMap() {
		return styleMap;
	}

	public static MapStyleModel getExtension(final NodeModel node) {
		return node.getExtension(MapStyleModel.class);
	}

	private Color backgroundColor;

	public MapStyleModel() {
		conditionalStyleModel = new ConditionalStyleModel();
		styleNodes = new LinkedHashMap<IStyle, NodeModel>();
		properties = new LinkedHashMap<String, String>();
		stylesComboBoxModel = new DefaultComboBoxModel();
	}

	public ConditionalStyleModel getConditionalStyleModel() {
		return conditionalStyleModel;
	}

	private void insertStyleMap(MapModel map, MapModel styleMap) {
		this.styleMap = styleMap;
		final NodeModel rootNode = styleMap.getRootNode();
		createNodeStyleMap(rootNode);
		styleMap.putExtension(IUndoHandler.class, map.getExtension(IUndoHandler.class));
		final MapStyleModel defaultStyleModel = new MapStyleModel();
		defaultStyleModel.styleNodes = styleNodes;
		initStylesComboBoxModel();
		rootNode.putExtension(defaultStyleModel);
	}

	public void refreshStyles() {
		final NodeModel rootNode = styleMap.getRootNode();
		styleNodes.clear();
		stylesComboBoxModel.removeAllElements();
		createNodeStyleMap(rootNode);
	}

	void createStyleMap(final MapModel parentMap, MapStyleModel mapStyleModel, final String styleMapStr) {
		final ModeController modeController = Controller.getCurrentModeController();
		MapModel styleMap = new StyleMapModel(parentMap.getIconRegistry(),
		    AttributeRegistry.getRegistry(parentMap));
		styleMap.createNewRoot();
		final MapReader mapReader = modeController.getMapController().getMapReader();
		final Reader styleReader = new StringReader(styleMapStr);
		NodeModel root;
		try {
			Map<Object, Object> hints = new HashMap<Object, Object>();
			hints.put(Hint.MODE, Mode.FILE);
			hints.put(NodeBuilder.FOLDING_LOADED, Boolean.TRUE);
			root = mapReader.createNodeTreeFromXml(styleMap, styleReader, hints);
			NodeStyleModel.setShapeConfiguration(root,
			    ShapeConfigurationModel.NULL_SHAPE.withShape(NodeStyleModel.Shape.oval).withUniform(true));
			NodeStyleModel.createNodeStyleModel(root).setFontSize(24);
			styleMap.setRoot(root);
			final Quantity<LengthUnits> styleBlockGap = ResourceController.getResourceController()
			    .getLengthQuantityProperty("style_block_gap");
			LocationModel.createLocationModel(root).setVGap(styleBlockGap);
			insertStyleMap(parentMap, styleMap);
			NodeModel predefinedStyleParentNode = createStyleGroupNode(styleMap, STYLES_PREDEFINED);
			createStyleGroupNode(styleMap, STYLES_USER_DEFINED);
			createStyleGroupNode(styleMap, STYLES_AUTOMATIC_LAYOUT);
			if (styleNodes.get(DEFAULT_STYLE) == null) {
				final NodeModel newNode = new NodeModel(DEFAULT_STYLE, styleMap);
				predefinedStyleParentNode.insert(newNode, 0);
				addStyleNode(newNode);
			}
			NodeModel defaultStyleModel = styleNodes.get(DEFAULT_STYLE);
			if (maxNodeWidth != null && null == NodeSizeModel.getMaxNodeWidth(defaultStyleModel))
				NodeSizeModel.setMaxNodeWidth(defaultStyleModel, maxNodeWidth);
			if (minNodeWidth != null && null == NodeSizeModel.getMinNodeWidth(defaultStyleModel))
				NodeSizeModel.setNodeMinWidth(defaultStyleModel, minNodeWidth);
			if (styleNodes.get(DETAILS_STYLE) == null) {
				final NodeModel newNode = new NodeModel(DETAILS_STYLE, styleMap);
				predefinedStyleParentNode.insert(newNode, 1);
				addStyleNode(newNode);
			}
			if (styleNodes.get(ATTRIBUTE_STYLE) == null) {
				final NodeModel newNode = new NodeModel(ATTRIBUTE_STYLE, styleMap);
				final int defaultFontSize = 9;
				NodeStyleModel.createNodeStyleModel(newNode).setFontSize(defaultFontSize);
				predefinedStyleParentNode.insert(newNode, 2);
				addStyleNode(newNode);
			}
			FontSizeExtension fontSizeExtension = parentMap.getExtension(FontSizeExtension.class);
			if (fontSizeExtension != null) {
				NodeStyleModel.createNodeStyleModel(styleNodes.get(ATTRIBUTE_STYLE))
				    .setFontSize(fontSizeExtension.fontSize);
			}
			if (styleNodes.get(NOTE_STYLE) == null) {
				final NodeModel newNode = new NodeModel(NOTE_STYLE, styleMap);
				NodeStyleModel.createNodeStyleModel(newNode).setBackgroundColor(Color.WHITE);
				predefinedStyleParentNode.insert(newNode, 3);
				addStyleNode(newNode);
			}
			if (styleNodes.get(FLOATING_STYLE) == null) {
				final NodeModel newNode = new NodeModel(FLOATING_STYLE, styleMap);
				EdgeModel.createEdgeModel(newNode).setStyle(EdgeStyle.EDGESTYLE_HIDDEN);
				CloudModel.createModel(newNode).setShape(CloudModel.Shape.ROUND_RECT);
				predefinedStyleParentNode.insert(newNode, 4);
				addStyleNode(newNode);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected NodeModel createStyleGroupNode(MapModel styleMap, String groupName) {
		NodeModel root = styleMap.getRootNode();
		NodeModel predefinedStyleParentNode = getStyleNodeGroup(styleMap, groupName);
		if (predefinedStyleParentNode == null) {
			predefinedStyleParentNode = new NodeModel(styleMap);
			predefinedStyleParentNode.setUserObject(new StyleTranslatedObject(groupName));
			root.insert(predefinedStyleParentNode);
		}
		NodeStyleModel.setShape(predefinedStyleParentNode, NodeStyleModel.Shape.bubble);
		return predefinedStyleParentNode;
	}

	private void createNodeStyleMap(final NodeModel node) {
		if (node.hasChildren()) {
			final Enumeration<NodeModel> children = node.children();
			while (children.hasMoreElements()) {
				createNodeStyleMap(children.nextElement());
			}
			return;
		}
		if (node.depth() >= 2) {
			addStyleNode(node);
		}
	}

	public void addStyleNode(final NodeModel node) {
		final IStyle userObject = (IStyle) node.getUserObject();
		if (null == styleNodes.put(userObject, node))
			stylesComboBoxModel.addElement(userObject);
	}

	private void initStylesComboBoxModel() {
		stylesComboBoxModel.removeAllElements();
		for (IStyle s : getStyles())
			stylesComboBoxModel.addElement(s);
	}

	public void removeStyleNode(final NodeModel node) {
		final Object userObject = node.getUserObject();
		if (null != styleNodes.remove(userObject))
			stylesComboBoxModel.removeElement(userObject);
	}

	public NodeModel getStyleNodeSafe(final IStyle style) {
		final NodeModel node = getStyleNode(style);
		if (node != null)
			return node;
		return getStyleNode(DEFAULT_STYLE);
	}

	public NodeModel getStyleNode(final IStyle style) {
		if (style instanceof StyleNode) {
			return ((StyleNode) style).getNode();
		}
		final NodeModel node = styleNodes.get(style);
		return node;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(final Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public Set<IStyle> getStyles() {
		return styleNodes.keySet();
	}

	private float zoom = 1f;

	public float getZoom() {
		return zoom;
	}

	public MapViewLayout getMapViewLayout() {
		return mapViewLayout;
	}

	void setMapViewLayout(final MapViewLayout mapViewLayout) {
		this.mapViewLayout = mapViewLayout;
	}

	void setZoom(final float zoom) {
		this.zoom = zoom;
	}

	private MapViewLayout mapViewLayout = MapViewLayout.MAP;
	private Quantity<LengthUnits> maxNodeWidth = null;
	private Quantity<LengthUnits> minNodeWidth = null;

	public void setMaxNodeWidth(final Quantity<LengthUnits> maxNodeWidth) {
		this.maxNodeWidth = maxNodeWidth;
	}

	public void setMinNodeWidth(final Quantity<LengthUnits> minNodeWidth) {
		this.minNodeWidth = minNodeWidth;
	}

	void copyFrom(MapStyleModel source, boolean overwrite) {
		if (overwrite && source.styleMap != null || styleMap == null) {
			styleMap = source.styleMap;
			styleNodes = source.styleNodes;
			initStylesComboBoxModel();
			conditionalStyleModel = source.conditionalStyleModel;
		}
		if (overwrite && source.backgroundColor != null || backgroundColor == null) {
			backgroundColor = source.backgroundColor;
		}
	}

	public void setProperty(String key, String value) {
		if (value != null) {
			properties.put(key, value);
		}
		else {
			properties.remove(key);
		}
	}

	public String getProperty(String key) {
		return properties.get(key);
	}

	public NodeModel getStyleNodeGroup(NodeModel styleNode) {
		final int depth = styleNode.depth();
		if (depth < 1)
			return null;
		NodeModel node = styleNode;
		for (int i = depth; i > 1; i--) {
			node = node.getParentNode();
		}
		return node;
	}

	public NodeModel getStyleNodeGroup(final MapModel styleMap, final String group) {
		final NodeModel rootNode = styleMap.getRootNode();
		final int childCount = rootNode.getChildCount();
		for (int i = 0; i < childCount; i++) {
			final NodeModel childNode = rootNode.getChildAt(i);
			final StyleTranslatedObject userObject = (StyleTranslatedObject) childNode.getUserObject();
			if (userObject.getObject().equals(group)) {
				return childNode;
			}
		}
		return null;
	}

	ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();

	ComboBoxModel getStylesAsComboBoxModel() {
		return stylesComboBoxModel;
	}
}
