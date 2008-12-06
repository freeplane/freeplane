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
package org.freeplane.controller;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.freeplane.controller.help.HelpController;
import org.freeplane.controller.print.PrintController;
import org.freeplane.controller.resources.ResourceController;
import org.freeplane.controller.views.MapViewManager;
import org.freeplane.controller.views.ViewController;
import org.freeplane.main.FreemindVersionInformation;
import org.freeplane.map.attribute.ModelessAttributeController;
import org.freeplane.map.tree.MapModel;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.modes.ModeController;
import org.freeplane.service.filter.FilterController;
import org.freeplane.ui.AlwaysEnabledAction;

/**
 * Provides the methods to edit/change a Node. Forwards all messages to
 * MapModel(editing) or MapView(navigation).
 */
public class Controller {
	public static final String JAVA_VERSION = System
	    .getProperty("java.version");
	public static final String ON_START_IF_NOT_SPECIFIED = "on_start_if_not_specified";
	public static final FreemindVersionInformation VERSION = new FreemindVersionInformation(
	    "0.9.0 Freeplane 21");
	public static final String XML_VERSION = "0.9.0";
	final private HashMap<Object, Action> actions;
	private ModelessAttributeController attributeController;
	private FilterController filterController;
	private HelpController helpController;
	/**
	 * Converts from a local link to the real file URL of the documentation map.
	 * (Used to change this behaviour under MacOSX).
	 */
	private ModeController modeController;
	final private HashMap<String, ModeController> modeControllers;
	private PrintController printController;
	final private Action quit;
	final private ResourceController resourceController;
	private ViewController viewController;

	public Controller(final ResourceController resourceController) {
		this.resourceController = resourceController;
		Freeplane.setController(this);
		actions = new HashMap<Object, Action>();
		modeControllers = new HashMap();
		quit = new QuitAction(resourceController);
		addAction("quit", quit);
		resourceController.init();
	}

	public void addAction(final Object key, final Action value) {
		assert key != null;
		assert value != null;
		final Action oldAction = actions.put(key, value);
		assert oldAction == null;
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

	public void enableActions(final boolean enabled) {
		final Iterator<Action> iterator = actions.values().iterator();
		while (iterator.hasNext()) {
			final Action action = iterator.next();
			if (action.getClass().getAnnotation(AlwaysEnabledAction.class) == null) {
				action.setEnabled(enabled);
			}
		}
	}

	public void errorMessage(final Object message) {
		String myMessage = "";
		if (message != null) {
			myMessage = message.toString();
		}
		else {
			myMessage = Freeplane.getController().getResourceController()
			    .getResourceString("undefined_error");
			if (myMessage == null) {
				myMessage = "Undefined error";
			}
		}
		JOptionPane.showMessageDialog(Freeplane.getController()
		    .getViewController().getContentPane(), myMessage, "FreeMind",
		    JOptionPane.ERROR_MESSAGE);
	}

	public void errorMessage(final Object message, final JComponent component) {
		JOptionPane.showMessageDialog(component, message.toString(),
		    "FreeMind", JOptionPane.ERROR_MESSAGE);
	}

	public Action getAction(final String key) {
		return actions.get(key);
	}

	public ModelessAttributeController getAttributeController() {
		return attributeController;
	}

	public FilterController getFilterController() {
		return filterController;
	}

	public FreemindVersionInformation getFreemindVersion() {
		return Controller.VERSION;
	}

	public HelpController getHelpController() {
		return helpController;
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

	/** @return the current modeController. */
	public ModeController getModeController() {
		return modeController;
	}

	public ModeController getModeController(final String modeName) {
		return modeControllers.get(modeName);
	}

	/** Returns the current model */
	public MapModel getModel() {
		if (getMapView() != null) {
			return getMapView().getModel();
		}
		return null;
	}

	public Set getModes() {
		return modeControllers.keySet();
	}

	public PrintController getPrintController() {
		return printController;
	}

	public ResourceController getResourceController() {
		return resourceController;
	}

	/**
	 * @return
	 */
	public ViewController getViewController() {
		return viewController;
	}

	public void informationMessage(final Object message) {
		JOptionPane.showMessageDialog(Freeplane.getController()
		    .getViewController().getContentPane(), message.toString(),
		    "FreeMind", JOptionPane.INFORMATION_MESSAGE);
	}

	public void informationMessage(final Object message,
	                               final JComponent component) {
		JOptionPane.showMessageDialog(component, message.toString(),
		    "FreeMind", JOptionPane.INFORMATION_MESSAGE);
	}

	void quit() {
		final String currentMapRestorable = getModeController().getUrlManager()
		    .getRestoreable(getMap());
		if (!getViewController().quit()) {
			return;
		}
		if (currentMapRestorable != null) {
			Freeplane.getController().getResourceController().setProperty(
			    Controller.ON_START_IF_NOT_SPECIFIED, currentMapRestorable);
		}
		if (modeController != null) {
			modeController.shutdown();
		}
		Freeplane.getController().getViewController().exit();
	}

	/**
	 * @param actionEvent
	 */
	public void quit(final ActionEvent actionEvent) {
		quit.actionPerformed(actionEvent);
	}

	public Action removeAction(final String key) {
		return actions.remove(key);
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

	public void setAttributeController(
	                                   final ModelessAttributeController attributeController) {
		this.attributeController = attributeController;
	}

	public void setFilterController(final FilterController filterController) {
		this.filterController = filterController;
	}

	public void setHelpController(final HelpController helpController) {
		this.helpController = helpController;
	}

	public void setPrintController(final PrintController printController) {
		this.printController = printController;
	}

	public void setViewController(final ViewController viewController) {
		this.viewController = viewController;
	}
}
