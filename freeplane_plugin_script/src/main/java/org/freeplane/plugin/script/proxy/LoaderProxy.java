package org.freeplane.plugin.script.proxy;

import java.io.File;
import java.net.URL;

import org.freeplane.api.Loader;
import org.freeplane.api.Map;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.url.mindmapmode.MapLoader;
import org.freeplane.plugin.script.ScriptContext;

class LoaderProxy implements Loader {

	static Loader of(ScriptContext scriptContext) {
		return new LoaderProxy(scriptContext);
	}

	static Loader of(File file, ScriptContext scriptContext) {
		return new LoaderProxy(scriptContext).load(file);
	}

	static Loader of(URL url, ScriptContext scriptContext) {
		return new LoaderProxy(scriptContext).load(url);
	}

	static Loader of(String file, ScriptContext scriptContext) {
		return new LoaderProxy(scriptContext).load(file);
	}

	private final ScriptContext scriptContext;
	private final MapLoader mapLoader;

	LoaderProxy(ScriptContext scriptContext) {
		super();
		mapLoader = new MapLoader(MModeController.getMModeController());
		this.scriptContext = scriptContext;
	}

	private Loader load(File file) {
		mapLoader.load(file);
		return this;
	}

	private Loader load(URL url) {
		mapLoader.load(url);
		return this;
	}

	private Loader load(String file) {
		mapLoader.load(new File(file));
		return this;
	}

	@Override
	public LoaderProxy newMapLocation(File file) {
		mapLoader.newMapLocation(file);
		return this;
	}

	@Override
	public LoaderProxy unsetMapLocation() {
		mapLoader.unsetMapLocation();
		return this;
	}

	@Override
	public LoaderProxy newMapLocation(String file) {
		 mapLoader.newMapLocation(new File(file));
		 return this;
	}

	@Override
	public LoaderProxy saveAfterLoading() {
		mapLoader.saveAfterLoading();
		return this;
	}

	@Override
	public LoaderProxy selectNodeById(String nodeId) {
		mapLoader.selectNodeById(nodeId);
		return this;
	}

	@Override
	public LoaderProxy withView() {
		mapLoader.withView();
		return this;
	}

	@Override
	public Map getMap() {
		MapModel newMap = mapLoader.getMap();
		return new MapProxy(newMap, scriptContext);
	}
}
