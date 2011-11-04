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
import org.freeplane.plugin.workspace.io.creator.IFileTypeHandler;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

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

	public void scanFileSystem(final AWorkspaceTreeNode node, final File file) {

		if (file != null && file.exists()) {
			if (file.isDirectory()) {
				iterateFolder(node, file);
			}
			else {
				createFileNode(node, file);
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

	private void iterateFolder(AWorkspaceTreeNode parent, File folder) {
		for (File file : folder.listFiles(new FolderFilter())) {
			AWorkspaceTreeNode newParent = createFileNode(parent, FileReadManager.FOLDER_HANDLE, file);
			iterateFolder(newParent, file);

		}
		for (File file : folder.listFiles(new NoFolderFilter())) {
			createFileNode(parent, file);
		}
	}

	private AWorkspaceTreeNode createFileNode(final AWorkspaceTreeNode parent, final File file) {
		String fileExtension = FileReadManager.DEFAULT_HANDLE;
		int dot = file.getPath().lastIndexOf('.');
		if (-1 != dot) {
			fileExtension = file.getPath().substring(dot);
		}
		return createFileNode(parent, fileExtension, file);
	}

	private AWorkspaceTreeNode createFileNode(final AWorkspaceTreeNode parent, String fileExtension, final File file) {
		List<IFileTypeHandler> handlers = getFileTypeHandlers().list(fileExtension);
		if (handlers == null) {
			fileExtension = FileReadManager.DEFAULT_HANDLE;
			handlers = getFileTypeHandlers().list(fileExtension);
		}
		if (handlers != null && handlers.size() == 1) { //FIXME: what if there is more than one handler for a single type?
			IFileTypeHandler nodeCreator = handlers.get(0);
			AWorkspaceTreeNode newParent = nodeCreator.createFileNode(parent, fileExtension, file);
			NodeCreatedEvent event = new NodeCreatedEvent(newParent);
			informNodeCreatedListeners(event);
			return newParent;
		}
		return parent;
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
