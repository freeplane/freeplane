package org.freeplane.plugin.workspace.io;

import java.io.File;


public interface IFileHandler {
	Object createFileNode(Object parent, String fileExtension, File file);
}
