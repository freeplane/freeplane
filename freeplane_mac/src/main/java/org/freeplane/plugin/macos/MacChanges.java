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
import java.net.URI;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;

import com.apple.eawt.*;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.OpenFilesEvent;
import com.apple.eawt.AppEvent.OpenURIEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;

public class MacChanges implements  AboutHandler, OpenFilesHandler, PreferencesHandler, OpenURIHandler, QuitHandler{

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
			fmMacApplication.setAboutHandler(this);
			fmMacApplication.setPreferencesHandler(this);
			fmMacApplication.setOpenFileHandler(this);
			fmMacApplication.setOpenURIHandler(this);
			fmMacApplication.setQuitHandler(this);
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


	private MModeController getModeController() {
		return (MModeController) controller.getModeController(MModeController.MODENAME);
	}
	
	
	public void handleQuitRequestWith(QuitEvent event, QuitResponse response) {
		try {
			if(! isStarting())
				controller.quit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.cancelQuit();
	}

	
	public void openURI(OpenURIEvent event) {
		URI uri = event.getURI();
		
		try {
			if(isStarting()) {
				// restore at startup:
				loadedMapCounter++;
				System.setProperty("org.freeplane.param" + loadedMapCounter, uri.toString());				
			} else {
				// Direct loading
				LinkController.getController().loadURI(uri);
			}
		} catch (Exception e) {
			LogUtils.warn(e);
		}
	}

	
	public void handlePreferences(PreferencesEvent event) {
		AFreeplaneAction action = getModeController().getAction("ShowPreferencesAction");
		if(action != null)
			action.actionPerformed(null);
		
	}

	
	public void openFiles(OpenFilesEvent event) {
		for(File file : event.getFiles()){
			String filePath = file.getPath();
			openFile(filePath);
		}
	}

	private void openFile(String filePath) {
		try {
			if(isStarting()) {
				// restore at startup:
				loadedMapCounter++;
				System.setProperty("org.freeplane.param" + loadedMapCounter, filePath);				
			} else {
				// Direct loading
				getModeController().getMapController().newMap(Compat.fileToUrl(new File(filePath)));
			}
		} catch (Exception e) {
			LogUtils.warn(e);
		}
	}

	private boolean isStarting() {
		return controller.getViewController() == null;
	}

	
	public void handleAbout(AboutEvent event) {
		AFreeplaneAction action = getModeController().getController().getAction("AboutAction");
		if(action != null)
			action.actionPerformed(null);
		
	}
}