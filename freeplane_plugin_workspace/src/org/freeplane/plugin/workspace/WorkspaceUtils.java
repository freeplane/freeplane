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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.channels.FileChannel;

import javax.swing.JOptionPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.url.UrlManager;
import org.freeplane.plugin.workspace.config.WorkspaceConfiguration;
import org.freeplane.plugin.workspace.config.node.LinkTypeFileNode;
import org.freeplane.plugin.workspace.config.node.PhysicalFolderNode;
import org.freeplane.plugin.workspace.config.node.VirtualFolderNode;
import org.freeplane.plugin.workspace.dialog.WorkspaceChooserDialogPanel;
import org.freeplane.plugin.workspace.io.node.DefaultFileNode;
import org.freeplane.plugin.workspace.model.WorkspaceIndexedTreeModel;
import org.freeplane.plugin.workspace.model.node.AFolderNode;
import org.freeplane.plugin.workspace.model.node.ALinkNode;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

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

	public static void showWorkspaceChooserDialog() {
		WorkspaceChooserDialogPanel dialog = new WorkspaceChooserDialogPanel();
		
		JOptionPane.showMessageDialog(UITools.getFrame(), dialog, TextUtils.getRawText("no_location_set"), JOptionPane.PLAIN_MESSAGE);
	
		String location = dialog.getLocationPath();
		String profileName = dialog.getProfileName();
	
		if (location.length() == 0 || profileName.length() == 0) {
			location = "."+File.separator;
		}
	
		File f = new File(location);
		WorkspaceController.getController().getPreferences().setNewWorkspaceLocation(WorkspaceUtils.getURI(f));
		WorkspaceController.getController().getPreferences().setWorkspaceProfile(profileName);
	}
	
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
			e.printStackTrace();
			return;
		}

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

	public static PhysicalFolderNode createPhysicalFolderNode(final File path, final AWorkspaceTreeNode parent) {
		if (!path.isDirectory()) {
			LogUtils.warn("the given path is no folder.");
			return null;
		}

		PhysicalFolderNode node = new PhysicalFolderNode(AFolderNode.FOLDER_TYPE_PHYSICAL);
		String name = path.getName();

		node.setName(name == null ? "directory" : name);

		if (path != null) {
			node.setPath(MLinkController.toLinkTypeDependantURI(getWorkspaceBaseFile(), path,
					LinkController.LINK_RELATIVE_TO_WORKSPACE));
		}

		addAndSave(findAllowedTargetNode(parent), node);
		return node;
	}

	public static void createLinkTypeFileNode(final File path, final AWorkspaceTreeNode parent) {
		if (!path.isFile()) {
			LogUtils.warn("the given path is no file.");
			return;
		}

		LinkTypeFileNode node = new LinkTypeFileNode(ALinkNode.LINK_TYPE_FILE);
		String name = path.getName();

		node.setName(name == null ? "fileLink" : name);

		if (path != null) {
			LogUtils.info("FilesystemPath: " + path);
			node.setLinkPath(MLinkController.toLinkTypeDependantURI(getWorkspaceBaseFile(), path,
					LinkController.LINK_RELATIVE_TO_WORKSPACE));
		}

		addAndSave(findAllowedTargetNode(parent), node);
	}

	public static void createVirtualFolderNode(String folderName, final AWorkspaceTreeNode parent) {
		if (folderName == null || folderName.trim().length() <= 0) {
			return;
		}

		AWorkspaceTreeNode targetNode = (AWorkspaceTreeNode) (parent == null ? WorkspaceController.getController()
				.getWorkspaceModel().getRoot() : parent);

		VirtualFolderNode node = new VirtualFolderNode(AFolderNode.FOLDER_TYPE_VIRTUAL);
		node.setName(folderName);

		addAndSave(targetNode, node);
	}

	public static URI getWorkspaceBaseURI() {
		URI ret = null;
		ret = getWorkspaceBaseFile().toURI();
		return ret;
	}

	public static URI getProfileBaseURI() {
		URI base = getWorkspaceBaseFile().toURI();
		try {
			return new URI(base.getScheme(), base.getUserInfo(), base.getHost(), base.getPort(), base.getPath() + "/."
					+ WorkspaceController.getController().getPreferences().getWorkspaceProfile(), base.getQuery(),
					base.getFragment());
		}
		catch (URISyntaxException e) {
		}
		return null;
	}

	public static File getWorkspaceBaseFile() {
		String location = ResourceController.getResourceController().getProperty("workspace_location");
		if (location == null || location.length() == 0) {
			location = ResourceController.getResourceController().getProperty("workspace_location_new");
		}
		return new File(location);
	}

	public static File getProfileBaseFile() {
		return new File(getProfileBaseURI());
	}

	public static String stripIllegalChars(String string) {
		if (string == null) {
			return null;
		}
		//FIXME: DOCEAR - allow "space" in alpha 2
		return string.replaceAll("[^a-zA-Z0-9äöüÄÖÜ]+", "");
	}

	public static URI absoluteURI(final URI uri) {
		try {
			URLConnection urlConnection = uri.toURL().openConnection();
			if (urlConnection == null) {
				return null;
			}
			else {
				// URI test = urlConnection.getURL().toURI();
				return urlConnection.getURL().toURI().normalize();
			}
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return uri.normalize();

	}
	
	public static URI getWorkspaceRelativeURI(File file) {
		return LinkController.toRelativeURI(null, file, LinkController.LINK_RELATIVE_TO_WORKSPACE);
	}

	public static File resolveURI(final URI uri, final MapModel map) {
		try {
		return resolveURI(UrlManager.getController().getAbsoluteUri(map, uri));
		} 
		catch (MalformedURLException ex) {
			LogUtils.warn(ex);
		}
		return null;
	}
	
	public static File resolveURI(final URI uri) {
		if(uri.getFragment() != null) {
			return null;
		}
		URI absoluteUri = absoluteURI(uri);
		if (absoluteUri == null) {
			return null;
		}
		if(absoluteUri.getScheme().equalsIgnoreCase("file")){
			return new File(absoluteUri);
		}
		return null;
	}

	public static URI getURI(final File f) {
		return f.toURI();
	}

	/**
	 * @param targetNode
	 * @param node
	 */
	private static void addAndSave(AWorkspaceTreeNode targetNode, AWorkspaceTreeNode node) {
		WorkspaceUtils.getModel().addNodeTo(node, targetNode);
		WorkspaceUtils.getModel().reload(targetNode);
		saveCurrentConfiguration();
	}

	public static AWorkspaceTreeNode findAllowedTargetNode(final AWorkspaceTreeNode node) {
		AWorkspaceTreeNode targetNode = node;
		// DOCEAR: drops are not allowed on physical nodes, for the moment
		while (targetNode instanceof DefaultFileNode || targetNode instanceof PhysicalFolderNode
				|| targetNode instanceof ALinkNode) {
			targetNode = (AWorkspaceTreeNode) targetNode.getParent();
		}
		return targetNode;
	}

	/**
	 * @return
	 */
	public static WorkspaceIndexedTreeModel getModel() {
		return WorkspaceController.getController().getWorkspaceModel();
	}
	
	public static AWorkspaceTreeNode getNodeForPath(String path) {
		if(path == null || path.length() <= 0) {
			return null;
		}
		String key = "";
		for(String token : path.split("/")) {
			key += "/"+Integer.toHexString(token.hashCode()).toUpperCase(); 
		}		
		return getModel().getNode(key);
	}

}
