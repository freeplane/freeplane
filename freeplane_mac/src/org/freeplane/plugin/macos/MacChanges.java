/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2010.
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
package org.freeplane.plugin.macos;

import java.io.File;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.mindmapmode.MModeController;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

/** This plugin changes some things for mac users.
 * @author foltin
 */
public class MacChanges extends ApplicationAdapter  {

	private static Application fmMacApplication;

	private final MModeController modeController;

	private boolean mIsStartupPhase = false;
	
	static public void apply(Controller controller) {
		new MacChanges(controller);
	}
	
	private MacChanges(Controller controller) {
		this.modeController = (MModeController) controller.getModeController(MModeController.MODENAME);
		if(fmMacApplication==null){
			// if a handleOpen comes here, directly, we know that FM is currently starting.
			mIsStartupPhase = true;
			fmMacApplication = Application.getApplication();
			fmMacApplication.addApplicationListener(this);
			fmMacApplication.addPreferencesMenuItem();
			fmMacApplication.addAboutMenuItem();
			fmMacApplication.setEnabledPreferencesMenu(true);
//			fmMacApplication.removePreferencesMenuItem();
			mIsStartupPhase = false;
		}
		Set<String> modes = controller.getModes();
		for(String mode:modes){
			MenuBuilder builder = controller.getModeController(mode).getUserInputListenerFactory().getMenuBuilder();
			String[] keys = {
					"/map_popup/toolbars/ToggleMenubarAction",
					"/menu_bar/file/quit",
					"/menu_bar/extras/first/options/PropertyAction",
					"/menu_bar/help/doc/AboutAction"
			};
			for(String key:keys){
				if(builder.contains(key)){
					builder.removeElement(key);
				}
			}
		}
	}


	public void handleQuit(ApplicationEvent event) {
		modeController.getController().quit();
		event.setHandled(false);
	}

	public void handleAbout(ApplicationEvent event) {
		modeController.getController().getAction("AboutAction").actionPerformed(null);
		event.setHandled(true);
	}


	public void handleOpenFile(final ApplicationEvent event) {
		try {
			if(mIsStartupPhase) {
				// restore at startup:
				System.setProperty("org.freeplane.param1", event.getFilename());
			} else {
				// Direct loading
				modeController.getMapController().newMap(Compat.fileToUrl(new File(event.getFilename())));
			}
			event.setHandled(true);
		} catch (Exception e) {
			LogTool.warn(e);
		}
	}
	
	public void handlePreferences(ApplicationEvent event) {
		modeController.getAction("PropertyAction").actionPerformed(null);
		event.setHandled(true);
	}
}