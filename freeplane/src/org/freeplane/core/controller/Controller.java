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
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.freeplane.core.extension.ExtensionContainer;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.frame.IMapViewManager;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.resources.FreeplaneResourceBundle;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.ResourceControllerProperties;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.LogTool;

/**
 * Provides the methods to edit/change a Node. Forwards all messages to
 * MapModel(editing) or MapView(navigation).
 */
public class Controller extends AController {
	public static void setLookAndFeel(final String lookAndFeel) {
		try {
			if (lookAndFeel.equals("windows")) {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			}
			else if (lookAndFeel.equals("motif")) {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
			}
			else if (lookAndFeel.equals("mac")) {
				UIManager.setLookAndFeel("javax.swing.plaf.mac.MacLookAndFeel");
			}
			else if (lookAndFeel.equals("metal")) {
				UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			}
			else if (lookAndFeel.equals("gtk")) {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			}
			else if (lookAndFeel.equals("nothing")) {
			}
			else if (lookAndFeel.indexOf('.') != -1) {
				UIManager.setLookAndFeel(lookAndFeel);
			}
			else {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
		}
		catch (final Exception ex) {
			System.err.println("Error while setting Look&Feel" + lookAndFeel);
		}
	}

	private final ExtensionContainer extensionContainer;
	/**
	 * Converts from a local link to the real file URL of the documentation map.
	 * (Used to change this behavior under MacOSX).
	 */
	private ModeController modeController;
	final private Map<String, ModeController> modeControllers = new HashMap<String, ModeController>();
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

	public void errorMessage(final Object message) {
		String myMessage = "";
		if (message != null) {
			myMessage = message.toString();
		}
		else {
			myMessage = FreeplaneResourceBundle.getText("undefined_error");
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
			ResourceController.getResourceController().setProperty(
			    ResourceControllerProperties.ON_START_IF_NOT_SPECIFIED, currentMapRestorable);
		}
		if (modeController != null) {
			modeController.shutdown();
		}
		getViewController().stop();
		extensionContainer.getExtensions().clear();
		return true;
	}
}
