package org.freeplane.plugin.workspace.model.project;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;

import org.apache.commons.io.IOExceptionWithCause;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.creator.ActionCreator;
import org.freeplane.plugin.workspace.creator.FolderCreator;
import org.freeplane.plugin.workspace.creator.FolderTypePhysicalCreator;
import org.freeplane.plugin.workspace.creator.FolderTypeVirtualCreator;
import org.freeplane.plugin.workspace.creator.LinkCreator;
import org.freeplane.plugin.workspace.creator.LinkTypeFileCreator;
import org.freeplane.plugin.workspace.creator.ProjectRootCreator;
import org.freeplane.plugin.workspace.io.IProjectSettingsIOHandler;
import org.freeplane.plugin.workspace.io.xml.ProjectNodeWriter;
import org.freeplane.plugin.workspace.io.xml.ProjectSettingsWriter;
import org.freeplane.plugin.workspace.model.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.IResultProcessor;
import org.freeplane.plugin.workspace.nodes.ProjectRootNode;

public class ProjectLoader implements IProjectSettingsIOHandler {
	private final ReadManager readManager;
	private final WriteManager writeManager;

	public final static int WSNODE_FOLDER = 1;
	public final static int WSNODE_LINK = 2;
	public final static int WSNODE_ACTION = 4;

	private FolderCreator folderCreator = null;
	private LinkCreator linkCreator = null;
	private ActionCreator actionCreator = null;
	private ProjectRootCreator projectRootCreator = null;
	
	private ProjectSettingsWriter projectWriter;
	private IResultProcessor resultProcessor;
		
	public ProjectLoader() {
		this.readManager = new ReadManager();
		this.writeManager = new WriteManager();
		this.projectWriter = new ProjectSettingsWriter(writeManager);
		
		initReadManager();
		initWriteManager();
	}
	
	private void initReadManager() {
		readManager.addElementHandler("workspace", getProjectRootCreator());
		readManager.addElementHandler("project", getProjectRootCreator());
		readManager.addElementHandler("folder", getFolderCreator());
		readManager.addElementHandler("link", getLinkCreator());
		readManager.addElementHandler("action", getActionCreator());

		registerTypeCreator(ProjectLoader.WSNODE_FOLDER, "virtual", new FolderTypeVirtualCreator());
		registerTypeCreator(ProjectLoader.WSNODE_FOLDER, "physical", new FolderTypePhysicalCreator());
		registerTypeCreator(ProjectLoader.WSNODE_LINK, "file", new LinkTypeFileCreator());
	}

	private void initWriteManager() {
		ProjectNodeWriter writer = new ProjectNodeWriter();
		writeManager.addElementWriter("project", writer);
		writeManager.addAttributeWriter("project", writer);

		writeManager.addElementWriter("folder", writer);
		writeManager.addAttributeWriter("folder", writer);

		writeManager.addElementWriter("link", writer);
		writeManager.addAttributeWriter("link", writer);
		
		writeManager.addElementWriter("action", writer);
		writeManager.addAttributeWriter("action", writer);
	}

	protected ProjectRootCreator getProjectRootCreator() {
		if (this.projectRootCreator == null) {
			this.projectRootCreator = new ProjectRootCreator();
			this.projectRootCreator.setResultProcessor(getDefaultResultProcessor());
		}
		return this.projectRootCreator;
	}

	private FolderCreator getFolderCreator() {
		if (this.folderCreator == null) {
			this.folderCreator = new FolderCreator();
			this.folderCreator.setResultProcessor(getDefaultResultProcessor());
		}
		return this.folderCreator;
	}

	private ActionCreator getActionCreator() {
		if (this.actionCreator == null) {
			this.actionCreator = new ActionCreator();
			this.actionCreator.setResultProcessor(getDefaultResultProcessor());
		}
		return this.actionCreator;
	}
	
	private LinkCreator getLinkCreator() {
		if (this.linkCreator == null) {
			this.linkCreator = new LinkCreator();
			this.linkCreator.setResultProcessor(getDefaultResultProcessor());
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
			case WSNODE_ACTION: {
				getActionCreator().addTypeCreator(typeName, creator);
				break;
			}
			default: {
				throw new IllegalArgumentException("not allowed argument for nodeType. Use only WorkspaceConfiguration.WSNODE_ACTION, WorkspaceConfiguration.WSNODE_FOLDER or WorkspaceConfiguration.WSNODE_LINK.");
			}
		}
		if(creator.getResultProcessor() == null) {
			creator.setResultProcessor(getDefaultResultProcessor());
		}

	}

	protected void load(final URI xmlFile) throws MalformedURLException, XMLException, IOException {
		final TreeXmlReader reader = new TreeXmlReader(readManager);
		reader.load(new InputStreamReader(new BufferedInputStream(xmlFile.toURL().openStream())));
	}
	
	public synchronized LOAD_RETURN_TYPE loadProject(AWorkspaceProject project) throws IOException {
		try {
			File projectSettings = new File(URIUtils.getAbsoluteFile(project.getProjectDataPath()),"settings.xml");
			if(projectSettings.exists()) {
				getDefaultResultProcessor().setProject(project);
				this.load(projectSettings.toURI());
				return LOAD_RETURN_TYPE.EXISTING_PROJECT;
			}
			else {
				createDefaultProject(project);
				return LOAD_RETURN_TYPE.NEW_PROJECT;
			}
		}
		catch (Exception e) {
			throw new IOExceptionWithCause(e);
		}
	}

	private void createDefaultProject(AWorkspaceProject project) {
		ProjectRootNode root = new ProjectRootNode();
		root.setProjectID(project.getProjectID());				
		root.setModel(project.getModel());
		root.setName(URIUtils.getAbsoluteFile(project.getProjectHome()).getName());
		project.getModel().setRoot(root);
		// create and load all default nodes
		root.initiateMyFile(project);
		root.refresh();
	}
	
	public IResultProcessor getDefaultResultProcessor() {
		if(this.resultProcessor == null) {
			this.resultProcessor = new DefaultResultProcessor();
		}
		return this.resultProcessor;
	}

	private void storeProject(Writer writer, AWorkspaceProject project) throws IOException {
		this.projectWriter.storeProject(writer, project);		
	}

	public void storeProject(AWorkspaceProject project) throws IOException {
		File outFile = URIUtils.getAbsoluteFile(project.getProjectDataPath());
		outFile = new File(outFile, "settings.xml");
		if(!outFile.exists()) {
			outFile.getParentFile().mkdirs();
			outFile.createNewFile();
		}
		Writer writer = new FileWriter(outFile);
		storeProject(writer, project);		
	}
	
	protected ReadManager getReadManager() {
		return readManager;
	}
	
	private class DefaultResultProcessor implements IResultProcessor {

		private AWorkspaceProject project;

		public AWorkspaceProject getProject() {
			return project;
		}

		public void setProject(AWorkspaceProject project) {
			this.project = project;
		}

		public void process(AWorkspaceTreeNode parent, AWorkspaceTreeNode node) {
			if(getProject() == null) {
				LogUtils.warn("Missing project container! cannot add node to a model.");
				return;
			}
			if(node instanceof ProjectRootNode) {
				getProject().getModel().setRoot(node);
				if(((ProjectRootNode) node).getProjectID() == null) {
					((ProjectRootNode) node).setProjectID(getProject().getProjectID());
				}
				((ProjectRootNode) node).initiateMyFile(getProject());
			}
			else {
				if(parent == null) {
					if (!getProject().getModel().containsNode(node.getKey())) {
						getProject().getModel().addNodeTo(node, (AWorkspaceTreeNode) parent);			
					}
				}
				else {
					if (!parent.getModel().containsNode(node.getKey())) {
						parent.getModel().addNodeTo(node, (AWorkspaceTreeNode) parent);			
					}
				}
			}
		}

	}
}
