package org.freeplane.plugin.workspace.io;

import java.io.File;
import java.util.List;

import org.freeplane.core.io.ListHashTable;

public class FilesystemReader {
	
		
	private final FileReadManager typeManager;
	
	public FilesystemReader(final FileReadManager typeManager) {
		this.typeManager = typeManager;
	}
	
	private ListHashTable<String, IFileTypeHandler> getFileTypeHandlers() {
		return typeManager.getFileTypeHandlers();
	}
		
		
	private void iterateFolder(Object object, File folder) {		
		for(File file : folder.listFiles()) {
			if(file.isDirectory()) {
				Object path = createFileNode(object, FileReadManager.FOLDER_HANDLE, file);
				iterateFolder(path, file);
			}
			else {
				createFileNode(object, file);
			}
		}		
	}
	
	public void scanFilesystem(final Object object, final File file) {
		
		if(file != null && file.exists()) {
			if(file.isDirectory()) {		
				iterateFolder(object,file);
			} 
			else {
				createFileNode(object,file);
			}
		}
	}
	
	private Object createFileNode(final Object path, final File file) {
		String fileExtension = FileReadManager.DEFAULT_HANDLE;
		return createFileNode(path, fileExtension, file);		
	}
	
	private Object createFileNode(final Object path, final String fileExtension, final File file) {		
		List<IFileTypeHandler> handlers = getFileTypeHandlers().list(fileExtension);	
		if (handlers != null && handlers.size() == 1) {
			IFileTypeHandler nodeCreator = handlers.get(0);
			return nodeCreator.createFileNode(path, fileExtension, file);
		}
		return path;
	}	
}
