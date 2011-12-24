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

import java.awt.EventQueue;
import java.io.File;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.ViewController;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

/** This plugin changes some things for mac users.
 * @author foltin
 */
public class MacChanges extends ApplicationAdapter  {

	private static Application fmMacApplication;

	private final Controller controller;

	private int loadedMapCounter = 0;
	
	static public void apply(Controller controller) {
		new MacChanges(controller);
	}
	
	private MacChanges(Controller controller) {
		this.controller = controller;
		if(fmMacApplication==null){
			// if a handleOpen comes here, directly, we know that FM is currently starting.
			fmMacApplication = Application.getApplication();
			fmMacApplication.addApplicationListener(this);
			fmMacApplication.addPreferencesMenuItem();
			fmMacApplication.addAboutMenuItem();
			fmMacApplication.setEnabledPreferencesMenu(true);
			// wait until handleOpenFile finishes if it was called in event thread
			try {
				EventQueue.invokeAndWait(new Runnable() {
					public void run() {
					};
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	public void handleQuit(ApplicationEvent event) {
		getModeController().getController().quit();
		event.setHandled(false);
	}

	public void handleAbout(ApplicationEvent event) {
		getModeController().getController().getAction("AboutAction").actionPerformed(null);
		event.setHandled(true);
	}


	public void handleOpenFile(final ApplicationEvent event) {
		try {
			ViewController viewController = controller.getViewController();
			if(viewController == null) {
				// restore at startup:
				loadedMapCounter++;
				System.setProperty("org.freeplane.param" + loadedMapCounter, event.getFilename());				
			} else {
				// Direct loading
				getModeController().getMapController().newMap(Compat.fileToUrl(new File(event.getFilename())));
			}
			event.setHandled(true);
		} catch (Exception e) {
			LogUtils.warn(e);
		}
	}
	
	public void handlePreferences(ApplicationEvent event) {
		getModeController().getAction("PropertyAction").actionPerformed(null);
		event.setHandled(true);
	}

	private MModeController getModeController() {
		return (MModeController) controller.getModeController(MModeController.MODENAME);
	}
}