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
import java.util.HashMap;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.freeplane.core.extension.ExtensionHashMap;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.frame.MapViewManager;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.map.MapModel;
import org.freeplane.core.map.ModeController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ActionController;
import org.freeplane.core.url.UrlManager;
import org.freeplane.view.swing.map.MapView;

/**
 * Provides the methods to edit/change a Node. Forwards all messages to
 * MapModel(editing) or MapView(navigation).
 */
public class Controller{
	public IExtension getExtension(Class clazz) {
	    return extensions.getExtension(clazz);
    }

	private static Controller controllerInstance;
	public static final String JAVA_VERSION = System.getProperty("java.version");
	public static final String ON_START_IF_NOT_SPECIFIED = "on_start_if_not_specified";
	private static ResourceController resourceController;
	public static final FreemindVersionInformation VERSION = new FreemindVersionInformation(
	    "0.9.0 Freeplane 21");
	public static final String XML_VERSION = "0.9.0";

	public static Controller getController() {
		return controllerInstance;
	}

	/** @return the current modeController. */
	public static ModeController getModeController() {
		return controllerInstance.modeController;
	}

	static public ResourceController getResourceController() {
		return resourceController;
	}

	public static String getText(final String string) {
		return string == null ? null : resourceController.getText(string);
	}

	public static boolean isMacOsX() {
		boolean underMac = false;
		final String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac OS")) {
			underMac = true;
		}
		return underMac;
	}

	private final ActionController actionController;
	final private ExtensionHashMap extensions;
	/**
	 * Converts from a local link to the real file URL of the documentation map.
	 * (Used to change this behaviour under MacOSX).
	 */
	private ModeController modeController;
	private ViewController viewController;
	final private HashMap<String, ModeController> modeControllers;
	final private Action quit;

	public Controller(final ResourceController resourceController) {
		if (Controller.controllerInstance != null) {
			throw new RuntimeException("Controller already created");
		}
		Controller.resourceController = resourceController;
		Controller.controllerInstance = this;
		extensions = new ExtensionHashMap();
		actionController = new ActionController();
		modeControllers = new HashMap();
		quit = new QuitAction(resourceController);
		addAction("quit", quit);
		resourceController.init();
	}

	public void addAction(final Object key, final Action value) {
		actionController.addAction(key, value);
	}

	public boolean addExtension(final Class clazz, final IExtension extension) {
		return extensions.addExtension(clazz, extension);
	}

	public boolean addExtension(final IExtension extension) {
		return extensions.addExtension(extension);
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

	public void errorMessage(final Object message) {
		String myMessage = "";
		if (message != null) {
			myMessage = message.toString();
		}
		else {
			myMessage = Controller.getText("undefined_error");
			if (myMessage == null) {
				myMessage = "Undefined error";
			}
		}
		JOptionPane.showMessageDialog(Controller.getController().getViewController()
		    .getContentPane(), myMessage, "FreeMind", JOptionPane.ERROR_MESSAGE);
	}

	public void errorMessage(final Object message, final JComponent component) {
		JOptionPane.showMessageDialog(component, message.toString(), "FreeMind",
		    JOptionPane.ERROR_MESSAGE);
	}

	public Action getAction(final String key) {
		return actionController.getAction(key);
	}

	public FreemindVersionInformation getFreemindVersion() {
		return Controller.VERSION;
	}

	/**
	 * @return
	 */
	public MapModel getMap() {
		return getViewController().getMap();
	}

	/**
	 * @return
	 */
	public MapView getMapView() {
		return getViewController().getMapView();
	}

	public MapViewManager getMapViewManager() {
		return getViewController().getMapViewManager();
	}

	public ModeController getModeController(final String modeName) {
		return modeControllers.get(modeName);
	}

	public Set getModes() {
		return modeControllers.keySet();
	}

	/**
	 * @return
	 */
	public ViewController getViewController() {
		return viewController;
	}

	public void informationMessage(final Object message) {
		JOptionPane.showMessageDialog(Controller.getController().getViewController()
		    .getContentPane(), message.toString(), "FreeMind", JOptionPane.INFORMATION_MESSAGE);
	}

	public void informationMessage(final Object message, final JComponent component) {
		JOptionPane.showMessageDialog(component, message.toString(), "FreeMind",
		    JOptionPane.INFORMATION_MESSAGE);
	}

	void quit() {
		final String currentMapRestorable = UrlManager
		    .getController(Controller.getModeController()).getRestoreable(getMap());
		if (!getViewController().quit()) {
			return;
		}
		if (currentMapRestorable != null) {
			Controller.getResourceController().setProperty(Controller.ON_START_IF_NOT_SPECIFIED,
			    currentMapRestorable);
		}
		if (modeController != null) {
			modeController.shutdown();
		}
		Controller.getController().getViewController().exit();
	}

	/**
	 * @param actionEvent
	 */
	public void quit(final ActionEvent actionEvent) {
		quit.actionPerformed(actionEvent);
	}

	public Action removeAction(final String key) {
		return actionController.removeAction(key);
	}

	public IExtension removeExtension(final Class clazz) {
		return extensions.removeExtension(clazz);
	}

	public boolean removeExtension(final IExtension extension) {
		return extensions.removeExtension(extension);
	}

	public void selectMode(final ModeController newModeController) {
		if (modeController == newModeController) {
			return;
		}
		if (modeController != null) {
			modeController.shutdown();
		}
		viewController.selectMode(modeController, newModeController);
		modeController = newModeController;
		newModeController.startup();
	}

	public boolean selectMode(final String modeName) {
		final ModeController newModeController = modeControllers.get(modeName);
		if (modeController == newModeController) {
			return true;
		}
		selectMode(newModeController);
		return getMapViewManager().changeToMode(modeName);
	}
	
	public void setViewController(final ViewController viewController) {
		this.viewController = viewController;
	}

}
