package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.pdf.PdfReaderFileFilter;
import org.docear.plugin.pdfutilities.ui.PdfReaderDefinitionDialog;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

public class ShowPdfReaderDefinitionDialogAction extends AFreeplaneAction {
	
	private static final long serialVersionUID = 1L;
	public static String KEY = "docear.show_pdf_reader_definition";

	public ShowPdfReaderDefinitionDialogAction() {
		super(KEY);
	}

	
	public void actionPerformed(ActionEvent e) {
		PdfReaderDefinitionDialog dialog = new PdfReaderDefinitionDialog();
		int option = JOptionPane.showConfirmDialog(UITools.getFrame(), dialog, TextUtils.getText("docear.pdf_reader_definition.headline"), JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			String readerCommand = dialog.getReaderCommand();
			if (readerCommand != null && readerCommand.trim().length() > 0) {
				PdfReaderFileFilter readerFilter = new PdfReaderFileFilter();
				if (!readerFilter.isPdfXChange(readerCommand) && !readerFilter.isFoxit(readerCommand) && !readerFilter.isAdobe(readerCommand)) {
					JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText(PdfUtilitiesController.OPEN_ON_PAGE_WARNING_KEY), TextUtils.getText("warning"), JOptionPane.WARNING_MESSAGE);
				}
				
				ResourceController.getResourceController().setProperty(PdfUtilitiesController.OPEN_ON_PAGE_READER_COMMAND_KEY, readerCommand);
				
				
			}
		}
	}

}
