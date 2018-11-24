package org.freeplane.plugin.script;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.freeplane.core.util.Compat;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.explorer.AccessedNodes;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.dependencies.RelatedElements;

public class ScriptContext implements AccessedNodes{

	private final NodeScript nodeScript;

	private final RelatedElements relatedElements;

	public ScriptContext(NodeScript nodeScript) {
		this.nodeScript = nodeScript;
		this.relatedElements = nodeScript != null ? new RelatedElements(nodeScript.node) : null;
	}

	public URL getBaseUrl() {
		return nodeScript != null ? nodeScript.getBaseUrl() : null;
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
					return file.getAbsoluteFile().toURL();
			}
		}
		catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void accessAttribute(final NodeModel accessedNode, Attribute accessedAttribute) {
		if(nodeScript != null)
			relatedElements.relateAttribute(accessedNode, accessedAttribute);
	}

	@Override
	public void accessValue(NodeModel accessedNode) {
		if(nodeScript != null)
			relatedElements.relateNode(accessedNode);
	}

	@Override
	public void accessNode(final NodeModel accessedNode) {
		if(nodeScript != null) {
			FormulaDependencies.accessNode(nodeScript.node, accessedNode);
			relatedElements.relateMap(accessedNode.getMap());
		}
	}

	@Override
	public void accessBranch(final NodeModel accessedNode) {
		if(nodeScript != null)
			FormulaDependencies.accessBranch(nodeScript.node, accessedNode);
	}

	@Override
	public void accessAll() {
		if(nodeScript != null)
			FormulaDependencies.accessAll(nodeScript.node);
	}
	@Override
	public void accessGlobalNode() {
		if(nodeScript != null)
			FormulaDependencies.accessGlobalNode(nodeScript.node);
	}

	public RelatedElements getRelatedElements() {
		if(nodeScript != null)
			return relatedElements;
		else
			throw new IllegalStateException("Accessed values not tracked without related node");
	}

	@Override
	public String toString() {
		return nodeScript.toString();
	}

}
