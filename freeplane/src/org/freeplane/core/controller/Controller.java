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
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.freeplane.core.extension.ExtensionHashMap;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.frame.IMapViewManager;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ActionController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.UIBuilder;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.url.UrlManager;

/**
 * Provides the methods to edit/change a Node. Forwards all messages to
 * MapModel(editing) or MapView(navigation).
 */
public class Controller {
	public static final String JAVA_VERSION = System.getProperty("java.version");
	public static final String ON_START_IF_NOT_SPECIFIED = "on_start_if_not_specified";
	private static ResourceController resourceController;
	public static final FreeplaneVersionInformation VERSION = new FreeplaneVersionInformation("0.9.0 Freeplane 21");
	public static final String XML_VERSION = "0.9.0";

	public static FreeplaneVersionInformation getFreeplaneVersion() {
		return Controller.VERSION;
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

	static public void setResourceController(final ResourceController resourceController) {
		Controller.resourceController = resourceController;
	}

	private final ActionController actionController;
	final private ExtensionHashMap extensions;
	final private LastOpenedList lastOpened;
	/**
	 * Converts from a local link to the real file URL of the documentation map.
	 * (Used to change this behaviour under MacOSX).
	 */
	private ModeController modeController;
	final private HashMap<String, ModeController> modeControllers;
	final private Action quit;
	private ViewController viewController;

	public Controller() {
		extensions = new ExtensionHashMap();
		actionController = new ActionController();
		modeControllers = new HashMap();
		quit = new QuitAction(this);
		addAction("quit", quit);
		lastOpened = new LastOpenedList(this, Controller.getResourceController().getProperty("lastOpened"));
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
		JOptionPane.showMessageDialog(getViewController().getContentPane(), myMessage, "Freeplane",
		    JOptionPane.ERROR_MESSAGE);
	}

	public void errorMessage(final Object message, final JComponent component) {
		JOptionPane.showMessageDialog(component, message.toString(), "Freeplane", JOptionPane.ERROR_MESSAGE);
	}

	public Action getAction(final String key) {
		return actionController.getAction(key);
	}

	public IExtension getExtension(final Class clazz) {
		return extensions.getExtension(clazz);
	}

	public LastOpenedList getLastOpenedList() {
		return lastOpened;
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
		return modeControllers.get(modeName);
	}

	public Set getModes() {
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

	public boolean shutdown() {
		final String currentMapRestorable = UrlManager.getController(getModeController()).getRestoreable(getMap());
		if (!getViewController().quit()) {
			return false;
		}
		if (currentMapRestorable != null) {
			Controller.getResourceController().setProperty(Controller.ON_START_IF_NOT_SPECIFIED, currentMapRestorable);
		}
		if (modeController != null) {
			modeController.shutdown();
		}
		getViewController().stop();
		final String lastOpenedString = lastOpened.save();
		Controller.getResourceController().setProperty("lastOpened", lastOpenedString);
		extensions.clear();
		return true;
	}

	public void updateMenus(final MenuBuilder menuBuilder) {
		menuBuilder.removeChildElements(FreeplaneMenuBar.FILE_MENU + "/last");
		boolean firstElement = true;
		final LastOpenedList lst = getLastOpenedList();
		for (final ListIterator it = lst.listIterator(); it.hasNext();) {
			final String key = (String) it.next();
			final JMenuItem item = new JMenuItem(key);
			if (firstElement) {
				firstElement = false;
				item.setAccelerator(KeyStroke.getKeyStroke(Controller.getResourceController().getAdjustableProperty(
				    "keystroke_open_first_in_history")));
			}
			final ActionListener lastOpenedActionListener = new LastOpenedActionListener(this);
			item.addActionListener(lastOpenedActionListener);
			menuBuilder.addMenuItem(FreeplaneMenuBar.FILE_MENU + "/last", item, UIBuilder.AS_CHILD);
		}
	}
}
