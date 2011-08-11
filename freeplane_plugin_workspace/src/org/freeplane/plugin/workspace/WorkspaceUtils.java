/**
 * author: Marcel Genzmehr
 * 09.08.2011
 */
package org.freeplane.plugin.workspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.plugin.workspace.config.node.FilesystemFolderNode;
import org.freeplane.plugin.workspace.config.node.FilesystemLinkNode;

/**
 * 
 */
public class WorkspaceUtils {
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public static void saveCurrentConfiguration() {
		String temp = WorkspaceController.getCurrentWorkspaceController().getWorkspaceLocation() + File.separator + "workspace_temp.xml";
		String config = WorkspaceController.getCurrentWorkspaceController().getWorkspaceLocation() + File.separator + "workspace.xml";
		
		try {
			WorkspaceController.getCurrentWorkspaceController().saveConfigurationAsXML(new FileWriter(temp));
			
			FileChannel from = new FileInputStream(temp).getChannel();
			FileChannel to = new FileOutputStream(config).getChannel();
			
			to.transferFrom(from, 0, from.size());
			to.close();
			from.close();
			
			File tempFile = new File(temp);
			tempFile.delete();
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void createFilesystemFolderNode(final File path, final DefaultMutableTreeNode parent) {
		if(!path.isDirectory()) {
			LogUtils.warn("the given path is no folder.");
			return;
		}
		
		DefaultMutableTreeNode targetNode = parent;
		if(parent.getUserObject() instanceof FilesystemFolderNode) { 
			targetNode = (DefaultMutableTreeNode) parent.getParent();
		}
		
		IndexedTree tree = WorkspaceController.getCurrentWorkspaceController().getTree();
		
		FilesystemFolderNode node = new FilesystemFolderNode(path.getPath().replace(File.separator, ""));
		String name = path.getName();
		
		node.setName(name==null? "folder" : name);
				
		if(path!=null) {
			LogUtils.info("FilesystemPath: "+path);
			node.setFolderPath(MLinkController.toLinkTypeDependantURI(getWorkspaceBaseFile(), path, LinkController.LINK_RELATIVE_TO_WORKSPACE));
		}

		Object key = tree.getKeyByUserObject(targetNode.getUserObject());
		tree.addElement(key, node, IndexedTree.AS_CHILD);
		
		WorkspaceController.getCurrentWorkspaceController().getViewModel().reload(targetNode);
		
		saveCurrentConfiguration();
	}

	
	
	public static void createFilesystemLinkNode(final File path, final DefaultMutableTreeNode parent) {
		if(!path.isFile()) {
			LogUtils.warn("the given path is no file.");
			return;
		}
		
		DefaultMutableTreeNode targetNode = parent;
		if(parent.getUserObject() instanceof FilesystemLinkNode) { 
			targetNode = (DefaultMutableTreeNode) parent.getParent();
		}
		
		IndexedTree tree = WorkspaceController.getCurrentWorkspaceController().getTree();
		
		FilesystemLinkNode node = new FilesystemLinkNode(path.getPath().replace(File.separator, ""));
		String name = path.getName();
		
		node.setName(name==null? "fileLink" : name);
				
		if(path!=null) {
			LogUtils.info("FilesystemPath: "+path);			
			node.setLinkPath(MLinkController.toLinkTypeDependantURI(getWorkspaceBaseFile(), path, LinkController.LINK_RELATIVE_TO_WORKSPACE));
		}

		Object key = tree.getKeyByUserObject(targetNode.getUserObject());
		tree.addElement(key, node, IndexedTree.AS_CHILD);
		
		WorkspaceController.getCurrentWorkspaceController().getViewModel().reload(targetNode);
		
		saveCurrentConfiguration();
	}
	
	public static URL getWorkspaceBaseUrl() {
		URL ret = null;
		try {
			ret = getWorkspaceBaseFile().toURL();
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public static File getWorkspaceBaseFile() {
		return new File(ResourceController.getResourceController().getProperty("workspace_location"));
	}
}
