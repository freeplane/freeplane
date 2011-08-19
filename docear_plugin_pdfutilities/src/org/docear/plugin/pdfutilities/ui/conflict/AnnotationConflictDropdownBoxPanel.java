package org.docear.plugin.pdfutilities.ui.conflict;

import java.util.Collection;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.features.AnnotationNodeModel;
import org.docear.plugin.pdfutilities.features.IAnnotation;
import org.docear.plugin.pdfutilities.util.Tools;
import org.freeplane.features.map.NodeModel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class AnnotationConflictDropdownBoxPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel label;
	private JComboBox comboBox;
	private ISolveConflictCommand command = new DoNothingCommand();
	private IAnnotation target;
	/**
	 * Create the panel.
	 */
	public AnnotationConflictDropdownBoxPanel() {
		this.init();		
	}

	private void init() {
		setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("left:80px:grow"),
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("right:80px:grow"),},
			new RowSpec[] {
				RowSpec.decode("fill:default"),}));
		
		label = new JLabel("Annotation name in <file name> : ");
		add(label, "1, 1, left, default");
		
		comboBox = new JComboBox();
		comboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				selectionChanged(e);
			}
		});
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"<file annotation name>"}));
		add(comboBox, "3, 1, fill, default");
	}
	
	public void setAnnotationData(Collection<IAnnotation> annotations, IAnnotation target){
		//TODO: DOCEAR remove hard coded strings
		this.target = target;
		String fileName = getFileName(target);		
		this.label.setText("Annotation name in " + fileName + " : ");
		this.comboBox.setModel(new DefaultComboBoxModel(annotations.toArray()));
		this.comboBox.setSelectedItem(target);		
	}
	
	private void selectionChanged(ItemEvent e){
		Object selected = this.comboBox.getSelectedItem();
		if(selected.equals(this.target)){
			this.command = new DoNothingCommand();
		}
		else if(this.target instanceof AnnotationNodeModel){
			NodeModel node = ((AnnotationNodeModel) this.target).getNode();
			this.command = new SolveConflictForNode(node, ((IAnnotation) selected).getTitle());
		}
		else if(this.target instanceof AnnotationModel){
			AnnotationModel model = (AnnotationModel) this.target;
			this.command = new SolveConflictForPdf(model, ((IAnnotation) selected).getTitle());
		}		
	}
	
	private String getFileName(IAnnotation selected) {
		if(selected instanceof AnnotationNodeModel){
			NodeModel node = ((AnnotationNodeModel) selected).getNode();
			return node.getMap().getFile().getName();
		}
		else{
			return Tools.getFilefromUri(selected.getUri()).getName();
		}
	}

	public Object getSelected(){
		return this.comboBox.getSelectedItem();
	}

	public ISolveConflictCommand getCommand() {
		return command;
	}

	public void setCommand(ISolveConflictCommand command) {
		this.command = command;
	}

}
