package org.freeplane.plugin.script.proxy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.regex.Pattern;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.url.mindmapmode.MapLoader;
import org.freeplane.plugin.script.ScriptContext;

class LoaderProxy implements Proxy.Loader {

	static Proxy.Loader of(ScriptContext scriptContext) {
		return new LoaderProxy(scriptContext);
	}

	static Proxy.Loader of(File file, ScriptContext scriptContext) {
		return new LoaderProxy(scriptContext).load(file);
	}

	static Proxy.Loader of(URL url, ScriptContext scriptContext) {
		return new LoaderProxy(scriptContext).load(url);
	}

	static Proxy.Loader of(String fileOrContent, ScriptContext scriptContext) {
		return new LoaderProxy(scriptContext).load(fileOrContent);
	}

	static Proxy.Loader of(InputStream inputStream, ScriptContext scriptContext) {
		return new LoaderProxy(scriptContext).load(inputStream);
	}

	private final ScriptContext scriptContext;
	private final MapLoader mapLoader;

	LoaderProxy(ScriptContext scriptContext) {
		super();
		mapLoader = new MapLoader(MModeController.getMModeController());
		this.scriptContext = scriptContext;
	}

	private LoaderProxy load(File file) {
		if(file.isAbsolute() || scriptContext == null)
			mapLoader.load(file);
		else
			load(file.getPath().replace('\\', '/'));
		return this;
	}

	private LoaderProxy load(URL url) {
		mapLoader.load(url);
		return this;
	}

	private LoaderProxy load(String filePathOrContent) {
		if(containsMindMapContent(filePathOrContent)) {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(filePathOrContent.getBytes());
			load(inputStream);
		} else
			mapLoader.load(provideScriptContext().toUrl(filePathOrContent));
		return this;
	}

	private LoaderProxy load(InputStream inputStream) {
		mapLoader.setInputStream(inputStream);
		return this;
	}

	Pattern MAP_CONTENT_BEGIN = Pattern.compile("^\\s*<");
	private boolean containsMindMapContent(String filePathOrContent) {
		return MAP_CONTENT_BEGIN.matcher(filePathOrContent).find();
	}

	private ScriptContext provideScriptContext() {
		return scriptContext != null ? scriptContext : new ScriptContext(null);
	}

	@Override
	public LoaderProxy newMapLocation(final File file) {
		final File absoluteFile = AccessController.doPrivileged(new PrivilegedAction<File>() {
			@Override
			public File run() {
				 return provideScriptContext().toAbsoluteFile(file);
			}
		});
		mapLoader.newMapLocation(absoluteFile);
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
	public Proxy.Map load() {
		MapModel newMap = mapLoader.getMap();
		return new MapProxy(newMap, scriptContext);
	}

	@Override
	public Proxy.Map getMap() {
		return load();
	}
}
