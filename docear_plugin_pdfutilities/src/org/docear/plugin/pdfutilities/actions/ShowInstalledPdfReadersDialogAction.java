package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;

import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.freeplane.core.ui.AFreeplaneAction;

public class ShowInstalledPdfReadersDialogAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static String KEY = "docear.show_install_pdf_readers";

	public ShowInstalledPdfReadersDialogAction() {
		super(KEY);
	}

	public void actionPerformed(ActionEvent e) {		
		PdfUtilitiesController.getController().showViewerSelectionIfNecessary(true);
	}

}
