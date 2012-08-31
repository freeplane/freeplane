package org.docear.plugin.pdfutilities.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.docear.plugin.core.ui.MultiLineActionLabel;
import org.docear.plugin.pdfutilities.pdf.PdfReaderFileFilter;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class ViewerSettingsChangeErrorDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create the panel.
	 */
	public ViewerSettingsChangeErrorDialog(final String readerCommand) {
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		

		JLabel message = new JLabel(TextUtils.getText("docear.validate_pdf_xchange.settings_change_error"));
		add(message, "2, 2");
		
		MultiLineActionLabel link = new MultiLineActionLabel(TextUtils.getText("docear.validate_pdf_xchange.settings_change_error.link"));
		add(link, "2, 4");
		link.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if ("open_url".equals(e.getActionCommand())) {
					String anchor = "#compatible_pdf_readers";
					PdfReaderFileFilter readerFilter = new PdfReaderFileFilter();
					if (readerFilter.isPdfXChange(readerCommand)) {
						anchor = "#pdfxcv";
					}
					else if (readerFilter.isAcrobat(readerCommand)) {
						anchor = "#acrobat";
					}
					try {
						Controller.getCurrentController().getViewController().openDocument(URI.create("http://www.docear.org/support/user-manual/"+anchor));
					}
					catch (IOException ex) {						
						LogUtils.warn(ex);
					}
				}
			}
		});

	}
}
