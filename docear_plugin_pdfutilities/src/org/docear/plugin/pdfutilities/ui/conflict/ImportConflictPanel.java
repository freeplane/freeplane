package org.docear.plugin.pdfutilities.ui.conflict;

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

import org.freeplane.core.util.TextUtils;

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
				ColumnSpec.decode("5dlu"), //$NON-NLS-1$
				ColumnSpec.decode("default:grow"), //$NON-NLS-1$
				ColumnSpec.decode("5dlu"),}, //$NON-NLS-1$
			new RowSpec[] {
				RowSpec.decode("fill:5dlu"),				 //$NON-NLS-1$
				RowSpec.decode("fill:5dlu"),})); //$NON-NLS-1$
		
		JPanel HeaderPanel = new JPanel();
		HeaderPanel.setBackground(new Color(-1643275));
		add(HeaderPanel, BorderLayout.NORTH);
		HeaderPanel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("5dlu"), //$NON-NLS-1$
				ColumnSpec.decode("default:grow"), //$NON-NLS-1$
				ColumnSpec.decode("5dlu"),}, //$NON-NLS-1$
			new RowSpec[] {
				RowSpec.decode("fill:5dlu"), //$NON-NLS-1$
				RowSpec.decode("fill:default:grow"), //$NON-NLS-1$
				RowSpec.decode("fill:5dlu"), //$NON-NLS-1$
				RowSpec.decode("fill:default:grow"), //$NON-NLS-1$
				RowSpec.decode("fill:20dlu"),})); //$NON-NLS-1$
		
		JLabel lblAnnotationImportConflict = new JLabel(TextUtils.getText("ImportConflictPanel.13")); //$NON-NLS-1$
		lblAnnotationImportConflict.setFont(new Font(TextUtils.getText("ImportConflictPanel.14"), Font.BOLD, 14)); //$NON-NLS-1$
		HeaderPanel.add(lblAnnotationImportConflict, "2, 2"); //$NON-NLS-1$
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(new Color(-1643275));
		HeaderPanel.add(panel_1, "2, 4, fill, fill"); //$NON-NLS-1$
		panel_1.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("15dlu"), //$NON-NLS-1$
				ColumnSpec.decode("default:grow"),}, //$NON-NLS-1$
			new RowSpec[] {
				RowSpec.decode("fill:default:grow"),})); //$NON-NLS-1$
		
		JLabel lblChooseWhichAnnotations = new JLabel(TextUtils.getText("ImportConflictPanel.20")); //$NON-NLS-1$
		lblChooseWhichAnnotations.setFont(new Font(TextUtils.getText("ImportConflictPanel.21"), Font.PLAIN, 13)); //$NON-NLS-1$
		panel_1.add(lblChooseWhichAnnotations, "2, 1");		 //$NON-NLS-1$
	}
	
	public void addPdfConflictPanel(PdfConflictPanel pdfConflictPanel){
		FormLayout formLayout = (FormLayout)panel.getLayout();		
		if(formLayout.getRowCount() > 2){
			formLayout.insertRow(formLayout.getRowCount(), RowSpec.decode("fill:5dlu")); //$NON-NLS-1$
		}		
		
		formLayout.insertRow(formLayout.getRowCount(), RowSpec.decode("fill:default"));		 //$NON-NLS-1$
		this.panel.add(pdfConflictPanel, "2, " + (formLayout.getRowCount() - 1) + ", fill, fill"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
