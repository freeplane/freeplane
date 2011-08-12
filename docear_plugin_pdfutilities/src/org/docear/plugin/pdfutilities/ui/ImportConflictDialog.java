package org.docear.plugin.pdfutilities.ui;

import java.awt.BorderLayout;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

public class ImportConflictDialog extends JDialog {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private ImportConflictPanel importConflictPanel;
	

	/**
	 * Show the dialog.
	 */
	public void showDialog() {	
		this.pack();
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setVisible(true);		
	}

	/**
	 * Create the dialog.
	 */
	public ImportConflictDialog() {
		init();		
	}
	
	public ImportConflictDialog(ImportConflictModel conflicts) {
		init();
		
		for(URI uri : conflicts.getUrisFromConflictedPdfs()){
			PdfConflictPanel pdfConflictPanel = new PdfConflictPanel(uri, conflicts);
			importConflictPanel.addPdfConflictPanel(pdfConflictPanel);
		}
	}

	private void init() {
		setBounds(100, 100, 821, 543);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				RowSpec.decode("fill:default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,}));
		
		importConflictPanel = new ImportConflictPanel();
		contentPanel.add(importConflictPanel, "1, 1, fill, fill");		
		
		JPanel buttonPane = new JPanel();
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		buttonPane.setLayout(new FormLayout(new ColumnSpec[] {
				new ColumnSpec(ColumnSpec.FILL, Sizes.bounded(Sizes.PREFERRED, Sizes.constant("4dlu", true), Sizes.constant("100dlu", true)), 1),
				FormFactory.BUTTON_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.BUTTON_COLSPEC,
				ColumnSpec.decode("20dlu"),
				FormFactory.BUTTON_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("right:max(50dlu;pref)"),
				ColumnSpec.decode("right:4dlu"),},
			new RowSpec[] {
				RowSpec.decode("23px"),
				FormFactory.LINE_GAP_ROWSPEC,}));
		
		JButton btnNewButton_1 = new JButton("Finish");
		buttonPane.add(btnNewButton_1, "6, 1");
	
		JButton btnNewButton = new JButton("Cancel");
		buttonPane.add(btnNewButton, "8, 1, fill, fill");
	}

}
