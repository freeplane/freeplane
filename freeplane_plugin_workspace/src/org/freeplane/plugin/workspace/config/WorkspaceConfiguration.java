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
import java.io.Writer;
import java.net.URL;

import javax.swing.JOptionPane;

import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspacePreferences;
import org.freeplane.plugin.workspace.config.creator.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.config.creator.FolderCreator;
import org.freeplane.plugin.workspace.config.creator.FolderTypePhysicalCreator;
import org.freeplane.plugin.workspace.config.creator.FolderTypeVirtualCreator;
import org.freeplane.plugin.workspace.config.creator.LinkCreator;
import org.freeplane.plugin.workspace.config.creator.LinkTypeFileCreator;
import org.freeplane.plugin.workspace.config.creator.WorkspaceRootCreator;
import org.freeplane.plugin.workspace.io.xml.ConfigurationWriter;
import org.freeplane.plugin.workspace.io.xml.WorkspaceNodeWriter;

public class WorkspaceConfiguration {
	final private ReadManager readManager;
	final private WriteManager writeManager;
	
	public final static int WSNODE_FOLDER = 1;
	public final static int WSNODE_LINK = 2;

	private final static String DEFAULT_CONFIG_FILE_NAME = "workspace_default.xml";
	private final static String DEFAULT_CONFIG_FILE_NAME_DOCEAR = "workspace_default_docear.xml";
	private final static String CONFIG_FILE_NAME = "workspace.xml";
	
	private FolderCreator folderCreator = null;
	private LinkCreator linkCreator = null;
	private WorkspaceRootCreator workspaceRootCreator = null;
	private boolean configValid = false;
	private IConfigurationInfo configurationInfo;
	
	private ConfigurationWriter configWriter;

	public WorkspaceConfiguration() {
		this.readManager = new ReadManager();
		this.writeManager = new WriteManager();
		this.configWriter = new ConfigurationWriter(writeManager);
		initReadManager();
		initWriteManager();
	}
	
	public IConfigurationInfo getConfigurationInfo() {
		return this.configurationInfo;
	}

	private void initializeConfig() throws FileNotFoundException, IOException {
		ResourceController resCtrl = Controller.getCurrentController().getResourceController();
		String workspaceLocation = resCtrl.getProperty(WorkspacePreferences.WORKSPACE_LOCATION);
		String workspaceLocationNew = resCtrl.getProperty(WorkspacePreferences.WORKSPACE_LOCATION_NEW);

		if (workspaceLocationNew != null && workspaceLocationNew.trim().length() > 0) {
			File configFile = new File(workspaceLocationNew + File.separator + CONFIG_FILE_NAME);
			if (!configFile.exists()) {
				workspaceLocation = initializeNewConfig(workspaceLocationNew);
			}
			else {
				if(workspaceLocation == null) {
					resCtrl.setProperty(WorkspacePreferences.WORKSPACE_LOCATION, workspaceLocationNew);
				}
				workspaceLocation = workspaceLocationNew;				
			}
		}

		File configFile = new File(workspaceLocation + File.separator + CONFIG_FILE_NAME);

		if (!configFile.exists()) {
			setConfigValid(false);
			return;
		}
		WorkspaceController.getController().getIndexTree().removeChildElements(WorkspaceController.getController().getIndexTree());
		//WorkspaceController.getCurrentWorkspaceController().getTree().getRoot().removeAllChildren();
		this.load(configFile.toURI().toURL());
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
				File folder = new File(workspaceLocationNew);
				if (!folder.exists() || !folder.isDirectory()) {
					if (!folder.mkdirs()) {
						JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getContentPane(),
								TextUtils.getText("error_create_workspace_folder")+" "+workspaceLocationNew,
								TextUtils.getText("error_create_workspace_folder_title"), JOptionPane.ERROR_MESSAGE);
						return null;
					}
				}
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
		String appName = Controller.getCurrentController().getResourceController().getProperty("ApplicationName", "Freeplane");
		InputStream in;
		if(appName.equalsIgnoreCase("docear")) {
			in = getClass().getResourceAsStream(DEFAULT_CONFIG_FILE_NAME_DOCEAR);
		}
		else {
			in = getClass().getResourceAsStream(DEFAULT_CONFIG_FILE_NAME);
		}
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
		readManager.addElementHandler("workspace", getWorkspaceRootCreator());
		readManager.addElementHandler("folder", getFolderCreator());
		readManager.addElementHandler("link", getLinkCreator());
		
		registerTypeCreator(WorkspaceConfiguration.WSNODE_FOLDER, "virtual", new FolderTypeVirtualCreator());
		registerTypeCreator(WorkspaceConfiguration.WSNODE_FOLDER, "physical", new FolderTypePhysicalCreator());
		registerTypeCreator(WorkspaceConfiguration.WSNODE_LINK, "file", new LinkTypeFileCreator());
	}

	private void initWriteManager() {
		WorkspaceNodeWriter writer = new WorkspaceNodeWriter();
		writeManager.addElementWriter("workspace", writer);
		writeManager.addAttributeWriter("workspace", writer);
		
		writeManager.addElementWriter("folder", writer);
		writeManager.addAttributeWriter("folder", writer);
		
		writeManager.addElementWriter("link", writer);
		writeManager.addAttributeWriter("link", writer);
	}
	
	private WorkspaceRootCreator getWorkspaceRootCreator() {
		if(this.workspaceRootCreator == null) {
			//LogUtils.info("WORKSPACE: get new WorkspaceRootCreator");
			this.workspaceRootCreator = new WorkspaceRootCreator(this);
		}
		return this.workspaceRootCreator;
	}
	
	private FolderCreator getFolderCreator() {
		if(this.folderCreator == null) {
			//LogUtils.info("WORKSPACE: get new FolderCreator");
			this.folderCreator = new FolderCreator();
		}
		return this.folderCreator;
	}
	
	private LinkCreator getLinkCreator() {
		if(this.linkCreator == null) {
			//LogUtils.info("WORKSPACE: get new LinkCreator");
			this.linkCreator = new LinkCreator();
		}
		return this.linkCreator;
	}

	public void registerTypeCreator(final int nodeType, final String typeName, final AWorkspaceNodeCreator creator) {
		if(typeName == null || typeName.trim().length() <= 0) return;
		switch(nodeType) {
			case WSNODE_FOLDER: {
				getFolderCreator().addTypeCreator(typeName, creator);
				break;
			}
			case WSNODE_LINK: {
				getLinkCreator().addTypeCreator(typeName, creator);
				break;
			}
			default: {
				throw new IllegalArgumentException("not allowed argument for nodeType. Use only WorkspaceConfiguration.WSNODE_FOLDER or WorkspaceConfiguration.WSNODE_LINK.");
			}
		}
		
	}
	
	public void reload() {
		try {
			initializeConfig();
		}
		catch (Exception e) {
			e.printStackTrace();
			this.setConfigValid(false);
		}		
	}
	
	private void load(final URL xmlFile) {
		LogUtils.info("WORKSPACE: load Config from XML: "+xmlFile);
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

	/**
	 * @param node
	 */
	public void setConfigurationInfo(IConfigurationInfo info) {
		this.configurationInfo = info;		
	}
	
	public void saveConfiguration(Writer writer) {
		try {
			this.configWriter.writeConfigurationAsXml(writer);
		}
		catch (final IOException e) {
			LogUtils.severe(e);
		}
	}
	
}
