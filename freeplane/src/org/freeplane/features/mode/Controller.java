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
package org.freeplane.features.mode;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.freeplane.core.extension.ExtensionContainer;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.OptionPanelController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.IValidator;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.ui.ViewController;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;

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
	private ModeController modeController4build;
	final private Map<String, ModeController> modeControllers = new LinkedHashMap<String, ModeController>();
	private ViewController viewController;
	private final ResourceController resourceController;
	private final List<IValidator> optionValidators = new ArrayList<IValidator>();
	final private OptionPanelController optionPanelController;

	public Controller(ResourceController resourceController) {
		super();
		if(currentController == null){
			currentController = this;
		}
		this.resourceController = resourceController; 
		this.optionPanelController = new OptionPanelController();
		extensionContainer = new ExtensionContainer(new HashMap<Class<? extends IExtension>, IExtension>());
		addAction(new MoveToRootAction());
		addAction(new CenterSelectedNodeAction());
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
	 * @param withoutSave
	 *            true= without save.
	 */
	public void close(final boolean withoutSave) {
		getMapViewManager().close(withoutSave);
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
		return modeController4build != null ? modeController4build : modeController;
	}

	public ModeController getModeController(final String modeName) {
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

	public void selectMode(ModeController newModeController) {
		modeController4build = null;
		final ModeController oldModeController = modeController;
		if (oldModeController == newModeController) {
			return;
		}
		if (oldModeController != null) {
			oldModeController.shutdown();
		}
		LogUtils.info("requesting mode: " + newModeController.getModeName());
		modeController = newModeController;
		viewController.selectMode(oldModeController, newModeController);
		getMapViewManager().changeToMode(newModeController.getModeName());
		newModeController.startup();
	}

	public void selectMode(final String modeName) {
		final ModeController newModeController = modeControllers.get(modeName);
		if (newModeController == null) {
			return;
		}
		if (modeController == newModeController) {
			return;
		}
		selectMode(newModeController);
	}

	public void setViewController(final ViewController viewController) {
		this.viewController = viewController;
	}

	public boolean shutdown() {
		getViewController().saveProperties();
		ResourceController.getResourceController().saveProperties();
		if (!getViewController().quit()) {
			return false;
		}
		extensionContainer.getExtensions().clear();
		return true;
	}

	public static void exec(final String command) throws IOException {
		exec(command, false);
	}
	
	public static void exec(final String command, boolean waitFor) throws IOException {
		if (Compat.isWindowsOS()) {
			LogUtils.info("using jna to execute " + command);
			windowsNativeExec(command, waitFor);
		}
		else {
			LogUtils.info("execute " + command);
			Runtime.getRuntime().exec(command);
		}
	}
	
	public static void exec(final String[] command) throws IOException {
		exec(command, false);
	}
	
	public static void exec(final String[] command, boolean waitFor) throws IOException {
		if (Compat.isWindowsOS()) {
			String commandString = command[0];
			for (int i=1; i<command.length; i++) {
				commandString += " " + command[i];
			}
			LogUtils.info("using jna to execute " + command);
			windowsNativeExec(commandString, waitFor);
		}
		else {
			LogUtils.info("execute " + Arrays.toString(command));
			Runtime.getRuntime().exec(command);
		}
	}
	
	private static void windowsNativeExec(String command, boolean waitFor) throws IllegalStateException {
		WinBase.PROCESS_INFORMATION.ByReference processInfo = new WinBase.PROCESS_INFORMATION.ByReference();
		WinBase.STARTUPINFO startupInfo = new WinBase.STARTUPINFO();

		try {
    		if (!Kernel32.INSTANCE.CreateProcess(
    		    null,           // Application name, not needed if supplied in command line
    		    command,        // Command line
    		    null,           // Process security attributes
    		    null,           // Thread security attributes
    		    true,           // Inherit handles
    		    new WinDef.DWORD(0) ,              // Creation flags
    		    null,           // Environment
    		    null,           // Directory
    		    startupInfo,
    		    processInfo)) {
    		    throw new IllegalStateException("Error creating process. Last error: " +
    		        Kernel32.INSTANCE.GetLastError());
    		}
    
    		if (waitFor) {
    			Kernel32.INSTANCE.WaitForSingleObject(processInfo.hProcess, Kernel32.INFINITE);
    		}
		}
		finally {
    		// The CreateProcess documentation indicates that it is very important to 
    		// close the returned handles
    		Kernel32.INSTANCE.CloseHandle(processInfo.hThread);
    		Kernel32.INSTANCE.CloseHandle(processInfo.hProcess);
		}
	}

	private static ThreadLocal<Controller> threadController = new ThreadLocal<Controller>();
	private static Controller currentController = null;
	public static Controller getCurrentController() {
		final Controller controller = threadController.get();
		return controller != null ? controller : currentController;
	}
	
	public static void setCurrentController(final Controller controller){
		currentController = controller;
	}

	public static ModeController getCurrentModeController() {
	    return getCurrentController().getModeController();
    }

	public void selectModeForBuild(ModeController modeController4build) {
	    this.modeController4build = modeController4build;
	    
    }

	public ResourceController getResourceController() {
	    return resourceController;
    }

	public void addOptionValidator(IValidator validator) {
		optionValidators.add(validator);
    }
	
	public List<IValidator> getOptionValidators() {
		return optionValidators;
	}
	
	public OptionPanelController getOptionPanelController() {
	    return optionPanelController;
    }
}
