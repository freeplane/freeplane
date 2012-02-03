/**
 * author: Marcel Genzmehr
 * 19.07.2011
 */
package org.freeplane.plugin.workspace.io;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.freeplane.core.io.ListHashTable;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.plugin.workspace.io.creator.IFileTypeHandler;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

public class FilesystemManager {

	private final FileReadManager typeManager;
	private boolean filtering = true;

	public FilesystemManager(final FileReadManager typeManager) {
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
				if(node instanceof IFileSystemRepresentation) {
					iterateDirectory(node, file, ((IFileSystemRepresentation) node).orderDescending());
				} 
				else {
					iterateDirectory(node, file, false);
				}
			}
			else {
				createFileNode(node, file);
			}
		}
	}
	
	/**
	 * @param directoryName
	 * @param parentDir
	 * @throws IOException 
	 */
	public void createDirectory(String directoryName, File parentDir) throws IOException {
		File newDir = new File(parentDir, directoryName);
		if(!newDir.mkdirs()) {
			throw new IOException("could not create directory: "+newDir.getPath());
		}		
	}
	
	private ListHashTable<String, IFileTypeHandler> getFileTypeHandlers() {
		return typeManager.getFileTypeHandlers();
	}

	private void iterateDirectory(AWorkspaceTreeNode parent, File directory, final boolean orderDescending) {
		boolean orderDesc = orderDescending;
//		if(parent instanceof IFileSystemRepresentation) {
//			orderDesc = ((IFileSystemRepresentation) parent).orderDescending();
//		}
		
		for (File file : sortFiles(directory.listFiles(new DirectoryFilter()), orderDesc, true)) {
			AWorkspaceTreeNode newParent = createFileNode(parent, FileReadManager.DIRECTORY_HANDLE, file);
			iterateDirectory(newParent, file, orderDesc);

		}
		for (File file : sortFiles(directory.listFiles(new FilesOnlyFilter()), orderDesc, true)) {
			createFileNode(parent, file);
		}
	}

	/**
	 * @param files
	 * @param orderAscending
	 * @param ignoreCase
	 * @return
	 */
	private File[] sortFiles(File[] files, final boolean orderDescending, final boolean ignoreCase) {
		Comparator<File> comparator = new Comparator<File>() {
			public int compare(File o1, File o2) {
				File f1 = o1;
				File f2 = o2;
				if(ignoreCase) {
					f1 = new File(f1.getParentFile(), f1.getName().toLowerCase());
					f2 = new File(f2.getParentFile(), f2.getName().toLowerCase());
				}
				int compareResult = f1.compareTo(f2);
				if(orderDescending) {
					return compareResult*-1;
				}
				return compareResult;
			}
		};
		Arrays.sort(files, comparator);
		return files;
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
			return newParent;
		}
		return parent;
	}	
	
	
	/***********************************************************************************
	 * INTERNAL CLASS DEFINITIONS
	 **********************************************************************************/
	
	private class DirectoryFilter implements FileFilter  {
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
	
	private class FilesOnlyFilter implements FileFilter {
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
