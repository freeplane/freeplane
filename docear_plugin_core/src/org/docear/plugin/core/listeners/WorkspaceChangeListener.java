package org.docear.plugin.core.listeners;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.IDocearLibrary;
import org.docear.plugin.core.workspace.creator.FolderTypeLibraryCreator;
import org.docear.plugin.core.workspace.creator.FolderTypeLiteratureRepositoryCreator;
import org.docear.plugin.core.workspace.creator.FolderTypeProjectsCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeIncomingCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeLiteratureAnnotationsCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeMyPublicationsCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeReferencesCreator;
import org.docear.plugin.core.workspace.node.FolderTypeLibraryNode;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceConfiguration;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.event.IWorkspaceEventListener;
import org.freeplane.plugin.workspace.event.WorkspaceEvent;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;
import org.freeplane.plugin.workspace.nodes.WorkspaceRoot;

public class WorkspaceChangeListener implements IWorkspaceEventListener {

	private boolean workspacePrepared = false;
	

	public void openWorkspace(WorkspaceEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void closeWorkspace(WorkspaceEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void workspaceChanged(WorkspaceEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void toolBarChanged(WorkspaceEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void workspaceReady(WorkspaceEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void configurationLoaded(WorkspaceEvent event) {
		linkWelcomeMindmapAfterWorkspaceCreation();
		IDocearLibrary lib = DocearController.getController().getLibrary();
		if(lib != null && lib instanceof FolderTypeLibraryNode) {
			WorkspaceController.getController().getExpansionStateHandler().addPathKey(((AWorkspaceTreeNode)lib).getKey());
			WorkspaceController.getController().refreshWorkspace();
		}
			
	}

	public void configurationBeforeLoading(WorkspaceEvent event) {
		removeLibraryPaths();
		prepareWorkspace();
	}
	
	private void removeLibraryPaths() {
		CoreConfiguration.projectPathObserver.reset();
		CoreConfiguration.referencePathObserver.reset();
		CoreConfiguration.repositoryPathObserver.reset();
	}
	
	private void prepareWorkspace() {
		if(!workspacePrepared) {
			WorkspaceController controller = WorkspaceController.getController();
			controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_FOLDER, FolderTypeLibraryCreator.FOLDER_TYPE_LIBRARY, new FolderTypeLibraryCreator());
			controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_FOLDER, FolderTypeLiteratureRepositoryCreator.FOLDER_TYPE_LITERATUREREPOSITORY, new FolderTypeLiteratureRepositoryCreator());
			controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_FOLDER, FolderTypeProjectsCreator.FOLDER_TYPE_PROJECTS, new FolderTypeProjectsCreator());
			controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_LINK, LinkTypeMyPublicationsCreator.LINK_TYPE_MYPUBLICATIONS , new LinkTypeMyPublicationsCreator());
			controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_LINK, LinkTypeReferencesCreator.LINK_TYPE_REFERENCES , new LinkTypeReferencesCreator());
			controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_LINK, LinkTypeLiteratureAnnotationsCreator.LINK_TYPE_LITERATUREANNOTATIONS , new LinkTypeLiteratureAnnotationsCreator());
			controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_LINK, LinkTypeIncomingCreator.LINK_TYPE_INCOMING , new LinkTypeIncomingCreator());
			
			controller.getConfiguration().setDefaultConfigTemplateUrl(getClass().getResource("/conf/workspace_default_docear.xml"));
			
			modifyContextMenus();
		}
		workspacePrepared  = true;
		//controller.loadWorkspace();		
		copyInfoIfNeeded();		
	}
	
	private void copyInfoIfNeeded() {
		File infoFile = new File(WorkspaceUtils.getProfileBaseFile(), "!!!info.txt");
		if(!infoFile.exists()) {
			createAndCopy(infoFile, "/conf/!!!info.txt");
		}
		
		File _dataInfoFile = new File(WorkspaceUtils.getDataDirectory(), "!!!info.txt");
		if(!_dataInfoFile.exists()) {
			createAndCopy(_dataInfoFile, "/conf/!!!info.txt");
		}
		
		File _welcomeFile = new File(WorkspaceUtils.getDataDirectory(), "/help/docear-welcome.mm");
		if(!_welcomeFile.exists()) {
			createAndCopy(_welcomeFile, "/conf/docear-welcome.mm");			
		}
		
		
		File libPath = WorkspaceUtils.resolveURI(DocearController.getController().getLibraryPath());
		
		File _tempFile = new File(libPath, "temp.mm");			
		if(!_tempFile.exists()) {
			createAndCopy(_tempFile, "/conf/simple_mindmap.template");
			createAndRenameMap(_tempFile, "temp");
		}
		
		File _trashFile = new File(libPath, "trash.mm");
		if(!_trashFile.exists()) {
			createAndCopy(_trashFile, "/conf/simple_mindmap.template");
			createAndRenameMap(_trashFile, "trash");
			
		}		
	}

	/**
	 * @param string
	 */
	private void createAndRenameMap(File file ,String name) {
		final MMapIO mapIO = (MMapIO) Controller.getCurrentModeController().getExtension(MMapIO.class);
		try {
			MapModel map = new MapModel();
			mapIO.loadTree(map, file);
			map.getRootNode().setText(name);
			mapIO.writeToFile(map, file);
		}
		catch (Exception e) {
			LogUtils.severe("Could not create '"+name+"' map.",e);
		}		
	}

	/**
	 * @param file
	 * @param resourcePath
	 */
	private void createAndCopy(File file, String resourcePath) {
		try {
			createFile(file);
			FileUtils.copyInputStreamToFile(CoreConfiguration.class.getResourceAsStream(resourcePath), file);
		}
		catch (IOException e) {
			LogUtils.warn(e);
		}	
	}

	/**
	 * @param file
	 * @throws IOException
	 */
	private void createFile(File file) throws IOException {
		if(!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
			return;
		}
		file.createNewFile();
	}
	
	private void linkWelcomeMindmapAfterWorkspaceCreation() {		
		AWorkspaceTreeNode parent = WorkspaceUtils.getNodeForPath(((WorkspaceRoot) WorkspaceUtils.getModel().getRoot()).getName()+"/Miscellaneous");
		if (parent == null) {
			return;
		}
		File _welcomeFile = new File(WorkspaceUtils.getDataDirectory(), "/help/docear-welcome.mm");
		LinkTypeFileNode node = new LinkTypeFileNode();
		node.setName(_welcomeFile.getName());
		node.setLinkPath(WorkspaceUtils.getWorkspaceRelativeURI(_welcomeFile));
		WorkspaceUtils.getModel().addNodeTo(node, parent, false);
		parent.refresh();
	}
	
	private void modifyContextMenus() {		
		AWorkspaceTreeNode root =  (AWorkspaceTreeNode) WorkspaceUtils.getModel().getRoot();
		WorkspacePopupMenuBuilder.insertAction(root.getContextMenu(), "workspace.action.docear.locations.change", 3);
	}

}
