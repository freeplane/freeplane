package org.docear.plugin.pdfutilities.listener;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.docear.plugin.core.util.CoreUtils;
import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.pdf.PdfReaderFileFilter;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
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
		
		
		boolean openOnPageWine = ResourceController.getResourceController().getBooleanProperty(PdfUtilitiesController.OPEN_PDF_VIEWER_ON_PAGE_KEY_WINE);
		final String readerPathWine = ResourceController.getResourceController().getProperty(PdfUtilitiesController.OPEN_ON_PAGE_READER_PATH_KEY_WINE);

		String[] command = null;
		if (openOnPageWine && readerPathWine.length() > 0) {
			String wineFile = Tools.getFilefromUri(uri).getAbsolutePath();
			wineFile = "Z:" + wineFile.replace("/", "\\") + "";

			command = new String[3];
			command[0] = "wine";
			command[1] = readerPathWine;
			command[2] = wineFile;
			
		} else if(Compat.isWindowsOS() && ResourceController.getResourceController().getBooleanProperty(PdfUtilitiesController.OPEN_PDF_VIEWER_ON_PAGE_KEY) ) {
			String readerPath = ResourceController.getResourceController().getProperty(PdfUtilitiesController.OPEN_ON_PAGE_READER_PATH_KEY);
			if(isValidReaderPath(readerPath)) {
				command = new String[2];
				command[0] = readerPath;
				command[1] = Tools.getFilefromUri(uri).getAbsolutePath();
			}
		}
		
		if(command == null) {
			return;
		}
		try {
			Controller.exec(command);
			event.consume();
		}
		catch (IOException e) {
			LogUtils.severe(e);
		}

	}
	
	private boolean isValidReaderPath(String readerPath) {
		return readerPath != null && readerPath.length() > 0 && new File(readerPath).exists()
				&& new PdfReaderFileFilter().accept(new File(readerPath));
	}

}
