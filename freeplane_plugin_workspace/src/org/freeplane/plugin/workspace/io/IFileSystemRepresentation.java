package org.freeplane.plugin.workspace.io;

import java.io.File;

import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;

public interface IFileSystemRepresentation {
	public File getFile();
	
	public void orderDescending(boolean enable);
	
	@ExportAsAttribute(name="orderDescending")
	public boolean orderDescending();
}
