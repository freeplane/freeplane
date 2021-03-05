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

import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.OpenURIEvent;
import java.awt.desktop.OpenURIHandler;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.io.File;
import java.net.URI;
import java.net.URL;

import javax.swing.JFrame;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.ViewController;
import org.freeplane.main.application.MacOptions;

import com.apple.eawt.Application;
import com.apple.eawt.FullScreenUtilities;


public class MacChanges implements  AboutHandler, OpenFilesHandler, PreferencesHandler, OpenURIHandler, QuitHandler{

	private static Desktop fmMacApplication;

	private final Controller controller;

	static public void apply(Controller controller) {
		new MacChanges(controller);
	}
	
	public static void setFullScreen(JFrame window, boolean requestFullScreen) {
		boolean hasFullScreen = window.getY() == 0;
		if(hasFullScreen != requestFullScreen)
			Application.getApplication().requestToggleFullScreen(window);
		window.getRootPane().putClientProperty(ViewController.FULLSCREEN_ENABLED_PROPERTY, requestFullScreen);
	}

	private MacChanges(Controller controller) {
		this.controller = controller;
		if(fmMacApplication==null){
		    String helpMenuTitle = TextUtils.getRawText("menu_help");
		    ResourceController resourceController = ResourceController.getResourceController();
		    if(resourceController.getBooleanProperty("use_emoji_icons"))
		    	resourceController.putResourceString("menu_help", helpMenuTitle + " ");
		    final URL macProperties = this.getClass().getResource("freeplane_mac.properties");
		    Controller.getCurrentController().getResourceController().addDefaults(macProperties);

		    // if a handleOpen comes here, directly, we know that FM is currently starting.
		    fmMacApplication = Desktop.getDesktop();
			fmMacApplication.setAboutHandler(this);
			fmMacApplication.setPreferencesHandler(this);
			fmMacApplication.setOpenFileHandler(this);
			fmMacApplication.setOpenURIHandler(this);
			fmMacApplication.setQuitHandler(this);
			// wait until handleOpenFile finishes if it was called in event thread
			try {
				EventQueue.invokeAndWait(new Runnable() {
					@Override
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


	@Override
	public void handleQuitRequestWith(QuitEvent event, QuitResponse response) {
		try {
			if(! isStarting())
				controller.quit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.cancelQuit();
	}


	@Override
	public void openURI(OpenURIEvent event) {
		URI uri = event.getURI();

		try {
			if(isStarting()) {
				// restore at startup:
			    MacOptions.macFilesToOpen.add(uri.toString());
			} else {
				// Direct loading
				LinkController.getController().loadURI(uri);
			}
		} catch (Exception e) {
			LogUtils.warn(e);
		}
	}


	@Override
	public void handlePreferences(PreferencesEvent event) {
		final MModeController modeController = getModeController();
		if(modeController != null) {
			AFreeplaneAction action = modeController.getAction("ShowPreferencesAction");
			if(action != null)
				action.actionPerformed(null);
		}

	}


	@Override
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
                MacOptions.macFilesToOpen.add(filePath);

			} else {
				// Direct loading
				getModeController().getMapController().openMap(Compat.fileToUrl(new File(filePath)));
			}
		} catch (Exception e) {
			LogUtils.warn(e);
		}
	}

	private boolean isStarting() {
		return controller.getViewController() == null;
	}


	@Override
	public void handleAbout(AboutEvent event) {
		final MModeController modeController = getModeController();
		if(modeController != null) {
			AFreeplaneAction action = modeController.getController().getAction("AboutAction");
			if(action != null)
				action.actionPerformed(null);
		}
	}
}
