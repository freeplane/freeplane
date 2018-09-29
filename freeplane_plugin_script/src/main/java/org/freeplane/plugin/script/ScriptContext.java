package org.freeplane.plugin.script;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.freeplane.core.util.Compat;
import org.freeplane.features.explorer.AccessedNodes;
import org.freeplane.features.map.NodeModel;

public class ScriptContext implements AccessedNodes{

	private final NodeScript nodeScript;

	public ScriptContext(NodeScript nodeScript) {
		this.nodeScript = nodeScript;
	}

	public URL getBaseUrl() {
		return nodeScript.getBaseUrl();
	}

	public File toAbsoluteFile(File file) {
		final File absoluteFile;
		if(file.isAbsolute())
			absoluteFile = file;
		else {
			final URL baseUrl = getBaseUrl();
			if (baseUrl == null)
				throw new IllegalStateException("Can not use relative files without base URL");
			else {
				final File parentFile = Compat.urlToFile(baseUrl).getAbsoluteFile().getParentFile();
				absoluteFile = new File(parentFile, file.getPath());
			}
		}
		return absoluteFile;
	}

	public URL toUrl(String path) {
		try {
			File file = new File(path);
			if(file.isAbsolute()) {
				return file.toURL();
			}
			else  {
				URL baseUrl = getBaseUrl();
				if (baseUrl != null){
					return new URL(baseUrl, path);
				}
				else
					throw new IllegalStateException("Can not use relative URL without base URL");
			}
		}
		catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void accessNode(final NodeModel accessedNode) {
		FormulaDependencies.accessNode(nodeScript.getNodeModel(), accessedNode);
	}

	@Override
	public void accessBranch(final NodeModel accessedNode) {
		FormulaDependencies.accessBranch(nodeScript.getNodeModel(), accessedNode);
	}

	@Override
	public void accessAll() {
		FormulaDependencies.accessAll(nodeScript.getNodeModel());
	}
	@Override
	public void accessGlobalNode() {
		FormulaDependencies.accessGlobalNode(nodeScript.getNodeModel());
	}

	@Override
	public String toString() {
		return nodeScript.toString();
	}
}
