/**
 * author: Marcel Genzmehr
 * 22.08.2011
 */
package org.docear.plugin.core;

import java.net.URI;
import java.util.List;

/**
 * 
 */
public interface IDocearLibrary {
	public List<URI> getMindmaps();
	public URI getLibraryPath();
	public URI getBibtexDatabase();
}
