package org.docear.plugin.pdfutilities.listener;

import java.io.File;
import java.net.URI;

import org.docear.plugin.core.util.CoreUtils;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.nodes.ALinkNode;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;

public class WorkspaceNodeOpenDocumentListener implements IWorkspaceNodeActionListener {
	
	public void handleAction(WorkspaceActionEvent event) {
		
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
		File file = CoreUtils.resolveURI(uri);
		if(file!=null  && !file.exists()) {
			WorkspaceUtils.showFileNotFoundMessage(file);
			event.consume();
			return;
		}
		
		
		boolean openOnPage = ResourceController.getResourceController().getBooleanProperty(PdfUtilitiesController.OPEN_PDF_VIEWER_ON_PAGE_KEY);		
		
		if (openOnPage) {
			PdfUtilitiesController.getController().openPdfOnPage(uri, 1);
			event.consume();
		}
	}
}
