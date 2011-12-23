/**
 * author: Marcel Genzmehr
 * 22.08.2011
 */
package org.docear.plugin.core;

import java.net.URI;
import java.util.List;
import java.util.Set;

import org.docear.plugin.core.workspace.node.FolderTypeProjectsNode;

/**
 * 
 */
public interface IDocearLibrary {
	public List<URI> getMindmaps();
	public URI getLibraryPath();
	public URI getBibtexDatabase();
	public Set<FolderTypeProjectsNode> getProjects();
	public List<URI> getProjectPaths();
}
