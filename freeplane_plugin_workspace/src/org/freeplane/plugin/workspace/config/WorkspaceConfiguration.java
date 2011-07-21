package org.freeplane.plugin.workspace.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;

import javax.swing.tree.MutableTreeNode;

import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.features.mode.Controller;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.plugin.workspace.config.creator.FilesystemFolderCreator;
import org.freeplane.plugin.workspace.config.creator.FilesystemLinkCreator;
import org.freeplane.plugin.workspace.config.creator.GroupCreator;
import org.freeplane.plugin.workspace.config.creator.WorkspaceCreator;
import org.freeplane.plugin.workspace.WorkspacePreferences;

public class WorkspaceConfiguration {
	final private ReadManager readManager;
	private IndexedTree tree;
	private final URL DEFAULT_CONFIG = this.getClass().getResource("workspace_default.xml");
	private final static String CONFIG_FILE_NAME = "workspace.xml";

	private boolean configValid = false;

	public WorkspaceConfiguration() {
		readManager = new ReadManager();
		tree = new IndexedTree(null);
		initReadManager();
		try {
			initializeConfig();
		}
		catch (Exception e) {
			e.printStackTrace();
			this.setConfigValid(false);
		}
	}

	private void initializeConfig() throws FileNotFoundException, IOException {
		ResourceController resCtrl = Controller.getCurrentController().getResourceController();
		String workspaceLocation = resCtrl.getProperty(WorkspacePreferences.WORKSPACE_LOCATION);
		if (workspaceLocation == null || workspaceLocation.isEmpty()) {
			setConfigValid(false);
			resCtrl.setProperty(WorkspacePreferences.SHOW_WORKSPACE_RESOURCE, false);
			return;
		}
		File config = new File(workspaceLocation + File.separator + CONFIG_FILE_NAME);
		
		if (!config.exists()) {
			copyDefaultConfigTo(config);
		}		
		this.load(new URL("file://"+config.getAbsolutePath()));
		
		setConfigValid(true);
	}

	private void copyDefaultConfigTo(File config) throws FileNotFoundException, IOException {
		FileChannel from = ((FileInputStream) DEFAULT_CONFIG.openStream()).getChannel();
		FileChannel to = new FileOutputStream(config).getChannel();

		to.transferFrom(from, 0, from.size());
	}

	public boolean isConfigValid() {
		return configValid;
	}

	public void setConfigValid(boolean configValid) {
		this.configValid = configValid;
	}

	private void initReadManager() {
		readManager.addElementHandler("workspace_structure", new WorkspaceCreator(tree));
		readManager.addElementHandler("group", new GroupCreator(tree));
		readManager.addElementHandler("filesystem_folder", new FilesystemFolderCreator(tree));
		readManager.addElementHandler("filesystem_link", new FilesystemLinkCreator(tree));
	}

	public void load(final URL xmlFile) {
		final TreeXmlReader reader = new TreeXmlReader(readManager);
		try {
			reader.load(new InputStreamReader(new BufferedInputStream(xmlFile.openStream())));
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
		catch (final XMLException e) {
			throw new RuntimeException(e);
		}
	}

	public MutableTreeNode getConfigurationRoot() {
		return tree.getRoot();
	}

}
