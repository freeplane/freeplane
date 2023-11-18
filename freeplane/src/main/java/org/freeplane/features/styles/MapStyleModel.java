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
import java.util.List;
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
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.attribute.FontSizeExtension;
import org.freeplane.features.attribute.mindmapmode.MAttributeController;
import org.freeplane.features.cloud.CloudModel;
import org.freeplane.features.cloud.CloudShape;
import org.freeplane.features.edge.EdgeColorsConfigurationFactory;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.icon.mindmapmode.MIconController;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapReader;
import org.freeplane.features.map.MapWriter.Hint;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.NodeModel.Side;
import org.freeplane.features.map.NodeBuilder;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.nodestyle.NodeBorderModel;
import org.freeplane.features.nodestyle.NodeGeometryModel;
import org.freeplane.features.nodestyle.NodeSizeModel;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.NodeStyleShape;
import org.freeplane.features.url.MapVersionInterpreter;
import org.freeplane.view.swing.map.MapView;
/**
 * @author Dimitry Polivaev
 * Mar 12, 2009
 */
public class MapStyleModel implements IExtension {
    private static final int FREEPLANE_VERSION_WITH_RICH_SELECTION_STYLE = 14;
	public static final String STYLES_PREDEFINED = "styles.predefined";
    public static final String STYLES_USER_DEFINED = "styles.user-defined";
    private static final StyleTranslatedObject STYLE_USER_DEFINED_TRANSLATED_OBJECT = new StyleTranslatedObject(STYLES_USER_DEFINED);
    private static final StyleTranslatedObject STYLE_PREDEFINED_TRANSLATED_OBJECT = new StyleTranslatedObject(STYLES_PREDEFINED);
	public static final String STYLES_AUTOMATIC_LAYOUT = "styles.AutomaticLayout";
    public static final IStyle DEFAULT_STYLE = new StyleTranslatedObject("default");
    public static final IStyle NEW_STYLE = new StyleTranslatedObject("newStyle");
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

    public static boolean isPredefinedStyleNode(NodeModel node) {
        return node.isLeaf() && node.getParentNode().getUserObject().equals(STYLE_PREDEFINED_TRANSLATED_OBJECT);
    }

	private Map<IStyle, NodeModel> styleNodes;
	private NodeModel defaultStyleNode;
	private MapModel styleMap;
	private ConditionalStyleModel conditionalStyleModel;
	private final DefaultComboBoxModel stylesComboBoxModel;
	private final Map<String, String> properties;

	Map<String, String> getProperties() {
		return properties;
	}

	public static MapStyleModel getExtension(final MapModel map) {
		MapStyleModel model = map.getRootNode().getExtension(MapStyleModel.class);
		if(model == null)
			model = map.getExtension(MapStyleModel.class);
		return Objects.requireNonNull(model);
	}

	public MapModel getStyleMap() {
		return styleMap;
	}

	static MapStyleModel getExtensionOrNull(final NodeModel node) {
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
		initStylesComboBoxModel();
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
        MapModel styleMap = new StyleMapModel(parentMap.getNodeDuplicator(), parentMap.getIconRegistry(),
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
			    NodeGeometryModel.NULL_SHAPE.withShape(NodeStyleShape.oval).withUniform(true));
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
				CloudModel.createModel(newNode).setShape(CloudShape.ROUND_RECT);
				predefinedStyleParentNode.insert(newNode, 4);
				addStyleNode(newNode);
			}
            NodeModel selectionStyleNode = styleNodes.get(SELECTION_STYLE);
            if (selectionStyleNode == null) {
                selectionStyleNode = new NodeModel(SELECTION_STYLE, styleMap);
                ResourceController resourceController = ResourceController.getResourceController();
                Color standardSelectionBackgroundColor = ColorUtils.stringToColor(resourceController.getProperty(
                        MapView.RESOURCES_SELECTED_NODE_COLOR));
                Color standardSelectionRectangleColor = ColorUtils.stringToColor(resourceController.getProperty(
                        MapView.RESOURCES_SELECTED_NODE_RECTANGLE_COLOR));
                NodeStyleModel.setBackgroundColor(selectionStyleNode, standardSelectionBackgroundColor);
                NodeBorderModel.setBorderColor(selectionStyleNode, standardSelectionRectangleColor);
                NodeBorderModel.setBorderColorMatchesEdgeColor(selectionStyleNode, false);
                predefinedStyleParentNode.insert(selectionStyleNode, 5);
                addStyleNode(selectionStyleNode);
            }
            else if (MapVersionInterpreter.isOlderThan(parentMap, FREEPLANE_VERSION_WITH_RICH_SELECTION_STYLE)) {
                NodeStyleModel.setShapeConfiguration(selectionStyleNode, NodeGeometryModel.NULL_SHAPE);
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
			predefinedStyleParentNode.setSide(Side.BOTTOM_OR_RIGHT);
			root.insert(predefinedStyleParentNode);
		}
		NodeStyleModel.setShape(predefinedStyleParentNode, NodeStyleShape.bubble);
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
		Object userObject = node.getUserObject();
		if(! (userObject instanceof IStyle)) {
		    String description = userObject != null ? userObject + ", " + userObject.getClass().getName() : "null";
            LogUtils.severe("Bad user object " + description);
            return;
		}
        final IStyle style = (IStyle) userObject;
		if (null == styleNodes.put(style, node) && ! isPredefinedStyleNode(node))
			stylesComboBoxModel.addElement(style);
		if(style.equals(DEFAULT_STYLE))
		    defaultStyleNode = node;
	}

	private void initStylesComboBoxModel() {
		stylesComboBoxModel.removeAllElements();
		stylesComboBoxModel.addElement(DEFAULT_STYLE);
		stylesComboBoxModel.addElement(FLOATING_STYLE);
		NodeModel userStyleParentNode = getStyleNodeGroup(styleMap, MapStyleModel.STYLES_USER_DEFINED);
		if(userStyleParentNode != null)
		    for (NodeModel userStyleNode: userStyleParentNode.getChildren())
		        stylesComboBoxModel.addElement(userStyleNode.getUserObject());
        NodeModel levelStyleParentNode = getStyleNodeGroup(styleMap, MapStyleModel.STYLES_AUTOMATIC_LAYOUT);
        if(levelStyleParentNode != null)
            for (NodeModel userStyleNode: levelStyleParentNode.getChildren())
                stylesComboBoxModel.addElement(userStyleNode.getUserObject());
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

    public List<IStyle> getNodeStyles() {
        NodeModel userStyles = getStyleNodeGroup(styleMap, STYLES_USER_DEFINED);
        NodeModel levelStyles = getStyleNodeGroup(styleMap, STYLES_AUTOMATIC_LAYOUT);
        ArrayList<IStyle> styles = new ArrayList<IStyle>(2 + userStyles.getChildCount() + levelStyles.getChildCount());
        styles.add(DEFAULT_STYLE);
        styles.add(FLOATING_STYLE);
        for(NodeModel styleNode : userStyles.getChildren()) {
            IStyle style = (IStyle) styleNode.getUserObject();
            styles.add(style);
        }
        for(NodeModel styleNode : levelStyles.getChildren()) {
            IStyle style = (IStyle) styleNode.getUserObject();
            styles.add(style);
        }

        return styles;
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

	void setStylesFrom(MapStyleModel source, boolean overwrite) {
		if (overwrite && source.styleMap != null || styleMap == null) {
			setStylesFrom(source);
		}
		if (overwrite && source.backgroundColor != null || backgroundColor == null) {
			setBackgroundFrom(source);
		}
	}

    private void setBackgroundFrom(MapStyleModel source) {
        backgroundColor = source.backgroundColor;
    }

    private void setStylesFrom(MapStyleModel source) {
        styleMap = source.styleMap;
        styleMap.putExtension(MapStyleModel.class, this);
        styleNodes = source.styleNodes;
        defaultStyleNode = styleNodes.get(DEFAULT_STYLE);
        initStylesComboBoxModel();
        conditionalStyleModel = source.conditionalStyleModel;
    }

    void setNonStyleUserPropertiesFrom(MapStyleModel oldStyleModel) {
        String edgeColorConfiguration = properties.get(EdgeColorsConfigurationFactory.EDGE_COLOR_CONFIGURATION_PROPERTY);
        properties.clear();
        properties.putAll(oldStyleModel.properties);
        if(edgeColorConfiguration != null) {
            properties.put(EdgeColorsConfigurationFactory.EDGE_COLOR_CONFIGURATION_PROPERTY, edgeColorConfiguration);
        }
    }


    void addUserStylesFrom(MapStyleModel source) {

        NodeModel targetGroup = getStyleNodeGroup(styleMap, STYLES_USER_DEFINED);
        NodeModel sourceGroup = getStyleNodeGroup(source.styleMap, STYLES_USER_DEFINED);

        for(NodeModel styleNode : sourceGroup.getChildren()) {
            IStyle sourceStyle = (IStyle) styleNode.getUserObject();
            if(! styleNodes.containsKey(sourceStyle)) {
                NodeModel duplicate = styleNode.duplicate(false);
                duplicate.setMap(targetGroup.getMap());
                targetGroup.insert(duplicate);
                styleNodes.put(sourceStyle, duplicate);
                stylesComboBoxModel.addElement(sourceStyle);
            }
        }
    }

    void addConditionalStylesFrom(MapStyleModel source) {
        conditionalStyleModel.addDifferentConditions(source.conditionalStyleModel);
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
  public static final String FOLLOWED_MAP_LOCATION_PROPERTY = "followedMapLocation";
  public static final String FOLLOWED_TEMPLATE_LOCATION_PROPERTY = "followedTemplateLocation";
    public static final String ASSOCIATED_TEMPLATE_LOCATION_PROPERTY = "associatedTemplateLocation";
    public static final String FOLLOWED_MAP_LAST_TIME = "followedMapLastTime";

	ComboBoxModel getStylesAsComboBoxModel() {
		return stylesComboBoxModel;
	}

    void copyStyle(NodeModel copiedStyleNode, IStyle styleKey) {
        NodeModel targetStyleNode = getStyleNode(styleKey);
        ModeController modeController = Controller.getCurrentModeController();
        if(targetStyleNode == null) {
            NodeModel sourceGroupNode = copiedStyleNode.getParentNode();
            String group = (String) ((StyleTranslatedObject)sourceGroupNode.getUserObject()).getObject();
            final MapModel styleMap = getStyleMap();
            NodeModel targetGroupNode = getStyleNodeGroup(styleMap, group);
            if(group.equals(MapStyleModel.STYLES_AUTOMATIC_LAYOUT)) {
                while(targetGroupNode.getChildCount() < sourceGroupNode.getChildCount() - 1) {
                    NodeModel source = sourceGroupNode.getChildAt(targetGroupNode.getChildCount());
                    targetStyleNode = new NodeModel(styleMap);
                    targetStyleNode.setUserObject(source.getUserObject());
                    targetGroupNode.insert(targetStyleNode);
                    modeController.copyExtensions(LogicalStyleKeys.NODE_STYLE, source, targetStyleNode);
                    addStyleNode(targetStyleNode);
                }
            }
            targetStyleNode = new NodeModel(styleMap);
            targetStyleNode.setUserObject(copiedStyleNode.getUserObject());
            targetGroupNode.insert(targetStyleNode);
            addStyleNode(targetStyleNode);
        } else {
            modeController.removeExtensions(LogicalStyleKeys.NODE_STYLE, targetStyleNode, targetStyleNode);
            modeController.removeExtensions(MIconController.Keys.ICONS, targetStyleNode, targetStyleNode);
        }
        modeController.copyExtensions(LogicalStyleKeys.NODE_STYLE, copiedStyleNode, targetStyleNode);
        modeController.copyExtensions(MIconController.Keys.ICONS, copiedStyleNode, targetStyleNode);
        MAttributeController.getController().copyAttributesToNode(copiedStyleNode, targetStyleNode);
    }

}
