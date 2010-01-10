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
package org.freeplane.features.mindmapmode.addins.styles;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenu;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.extension.IExtensionCopier;
import org.freeplane.core.frame.IMapSelectionListener;
import org.freeplane.core.modecontroller.IMapChangeListener;
import org.freeplane.core.modecontroller.INodeChangeListener;
import org.freeplane.core.modecontroller.INodeSelectionListener;
import org.freeplane.core.modecontroller.MapChangeEvent;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.modecontroller.NodeChangeEvent;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.common.addins.styles.LogicalStyleController;
import org.freeplane.features.common.addins.styles.LogicalStyleKeys;
import org.freeplane.features.common.addins.styles.LogicalStyleModel;
import org.freeplane.features.common.addins.styles.MapStyle;
import org.freeplane.features.common.addins.styles.MapStyleModel;
import org.freeplane.features.common.cloud.CloudModel;
import org.freeplane.features.common.nodestyle.NodeStyleModel;

/**
 * @author Dimitry Polivaev
 * 28.09.2009
 */
public class MLogicalStyleController extends LogicalStyleController {
	
	private static class  StyleRemover implements INodeChangeListener{
		public StyleRemover(){
		}
		public void nodeChanged(NodeChangeEvent event) {
	        final ModeController modeController = event.getModeController();
			if(modeController == null || modeController.isUndoAction()){
	        	return ;
	        }
	        if(! event.getProperty().equals(LogicalStyleModel.class)){
	        	return;
	        }
	        final NodeModel node = event.getNode();
	        final MapModel map = node.getMap();
	        final Object styleKey = event.getNewValue();
	       	final MapStyleModel mapStyles = MapStyleModel.getExtension(map);
	    	final NodeModel styleNode = mapStyles.getStyleNode(styleKey);
	    	if(styleNode == null){
	    		return;
	    	}
	    	modeController.undoableRemoveExtensions(LogicalStyleKeys.NODE_STYLE, node, styleNode);
        }

	};
	
	private static class ExtensionCopier implements IExtensionCopier{

		public void copy(Object key, NodeModel from, NodeModel to) {
			if(! key.equals(LogicalStyleKeys.LOGICAL_STYLE)){
				return;
			}
			copy(from, to);
		}

		public void copy(NodeModel from, NodeModel to) {
			final LogicalStyleModel fromStyle = (LogicalStyleModel) from.getExtension(LogicalStyleModel.class);
			if(fromStyle == null){
				return;
			}
			LogicalStyleModel toStyle = LogicalStyleModel.createExtension(to);
			toStyle.setStyle(fromStyle.getStyle());
		}

		public void remove(Object key, NodeModel from) {
			if(! key.equals(LogicalStyleKeys.LOGICAL_STYLE)){
				return;
			}
			from.removeExtension(CloudModel.class);
		}

		public void remove(Object key, NodeModel from, NodeModel which) {
			if(! key.equals(LogicalStyleKeys.LOGICAL_STYLE)){
				return;
			}
	    	final LogicalStyleModel whichStyle = (LogicalStyleModel) which.getExtension(LogicalStyleModel.class);
	    	if(whichStyle == null){
	    		return;
	    	}
	    	final LogicalStyleModel fromStyle = (LogicalStyleModel) from.getExtension(LogicalStyleModel.class);
	    	if(fromStyle == null){
	    		return;
	    	}
	    	from.removeExtension(fromStyle);
		}
		
	}

	private ModeController modeController;
	final private List<AssignStyleAction> actions;

	public MLogicalStyleController(ModeController modeController) {
	    super(modeController);
	    this.modeController = modeController;
	    modeController.getMapController().addNodeChangeListener(new StyleRemover());
		modeController.registerExtensionCopier(new ExtensionCopier());
		modeController.addAction(new RedefineStyleAction(modeController.getController()));
	    actions = new LinkedList<AssignStyleAction>();
	    final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
	    modeController.getController().getMapViewManager().addMapSelectionListener(new IMapSelectionListener() {
			public void beforeMapChange(MapModel oldMap, MapModel newMap) {
				removeStyleMenu(menuBuilder, "/menu_bar");
				removeStyleMenu(menuBuilder, "/node_popup");
			}
			
			public void afterMapClose(MapModel oldMap) {
			}
			
			public void afterMapChange(MapModel oldMap, MapModel newMap) {
				addStyleMenu(menuBuilder, "/menu_bar", newMap);
				addStyleMenu(menuBuilder, "/node_popup", newMap);
			}
		});
	    
	    final MapController mapController = modeController.getMapController();
		mapController.addMapChangeListener(new IMapChangeListener() {
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
					removeStyleMenu(menuBuilder, "/menu_bar");
					addStyleMenu(menuBuilder, "/menu_bar", event.getMap());
					removeStyleMenu(menuBuilder, "/node_popup");
					addStyleMenu(menuBuilder, "/node_popup", event.getMap());
				}
			}
		});
		mapController.addNodeSelectionListener(new INodeSelectionListener() {
			public void onSelect(NodeModel node) {
				selectActions();
			}
			
			public void onDeselect(NodeModel node) {

			}
		});
    }
	
	protected void removeStyleMenu(MenuBuilder menuBuilder, final String formatMenuString) {
		menuBuilder.removeChildElements(formatMenuString + "/styles/assign");
		actions.clear();
	    
    }

	protected void addStyleMenu(MenuBuilder menuBuilder, final String formatMenuString, MapModel newMap) {
	    if(newMap == null){
	    	return;
	    }
	    final MapStyleModel extension = MapStyleModel.getExtension(newMap);
	    if(extension == null){
	    	return;
	    }
	    final NodeModel rootNode = extension.getStyleMap().getRootNode();
	    addStyleMenu(menuBuilder, formatMenuString+ "/styles/assign", rootNode);
    }

	private void addStyleMenu(MenuBuilder menuBuilder, String category, NodeModel rootNode) {
		final List<NodeModel> children = rootNode.getChildren();
		for(NodeModel child:children){
		    Object style = child.getUserObject();
			if(child.hasChildren()){
				final String newCategory = category + '/' + style;
				menuBuilder.addMenuItem(category, new JMenu(style.toString()), newCategory, MenuBuilder.AS_CHILD);
				addStyleMenu(menuBuilder, newCategory, child);
			}
			else{
			    final AssignStyleAction action = new AssignStyleAction(style, modeController.getController(), style.toString(), null);
			    actions.add(action);
			    menuBuilder.addAction(category, action, MenuBuilder.AS_CHILD);
			}
		}
	    
    }

	public void setStyle(final NodeModel node, final Object style)
	{
		final LogicalStyleModel model = LogicalStyleModel.createExtension(node);
		final Object oldStyle = model.getStyle();
	    if(oldStyle != null && oldStyle.equals(style) || oldStyle == style){
			modeController.getMapController().nodeChanged(node, LogicalStyleModel.class, oldStyle, style);
	    	return;
	    }
		IActor actor = new IActor() {
			public void undo() {
				model.setStyle(oldStyle);
				modeController.getMapController().nodeChanged(node, LogicalStyleModel.class, style, oldStyle);
				selectActions();
			}
			
			public String getDescription() {
				return "setStyle";
			}
			
			public void act() {
				model.setStyle(style);
				modeController.getMapController().nodeChanged(node, LogicalStyleModel.class, oldStyle, style);
				selectActions();
			}
		};
		modeController.execute(actor, node.getMap());
	}

	 void selectActions() {
	    for(AssignStyleAction action:actions){
	    	action.setSelected();
	    }
    }

	public void setStyle(Object style) {
		List<NodeModel> selectedNodes = modeController.getMapController().getSelectedNodes();
		for (final NodeModel selected  : selectedNodes)
		{
			setStyle(selected, style);
		}
	}
}
