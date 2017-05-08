
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOExceptionWithCause;
import org.freeplane.core.extension.ExtensionContainer;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.OptionPanelController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.IValidator;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.IMapSelection.NodePosition;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.ui.ViewController;
import org.freeplane.main.application.ApplicationLifecycleListener;

/**
 * Provides the methods to edit/change a Node. Forwards all messages to
 * MapModel(editing) or MapView(navigation).
 */
public class Controller extends AController implements FreeplaneActions, IMapLifeCycleListener{
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
	private final OptionPanelController optionPanelController;
	private IMapViewManager mapViewManager;
	private List<ApplicationLifecycleListener> applicationLifecycleListeners = new ArrayList<ApplicationLifecycleListener>(0);
	final private Collection<IMapLifeCycleListener> mapLifeCycleListeners;

	public Controller(ResourceController resourceController) {
		super();
		if(currentController == null){
			currentController = this;
		}
		mapLifeCycleListeners = new LinkedList<IMapLifeCycleListener>();
		this.resourceController = resourceController; 
		this.optionPanelController = new OptionPanelController();
		extensionContainer = new ExtensionContainer(new HashMap<Class<? extends IExtension>, IExtension>());
		addAction(new MoveToRootAction());
		addAction(new MoveSelectedNodeAction(NodePosition.EAST));
		addAction(new MoveSelectedNodeAction(NodePosition.CENTER));
		addAction(new MoveSelectedNodeAction(NodePosition.WEST));
		addAction(new CloseAllMapsAction());
		addAction(new CloseAllOtherMapsAction());
	}

	public void addExtension(final Class<? extends IExtension> clazz, final IExtension extension) {
		extensionContainer.addExtension(clazz, extension);
	}

	public void addModeController(final ModeController modeController) {
		modeControllers.put(modeController.getModeName(), modeController);
	}

	public void addMapLifeCycleListener(final IMapLifeCycleListener listener) {
		mapLifeCycleListeners.add(listener);
	}
	
	public void removeMapLifeCycleListener(final IMapLifeCycleListener listener) {
		mapLifeCycleListeners.remove(listener);
	}

	public void close() {
		getMapViewManager().close();
	}

	public <T extends IExtension> T getExtension(final Class<T> clazz){
		return extensionContainer.getExtension(clazz);
	}

	/**
	 * @return
	 */
	public MapModel getMap() {
		return getMapViewManager().getModel();
	}

	public IMapViewManager getMapViewManager() {
		return mapViewManager;
	}

	public void setMapViewManager(IMapViewManager mapViewManager) {
    	this.mapViewManager = mapViewManager;
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
		return getMapViewManager().getMapSelection();
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
		if (!getViewController().quit()) {
			return false;
		}
		ResourceController.getResourceController().saveProperties();
		extensionContainer.getExtensions().clear();
		return true;
	}

	public static void exec(final String string) throws IOException {
		exec(string, false);
	}

	public static void exec(final String string, boolean waitFor) throws IOException {
		IControllerExecuteExtension ext = Controller.getCurrentController().getExtension(IControllerExecuteExtension.class);
		if(ext == null) {
			ext = Controller.getCurrentController().getDefaultExecuter();
		}
		
		ext.exec(string, waitFor);
	}
	
	public static void exec(final String[] command) throws IOException {
		exec(command, false);
	}
	
	public static void exec(final String[] command, boolean waitFor) throws IOException {
		IControllerExecuteExtension ext = Controller.getCurrentController().getExtension(IControllerExecuteExtension.class);
		if(ext == null) {
			ext = Controller.getCurrentController().getDefaultExecuter();
		}
		
		ext.exec(command, waitFor);
	}

	private IControllerExecuteExtension getDefaultExecuter() {
		return new IControllerExecuteExtension() {
			
			public void exec(String[] command, boolean waitFor) throws IOException {
		LogUtils.info("execute " + Arrays.toString(command));
				Process proc = Runtime.getRuntime().exec(command);
				waiting(waitFor, proc);
			}
			
			public void exec(String string, boolean waitFor) throws IOException {
				LogUtils.info("execute " + string);
				Process proc = Runtime.getRuntime().exec(string);
				waiting(waitFor, proc);
			}

			private void waiting(boolean waitFor, Process proc)
					throws IOExceptionWithCause {
				if(waitFor) {
					try {
						proc.waitFor();
					} catch (InterruptedException e) {
						throw new IOExceptionWithCause(e);
					}
				}
			}
		};
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


	public void addApplicationLifecycleListener(ApplicationLifecycleListener applicationLifecycleListener) {
		this.applicationLifecycleListeners.add(applicationLifecycleListener);
	}
	
	public void fireMapCreated(final MapModel map) {
		final IMapLifeCycleListener[] list = mapLifeCycleListeners.toArray(new IMapLifeCycleListener[]{});
		for (final IMapLifeCycleListener next : list) {
			next.onCreate(map);
		}
	}

	protected void fireMapRemoved(final MapModel map) {
		final IMapLifeCycleListener[] list = mapLifeCycleListeners.toArray(new IMapLifeCycleListener[]{});
		for (final IMapLifeCycleListener next : list) {
			next.onRemove(map);
		}
	}

	@Override
	public void onCreate(MapModel map) {
		fireMapCreated(map);
	}

	@Override
	public void onRemove(MapModel map) {
		fireMapRemoved(map);
		
	}
	
	public void fireStartupFinished() {
		for (ApplicationLifecycleListener listener : applicationLifecycleListeners) {
			listener.onStartupFinished();
		}
	}

	public void fireApplicationStopped() {
		for (ApplicationLifecycleListener listener : applicationLifecycleListeners) {
			listener.onApplicationStopped();
		}
	}

	public boolean closeAllMaps(){
		return closeAllMaps(null);
	}
	
	boolean closeAllMaps(MapModel mapToKeepOpen) {
		boolean closingNotCancelled = true;
		for (MapModel map = getMap(); map != null && map != mapToKeepOpen && closingNotCancelled; map = getMap()){
			closingNotCancelled = map.close();
		}
		HashSet<MapModel> otherMaps = new HashSet(getMapViewManager().getMaps().values());
		otherMaps.remove(mapToKeepOpen);
		otherMaps.remove(getMap());
		for (MapModel map : otherMaps){
			closingNotCancelled = map.close() && closingNotCancelled;
		}
		
		return closingNotCancelled;
	}

}
