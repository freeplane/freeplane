/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.features.mindmapmode.addins.mapstyle;

import java.util.Collection;

import org.freeplane.core.frame.IMapSelectionListener;
import org.freeplane.core.modecontroller.IMapChangeListener;
import org.freeplane.core.modecontroller.MapChangeEvent;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.common.addins.mapstyle.MapStyle;
import org.freeplane.features.common.addins.mapstyle.MapStyleModel;
import org.freeplane.features.common.addins.mapstyle.LogicalStyleController;
import org.freeplane.features.common.addins.mapstyle.NodeStyleModel;

/**
 * @author Dimitry Polivaev
 * 28.09.2009
 */
public class MLogicalStyleController extends LogicalStyleController {

	private ModeController modeController;

	public MLogicalStyleController(ModeController modeController) {
	    super(modeController);
	    this.modeController = modeController;
	    final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
	    modeController.getController().getMapViewManager().addMapSelectionListener(new IMapSelectionListener() {
			public void beforeMapChange(MapModel oldMap, MapModel newMap) {
				removeStyleMenu(menuBuilder, "/menu_bar/format");
			}
			
			public void afterMapClose(MapModel oldMap) {
			}
			
			public void afterMapChange(MapModel oldMap, MapModel newMap) {
				addStyleMenu(menuBuilder, "/menu_bar/format", newMap);
			}
		});
	    
	    modeController.getMapController().addMapChangeListener(new IMapChangeListener() {
			public void onPreNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
			}
			
			public void onPreNodeDelete(NodeModel oldParent, NodeModel selectedNode, int index) {
			}
			
			public void onNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
			}
			
			public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
			}
			
			public void onNodeDeleted(NodeModel parent, NodeModel child, int index) {
			}
			
			public void mapChanged(MapChangeEvent event) {
				if(event.getProperty().equals(MapStyle.MAP_STYLES)){
					removeStyleMenu(menuBuilder, "/menu_bar/format");
					addStyleMenu(menuBuilder, "/menu_bar/format", event.getMap());
				}
			}
		});
    }
	
	protected void removeStyleMenu(MenuBuilder menuBuilder, final String formatMenuString) {
		menuBuilder.removeChildElements(formatMenuString + "/styles/assign");
	    
    }

	protected void addStyleMenu(MenuBuilder menuBuilder, final String formatMenuString, MapModel newMap) {
	    if(newMap == null){
	    	return;
	    }
	    final MapStyleModel extension = MapStyleModel.getExtension(newMap);
	    if(extension == null){
	    	return;
	    }
		final Collection<Object> styles = extension.getStyles();
	    for (Object style:styles){
	    	final String key;
	    	if(style instanceof NamedObject){
	    		key = ((NamedObject)style).getObject().toString();
	    	}
	    	else{
	    		key = style.toString();
	    	}
	    	final AssignStyleAction action = new AssignStyleAction(key, modeController.getController(), style.toString(), null);
	    	menuBuilder.addAction(formatMenuString+ "/styles/assign", action, MenuBuilder.AS_CHILD);
	    }
	    
    }

	public void setStyle(final NodeModel node, final String style)
	{
		final NodeStyleModel model = NodeStyleModel.createExtension(node);
		final String oldStyle = model.getStyle();
	    if(oldStyle != null && oldStyle.equals(style) || oldStyle == style){
	    	return;
	    }
		IActor actor = new IActor() {
			public void undo() {
				model.setStyle(oldStyle);
				modeController.getMapController().nodeChanged(node, NodeStyleModel.class, style, oldStyle);
			}
			
			public String getDescription() {
				return "setStyle";
			}
			
			public void act() {
				model.setStyle(style);
				modeController.getMapController().nodeChanged(node, NodeStyleModel.class, oldStyle, style);
			}
		};
		modeController.execute(actor, node.getMap());
	}
}
