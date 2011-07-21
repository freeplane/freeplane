package org.freeplane.plugin.workspace.io;

import org.freeplane.core.io.IElementHandler;
import org.freeplane.core.io.ListHashTable;
import org.freeplane.core.io.ReadManager;

public class FilesystemReader {
	
	private final ReadManager typeManager;
	
	public FilesystemReader(final ReadManager typeManager) {
		this.typeManager = typeManager;
	}
	
	private ListHashTable<String, IElementHandler> getElementHandlers() {
		return typeManager.getElementHandlers();
	}

}
