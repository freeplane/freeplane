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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.channels.FileChannel;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.plugin.workspace.config.WorkspaceConfiguration;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;
import org.freeplane.plugin.workspace.config.node.FolderNode;
import org.freeplane.plugin.workspace.config.node.LinkNode;
import org.freeplane.plugin.workspace.config.node.LinkTypeFileNode;
import org.freeplane.plugin.workspace.config.node.PhysicalFolderNode;
import org.freeplane.plugin.workspace.config.node.VirtualFolderNode;
import org.freeplane.plugin.workspace.io.node.DefaultFileNode;

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
		String profileName = ResourceController.getResourceController().getProperty(WorkspacePreferences.WORKSPACE_PROFILE, null);

		URI uri;
		File temp, config;
		try {
			uri = new URI(WorkspaceController.WORKSPACE_RESOURCE_URL_PROTOCOL + ":/." + profileName + "/tmp_"
					+ WorkspaceConfiguration.CONFIG_FILE_NAME);
			temp = WorkspaceUtils.resolveURI(uri);
			uri = new URI(WorkspaceController.WORKSPACE_RESOURCE_URL_PROTOCOL + ":/." + profileName + "/"
					+ WorkspaceConfiguration.CONFIG_FILE_NAME);
			config = WorkspaceUtils.resolveURI(uri);
		}
		catch (URISyntaxException e) {
			// TODO DOCEAR: error message
			e.printStackTrace();
			return;
		}
		
		// String temp =
		// WorkspaceController.getController().getWorkspaceLocation() +
		// File.separator + "workspace_temp.xml";
		
		//String config = WorkspaceController.getController().getWorkspaceLocation() + File.separator + "workspace.xml";

		try {
			WorkspaceController.getController().saveConfigurationAsXML(new FileWriter(temp));

			FileChannel from = new FileInputStream(temp).getChannel();
			FileChannel to = new FileOutputStream(config).getChannel();

			to.transferFrom(from, 0, from.size());
			to.close();
			from.close();
			
			temp.delete();
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static void createPhysicalFolderNode(final File path, final DefaultMutableTreeNode parent) {
		if (!path.isDirectory()) {
			LogUtils.warn("the given path is no folder.");
			return;
		}

		PhysicalFolderNode node = new PhysicalFolderNode(FolderNode.FOLDER_TYPE_PHYSICAL);
		String name = path.getName();

		node.setName(name == null ? "folder" : name);

		if (path != null) {
			node.setFolderPath(MLinkController.toLinkTypeDependantURI(getWorkspaceBaseFile(), path,
					LinkController.LINK_RELATIVE_TO_WORKSPACE));
		}

		addAndSave(findAllowedTargetNode(parent), node);
	}

	public static void createLinkTypeFileNode(final File path, final DefaultMutableTreeNode parent) {
		if (!path.isFile()) {
			LogUtils.warn("the given path is no file.");
			return;
		}

		LinkTypeFileNode node = new LinkTypeFileNode(LinkNode.LINK_TYPE_FILE);
		String name = path.getName();

		node.setName(name == null ? "fileLink" : name);

		if (path != null) {
			LogUtils.info("FilesystemPath: " + path);
			node.setLinkPath(MLinkController.toLinkTypeDependantURI(getWorkspaceBaseFile(), path,
					LinkController.LINK_RELATIVE_TO_WORKSPACE));
		}

		addAndSave(findAllowedTargetNode(parent), node);
	}

	public static void createVirtualFolderNode(String folderName, final DefaultMutableTreeNode parent) {
		if (folderName == null || folderName.trim().length() <= 0) {
			return;
		}

		DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) (parent == null ? WorkspaceController.getController()
				.getViewModel().getRoot() : parent);

		VirtualFolderNode node = new VirtualFolderNode(FolderNode.FOLDER_TYPE_VIRTUAL);
		node.setName(folderName);

		addAndSave(targetNode, node);
	}

	public static URI getWorkspaceBaseURI() {
		URI ret = null;
		ret = getWorkspaceBaseFile().toURI();
		return ret;
	}

	public static File getWorkspaceBaseFile() {
		String location = ResourceController.getResourceController().getProperty("workspace_location");
		if (location == null || location.length() == 0) {
			location = ResourceController.getResourceController().getProperty("workspace_location_new");
		}
		return new File(location);
	}

	public static String stripIllegalChars(String string) {
		if (string == null) {
			return null;
		}
		
		return string.replaceAll("[^a-zA-Z0-9äöüÄÖÜ]+", "");
	}

	public static URI absoluteURI(final URI uri) {
		try {
			URLConnection urlConnection = uri.toURL().openConnection();
			if (urlConnection == null) {
				return null;
			}
			else {
				return urlConnection.getURL().toURI();
			}
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return uri;

	}

	public static File resolveURI(final URI uri) {
		URI absoluteUri = absoluteURI(uri);
		if (absoluteUri == null) {
			return null;
		}
		return new File(absoluteUri);
	}
	
	public static URI getURI(final File f) {
		return f.toURI();
	}
	
	/**
	 * @param targetNode
	 * @param node
	 */
	private static void addAndSave(DefaultMutableTreeNode targetNode, AWorkspaceNode node) {
		IndexedTree tree = WorkspaceController.getController().getIndexTree();
		Object key = tree.getKeyByUserObject(targetNode.getUserObject());
		tree.addElement(key, node, key + "/" + node.getId(), IndexedTree.AS_CHILD);

		WorkspaceController.getController().getViewModel().reload(targetNode);

		saveCurrentConfiguration();
	}

	public static DefaultMutableTreeNode findAllowedTargetNode(final DefaultMutableTreeNode node) {
		DefaultMutableTreeNode targetNode = node;
		// DOCEAR: drops are not allowed on physical nodes, for the moment
		while (targetNode.getUserObject() instanceof DefaultFileNode || targetNode.getUserObject() instanceof PhysicalFolderNode
				|| targetNode.getUserObject() instanceof LinkNode) {
			targetNode = (DefaultMutableTreeNode) targetNode.getParent();
		}
		return targetNode;
	}

}
