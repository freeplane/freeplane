package org.docear.plugin.pdfutilities.ui.conflict;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.docear.plugin.core.util.Tools;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class AnnotationConflictPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel label;
	private JPanel panel;

	/**
	 * Create the panel.
	 */
	public AnnotationConflictPanel() {
		init();
	}
	
	public AnnotationConflictPanel(Integer objectNumber) {
		init();
		this.setTitle(objectNumber);
	}
	

	private void init() {
		setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),}, //$NON-NLS-1$
			new RowSpec[] {
				RowSpec.decode("top:default"), //$NON-NLS-1$
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default:grow"),})); //$NON-NLS-1$
		
		label = new JLabel(TextUtils.getText("AnnotationConflictPanel.3")); //$NON-NLS-1$
		label.setForeground(Color.RED);
		label.setFont(new Font("Tahoma", Font.BOLD, 11)); //$NON-NLS-1$
		add(label, "1, 1"); //$NON-NLS-1$
		
		panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		add(panel, "1, 3, fill, fill"); //$NON-NLS-1$
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("5dlu"), //$NON-NLS-1$
				ColumnSpec.decode("default:grow"), //$NON-NLS-1$
				ColumnSpec.decode("5dlu"),}, //$NON-NLS-1$
			new RowSpec[] {
				RowSpec.decode("top:5dlu"), //$NON-NLS-1$
				RowSpec.decode("bottom:5dlu"),})); //$NON-NLS-1$
	}
	
	public void setTitle(Integer objectNumber){
		label.setText(TextUtils.getText("AnnotationConflictPanel.12") + objectNumber); //$NON-NLS-1$
	}
	
	public void setTitle(String annotationTitle){
		label.setText(TextUtils.getText("AnnotationConflictPanel.13") + Tools.reshapeString(annotationTitle, 100) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void addDropdownBoxPanel(AnnotationConflictDropdownBoxPanel dropdownBoxPanel){
		FormLayout formLayout = (FormLayout)panel.getLayout();		
		if(formLayout.getRowCount() > 2){
			formLayout.insertRow(formLayout.getRowCount(), FormFactory.NARROW_LINE_GAP_ROWSPEC);
		}
		
		formLayout.insertRow(formLayout.getRowCount(), RowSpec.decode("top:20px"));		 //$NON-NLS-1$
		this.panel.add(dropdownBoxPanel, "2, " + (formLayout.getRowCount() - 1) + ", fill, top"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	

}
