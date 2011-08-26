/**
 * author: Marcel Genzmehr
 * 19.07.2011
 */
package org.freeplane.plugin.workspace.io;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import org.freeplane.core.io.ListHashTable;
import org.freeplane.plugin.workspace.io.creator.IFileTypeHandler;

public class FilesystemReader {

	private final FileReadManager typeManager;
	private boolean filtering = true;

	public FilesystemReader(final FileReadManager typeManager) {
		this.typeManager = typeManager;
	}

	public boolean isFiltering() {
		return filtering;
	}

	public void setFiltering(boolean filtering) {
		this.filtering = filtering;
	}

	public void scanFilesystem(final Object object, final File file) {

		if (file != null && file.exists()) {
			if (file.isDirectory()) {
				iterateFolder(object, file);
			}
			else {
				createFileNode(object, file);
			}
		}
	}	
	
	private ListHashTable<String, IFileTypeHandler> getFileTypeHandlers() {
		return typeManager.getFileTypeHandlers();
	}

	private void iterateFolder(Object object, File folder) {
		for (File file : folder.listFiles(new FolderFilter())) {
			if (filtering && file.getName().startsWith(".")) {
				continue;
			}
			Object path = createFileNode(object, FileReadManager.FOLDER_HANDLE, file);
			iterateFolder(path, file);

		}
		for (File file : folder.listFiles(new NoFolderFilter())) {
			if (filtering && file.getName().startsWith(".")) {
				continue;
			}

			createFileNode(object, file);
		}
	}

	private Object createFileNode(final Object path, final File file) {
		String fileExtension = FileReadManager.DEFAULT_HANDLE;
		int dot = file.getPath().lastIndexOf('.');
		if (-1 != dot) {
			fileExtension = file.getPath().substring(dot);
			// LogUtils.info("Filetype: " + new
			// MimetypesFileTypeMap().getContentType(file) + " " +
			// file.getPath().substring(dot));
		}
		return createFileNode(path, fileExtension, file);
	}

	private Object createFileNode(final Object path, String fileExtension, final File file) {
		List<IFileTypeHandler> handlers = getFileTypeHandlers().list(fileExtension);
		if (handlers == null) {
			fileExtension = FileReadManager.DEFAULT_HANDLE;
			handlers = getFileTypeHandlers().list(fileExtension);
		}
		if (handlers != null && handlers.size() == 1) {
			IFileTypeHandler nodeCreator = handlers.get(0);
			return nodeCreator.createFileNode(path, fileExtension, file);
		}
		return path;
	}
	
	
	
	
	/***********************************************************************************
	 * INTERNAL CLASS DEFINITIONS
	 **********************************************************************************/
	
	private class FolderFilter implements FileFilter  {

		public boolean accept(File pathname) {
			if (pathname.isDirectory()) {
				return true;
			}
			return false;
		}
	}
	
	private class NoFolderFilter implements FileFilter {

		public boolean accept(File pathname) {
			if (pathname.isFile()) {
				return true;
			}
			return false;
		}
	}
	
}
