package org.freeplane.plugin.workspace.config;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.JOptionPane;

import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.plugin.workspace.WorkspacePreferences;
import org.freeplane.plugin.workspace.config.creator.FilesystemFolderCreator;
import org.freeplane.plugin.workspace.config.creator.FilesystemLinkCreator;
import org.freeplane.plugin.workspace.config.creator.GroupCreator;
import org.freeplane.plugin.workspace.config.creator.WorkspaceCreator;
import org.freeplane.plugin.workspace.io.xml.WorkspaceNodeWriter;

public class WorkspaceConfiguration {
	final private ReadManager readManager;
	final private WriteManager writeManager;

	private final URL DEFAULT_CONFIG = this.getClass().getResource("workspace_default.xml");
	private final static String DEFAULT_CONFIG_FILE_NAME = "workspace_default.xml";
	private final static String CONFIG_FILE_NAME = "workspace.xml";

	private boolean configValid = false;

	public WorkspaceConfiguration() {
		readManager = new ReadManager();
		writeManager = new WriteManager();
		initReadManager();
		initWriteManager();
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
		String workspaceLocationNew = resCtrl.getProperty(WorkspacePreferences.WORKSPACE_LOCATION_NEW);

		if (workspaceLocation == null || workspaceLocation.trim().length() == 0) {
			if (workspaceLocationNew == null || workspaceLocationNew.trim().length() == 0) {
				setConfigValid(false);
				resCtrl.setProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY, false);
				return;
			}
		}
		if (workspaceLocationNew != null && workspaceLocationNew.trim().length() > 0
				&& !workspaceLocationNew.equals(workspaceLocation)) {
			workspaceLocation = initializeNewConfig(workspaceLocationNew);
		}

		File configFile = new File(workspaceLocation + File.separator + CONFIG_FILE_NAME);

		if (!configFile.exists()) {
			setConfigValid(false);
			return;
		}

		this.load(new URL("file:///" + configFile.getPath()));
		setConfigValid(true);
	}

	private String initializeNewConfig(String workspaceLocationNew) throws FileNotFoundException, IOException {
		ResourceController resourceController = Controller.getCurrentController().getResourceController();
		File configFile = new File(workspaceLocationNew + File.separator + CONFIG_FILE_NAME);

		if (!configFile.exists()) {
			int yesorno = JOptionPane.OK_OPTION;
			yesorno = JOptionPane.showConfirmDialog(Controller.getCurrentController().getViewController().getContentPane(),
					TextUtils.getText("confirm_create_workspace_text") + workspaceLocationNew,
					TextUtils.getText("confirm_create_workspace_title"), JOptionPane.OK_CANCEL_OPTION);
			if (yesorno == JOptionPane.OK_OPTION) {
				// CREATE NEW WORKSPACE
				copyDefaultConfigTo(configFile);
				resourceController.setProperty(WorkspacePreferences.WORKSPACE_LOCATION, workspaceLocationNew);
				return workspaceLocationNew;
			}
			else {
				// DO NOT CREATE NEW WORKSPACE - USE OLD WORKSPACE INSTEAD
				resourceController.setProperty(WorkspacePreferences.WORKSPACE_LOCATION_NEW,
						resourceController.getProperty(WorkspacePreferences.WORKSPACE_LOCATION));
				return resourceController.getProperty(WorkspacePreferences.WORKSPACE_LOCATION);
			}
		}
		else {
			// NEW WORKSPACE EXISTS, USE IT
			resourceController.setProperty(WorkspacePreferences.WORKSPACE_LOCATION, workspaceLocationNew);
			return workspaceLocationNew;
		}

	}

	private void copyDefaultConfigTo(File config) throws FileNotFoundException, IOException {
		InputStream in = getClass().getResourceAsStream(DEFAULT_CONFIG_FILE_NAME);
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(config)));
		byte[] buffer = new byte[1024];
		int len = in.read(buffer);
		while (len != -1) {
		    out.write(buffer, 0, len);
		    len = in.read(buffer);
		}
		in.close();
		out.close();
	}

	public boolean isConfigValid() {
		return configValid;
	}

	public void setConfigValid(boolean configValid) {
		this.configValid = configValid;
	}

	private void initReadManager() {
		readManager.addElementHandler("workspace_structure", new WorkspaceCreator());
		readManager.addElementHandler("group", new GroupCreator());
		readManager.addElementHandler("filesystem_folder", new FilesystemFolderCreator());
		readManager.addElementHandler("filesystem_link", new FilesystemLinkCreator());
	}

	private void initWriteManager() {
		WorkspaceNodeWriter writer = new WorkspaceNodeWriter();
		writeManager.addElementWriter("workspace_structure", writer);
		writeManager.addAttributeWriter("workspace_structure", writer);

		writeManager.addElementWriter("group", writer);
		writeManager.addAttributeWriter("group", writer);

		writeManager.addElementWriter("filesystem_folder", writer);
		writeManager.addAttributeWriter("filesystem_folder", writer);

		writeManager.addElementWriter("filesystem_link", writer);
		writeManager.addAttributeWriter("filesystem_link", writer);
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

	public WriteManager getWriteManager() {
		return this.writeManager;
	}

}
