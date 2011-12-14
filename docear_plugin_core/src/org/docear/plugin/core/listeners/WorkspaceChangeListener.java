package org.docear.plugin.core.listeners;

import java.io.File;
import java.util.Locale;

import org.docear.plugin.core.CoreConfiguration;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.ConfigurationUtils;
import org.freeplane.main.application.FreeplaneStarter;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.controller.IWorkspaceListener;
import org.freeplane.plugin.workspace.controller.WorkspaceEvent;
import org.freeplane.plugin.workspace.model.WorkspaceIndexedTreeModel;

public class WorkspaceChangeListener implements IWorkspaceListener {

	public void workspaceChanged(WorkspaceEvent event) {
		if (event.getType() == WorkspaceEvent.WORKSPACE_EVENT_TYPE_RELOAD) {
			CoreConfiguration.projectPathObserver.reset();
			CoreConfiguration.referencePathObserver.reset();
			CoreConfiguration.repositoryPathObserver.reset();
		}
//		else if (event.getType() == WorkspaceEvent.WORKSPACE_EVENT_TYPE_CHANGED) {
//			final File baseDir = new File(FreeplaneStarter.getResourceBaseDir()).getAbsoluteFile().getParentFile();
//			final String map = ResourceController.getResourceController().getProperty("first_start_map");
//			final File absolutFile = ConfigurationUtils.getLocalizedFile(baseDir, map, Locale.getDefault().getLanguage());
//			
//			WorkspaceIndexedTreeModel model = WorkspaceController.getController().getWorkspaceModel();
//			model.getNode("Miscellaneous");
//		}
	}
	

}
