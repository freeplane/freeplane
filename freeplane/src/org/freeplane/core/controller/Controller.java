/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.core.controller;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


import org.freeplane.core.extension.ExtensionContainer;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.frame.IMapViewManager;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogTool;

/**
 * Provides the methods to edit/change a Node. Forwards all messages to
 * MapModel(editing) or MapView(navigation).
 */
public class Controller extends AController {
	private final ExtensionContainer extensionContainer;
	/**
	 * Converts from a local link to the real file URL of the documentation map.
	 * (Used to change this behavior under MacOSX).
	 */
	private ModeController modeController;
	final private Map<String, ModeController> modeControllers = new LinkedHashMap<String, ModeController>();
	private ViewController viewController;

	public Controller() {
		super();
		extensionContainer = new ExtensionContainer(new HashMap<Class<? extends IExtension>, IExtension>());
		addAction(new QuitAction(this));
	}

	public void addExtension(final Class<? extends IExtension> clazz, final IExtension extension) {
		extensionContainer.addExtension(clazz, extension);
	}

	public void addModeController(final ModeController modeController) {
		modeControllers.put(modeController.getModeName(), modeController);
	}

	/**
	 * Closes the actual map.
	 * 
	 * @param force
	 *            true= without save.
	 */
	public void close(final boolean force) {
		getMapViewManager().close(force);
	}

	public IExtension getExtension(final Class<? extends IExtension> clazz) {
		return extensionContainer.getExtension(clazz);
	}

	/**
	 * @return
	 */
	public MapModel getMap() {
		return getViewController().getMap();
	}

	public IMapViewManager getMapViewManager() {
		return getViewController().getMapViewManager();
	}

	/** @return the current modeController. */
	public ModeController getModeController() {
		return modeController;
	}

	public ModeController getModeController(final String modeName) {
		LogTool.info("requesting mode: " + modeName);
		return modeControllers.get(modeName);
	}

	public Set<String> getModes() {
		return modeControllers.keySet();
	}

	public IMapSelection getSelection() {
		return getViewController().getSelection();
	}

	/**
	 * @return
	 */
	public ViewController getViewController() {
		return viewController;
	}

	public void quit() {
		if (shutdown()) {
			System.exit(0);
		}
	}

	/**
	 * @param actionEvent
	 */
	public void quit(final ActionEvent actionEvent) {
		getActions().get("QuitAction").actionPerformed(actionEvent);
	}

	public boolean selectMode(final ModeController newModeController) {
		final ModeController oldModeController = modeController;
		if (oldModeController == newModeController) {
			return true;
		}
		if (oldModeController != null) {
			oldModeController.shutdown();
		}
		modeController = newModeController;
		if (!getMapViewManager().changeToMode(newModeController.getModeName())) {
			modeController = oldModeController;
			return false;
		}
		viewController.selectMode(oldModeController, newModeController);
		newModeController.startup();
		return true;
	}

	public boolean selectMode(final String modeName) {
		final ModeController newModeController = modeControllers.get(modeName);
		if (newModeController == null) {
			return false;
		}
		if (modeController == newModeController) {
			return true;
		}
		return selectMode(newModeController);
	}

	public void setViewController(final ViewController viewController) {
		this.viewController = viewController;
	}

	public boolean shutdown() {
		getViewController().saveProperties();
		ResourceController.getResourceController().saveProperties(this);
		if (!getViewController().quit()) {
			return false;
		}
		extensionContainer.getExtensions().clear();
		return true;
	}

	public static Process exec(String string) throws IOException {
		LogTool.info("execute " + string);
		return Runtime.getRuntime().exec(string);
	}

	public static Process exec(String[] command) throws IOException {
		LogTool.info("execute " + Arrays.toString(command));
		return Runtime.getRuntime().exec(command);
	}
}
