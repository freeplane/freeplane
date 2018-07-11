package org.freeplane.plugin.script.proxy;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.freeplane.api.Loader;
import org.freeplane.api.Map;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.util.Compat;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.plugin.script.ScriptContext;

class LoaderProxy implements Loader {

	static Loader of(ScriptContext scriptContext2) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method not implemented");
	}

	static Loader of(File file, ScriptContext scriptContext2) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method not implemented");
	}

	static Loader of(URL url, ScriptContext scriptContext2) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method not implemented");
	}

	static Loader of(String file, ScriptContext scriptContext2) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method not implemented");
	}

	private final URL url;
	private final ScriptContext scriptContext;
	private File associatedFile;
	private boolean saved;
	private boolean unsaved;
	private boolean withView;

	LoaderProxy(URL url, ScriptContext scriptContext) {
		super();
		this.url = url;
		this.scriptContext = scriptContext;
	}

	@Override
	public Loader associatedWith(File associatedFile) {
		this.associatedFile = associatedFile;
		this.saved = false;
		return this;
	}
	

	@Override
	public Loader associatedWith(String file) {
		return associatedWith(new File(file));
	}

	@Override
	public Loader savedAs(File file) {
		this.associatedFile = file;
		this.saved = true;
		return this;		
	}
	
	@Override
	public Loader savedAs(String file) {
		return savedAs(new File(file));
	}


	@Override
	public Loader unsaved() {
		this.unsaved = true;
		return this;
	}

	@Override
	public Loader withView() {
		this.withView = true; 
		return this;
	}

	@Override
	public Loader withoutView() {
		this.withView = false;
		return this;
	}

	@Override
	public Map getMap() {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method not implemented");
	}
	
	
	
	private Map openMap() {
		final MapModel oldMap = Controller.getCurrentController().getMap();
		final MMapIO mapIO = MMapIO.getInstance();
		final MapModel newMap = mapIO.openUntitledMap();
		restartTransaction(oldMap, newMap);
		return new MapProxy(newMap, scriptContext);
	}

	private Map openMap(URL url) {
		try {
			final MapModel oldMap = Controller.getCurrentController().getMap();
			Controller.getCurrentModeController().getMapController().openMap(url);
			final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
			final String key = mapViewManager.checkIfFileIsAlreadyOpened(url);
			// make the map the current map even if it was already opened
			if (key == null || !mapViewManager.tryToChangeToMapView(key))
				throw new RuntimeException("map " + url + " does not seem to be opened");
			final MapModel newMap = mapViewManager.getModel();
			restartTransaction(oldMap, newMap);
			return new MapProxy(newMap, scriptContext);
		}
		catch (Exception e) {
			throw new RuntimeException("error on newMap", e);
		}
	}

	private Map openMap(File file) {
		final URL url = fileToUrlOrNull(file);
		return url != null ? openMap(url) : null;
	}

	private Map openUntitledMap(File templateFile) {
		final MapModel oldMap = Controller.getCurrentController().getMap();
		final MMapIO mapIO = MMapIO.getInstance();
		final MapModel newMap = mapIO.openUntitledMap(templateFile);
		restartTransaction(oldMap, newMap);
		return new MapProxy(newMap, scriptContext);
	}

	private Map newMap() {
		return openMap();
	}

	private Map newMapFromTemplate(File templateFile) {
		return openUntitledMap(templateFile);
	}


	private Map newMap(URL url) {
		return openMap(url);
	}
	
	private Map readMap(File file) {
		final URL url = fileToUrlOrNull(file);
		return url != null ? readMap(url) : null;
	}

	private Map readMap(URL url) {
		final MMapIO mapIO = MMapIO.getInstance();
		MapModel newMap = mapIO.readMap(url);
		return new MapProxy(newMap, scriptContext);
		
	}

	private static URL fileToUrlOrNull(final File file) {
		try {
			return Compat.fileToUrl(file);
		}
		catch (MalformedURLException e) {
			return null;
		}
	}
	
	private Map createUntitledMap(File templateFile) {
		final URL url = fileToUrlOrNull(templateFile);
		return url != null ? createUntitledMap(url) : null;
	}

	private Map createUntitledMap(final URL template) {
		final MMapIO mapIO = MMapIO.getInstance();
		MapModel newMap = mapIO.createUntitledMap(template);
		return new MapProxy(newMap, scriptContext);
	}

	private void restartTransaction(final MapModel oldMap, final MapModel newmap) {
		final IUndoHandler oldUndoHandler = oldMap.getExtension(IUndoHandler.class);
		final IUndoHandler newUndoHandler = newmap.getExtension(IUndoHandler.class);
		final int transactionLevel = oldUndoHandler.getTransactionLevel();
        if(transactionLevel == 0){
            return;
        }
		if(transactionLevel == 1){
		    oldUndoHandler.commit();
		    newUndoHandler.startTransaction();
		    return;
		}
		throw new RuntimeException("can not create map inside transaction");
	}
}
