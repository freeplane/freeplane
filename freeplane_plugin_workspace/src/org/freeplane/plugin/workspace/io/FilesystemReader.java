/**
 * author: Marcel Genzmehr
 * 19.07.2011
 */
package org.freeplane.plugin.workspace.io;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.freeplane.core.io.ListHashTable;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;
import org.freeplane.plugin.workspace.io.NodeCreatedEvent.NodeCreatedType;
import org.freeplane.plugin.workspace.io.creator.IFileTypeHandler;

public class FilesystemReader {

	private final FileReadManager typeManager;
	private final HashSet<INodeCreatedListener> createdListeners = new HashSet<INodeCreatedListener>();
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
	
	public void addNodeCreatedListener(INodeCreatedListener listener) {
		if(listener == null || createdListeners.contains(listener)) {
			return;
		}
		
		createdListeners.add(listener);
	}
	
	public void removeNodeCreatedListener(INodeCreatedListener listener) {
		if(listener == null) {
			return;
		}
		createdListeners.remove(listener);
	}
	
	private ListHashTable<String, IFileTypeHandler> getFileTypeHandlers() {
		return typeManager.getFileTypeHandlers();
	}

	private void iterateFolder(Object object, File folder) {
		for (File file : folder.listFiles(new FolderFilter())) {
			Object path = createFileNode(object, FileReadManager.FOLDER_HANDLE, file);
			iterateFolder(path, file);

		}
		for (File file : folder.listFiles(new NoFolderFilter())) {
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
		Object parent = path;
		if(parent instanceof AWorkspaceNode) {
			parent = WorkspaceController.getController().getIndexTree().getKeyByUserObject(path);
		}
		List<IFileTypeHandler> handlers = getFileTypeHandlers().list(fileExtension);
		if (handlers == null) {
			fileExtension = FileReadManager.DEFAULT_HANDLE;
			handlers = getFileTypeHandlers().list(fileExtension);
		}
		if (handlers != null && handlers.size() == 1) { //FIXME: what if there is more than one handler for a single type?
			IFileTypeHandler nodeCreator = handlers.get(0);
			Object newPath = nodeCreator.createFileNode(parent, fileExtension, file);
			NodeCreatedEvent event = new NodeCreatedEvent(parent, newPath, 
					(fileExtension.equals(FileReadManager.FOLDER_HANDLE) ? NodeCreatedType.NODE_TYPE_FOLDER : NodeCreatedType.NODE_TYPE_FILE));
			informNodeCreatedListeners(event);
			return newPath;
		}
		return path;
	}
	
	public void informNodeCreatedListeners(NodeCreatedEvent event) {
		Iterator<INodeCreatedListener> iterator = this.createdListeners.iterator();
		while(iterator.hasNext()) {
			iterator.next().nodeCreated(event);
		}
	}
	
	
	
	
	/***********************************************************************************
	 * INTERNAL CLASS DEFINITIONS
	 **********************************************************************************/
	
	private class FolderFilter implements FileFilter  {
		private boolean filtering = true;
		
		public boolean accept(File pathname) {
			if(filtering && pathname.getName().startsWith(".") && !pathname.getName().equals("."+ResourceController.getResourceController().getProperty("workspace.profile"))) {
				return false;
			}
			if (pathname.isDirectory()) {
				return true;
			}
			return false;
		}
	}
	
	private class NoFolderFilter implements FileFilter {
		private boolean filtering = true;
		
		public boolean accept(File pathname) {
			if(filtering && pathname.getName().startsWith(".")) {
				return false;
			}
			if (pathname.isFile()) {
				return true;
			}
			return false;
		}
	}
	
}
