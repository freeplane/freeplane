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
import org.freeplane.plugin.workspace.WorkspaceConfiguration;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.node.LinkTypeFileNode;
import org.freeplane.plugin.workspace.controller.IWorkspaceEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceEvent;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

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
			try {
				if(!infoFile.getParentFile().exists() && !infoFile.getParentFile().mkdirs()) {
					return;
				}
				infoFile.createNewFile();
				FileUtils.copyInputStreamToFile(CoreConfiguration.class.getResourceAsStream("/conf/!!!info.txt"), infoFile);
			}
			catch (IOException e) {
				LogUtils.warn(e);
			}
			
		}
		File _dataInfoFile = new File(WorkspaceUtils.getProfileBaseFile().getParentFile().getParentFile(), "!!!info.txt");
		if(!_dataInfoFile.exists()) {
			try {
				if(!_dataInfoFile.getParentFile().exists() && !_dataInfoFile.getParentFile().mkdirs()) {
					return;
				}
				_dataInfoFile.createNewFile();
				FileUtils.copyInputStreamToFile(CoreConfiguration.class.getResourceAsStream("/conf/!!!info.txt"), _dataInfoFile);
			}
			catch (IOException e) {
				LogUtils.warn(e);
			}
			
		}
		
		File _welcomeFile = new File(WorkspaceUtils.getDataDirectory(), "/help/docear-welcome.mm");
		if(!_welcomeFile.exists()) {
			try {
				if(!_welcomeFile.getParentFile().exists() && !_welcomeFile.getParentFile().mkdirs()) {
					return;
				}
				_welcomeFile.createNewFile();
				FileUtils.copyInputStreamToFile(CoreConfiguration.class.getResourceAsStream("/conf/docear-welcome.mm"), _welcomeFile);
			}
			catch (IOException e) {
				LogUtils.warn(e);
			}
			
		}
		
	}
	
	private void linkWelcomeMindmapAfterWorkspaceCreation() {		
		AWorkspaceTreeNode parent = WorkspaceUtils.getNodeForPath("My Workspace/Miscellaneous");
		if (parent == null) {
			return;
		}
		File _welcomeFile = new File(WorkspaceUtils.getDataDirectory(), "/help/docear-welcome.mm");
		LinkTypeFileNode node = new LinkTypeFileNode();
		node.setName(_welcomeFile.getName());
		node.setLinkPath(WorkspaceUtils.getWorkspaceRelativeURI(_welcomeFile));
		WorkspaceUtils.getModel().addNodeTo(node, parent);
		parent.refresh();
	}
	
	private void modifyContextMenus() {		
		AWorkspaceTreeNode root =  (AWorkspaceTreeNode) WorkspaceUtils.getModel().getRoot();
		WorkspacePopupMenuBuilder.insertAction(root.getContextMenu(), "workspace.action.docear.locations.change", 3);
	}

}
