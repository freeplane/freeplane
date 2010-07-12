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
package org.freeplane.features.common.styles;

import java.awt.Color;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.MapReader;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.map.MapWriter.Mode;

/**
 * @author Dimitry Polivaev
 * Mar 12, 2009
 */
public class MapStyleModel implements IExtension {
	public static final NamedObject DEFAULT_STYLE = new NamedObject("default");
	private static final String STYLES = "styles";
	private Map<Object, NodeModel> styleNodes;
	private static boolean loadingStyleMap = false;
	private MapModel styleMap;
	final private ConditionalStyleModel conditionalStyleModel;

	public static MapStyleModel getExtension(final MapModel map) {
		return MapStyleModel.getExtension(map.getRootNode());
	}

	public MapModel getStyleMap() {
		return styleMap;
	}

	public static MapStyleModel getExtension(final NodeModel node) {
		return (MapStyleModel) node.getExtension(MapStyleModel.class);
	}

	private Color backgroundColor;

	public MapStyleModel() {
		conditionalStyleModel = new ConditionalStyleModel();
	}

	public ConditionalStyleModel getConditionalStyleModel() {
    	return conditionalStyleModel;
    }

	void createStyleMap(final MapModel parentMap, MapStyleModel mapStyleModel, final ModeController modeController, final String styleMapStr) {
		if (loadingStyleMap) {
			styleMap = null;
			styleNodes = null;
			return;
		}
		styleNodes = new LinkedHashMap<Object, NodeModel>();
		styleMap = new MapModel(modeController, null) {
			@Override
			public String getTitle() {
				return TextUtils.removeMnemonic(TextUtils.getText(STYLES));
			}
		};
		if(mapStyleModel != null){
			styleMap.addExtension(mapStyleModel.getConditionalStyleModel());
		}
		styleMap.addExtension(IUndoHandler.class, parentMap.getExtension(IUndoHandler.class));
		final MapReader mapReader = modeController.getMapController().getMapReader();
		NodeModel root;
		try {
			if (styleMapStr != null) {
				final Reader styleReader;
				styleReader = new StringReader(styleMapStr);
				root = mapReader.createNodeTreeFromXml(styleMap, styleReader, Mode.FILE);
			}
			else {
				loadingStyleMap = true;
				try {
					final ResourceController resourceController = ResourceController.getResourceController();
					final File freeplaneUserDirectory = new File(resourceController.getFreeplaneUserDirectory());
					final File styles = new File(freeplaneUserDirectory, "default.stylemm");
					try {
						root = load(styles.toURL(), mapReader, styleMap);
					}
					catch (final Exception e) {
						root = load(ResourceController.getResourceController().getResource("/styles/default.stylemm"),
						    mapReader, styleMap);
					}
				}
				finally {
					loadingStyleMap = false;
				}
			}
			if(mapStyleModel != null){
				styleMap.removeExtension(mapStyleModel.getConditionalStyleModel());
			}
			styleMap.setRoot(root);
			MapStyleModel extension = MapStyleModel.getExtension(styleMap);
			if (extension == null) {
				loadingStyleMap = true;
				try {
					extension = new MapStyleModel();
					styleMap.getRootNode().addExtension(extension);
				}
				finally {
					loadingStyleMap = false;
				}
			}
			extension.styleNodes = styleNodes;
			createNodeStyleMap(root);
			styleMap.setReadOnly(false);
		}
		catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		final Object userObject = node.getUserObject();
		styleNodes.put(userObject, node);
	}

	public void removeStyleNode(final NodeModel node) {
		final Object userObject = node.getUserObject();
		styleNodes.remove(userObject);
	}

	private NodeModel load(final URL url, final MapReader mapReader, final MapModel map) throws Exception {
		InputStreamReader urlStreamReader = null;
		urlStreamReader = new InputStreamReader(url.openStream());
		final NodeModel root = mapReader.createNodeTreeFromXml(map, urlStreamReader, Mode.FILE);
		urlStreamReader.close();
		return root;
	}

	public NodeModel getStyleNode(final Object style) {
		return styleNodes.get(style);
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(final Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public Collection<Object> getStyles() {
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
	private int maxNodeWidth = MapStyleModel.getDefaultMaxNodeWidth();

	public int getMaxNodeWidth() {
		return maxNodeWidth;
	}

	public void setMaxNodeWidth(final int maxNodeWidth) {
		this.maxNodeWidth = maxNodeWidth;
	}

	static int getDefaultMaxNodeWidth() {
		try {
			return Integer.parseInt(ResourceController.getResourceController().getProperty("max_node_width"));
		}
		catch (final NumberFormatException e) {
			return Integer.parseInt(ResourceController.getResourceController().getProperty(
			    "el__max_default_window_width")) * 2 / 3;
		}
	}
}
