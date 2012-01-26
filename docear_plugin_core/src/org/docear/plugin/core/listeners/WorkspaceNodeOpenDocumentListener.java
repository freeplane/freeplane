package org.docear.plugin.core.listeners;

import java.io.IOException;
import java.net.URI;

import org.docear.plugin.core.util.CoreUtils;
import org.docear.plugin.core.util.Tools;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.io.node.DefaultFileNode;
import org.freeplane.plugin.workspace.model.node.ALinkNode;

public class WorkspaceNodeOpenDocumentListener implements IWorkspaceNodeEventListener {

	public void handleEvent(WorkspaceNodeEvent event) {
		
		URI uri = null;
		if(event.getSource() instanceof DefaultFileNode) {
			uri = ((DefaultFileNode) event.getSource()).getFile().toURI();
		}
		else if(event.getSource() instanceof ALinkNode) {
			uri = ((ALinkNode) event.getSource()).getLinkPath();
		}

		if(uri == null || !CoreUtils.resolveURI(uri).getName().toLowerCase().endsWith(".pdf")) {
			return;
		}
		
		
		boolean openOnPageWine = ResourceController.getResourceController().getBooleanProperty("docear_open_on_page_wine");
		final String readerPathWine = ResourceController.getResourceController().getProperty(
				"docear_open_on_page_reader_path_wine");

		
		if (openOnPageWine && readerPathWine.length() > 0) {
			String wineFile = Tools.getFilefromUri(uri).getAbsolutePath();
			wineFile = "Z:" + wineFile.replace("/", "\\") + "";

			String[] command = new String[3];
			command[0] = "wine";
			command[1] = readerPathWine;
			command[2] = wineFile;			

			try {
				Controller.exec(command);
				event.consume();
			}
			catch (IOException e) {
				LogUtils.severe(e);
			}
		}

	}

}
