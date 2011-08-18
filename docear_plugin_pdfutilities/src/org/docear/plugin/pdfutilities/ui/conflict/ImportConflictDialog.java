package org.docear.plugin.pdfutilities.ui.conflict;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.docear.plugin.pdfutilities.features.AnnotationID;
import org.docear.plugin.pdfutilities.features.AnnotationNodeModel;
import org.docear.plugin.pdfutilities.features.IAnnotation;

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
	private Map<URI, PdfConflictPanel> pdfConflictPanels = new HashMap<URI, PdfConflictPanel>();
	private Collection<AnnotationConflictDropdownBoxPanel> dropdownBoxPanels = new ArrayList<AnnotationConflictDropdownBoxPanel>();
	private Frame parent;
	

	/**
	 * Show the dialog.
	 */
	public void showDialog() {
		this.setSize(640, 480);
		this.setLocationRelativeTo(parent);
		this.setModal(true);
		//this.pack();
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setVisible(true);		
	}

	/**
	 * Create the dialog.
	 */
	public ImportConflictDialog() {
		init();		
	}	
	
	
	public ImportConflictDialog(Frame parent, Map<AnnotationID, Collection<IAnnotation>> conflicts) {
		super(parent);
		this.parent = parent;
		init();	
		for(AnnotationID id : conflicts.keySet()){
			PdfConflictPanel pdfConflictPanel;
			if(this.pdfConflictPanels.containsKey(id.getUri())){
				pdfConflictPanel = this.pdfConflictPanels.get(id.getUri());
			}
			else{
				pdfConflictPanel = new PdfConflictPanel(id.getUri());
				this.pdfConflictPanels.put(id.getUri(), pdfConflictPanel);
			}
			
			AnnotationConflictPanel annotationConflictPanel = new AnnotationConflictPanel(id.getObjectNumber());
			pdfConflictPanel.addAnnotationConflictPanel(annotationConflictPanel);
			
			//add pdf annotation first
			for(IAnnotation conflictedAnnotation : conflicts.get(id)){
				if(conflictedAnnotation instanceof AnnotationNodeModel) continue;
				annotationConflictPanel.setTitle(conflictedAnnotation.getTitle());
				AnnotationConflictDropdownBoxPanel dropdownBoxPanel = new AnnotationConflictDropdownBoxPanel();
				dropdownBoxPanel.setAnnotationData(conflicts.get(id), conflictedAnnotation);
				annotationConflictPanel.addDropdownBoxPanel(dropdownBoxPanel);
				this.dropdownBoxPanels.add(dropdownBoxPanel);
			}
			//add conflicted mindmap nodes
			for(IAnnotation conflictedAnnotation : conflicts.get(id)){
				if(conflictedAnnotation instanceof AnnotationNodeModel){
					AnnotationConflictDropdownBoxPanel dropdownBoxPanel = new AnnotationConflictDropdownBoxPanel();
					dropdownBoxPanel.setAnnotationData(conflicts.get(id), conflictedAnnotation);
					annotationConflictPanel.addDropdownBoxPanel(dropdownBoxPanel);
					this.dropdownBoxPanels.add(dropdownBoxPanel);
				}
			}
			this.importConflictPanel.addPdfConflictPanel(pdfConflictPanel);				
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
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOK(e);
			}
		});
		buttonPane.add(btnNewButton_1, "6, 1");
	
		JButton btnNewButton = new JButton("Cancel");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel(e);
			}
		});
		buttonPane.add(btnNewButton, "8, 1, fill, fill");
	}

	protected void onOK(ActionEvent e) {
		for(AnnotationConflictDropdownBoxPanel panel : this.dropdownBoxPanels){
			panel.getCommand().solveConflict();
		}
		this.dispose();
	}

	protected void onCancel(ActionEvent e) {
		this.dispose();
	}

}
