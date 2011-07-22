package org.freeplane.plugin.workspace.io.creator;

import java.io.File;


public interface IFileTypeHandler {
	Object createFileNode(Object parent, String fileExtension, File file);
	public Object[] getSupportedFileTypes();
}
