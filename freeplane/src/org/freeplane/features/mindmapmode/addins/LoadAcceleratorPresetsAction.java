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
package org.freeplane.features.mindmapmode.addins;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.mindmapmode.MModeController;

/**
 * @author Dimitry Polivaev
 * 04.07.2009
 */
public class LoadAcceleratorPresetsAction extends AFreeplaneAction {
	final private String propFileName;
	final private URL resource;
	/**
	 * A simple help function to get the directory where to search for XSLT 
	 * export files distributed with Freeplane.
	 * @return The system directory where XSLT export files are supposed to be.
	 */
	static private File getAcceleratorsSysDirectory() {
		return new File(ResourceController.getResourceController().getResourceBaseDir(), "accelerators");
	}

	/**
	 * A simple help function to get the directory where to search for XSLT 
	 * export files written by the user.
	 * @return The user directory where XSLT export files are supposed to be.
	 */
	static private File getAcceleratorsUserDirectory() {
		return new File(ResourceController.getResourceController().getFreeplaneUserDirectory(), "accelerators");
	}

	final static public void install(ModeController modecontroller){		
		File[] dirs = {getAcceleratorsUserDirectory(), getAcceleratorsSysDirectory()};
		final Controller controller = modecontroller.getController();
		final MenuBuilder menuBuilder = modecontroller.getUserInputListenerFactory().getMenuBuilder();
		
		for(File dir:dirs){
			final File[] fileList = dir.listFiles();
			if(fileList == null){
				continue;
			}
			for(File prop:fileList){
				final String fileName = prop.getName();
				if(prop.isDirectory()){
					continue;
				}
				if(! fileName.endsWith(".properties")){
					continue;
				}
				try {
	                int propNameLength = fileName.lastIndexOf('.');
	                String propName = fileName.substring(0, propNameLength);
	                final String key = "LoadAcceleratorPresetsAction." + propName;
	                if(controller.getAction(key) != null){
	                	continue;
	                }
	                String title = ResourceBundles.getText(key + ".text", propName);
	                final LoadAcceleratorPresetsAction loadAcceleratorPresetsAction = new LoadAcceleratorPresetsAction(prop.toURL(), key, title, controller);
	                controller.addAction(loadAcceleratorPresetsAction);
	                menuBuilder.addAction("/menu_bar/extras/first/options/acceleratorPresets", key, loadAcceleratorPresetsAction, MenuBuilder.AS_CHILD);
                }
                catch (Exception e) {
                	UITools.errorMessage("can not load accelerators from" + prop.getPath());
                }
			}
		}
	}
	LoadAcceleratorPresetsAction(URL resource, String propFileName, String title, Controller controller) {
	    super("LoadAcceleratorPresetsAction." + propFileName, controller, title, null);
	    this.resource = resource;
	    this.propFileName = propFileName;
    }

	/**
     * 
     */
    private static final long serialVersionUID = 1L;

	public void actionPerformed(ActionEvent e) {
		try {
	        MenuBuilder.loadAcceleratorPresets(resource.openStream(), getController());
        }
        catch (IOException e1) {
	        // TODO Auto-generated catch block
	        e1.printStackTrace();
        }
	}
}
