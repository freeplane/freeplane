package org.docear.plugin.pdfutilities.ui;

import java.util.Collection;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.docear.plugin.pdfutilities.util.Tools;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class AnnotationConflictDropdownBoxPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel label;
	private JComboBox comboBox;

	/**
	 * Create the panel.
	 */
	public AnnotationConflictDropdownBoxPanel() {
		this.init();		
	}
	
	public AnnotationConflictDropdownBoxPanel(String fileName, Collection<AnnotationConflictModel> annotations, AnnotationConflictModel selected) {
		this.init();
		this.setAnnotationData(fileName, annotations, selected);
	}

	private void init() {
		setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("left:default:grow"),
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("right:default:grow"),},
			new RowSpec[] {
				RowSpec.decode("fill:default"),}));
		
		label = new JLabel("Annotation name in <file name> : ");
		add(label, "1, 1, left, default");
		
		comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"<file annotation name>"}));
		add(comboBox, "3, 1, fill, default");
	}
	
	public void setAnnotationData(String fileName, Collection<AnnotationConflictModel> annotations, AnnotationConflictModel selected){
		//TODO: DOCEAR remove hard coded strings
		this.label.setText("Annotation name in " + Tools.reshapeString(fileName, 30) + " : ");
		this.comboBox.setModel(new DefaultComboBoxModel(annotations.toArray()));
		this.comboBox.setSelectedItem(selected);
	}
	
	public Object getSelected(){
		return this.comboBox.getSelectedItem();
	}

}
