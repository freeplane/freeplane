package org.docear.plugin.pdfutilities.ui;

import javax.swing.JPanel;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JScrollPane;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.border.EmptyBorder;

public class ImportConflictPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel panel;

	/**
	 * Create the panel.
	 */
	public ImportConflictPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new EmptyBorder(0, 0, 0, 0));
		add(scrollPane, BorderLayout.CENTER);
		
		panel = new JPanel();
		panel.setBorder(null);
		scrollPane.setViewportView(panel);
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("5dlu"),
				ColumnSpec.decode("default:grow"),
				ColumnSpec.decode("5dlu"),},
			new RowSpec[] {
				RowSpec.decode("fill:5dlu"),				
				RowSpec.decode("fill:5dlu"),}));
		
		JPanel HeaderPanel = new JPanel();
		HeaderPanel.setBackground(new Color(-1643275));
		add(HeaderPanel, BorderLayout.NORTH);
		HeaderPanel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("5dlu"),
				ColumnSpec.decode("default:grow"),
				ColumnSpec.decode("5dlu"),},
			new RowSpec[] {
				RowSpec.decode("fill:5dlu"),
				RowSpec.decode("fill:default:grow"),
				RowSpec.decode("fill:5dlu"),
				RowSpec.decode("fill:default:grow"),
				RowSpec.decode("fill:20dlu"),}));
		
		JLabel lblAnnotationImportConflict = new JLabel("Annotation Import Conflict");
		lblAnnotationImportConflict.setFont(new Font("Dialog", Font.BOLD, 14));
		HeaderPanel.add(lblAnnotationImportConflict, "2, 2");
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(new Color(-1643275));
		HeaderPanel.add(panel_1, "2, 4, fill, fill");
		panel_1.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("15dlu"),
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				RowSpec.decode("fill:default:grow"),}));
		
		JLabel lblChooseWhichAnnotations = new JLabel("Choose which annotations should be overwritten ");
		lblChooseWhichAnnotations.setFont(new Font("Dialog", Font.PLAIN, 13));
		panel_1.add(lblChooseWhichAnnotations, "2, 1");
		
		
		PdfConflictPanel conflictPanel = new PdfConflictPanel();
		PdfConflictPanel conflictPanel1 = new PdfConflictPanel();
		PdfConflictPanel conflictPanel2 = new PdfConflictPanel();
		
		this.addPdfConflictPanel(conflictPanel);
		this.addPdfConflictPanel(conflictPanel1);
		this.addPdfConflictPanel(conflictPanel2);

	}
	
	public void addPdfConflictPanel(PdfConflictPanel pdfConflictPanel){
		FormLayout formLayout = (FormLayout)panel.getLayout();		
		if(formLayout.getRowCount() > 2){
			formLayout.insertRow(formLayout.getRowCount(), RowSpec.decode("fill:5dlu"));
		}		
		
		formLayout.insertRow(formLayout.getRowCount(), RowSpec.decode("fill:default"));		
		this.panel.add(pdfConflictPanel, "2, " + (formLayout.getRowCount() - 1) + ", fill, fill");
	}

}
