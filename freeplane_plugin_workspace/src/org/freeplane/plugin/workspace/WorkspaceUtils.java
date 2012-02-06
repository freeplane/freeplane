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

import javax.swing.JOptionPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;
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

	/**
	 * @param file
	 */
	public static void showFileNotFoundMessage(File file) {
		JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.format("workspace.node.link.notfound", 
				new Object[]{
					file.isDirectory()? TextUtils.getText("workspace.node.link.notfound.directory"):TextUtils.getText("workspace.node.link.notfound.file")
							,file.getName()
							,file.getParent()
				}));
	}
	
	public static void showWorkspaceChooserDialog() {
		WorkspaceChooserDialogPanel dialog = new WorkspaceChooserDialogPanel();
		
		JOptionPane.showMessageDialog(UITools.getFrame(), dialog, TextUtils.getRawText("no_location_set"), JOptionPane.PLAIN_MESSAGE);
	
		String location = dialog.getLocationPath();
		String profileName = dialog.getProfileName();
	
		if (location.length() == 0 || profileName.length() == 0) {
			location = "."+File.separator;
		}
	
		File f = new File(location);
		URI newProfileBase = WorkspaceUtils.getURI(new File(f, WorkspaceController.getController().getPreferences().getWorkspaceProfilesRoot()+profileName));
		if(WorkspaceController.isFirstApplicationStart() || !newProfileBase.equals(getProfileBaseURI())) {
			closeAllMindMaps();	
			WorkspaceController.getController().getPreferences().setNewWorkspaceLocation(WorkspaceUtils.getURI(f));
			WorkspaceController.getController().getPreferences().setWorkspaceProfile(profileName);
			WorkspaceController.getController().loadWorkspace();
		}
	}
	
	private static void closeAllMindMaps() {
		while(Controller.getCurrentController().getMap() != null) {
			Controller.getCurrentController().close(false);
		}	
	}
	
	public static void saveCurrentConfiguration() {
		String profile = WorkspaceController.getController().getPreferences().getWorkspaceProfileHome();

		URI uri;
		File temp, config;
		try {
			uri = new URI(WorkspaceController.WORKSPACE_RESOURCE_URL_PROTOCOL + ":/" + profile + "/tmp_"
					+ WorkspaceConfiguration.CONFIG_FILE_NAME);
			temp = WorkspaceUtils.resolveURI(uri);
			uri = new URI(WorkspaceController.WORKSPACE_RESOURCE_URL_PROTOCOL + ":/" + profile + "/"
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
		}
		catch (IOException e1) {
			LogUtils.severe(e1);
		}
		temp.delete();
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
	
	public static URI getDataDirectoryURI() {
		URI ret = null;
		ret = getDataDirectory().toURI();
		return ret;
	}

	public static URI getProfileBaseURI() {
		URI base = getWorkspaceBaseFile().toURI();
		try {
			return (new URI(base.getScheme(), base.getUserInfo(), base.getHost(), base.getPort(), base.getPath() + "/" 
					+ WorkspaceController.getController().getPreferences().getWorkspaceProfileHome()+"/", base.getQuery(),
					base.getFragment())).normalize();
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
	
	public static File getDataDirectory() {
		return new File(getWorkspaceBaseFile(), "_data");
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
		return absoluteURI(uri, null);

	}
	
	public static URI absoluteURI(final URI uri, MapModel map) {
		if(uri == null) {
			return null;
		}
		try {
			URLConnection urlConnection;
			// windows drive letters are interpreted as uri schemes -> make a file from the scheme-less uri string and use this to resolve the path
			if(Compat.isWindowsOS() && (uri.getScheme() != null && uri.getScheme().length() == 1)) { 
				urlConnection = (new File(uri.toString())).toURL().openConnection();
			} 
			else if(uri.getScheme() == null && !uri.getPath().startsWith(File.separator)) {
				if(map != null) {
					urlConnection = (new File(uri.toString())).toURL().openConnection();
				} 
				else {
					urlConnection = UrlManager.getController().getAbsoluteUri(map, uri).toURL().openConnection();
				}
			}
			else {
				urlConnection = uri.toURL().openConnection();				
			}
			
			if (urlConnection == null) {
				return null;
			}
			else {
				return urlConnection.getURL().toURI().normalize();
			}
		}
		catch (URISyntaxException e) {
			LogUtils.warn(e);
		}
		catch (IOException e) {
			LogUtils.warn(e);
		}
		catch (Exception e){
			LogUtils.warn(e);
		}
		return uri.normalize();

	}
	
	public static URI getWorkspaceRelativeURI(File file) {
		return LinkController.toRelativeURI(null, file, LinkController.LINK_RELATIVE_TO_WORKSPACE);
	}

	public static File resolveURI(final URI uri, final MapModel map) {
		if(uri == null || map == null) {
			return null;
		}
		try {
			return resolveURI(UrlManager.getController().getAbsoluteUri(map, uri));
		} 
		catch (Exception ex) {
			LogUtils.warn(ex);
		}
		return null;
	}
	
	public static File resolveURI(final URI uri) {
		if(uri == null) {
			return null;
		}
		try {
			if(uri.getFragment() != null) {
				return null;
			}
			URI absoluteUri = absoluteURI(uri);
			if (absoluteUri == null) {
				return null;
			}
			if("file".equalsIgnoreCase(absoluteUri.getScheme())){
				return new File(absoluteUri);
			}
		}
		catch(Exception ex) {
			LogUtils.warn(ex);
		}		
		return null;
	}

	public static URI getURI(final File f) {
		return f.toURI().normalize();
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
