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
import java.util.Properties;

import org.freeplane.core.io.ListHashTable;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.creator.DefaultFileNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class FileSystemManager {

	private final FileReadManager typeManager;
	private boolean filtering = true;
	private FileReadManager fileTypeManager;

	public FileSystemManager(final FileReadManager typeManager) {
		if(typeManager == null) {
			this.typeManager = getDefaultFileTypeManager();
		}
		else {
			this.typeManager = typeManager;
		}
	}

	public boolean isFiltering() {
		return filtering;
	}

	public void setFiltering(boolean filtering) {
		this.filtering = filtering;
	}
	
	public void scanFileSystem(AWorkspaceTreeNode node, File file) {
		scanFileSystem(node, file, null);
	}

	public void scanFileSystem(AWorkspaceTreeNode node, File file, FileFilter filter) {

		if (file != null && file.exists()) {
			if (file.isDirectory()) {
				if(node instanceof IFileSystemRepresentation) {
					iterateDirectory(node, file, filter, ((IFileSystemRepresentation) node).orderDescending());
				} 
				else {
					iterateDirectory(node, file, filter, false);
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
	public File createDirectory(String directoryName, File parentDir) throws IOException {
		File newDir = new File(parentDir, directoryName);
		int count = 0;
		while(newDir.exists() && count++ < 100) {
			newDir = new File(parentDir, directoryName + " ("+count+")");
		}
		if(!newDir.mkdirs()) {
			throw new IOException("could not create directory: "+newDir.getPath());
		}
		return newDir;
	}
	
	/**
	 * @param directoryName
	 * @param parentDir
	 * @throws IOException 
	 */
	public File createFile(String fileName, File parentDir) throws IOException {
		String pureName = fileName;
		String fileExtension = "";
		
		int strPointer = fileName.lastIndexOf(".");
		if(strPointer > -1) {
			pureName = fileName.substring(0, strPointer);
			fileExtension = fileName.substring(strPointer+1);
		}
		File newFile = new File(parentDir, pureName+"."+fileExtension);
		int count = 0;
		while(newFile.exists() && count++ < 100) {
			newFile = new File(parentDir, pureName + " ("+count+")" +(fileExtension.trim().length() > 0 ? "."+fileExtension : ""));
		}
		if(!newFile.createNewFile()) {
			throw new IOException("could not create file: "+newFile.getPath());
		}
		return newFile;
	}
	
	private ListHashTable<String, IFileTypeHandler> getFileTypeHandlers() {
		return typeManager.getFileTypeHandlers();
	}

	private void iterateDirectory(AWorkspaceTreeNode parent, File directory, FileFilter filter, final boolean orderDescending) {
		boolean orderDesc = orderDescending;
//		if(parent instanceof IFileSystemRepresentation) {
//			orderDesc = ((IFileSystemRepresentation) parent).orderDescending();
//		}
		
		for (File file : sortFiles(directory.listFiles(new DirectoryFilter(filter)), orderDesc, true)) {
			AWorkspaceTreeNode newParent = createFileNode(parent, FileReadManager.DIRECTORY_HANDLE, file);
			iterateDirectory(newParent, file, filter, orderDesc);

		}
		for (File file : sortFiles(directory.listFiles(new FilesOnlyFilter(filter)), orderDesc, true)) {
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
		private FileFilter extraFilter;
		
		public DirectoryFilter(FileFilter filter) {
			this.extraFilter = filter;
		}

		public boolean accept(File pathname) {
			if(filtering && pathname.getName().startsWith(".") && !pathname.getName().equals("."+ResourceController.getResourceController().getProperty("workspace.profile"))) {
				return false;
			}
			if (pathname.isDirectory()) {
				if(this.extraFilter == null) {
					return true;
				}
				return this.extraFilter.accept(pathname);
			}
			return false;
		}
	}
	
	private class FilesOnlyFilter implements FileFilter {
		private boolean filtering = true;
		private FileFilter extraFilter;
		
		public FilesOnlyFilter(FileFilter filter) {
			this.extraFilter = filter;
		}

		public boolean accept(File pathname) {
			if(filtering && pathname.getName().startsWith(".")) {
				return false;
			}
			if (pathname.isFile()) {
				if(this.extraFilter == null) {
					return true;
				}
				return this.extraFilter.accept(pathname);
			}
			return false;
		}
	}
	
	private FileReadManager getDefaultFileTypeManager() {
		if (this.fileTypeManager == null) {
			this.fileTypeManager = new FileReadManager();
			Properties props = new Properties();
			try {
				props.load(this.getClass().getResourceAsStream("/conf/filenodetypes.properties"));

				Class<?>[] args = {};
				for (Object key : props.keySet()) {
					try {
						Class<?> clazz = DefaultFileNodeCreator.class;
						
						clazz = this.getClass().getClassLoader().loadClass(key.toString());

						AFileNodeCreator handler = (AFileNodeCreator) clazz.getConstructor(args).newInstance();
						handler.setFileTypeList(props.getProperty(key.toString(), ""), "\\|");
						this.fileTypeManager.addFileHandler(handler);
					}
					catch (ClassNotFoundException e) {
						LogUtils.warn("Class not found [" + key + "]", e);
					}
					catch (ClassCastException e) {
						LogUtils.warn("Class [" + key + "] is not of type: PhysicalNode", e);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this.fileTypeManager;
	}
}
