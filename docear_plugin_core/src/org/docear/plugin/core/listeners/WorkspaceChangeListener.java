package org.docear.plugin.core.listeners;

import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.IDocearLibrary;
import org.docear.plugin.core.workspace.node.FolderTypeLibraryNode;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.controller.IWorkspaceListener;
import org.freeplane.plugin.workspace.controller.WorkspaceEvent;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

public class WorkspaceChangeListener implements IWorkspaceListener {

	public void workspaceChanged(WorkspaceEvent event) {
		if (event.getType() == WorkspaceEvent.WORKSPACE_EVENT_TYPE_RELOAD) {
			CoreConfiguration.projectPathObserver.reset();
			CoreConfiguration.referencePathObserver.reset();
			CoreConfiguration.repositoryPathObserver.reset();
			CoreConfiguration.copyInfoIfNeeded();
		}
		else if (event.getType() == WorkspaceEvent.WORKSPACE_EVENT_TYPE_CHANGED) {
			IDocearLibrary lib = DocearController.getController().getLibrary();
			if(lib != null && lib instanceof FolderTypeLibraryNode) {
				WorkspaceController.getController().getExpansionStateHandler().addPathKey(((AWorkspaceTreeNode)lib).getKey());
				WorkspaceController.getController().refreshWorkspace();
			}
//			final File baseDir = new File(FreeplaneStarter.getResourceBaseDir()).getAbsoluteFile().getParentFile();
//			final String map = ResourceController.getResourceController().getProperty("first_start_map");
//			final File absolutFile = ConfigurationUtils.getLocalizedFile(baseDir, map, Locale.getDefault().getLanguage());
//			
//			WorkspaceIndexedTreeModel model = WorkspaceController.getController().getWorkspaceModel();
//			model.getNode("Miscellaneous");
		}
	}
	

}
