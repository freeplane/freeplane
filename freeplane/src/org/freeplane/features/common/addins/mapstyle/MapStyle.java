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
import java.awt.event.ActionEvent;

import org.freeplane.core.addins.NodeHookDescriptor;
import org.freeplane.core.addins.PersistentNodeHook;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.enums.ResourceControllerProperties;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.frame.ColorTracker;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.modecontroller.IMapLifeCycleListener;
import org.freeplane.core.modecontroller.MapChangeEvent;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.FpColor;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.FreeplaneResourceBundle;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ActionDescriptor;
import org.freeplane.core.undo.IUndoableActor;
import org.freeplane.n3.nanoxml.IXMLElement;
import org.freeplane.view.swing.map.MapView;

/**
 * @author Dimitry Polivaev
 * Mar 9, 2009
 */
@NodeHookDescriptor(hookName="MapStyle")
public class MapStyle extends PersistentNodeHook implements IExtension, IMapLifeCycleListener{
	public static class Model implements IExtension{
		private Color backgroundColor;

		public Model() {
        }

		protected void setBackgroundColor(Color backgroundColor) {
        	this.backgroundColor = backgroundColor;
        }

		protected Color getBackgroundColor() {
        	return backgroundColor;
        }

		public static Model createExtension(NodeModel node) {
	        Model extension = (Model)node.getExtension(Model.class);
	        if(extension == null){
	        	extension = new Model();
	        	node.addExtension(extension);
	        }
			return extension;
        }
		public static Model getExtension(NodeModel node) {
	        return (Model)node.getExtension(Model.class);
        }
	}
	
	@ActionDescriptor(name = "MapBackgroundColor", //
		locations = { "/menu_bar/format/nodes" })
	private class MapBackgroundColorAction extends HookAction {
        private static final long serialVersionUID = 1L;

		public void actionPerformed(final ActionEvent e){
			final Controller controller = getController();
			Model model = (Model)getMapHook();
			final Color oldBackgroundColor;
			if(model != null){
				oldBackgroundColor = model.getBackgroundColor();
			}
			else{
				final String colorPropertyString = ResourceController.getResourceController().getProperty(ResourceControllerProperties.RESOURCES_BACKGROUND_COLOR);
				oldBackgroundColor = new FpColor(colorPropertyString).getColor();
			}

			final Color actionColor = ColorTracker.showCommonJColorChooserDialog(controller, controller.getSelection()
			    .getSelected(), FreeplaneResourceBundle.getText("choose_map_background_color"), 
			    oldBackgroundColor);
			setBackgroundColor(model, actionColor);
		}

	}

	public MapStyle(ModeController modeController) {
	    super(modeController);
	    registerAction(new MapBackgroundColorAction());
	    modeController.getMapController().addMapLifeCycleListener(this);
   }
	
	public void setBackgroundColor(final Model model, final Color actionColor) {
		final Color oldColor = model.getBackgroundColor();
			if(actionColor == oldColor || actionColor != null && actionColor.equals(oldColor)){
				return;
        }
		IUndoableActor actor = new IUndoableActor(){
			public void act() {
				model.setBackgroundColor(actionColor);
				getModeController().getMapController().fireMapChanged(
				    new MapChangeEvent(getController().getMap(), 
				    	ResourceControllerProperties.RESOURCES_BACKGROUND_COLOR,
				    	oldColor, actionColor));
			}
			public String getDescription() {
	            return "MapStyle.setBackgroundColor";
            }

			public void undo() {
				model.setBackgroundColor(oldColor);
				getModeController().getMapController().fireMapChanged(
					new MapChangeEvent(getController().getMap(), 
				    	ResourceControllerProperties.RESOURCES_BACKGROUND_COLOR,
				    	actionColor, oldColor));
            }};
		getModeController().execute(actor);
    }
	@Override
	protected IExtension createExtension(final NodeModel node, final IXMLElement element) {
		String colorString = element.getAttribute("background", null);
		final Color bgColor;
		if(colorString != null){
			bgColor = new FpColor(colorString).getColor();
		}
		else{
			bgColor = null; 
		}
		final Model model = new Model(); 
		model.setBackgroundColor(bgColor);
		return model;
	}
	
	@Override
	protected Class getExtensionClass() {
		return Model.class;
	}
	
	public Color getBackground(MapModel map){
		final IExtension extension = map.getRootNode().getExtension(Model.class);
		final Color backgroundColor = extension != null ? ((Model)extension).getBackgroundColor() : null;
		if (backgroundColor != null){
			return backgroundColor;
		}
		String stdcolor = ResourceController.getResourceController().getProperty(
			ResourceControllerProperties.RESOURCES_BACKGROUND_COLOR);
		Color standardMapBackgroundColor = TreeXmlReader.xmlToColor(stdcolor);
		return standardMapBackgroundColor;

	}
	@Override
    protected void saveExtension(IExtension extension, IXMLElement element) {
	    super.saveExtension(extension, element);
	    final Color backgroundColor = ((Model)extension).getBackgroundColor();
	    if(backgroundColor != null){
	    	element.setAttribute("background", FpColor.colorToXml(backgroundColor));
	    }
    }
	
	public void onCreate(MapModel map) {
        final NodeModel rootNode = map.getRootNode();
        if(rootNode.containsExtension(Model.class)){
        	return;
        }
        rootNode.addExtension(new Model());
        
    }

	public void onRemove(MapModel map) {
    }

}
