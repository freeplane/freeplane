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
		final String readerPath = ResourceController.getResourceController().getProperty(PdfUtilitiesController.OPEN_ON_PAGE_READER_PATH_KEY);

		String[] command = null;
		if (!Compat.isMacOsX() && !Compat.isWindowsOS() && openOnPage && readerPath.length() > 0) {
			String wineFile = Tools.getFilefromUri(uri).getAbsolutePath();
			wineFile = "Z:" + wineFile.replace("/", "\\") + "";

			command = new String[3];
			command[0] = "wine";
			command[1] = readerPath;
			command[2] = wineFile;
			
		} else if(Compat.isWindowsOS() && openOnPage ) {			
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
