package org.docear.plugin.pdfutilities.ui.conflict;

import java.awt.BorderLayout;
import java.net.URI;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.docear.plugin.core.util.Tools;
import org.freeplane.core.util.TextUtils;

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
		setBorder(new TitledBorder(null, TextUtils.getText("PdfConflictPanel_0"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		panel = new JPanel();
		scrollPane.setViewportView(panel);
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("5dlu"), //$NON-NLS-1$
				ColumnSpec.decode("default:grow"), //$NON-NLS-1$
				ColumnSpec.decode("5dlu"),}, //$NON-NLS-1$
			new RowSpec[] {
				RowSpec.decode("fill:5dlu"), //$NON-NLS-1$
				RowSpec.decode("fill:5dlu"),})); //$NON-NLS-1$
	}

	public void setTitle(String fileName){
		fileName = Tools.reshapeString(fileName, 200);
		this.setBorder(new TitledBorder(null, fileName, TitledBorder.LEADING, TitledBorder.TOP, null, null));
	}
	
	public void addAnnotationConflictPanel(AnnotationConflictPanel annotationConflictPanel){
		FormLayout formLayout = (FormLayout)panel.getLayout();		
		if(formLayout.getRowCount() > 2){
			formLayout.insertRow(formLayout.getRowCount(), RowSpec.decode("fill:5dlu")); //$NON-NLS-1$
		}		
		
		formLayout.insertRow(formLayout.getRowCount(), RowSpec.decode("fill:default"));		 //$NON-NLS-1$
		this.panel.add(annotationConflictPanel, "2, " + (formLayout.getRowCount() - 1) + ", fill, fill"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
