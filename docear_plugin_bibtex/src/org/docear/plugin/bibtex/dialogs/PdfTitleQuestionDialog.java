package org.docear.plugin.bibtex.dialogs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.docear.plugin.core.ui.MultiLineActionLabel;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

public class PdfTitleQuestionDialog extends JPanel {
	private static final long serialVersionUID = -403224510351985204L;
	private JTextField txtTitle;

	public PdfTitleQuestionDialog(String title, final URI uri) {
//		setPreferredSize(new Dimension(640, 120));
		setMinimumSize(new Dimension(640, 120));
		setMaximumSize(new Dimension(640, 200));
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("434px:grow"),},
			new RowSpec[] {
				new RowSpec(RowSpec.FILL, Sizes.bounded(Sizes.DEFAULT, Sizes.constant("40dlu", false), Sizes.constant("40dlu", false)), 0),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default"),}));
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		add(panel, "1, 1, 3, 1, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				RowSpec.decode("max(20dlu;default)"),}));
		
		JLabel lblNewLabel = new JLabel(TextUtils.getText("docear.metadata.title.help"));
		panel.add(lblNewLabel, "2, 1");
		
		//JLabel lblQuestion = new JLabel(TextUtils.getText("docear.metadata.title.question"));
		MultiLineActionLabel lblQuestion = new MultiLineActionLabel(TextUtils.getText("docear.metadata.title.question"));
		lblQuestion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if("open_document_link".equals(e.getActionCommand()) ){
					try {
//						Controller.getCurrentController().getViewController().openDocument(uri);
						boolean openOnPage = ResourceController.getResourceController().getBooleanProperty(PdfUtilitiesController.OPEN_PDF_VIEWER_ON_PAGE_KEY);		
						
						if (openOnPage) {
							PdfUtilitiesController.getController().openPdfOnPage(uri, 1);
						}
					} catch (Exception ex) {
						LogUtils.warn("could not open link: "+ ex.getLocalizedMessage());
					}
				}
			}
		});
		add(lblQuestion, "1, 3, 3, 1");
		
		JLabel lblTitle = new JLabel(TextUtils.getText("docear.metadata.title.label"));
		lblTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblTitle, "1, 5, right, default");
		
		txtTitle = new JTextField(title);
		lblTitle.setLabelFor(txtTitle);
		add(txtTitle, "3, 5, fill, default");
		txtTitle.setColumns(10);
		txtTitle.setSelectionStart(0);
		txtTitle.setSelectionEnd(0);
	}
	
	public String getTitle() {
		return String.valueOf(txtTitle.getText()).trim();
	}

}
