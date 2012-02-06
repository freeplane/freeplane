package org.freeplane.plugin.workspace;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.plugin.workspace.config.IConfigurationInfo;
import org.freeplane.plugin.workspace.config.LinkTypeFileIconHandler;
import org.freeplane.plugin.workspace.config.creator.FolderTypePhysicalCreator;
import org.freeplane.plugin.workspace.config.creator.FolderTypeVirtualCreator;
import org.freeplane.plugin.workspace.config.creator.LinkTypeFileCreator;
import org.freeplane.plugin.workspace.config.creator.WorkspaceRootCreator;
import org.freeplane.plugin.workspace.config.node.LinkTypeFileNode;
import org.freeplane.plugin.workspace.controller.WorkspaceEvent;
import org.freeplane.plugin.workspace.io.DefaultFileNodeIconHandler;
import org.freeplane.plugin.workspace.io.node.DefaultFileNode;
import org.freeplane.plugin.workspace.io.node.ImageFileNode;
import org.freeplane.plugin.workspace.io.node.MindMapFileNode;
import org.freeplane.plugin.workspace.io.xml.ConfigurationWriter;
import org.freeplane.plugin.workspace.io.xml.WorkspaceNodeWriter;
import org.freeplane.plugin.workspace.model.creator.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.creator.FolderCreator;
import org.freeplane.plugin.workspace.model.creator.LinkCreator;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

public class WorkspaceConfiguration {
	final private ReadManager readManager;
	final private WriteManager writeManager;

	public final static int WSNODE_FOLDER = 1;
	public final static int WSNODE_LINK = 2;

	private final static String DEFAULT_CONFIG_FILE_NAME = "workspace_default.xml";
	private URL DEFAULT_CONFIG_TEMPLATE_URL = WorkspaceConfiguration.class.getResource("/conf/"+DEFAULT_CONFIG_FILE_NAME);
	//private final static String DEFAULT_CONFIG_FILE_NAME_DOCEAR = "workspace_default_docear.xml";
	public final static String CONFIG_FILE_NAME = "workspace.xml";

	private final static String PLACEHOLDER_PROFILENAME = "@@PROFILENAME@@";

	private FolderCreator folderCreator = null;
	private LinkCreator linkCreator = null;
	private WorkspaceRootCreator workspaceRootCreator = null;
	private IConfigurationInfo configurationInfo;

	private ConfigurationWriter configWriter;

	public WorkspaceConfiguration() {
		this.readManager = new ReadManager();
		this.writeManager = new WriteManager();
		this.configWriter = new ConfigurationWriter(writeManager);
		
		WorkspaceController.getController().getNodeTypeIconManager().addNodeTypeIconHandler(LinkTypeFileNode.class, new LinkTypeFileIconHandler());
		WorkspaceController.getController().getNodeTypeIconManager().addNodeTypeIconHandler(DefaultFileNode.class, new DefaultFileNodeIconHandler());
		WorkspaceController.getController().getNodeTypeIconManager().addNodeTypeIconHandler(MindMapFileNode.class, new DefaultFileNodeIconHandler());
		WorkspaceController.getController().getNodeTypeIconManager().addNodeTypeIconHandler(ImageFileNode.class, new DefaultFileNodeIconHandler());
		initReadManager();
		initWriteManager();
	}

	public IConfigurationInfo getConfigurationInfo() {
		return this.configurationInfo;
	}
	
	public void setDefaultConfigTemplateUrl(URL templateUrl) {
		if(templateUrl == null) {
			return ;
		}
		this.DEFAULT_CONFIG_TEMPLATE_URL = templateUrl;
	}
	
	private boolean initializeConfig() throws NullPointerException, FileNotFoundException, IOException, URISyntaxException {
		String workspaceLocation = WorkspaceController.getController().getPreferences().getWorkspaceLocation();
		String profile = WorkspaceController.getController().getPreferences().getWorkspaceProfileHome();

		if (workspaceLocation == null) {
			return false;
		}
		WorkspaceController.getController().fireOpenWorkspace(new WorkspaceEvent(WorkspaceEvent.WORKSPACE_RELOAD, this));
		File configFile = new File(workspaceLocation + File.separator + profile + File.separator + CONFIG_FILE_NAME);
		boolean newConfig = false;
		if (!configFile.exists()) {
			// CREATE NEW WORKSPACE
			File profileFolder = new File(workspaceLocation + File.separator + profile);
			if (!profileFolder.exists() || !profileFolder.isDirectory()) {
				if (!profileFolder.mkdirs()) {
					JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getContentPane(),
							TextUtils.getText("error_create_workspace_folder") + " " + workspaceLocation,
							TextUtils.getText("error_create_workspace_folder_title"), JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
			copyDefaultConfigTo(configFile);
			newConfig = true;
		}

		WorkspaceController.getController().getWorkspaceModel()
				.removeAllElements((AWorkspaceTreeNode) WorkspaceController.getController().getWorkspaceModel().getRoot());
		this.load(configFile.toURI().toURL());
		if(newConfig) {
			WorkspaceController.getController().fireWorkspaceReady(new WorkspaceEvent(WorkspaceEvent.WORKSPACE_CHANGED, this));
		}
		return true;
	}

	private void copyDefaultConfigTo(File config) throws FileNotFoundException, IOException {
		String xml = getSubstitutedWorkspaceXml(DEFAULT_CONFIG_TEMPLATE_URL.openStream());
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(config)));
		out.write(xml.getBytes());
		out.close();
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
		if (this.workspaceRootCreator == null) {
			// LogUtils.info("WORKSPACE: get new WorkspaceRootCreator");
			this.workspaceRootCreator = new WorkspaceRootCreator(this);
		}
		return this.workspaceRootCreator;
	}

	private FolderCreator getFolderCreator() {
		if (this.folderCreator == null) {
			// LogUtils.info("WORKSPACE: get new FolderCreator");
			this.folderCreator = new FolderCreator();
		}
		return this.folderCreator;
	}

	private LinkCreator getLinkCreator() {
		if (this.linkCreator == null) {
			// LogUtils.info("WORKSPACE: get new LinkCreator");
			this.linkCreator = new LinkCreator();
		}
		return this.linkCreator;
	}

	public void registerTypeCreator(final int nodeType, final String typeName, final AWorkspaceNodeCreator creator) {
		if (typeName == null || typeName.trim().length() <= 0)
			return;
		switch (nodeType) {
		case WSNODE_FOLDER: {
			getFolderCreator().addTypeCreator(typeName, creator);
			break;
		}
		case WSNODE_LINK: {
			getLinkCreator().addTypeCreator(typeName, creator);
			break;
		}
		default: {
			throw new IllegalArgumentException(
					"not allowed argument for nodeType. Use only WorkspaceConfiguration.WSNODE_FOLDER or WorkspaceConfiguration.WSNODE_LINK.");
		}
		}

	}

	public boolean load() {
		try {
			return initializeConfig();
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void load(final URL xmlFile) {
		LogUtils.info("WORKSPACE: load Config from XML: " + xmlFile);
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

	private String getSubstitutedWorkspaceXml(InputStream fileStream) {
		String ret = "";
		try {
			ret = this.getFileContent(fileStream);
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		Pattern pattern = Pattern.compile(PLACEHOLDER_PROFILENAME);
		Matcher mainMatcher = pattern.matcher(ret);
		ret = mainMatcher.replaceAll(WorkspaceController.getController().getPreferences().getWorkspaceProfileHome());

		return ret;
	}

	private String getFileContent(InputStream fileStream) throws IOException {
		//InputStream in = getClass().getResourceAsStream(filename);
		Writer writer = new StringWriter();
		char[] buffer = new char[1024];

		try {
			Reader reader = new BufferedReader(new InputStreamReader(fileStream, "UTF-8"));
			int n;

			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}

		}
		finally {
			fileStream.close();
		}

		return writer.toString();
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
