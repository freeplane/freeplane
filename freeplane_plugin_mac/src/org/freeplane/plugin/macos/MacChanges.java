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

import javax.swing.SwingUtilities;

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

	private static final String FREE_MIND_JAVA = "FreeMind.app/Contents/Resources/Java";

	private static Application fmMacApplication;

	private final MModeController modeController;

	private boolean mIsStartupPhase = false;
	
	public MacChanges(MModeController modeController) {
		this.modeController = modeController;
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