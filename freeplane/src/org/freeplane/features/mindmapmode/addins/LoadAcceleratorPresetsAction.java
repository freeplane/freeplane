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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;

/**
 * @author Dimitry Polivaev
 * 04.07.2009
 */
public class LoadAcceleratorPresetsAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
	static File getAcceleratorsUserDirectory() {
		return new File(ResourceController.getResourceController().getFreeplaneUserDirectory(), "accelerators");
	}

	final static public void install(final ModeController modecontroller) {
		final File[] dirs = { LoadAcceleratorPresetsAction.getAcceleratorsUserDirectory(),
		        LoadAcceleratorPresetsAction.getAcceleratorsSysDirectory() };
		final Controller controller = modecontroller.getController();
		final SaveAcceleratorPresetsAction saveAction = new SaveAcceleratorPresetsAction(controller);
		controller.addAction(saveAction);
		final MenuBuilder menuBuilder = modecontroller.getUserInputListenerFactory().getMenuBuilder();
		menuBuilder.addAction("/menu_bar/extras/first/options/acceleratorPresets/save", "SaveAcceleratorPresetsAction",
		    saveAction, MenuBuilder.AS_CHILD);
		for (final File dir : dirs) {
			final File[] fileList = dir.listFiles();
			if (fileList == null) {
				continue;
			}
			for (final File prop : fileList) {
				final String fileName = prop.getName();
				if (prop.isDirectory()) {
					continue;
				}
				if (!fileName.endsWith(".properties")) {
					continue;
				}
				try {
					final int propNameLength = fileName.lastIndexOf('.');
					final String propName = fileName.substring(0, propNameLength);
					final String key = "LoadAcceleratorPresetsAction." + propName;
					if (controller.getAction(key) != null) {
						continue;
					}
					final String title = ResourceBundles.getText(key + ".text", propName);
					final LoadAcceleratorPresetsAction loadAcceleratorPresetsAction = new LoadAcceleratorPresetsAction(
					    prop.toURL(), key, title, controller);
					controller.addAction(loadAcceleratorPresetsAction);
					menuBuilder.addAction("/menu_bar/extras/first/options/acceleratorPresets/new", key,
					    loadAcceleratorPresetsAction, MenuBuilder.AS_CHILD);
				}
				catch (final Exception e) {
					UITools.errorMessage("can not load accelerators from" + prop.getPath());
				}
			}
		}
	}

	final private URL resource;

	LoadAcceleratorPresetsAction(final URL resource, final String propFileName, final String title,
	                             final Controller controller) {
		super("LoadAcceleratorPresetsAction." + propFileName, controller, title, null);
		this.resource = resource;
	}

	public void actionPerformed(final ActionEvent e) {
		try {
			MenuBuilder.loadAcceleratorPresets(resource.openStream(), getController());
		}
		catch (final IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}

class SaveAcceleratorPresetsAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SaveAcceleratorPresetsAction(final Controller controller) {
		super("SaveAcceleratorPresetsAction", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final String keyset = JOptionPane.showInputDialog(ResourceBundles.getText("enter_keyset_name"));
		if (keyset == null || keyset.equals("")) {
			return;
		}
		final File acceleratorsUserDirectory = LoadAcceleratorPresetsAction.getAcceleratorsUserDirectory();
		final File keysetFile = new File(acceleratorsUserDirectory, keyset + ".properties");
		if (keysetFile.exists()) {
			final int confirm = JOptionPane.showConfirmDialog(UITools.getFrame(), ResourceBundles
			    .getText("overwrite_keyset_question"), "Freeplane", JOptionPane.YES_NO_OPTION);
			if (confirm != JOptionPane.YES_OPTION) {
				return;
			}
		}
		final Properties keysetProperties = new Properties();
		final Set<Entry<Object, Object>> allProperties = ResourceController.getResourceController().getProperties()
		    .entrySet();
		for (final Entry<Object, Object> p : allProperties) {
			if (!p.getKey().toString().startsWith("acceleratorFor")) {
				continue;
			}
			keysetProperties.put(p.getKey(), p.getValue());
		}
		try {
			acceleratorsUserDirectory.mkdirs();
			
			final OutputStream output = new BufferedOutputStream(new FileOutputStream(keysetFile));
			keysetProperties.store(output, "");
			output.close();
			
			final String key = "LoadAcceleratorPresetsAction." + keyset;
			if (getController().getAction(key) != null) {
				return;
			}
			final String title = ResourceBundles.getText(key + ".text", keyset);
			final LoadAcceleratorPresetsAction loadAcceleratorPresetsAction = new LoadAcceleratorPresetsAction(
			    keysetFile.toURL(), key, title, getController());
			if(null == getController().getAction(loadAcceleratorPresetsAction.getKey())){
				getController().addAction(loadAcceleratorPresetsAction);
				getModeController().getUserInputListenerFactory().getMenuBuilder().addAction(
				    "/menu_bar/extras/first/options/acceleratorPresets/new", key, loadAcceleratorPresetsAction,
				    MenuBuilder.AS_CHILD);
			}
		}
		catch (final IOException e1) {
			UITools.errorMessage(ResourceBundles.getText("can_not_save_key_set"));
		}
	}
}
