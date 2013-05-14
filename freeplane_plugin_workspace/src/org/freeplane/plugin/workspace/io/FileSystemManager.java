/**
 * author: Marcel Genzmehr
 * 19.07.2011
 */
package org.freeplane.plugin.workspace.io;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.freeplane.core.io.ListHashTable;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.creator.DefaultFileNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class FileSystemManager {

	private final FileReadManager typeManager;
	private boolean filtering = true;
	private FileReadManager fileTypeManager;
	private static IConflictHandler directoryConflictHandler;
	private static IConflictHandler fileConflictHandler;

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
	
	public static void copyFile(File srcFile, File destDir) throws IOException {
		copyFile(srcFile, destDir, false);
	}
	
	public static void copyFile(File srcFile, File destDir, Boolean overwrite) throws IOException {
		if(srcFile == null || destDir == null) {
			throw new IllegalArgumentException("NULL");
		}
		if(!srcFile.exists() || !srcFile.isFile()) {
			throw new IllegalArgumentException("srcFile is not a file or does not exist");
		}
		
		copyFiles(Arrays.asList(new File[]{srcFile}), destDir, overwrite);
//		if(targetFile.exists()) {
//			if(silentOverwrite) {
//				if(!FileUtils.deleteQuietly(targetFile)) {
//					throw new IOException("can not copy file "+targetFile);
//				}
//			}
//			else {
//				throw new FileExistsException(targetFile);
//			}
//		}
//		
//		FileUtils.copyFile(file, targetFile);
	}
	
	public static void copyDirectory(File dir, File newParentDir) throws IOException {
		copyDirectory(dir, newParentDir, false);
	}
	
	public static void copyDirectory(File dir, File newParentDir, boolean overwrite) throws IOException {
		if(dir == null || newParentDir == null) {
			throw new IllegalArgumentException("NULL");
		}
		
		if(newParentDir.exists() && !newParentDir.isDirectory()) {
			throw new IOException("dest is no directory. a directory cannot be copied to a file.");
		}
		
		copyFiles(Arrays.asList(new File[]{dir}), newParentDir, overwrite);
	}
	
	public static void moveFile(File srcFile, File destDir) throws IOException {
		moveFile(srcFile, destDir, false);
	}
	
	public static void moveFile(File srcFile, File destDir, Boolean overwrite) throws IOException {
		if(srcFile == null || destDir == null) {
			throw new IllegalArgumentException("NULL");
		}
		if(!srcFile.exists() || !srcFile.isFile()) {
			throw new IllegalArgumentException("srcFile is not a file or does not exist");
		}
		
		moveFiles(Arrays.asList(new File[]{srcFile}), destDir, overwrite);
	}
	
	public static void moveDirectory(File dir, File newParentDir) throws IOException {
		copyDirectory(dir, newParentDir, false);
	}
	
	public static void moveDirectory(File dir, File newParentDir, boolean overwrite) throws IOException {
		if(dir == null || newParentDir == null) {
			throw new IllegalArgumentException("NULL");
		}
		
		if(newParentDir.exists() && !newParentDir.isDirectory()) {
			throw new IOException("dest is no directory. a directory cannot be copied to a file.");
		}
		
		moveFiles(Arrays.asList(new File[]{dir}), newParentDir, overwrite);
	}
	
	public static void copyFiles(List<File> files, File destDir, boolean overwrite) throws IOException {
		if(files == null || destDir == null) {
			throw new IllegalArgumentException("NULL");
		}
		if(destDir.exists() && !destDir.isDirectory()) {
			throw new IOException("destDir is no directory.");
		}
		List<ITask> opList = new ArrayList<ITask>();
		
		for (File file : files) {
			buildCopyOperationList(file, new File(destDir, file.getName()), opList);
		}
		
		Properties props = new Properties();
		props.setProperty("overwriteAll", String.valueOf(overwrite));
		props.setProperty("mergeAll", String.valueOf(overwrite));
		
		execOperations(opList, props);
	}
	
	public static void moveFiles(List<File> files, File destDir, boolean overwrite) throws IOException {
		if(files == null || destDir == null) {
			throw new IllegalArgumentException("NULL");
		}
		if(destDir.exists() && !destDir.isDirectory()) {
			throw new IOException("destDir is no directory.");
		}
		List<ITask> opList = new ArrayList<ITask>();
		
		for (File file : files) {
			buildMoveOperationList(file, new File(destDir, file.getName()), opList);
		}
		
		Properties props = new Properties();
		props.setProperty("overwriteAll", String.valueOf(overwrite));
		props.setProperty("mergeAll", String.valueOf(overwrite));
		
		execOperations(opList, props);
	}
	
	public static List<ITask> buildCopyOperationList(final File srcFile, final File destFile) {
		List<ITask> list = new ArrayList<ITask>();
		buildCopyOperationList(srcFile, destFile, list);
		return list;
	}
	
	public static List<ITask> buildMoveOperationList(final File srcFile, final File destFile) {
		List<ITask> list = new ArrayList<ITask>();
		buildMoveOperationList(srcFile, destFile, list);
		return list;
	}
	
	public static void buildCopyOperationList(final File srcFile, final File destFile, final List<ITask> ops) {
		if(srcFile.isDirectory()) {
			ops.add(new ITask() {				
				public void exec(Properties properties) throws IOException {
					if(onSkipList(destFile.getParentFile(), properties)) {
						addSkippedDir(destFile, properties);
						throw new SkipTaskException();
					}
					
					if(destFile.exists()) {
						properties.setProperty("opType", "1");
						if(!Boolean.parseBoolean(properties.getProperty("mergeAll", "false"))) {
							try {
								getDirectoryConflictHandler().resolveConflict(destFile, properties);
							}
							catch (SkipTaskException e) {
								addSkippedDir(destFile, properties);
								throw e;
							}
						}
						FileUtils.touch(destFile);
					}
					else {
						destFile.mkdirs();
					}
				}
			});
			for(File file : srcFile.listFiles()) {
				buildCopyOperationList(file, new File(destFile, file.getName()), ops);
			}
		}
		else {
			ops.add(new ITask() {
				public void exec(Properties properties) throws IOException {
					if(onSkipList(destFile.getParentFile(), properties)) {
						throw new SkipTaskException();
					}
					
					if(destFile.exists()) {
						properties.setProperty("opType", "1");
						if(!Boolean.parseBoolean(properties.getProperty("overwriteAll", "false"))) {
							getFileConflictHandler().resolveConflict(destFile, properties);
						}
					}
					FileUtils.copyFile(srcFile, destFile);
				}
			});
		}
	}
	
	public static void buildMoveOperationList(final File srcFile, final File destFile, final List<ITask> ops) {
		if(srcFile.isDirectory()) {
			ops.add(new ITask() {				
				public void exec(Properties properties) throws IOException {
					if(onSkipList(destFile.getParentFile(), properties)) {
						addSkippedDir(destFile, properties);
						throw new SkipTaskException();
					}
					
					if(destFile.exists()) {
						properties.setProperty("opType", "2");
						if(!Boolean.parseBoolean(properties.getProperty("mergeAll", "false"))) {
							try {
								getDirectoryConflictHandler().resolveConflict(destFile, properties);
							}
							catch (SkipTaskException e) {
								addSkippedDir(destFile, properties);
								throw e;
							}
						}
						FileUtils.touch(destFile);
					}
					else {
						destFile.mkdirs();
					}
				}
			});
			for(File file : srcFile.listFiles()) {
				buildMoveOperationList(file, new File(destFile, file.getName()), ops);
			}
		}
		else {
			ops.add(new ITask() {
				public void exec(Properties properties) throws IOException {
					if(onSkipList(destFile.getParentFile(), properties)) {
						throw new SkipTaskException();
					}
					
					if(destFile.exists()) {
						properties.setProperty("opType", "2");
						if(!Boolean.parseBoolean(properties.getProperty("overwriteAll", "false"))) {
							getFileConflictHandler().resolveConflict(destFile, properties);
						}
						if(!FileUtils.deleteQuietly(destFile)) {
							throw new SkipTaskException();
						}
					}
					FileUtils.moveFile(srcFile, destFile);
				}
			});
		}
	}
	
	private static void addSkippedDir(File dest, Properties properties) {
		if(properties == null || dest == null) {
			return;
		}
		String list = properties.getProperty("skippedDirs", "");
		String entry = dest.getPath()+";";
		if(!list.contains(entry)) {
			list += entry;
			properties.setProperty("skippedDirs", list);
		}
	}
	
	private static boolean onSkipList(File dest, Properties properties) {
		if(properties == null || dest == null) {
			return false;
		}
		String list = properties.getProperty("skippedDirs", "");
		String entry = dest.getPath()+";";
		if(list.contains(entry)) {
			return true;
		}
		return false;
	}

	public static IConflictHandler getDirectoryConflictHandler() {
		if(directoryConflictHandler == null) {
			directoryConflictHandler = new IConflictHandler() {
				public void resolveConflict(File file, Properties properties) throws IOException {
					LogUtils.info("Error in org.freeplane.plugin.workspace.io.FileSystemManager: directory already exists: " + file);
					throw new SkipTaskException();
				}
			};
		}
		return directoryConflictHandler;
	}
	
	public static IConflictHandler getFileConflictHandler() {
		if(fileConflictHandler == null) {
			fileConflictHandler = new IConflictHandler() {
				public void resolveConflict(File file, Properties properties) throws IOException {
					LogUtils.info("Error in org.freeplane.plugin.workspace.io.FileSystemManager: file already exists: " + file);
					throw new SkipTaskException();
				}
			};
		}
		return fileConflictHandler;
	}
	
	public static void setDirectoryConflictHandler(IConflictHandler handler) {
		directoryConflictHandler = handler;
	}

	public static void setFileConflictHandler(IConflictHandler handler) {
		fileConflictHandler = handler;
	}
	
	public static void execOperations(List<ITask> ops) throws IOException {
		execOperations(ops, null);
	}
	
	public static void execOperations(List<ITask> ops, Properties properties) throws IOException {
		if(ops == null) {
			return;
		}
		if(properties == null) {
			properties = new Properties();
		}
		Iterator<ITask> iter = ops.iterator();
		while (iter.hasNext()) {
			ITask op = iter.next();
			try {
				op.exec(properties);
				iter.remove();
			} 
			catch (SkipTaskException e) {
				iter.remove();
				continue;
			}
		}
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
		if (handlers != null && handlers.size() == 1) { //WORKSPACE - ToDo: what if there is more than one handler for a single type?
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
