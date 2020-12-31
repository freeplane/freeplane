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
import java.util.Objects;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataListener;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.attribute.FontSizeExtension;
import org.freeplane.features.cloud.CloudModel;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapReader;
import org.freeplane.features.map.MapWriter.Hint;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.NodeBuilder;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.nodestyle.NodeBorderModel;
import org.freeplane.features.nodestyle.NodeGeometryModel;
import org.freeplane.features.nodestyle.NodeSizeModel;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.view.swing.map.MapView;

/**
 * @author Dimitry Polivaev
 * Mar 12, 2009
 */
public class MapStyleModel implements IExtension {
	public static final String STYLES_PREDEFINED = "styles.predefined";
	public static final String STYLES_USER_DEFINED = "styles.user-defined";
    private static final StyleTranslatedObject STYLE_USER_DEFINED_TRANSLATED_OBJECT = new StyleTranslatedObject(STYLES_USER_DEFINED);
	public static final String STYLES_AUTOMATIC_LAYOUT = "styles.AutomaticLayout";
    public static final IStyle DEFAULT_STYLE = new StyleTranslatedObject("default");
    public static final IStyle SELECTION_STYLE = new StyleTranslatedObject("defaultstyle.selection");
	public static final IStyle DETAILS_STYLE = new StyleTranslatedObject("defaultstyle.details");
	public static final IStyle ATTRIBUTE_STYLE = new StyleTranslatedObject("defaultstyle.attributes");
	public static final IStyle NOTE_STYLE = new StyleTranslatedObject("defaultstyle.note");
	public static final IStyle FLOATING_STYLE = new StyleTranslatedObject("defaultstyle.floating");
	
    public static boolean isDefaultStyleNode(NodeModel node) {
        return node.getUserObject().equals(MapStyleModel.DEFAULT_STYLE);
    }
    
    public static boolean isStyleNode(NodeModel node) {
        return node.isLeaf() && node.getMap().getClass().equals(StyleMapModel.class);
    }
    

    public static boolean isUserStyleNode(NodeModel node) {
        return node.isLeaf() && node.getParentNode().getUserObject().equals(STYLE_USER_DEFINED_TRANSLATED_OBJECT);
    }
    
	private Map<IStyle, NodeModel> styleNodes;
	private NodeModel defaultStyleNode;
	private MapModel styleMap;
	private ConditionalStyleModel conditionalStyleModel;
	final private DefaultComboBoxModel stylesComboBoxModel;
	final private Map<String, String> properties;

	Map<String, String> getProperties() {
		return properties;
	}

	public static MapStyleModel getExtension(final MapModel map) {
		final MapStyleModel model = MapStyleModel.getExtension(map.getRootNode());
		return Objects.requireNonNull(model);
	}

	public MapModel getStyleMap() {
		return styleMap;
	}

	static MapStyleModel getExtension(final NodeModel node) {
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
		styleMap.putExtension(MapStyleModel.class, this);
		final NodeModel rootNode = styleMap.getRootNode();
		createNodeStyleMap(rootNode);
		styleMap.putExtension(IUndoHandler.class, map.getExtension(IUndoHandler.class));
		final MapStyleModel defaultStyleModel = new MapStyleModel();
		defaultStyleModel.styleNodes = styleNodes;
		defaultStyleModel.defaultStyleNode = styleNodes.get(DEFAULT_STYLE);
		initStylesComboBoxModel();
		rootNode.putExtension(defaultStyleModel);
	}

	public void refreshStyles() {
		final NodeModel rootNode = styleMap.getRootNode();
		styleNodes.clear();
		stylesComboBoxModel.removeAllElements();
		defaultStyleNode = null;
		createNodeStyleMap(rootNode);
	}

	void createStyleMap(final MapModel parentMap, final String styleMapStr) {
		final ModeController modeController = Controller.getCurrentModeController();
        MapModel styleMap = new StyleMapModel(parentMap.getIconRegistry(),
                AttributeRegistry.getRegistry(parentMap), modeController.getMapController());
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
			    NodeGeometryModel.NULL_SHAPE.withShape(NodeStyleModel.Shape.oval).withUniform(true));
			NodeStyleModel.createNodeStyleModel(root).setFontSize(24);
			styleMap.setRoot(root);
			final Quantity<LengthUnit> styleBlockGap = ResourceController.getResourceController()
			    .getLengthQuantityProperty("style_block_gap");
			LocationModel.createLocationModel(root).setVGap(styleBlockGap);
			insertStyleMap(parentMap, styleMap);
			NodeModel predefinedStyleParentNode = createStyleGroupNode(styleMap, STYLES_PREDEFINED);
			createStyleGroupNode(styleMap, STYLES_USER_DEFINED);
			createStyleGroupNode(styleMap, STYLES_AUTOMATIC_LAYOUT);
			if (defaultStyleNode == null) {
				defaultStyleNode = new NodeModel(DEFAULT_STYLE, styleMap);
				predefinedStyleParentNode.insert(defaultStyleNode, 0);
				addStyleNode(defaultStyleNode);
			}
			if (maxNodeWidth != null && null == NodeSizeModel.getMaxNodeWidth(defaultStyleNode))
				NodeSizeModel.setMaxNodeWidth(defaultStyleNode, maxNodeWidth);
			if (minNodeWidth != null && null == NodeSizeModel.getMinNodeWidth(defaultStyleNode))
				NodeSizeModel.setNodeMinWidth(defaultStyleNode, minNodeWidth);
			if (styleNodes.get(DETAILS_STYLE) == null) {
				final NodeModel newNode = new NodeModel(DETAILS_STYLE, styleMap);
				predefinedStyleParentNode.insert(newNode, 1);
				addStyleNode(newNode);
			}

			NodeLinks nodeLinks = NodeLinks.createLinkExtension(defaultStyleNode);
			if(nodeLinks.getLinks().isEmpty()) {
			    defaultStyleNode.createID();
			    LinkController linkController = LinkController.getController();
			    ConnectorModel connector = new ConnectorModel(defaultStyleNode, defaultStyleNode.getID(),
			            linkController.getStandardConnectorArrows(), linkController.getStandardDashArray(),
			            linkController.getStandardConnectorColor(), linkController.getStandardConnectorOpacity(),
			            linkController.getStandardConnectorShape(), linkController.getStandardConnectorWidth(),
			            linkController.getStandardLabelFontFamily(), linkController.getStandardLabelFontSize());
			    nodeLinks.addArrowlink(connector);
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
            if (styleNodes.get(SELECTION_STYLE) == null) {
                final NodeModel selectionStyleNode = new NodeModel(SELECTION_STYLE, styleMap);
                ResourceController resourceController = ResourceController.getResourceController();
                Color standardSelectionBackgroundColor = ColorUtils.stringToColor(resourceController.getProperty(
                        MapView.RESOURCES_SELECTED_NODE_COLOR));
                Color standardSelectionRectangleColor = ColorUtils.stringToColor(resourceController.getProperty(
                        MapView.RESOURCES_SELECTED_NODE_RECTANGLE_COLOR));
                NodeStyleModel.setShape(selectionStyleNode, NodeStyleModel.Shape.bubble);
                NodeStyleModel.setBackgroundColor(selectionStyleNode, standardSelectionBackgroundColor);
                NodeBorderModel.setBorderColor(selectionStyleNode, standardSelectionRectangleColor);
                NodeBorderModel.setBorderColorMatchesEdgeColor(selectionStyleNode, false);
                predefinedStyleParentNode.insert(selectionStyleNode, 5);
                addStyleNode(selectionStyleNode);
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
		if(userObject.equals(DEFAULT_STYLE))
		    defaultStyleNode = node;
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
		if(userObject.equals(DEFAULT_STYLE))
		    defaultStyleNode = null;
	}

	public NodeModel getStyleNodeSafe(final IStyle style) {
		final NodeModel node = getStyleNode(style);
		if (node != null)
			return node;
		return defaultStyleNode;
	}


    public NodeModel getDefaultStyleNode() {
        return defaultStyleNode;
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
	private Quantity<LengthUnit> maxNodeWidth = null;
	private Quantity<LengthUnit> minNodeWidth = null;

	public void setMaxNodeWidth(final Quantity<LengthUnit> maxNodeWidth) {
		this.maxNodeWidth = maxNodeWidth;
	}

	public void setMinNodeWidth(final Quantity<LengthUnit> minNodeWidth) {
		this.minNodeWidth = minNodeWidth;
	}

	void copyFrom(MapStyleModel source, boolean overwrite) {
		if (overwrite && source.styleMap != null || styleMap == null) {
			styleMap = source.styleMap;
			defaultStyleNode = styleNodes.get(DEFAULT_STYLE);
			styleMap.putExtension(MapStyleModel.class, this);
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
