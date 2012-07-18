package org.docear.plugin.core.io;

import java.io.File;

/**
 * @author mag
 *
 */
public interface DirectoryObserver {	
	public void fileCreated(File file);
	public void fileRemoved(File file);
}
