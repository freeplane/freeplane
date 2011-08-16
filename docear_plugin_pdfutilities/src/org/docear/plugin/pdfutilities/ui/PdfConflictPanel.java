package org.docear.plugin.pdfutilities.ui;

import java.awt.BorderLayout;
import java.net.URI;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.docear.plugin.pdfutilities.util.Tools;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class PdfConflictPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel panel;

	/**
	 * Create the panel.
	 */
	public PdfConflictPanel() {
		init();		
	}
	
	public PdfConflictPanel(URI uri) {
		init();	
		String fileName = Tools.getFilefromUri(uri).getName();
		this.setTitle(fileName);
	}	

	private void init() {
		setBorder(new TitledBorder(null, "PDF Name", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		panel = new JPanel();
		scrollPane.setViewportView(panel);
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("5dlu"),
				ColumnSpec.decode("default:grow"),
				ColumnSpec.decode("5dlu"),},
			new RowSpec[] {
				RowSpec.decode("fill:5dlu"),
				RowSpec.decode("fill:5dlu"),}));
	}

	public void setTitle(String fileName){
		fileName = Tools.reshapeString(fileName, 200);
		this.setBorder(new TitledBorder(null, fileName, TitledBorder.LEADING, TitledBorder.TOP, null, null));
	}
	
	public void addAnnotationConflictPanel(AnnotationConflictPanel annotationConflictPanel){
		FormLayout formLayout = (FormLayout)panel.getLayout();		
		if(formLayout.getRowCount() > 2){
			formLayout.insertRow(formLayout.getRowCount(), RowSpec.decode("fill:5dlu"));
		}		
		
		formLayout.insertRow(formLayout.getRowCount(), RowSpec.decode("fill:default"));		
		this.panel.add(annotationConflictPanel, "2, " + (formLayout.getRowCount() - 1) + ", fill, fill");
	}

}
