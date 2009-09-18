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
package org.freeplane.features.common.addins.mapstyle;

import java.awt.Color;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.MapReader;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.MapWriter.Mode;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.features.mindmapmode.MMapModel;
import org.freeplane.features.mindmapmode.UMapModel;
import org.freeplane.n3.nanoxml.XMLException;

/**
 * @author Dimitry Polivaev
 * Mar 12, 2009
 */
public class MapStyleModel implements IExtension {
	private static final String DEFAULT_STYLE = "default";
	private static final String STYLES = "styles";
	final private MapModel styleMap;
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

	public MapStyleModel(ModeController modeController, String styleMapStr) {
		styleMap = new UMapModel(null, modeController){

			@Override
            public String getTitle() {
	            return ResourceBundles.getText(STYLES);
            }
			
		};
		if(styleMapStr == null){
			NodeModel root = new NodeModel(styleMap);
			root.setText(ResourceBundles.getText(STYLES));
			styleMap.setRoot(root);
			createDefaultStyle();
			return;
		}
		final MapReader mapReader = modeController.getMapController().getMapReader();
		try {
			NodeModel root = mapReader.createNodeTreeFromXml(styleMap, new StringReader(styleMapStr), Mode.FILE);
			styleMap.setRoot(root);
        }
        catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
        catch (XMLException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
        if(getStyleNode(DEFAULT_STYLE) == null){
        	createDefaultStyle();
        }
	}

	private void createDefaultStyle() {
	    NodeModel defaultStyle = new NodeModel(styleMap);
	    defaultStyle.setText(DEFAULT_STYLE);
	    styleMap.getRootNode().insert(defaultStyle, 0);
    }
	
	public NodeModel getStyleNode(final String style){
		final NodeModel rootNode = styleMap.getRootNode();
		final Enumeration<NodeModel> children = rootNode.children();
		while(children.hasMoreElements()){
			NodeModel child = children.nextElement();
			if(child.getText().equals(style)){
				return child;
			}
		}
		return null;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(final Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
}
