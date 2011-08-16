package org.docear.plugin.core;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.JLabel;

public class LocationDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
		
	private JPanel mainPanel = new JPanel();
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
		
	/**
	 * Create the dialog.
	 */
	private void browsePdf() {
		
	}
	
	private void browseBibtex() {	
		
	}
	
	private void browseProjects() {	
		
	}
	
	private void onCancelButton() {
//		WorkspaceController.getCurrentWorkspaceController().setWorkspaceLocation("");
//		WorkspaceController.getCurrentWorkspaceController().showWorkspaceView(false);
		this.dispose();
	}
	
	private void onOkButton() {
//		WorkspaceController.getCurrentWorkspaceController().setWorkspaceLocation(location.getText());
		this.dispose();
	}
	
//	private void onShowButton() {
//		JFileChooser fileChooser = new JFileChooser();		
//		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//		if (literatureLocation != null) {
//			File file = new File(this.literatureLocation.getText());
//			if (file.exists()) {
//				fileChooser.setSelectedFile(file);
//			}
//		}
//
//		int retVal = fileChooser.showOpenDialog(UITools.getFrame());
//		if (retVal == JFileChooser.APPROVE_OPTION) {			
//			File selectedfile = fileChooser.getSelectedFile();
//			this.literatureLocation.setText(selectedfile.getPath());						
//		}
//	}
	
	public LocationDialog() {
		this.setModal(true);
		setTitle(TextUtils.getText("docear_initialization"));
		setBounds(100, 100, 516, 282);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{			
			contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("114px:grow"),
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("106px"),
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC,},
				new RowSpec[] {
					FormFactory.UNRELATED_GAP_ROWSPEC,
					RowSpec.decode("25px"),
					FormFactory.RELATED_GAP_ROWSPEC,
					RowSpec.decode("default:grow"),}));
		}
		{
			mainPanel = new JPanel();
			contentPanel.add(mainPanel, "1, 1, 12, 4, fill, fill");
			mainPanel.setLayout(new FormLayout(new ColumnSpec[] {
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("max(150dlu;default):grow"),
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.PREF_COLSPEC,},
				new RowSpec[] {
					RowSpec.decode("fill:4dlu"),
					FormFactory.DEFAULT_ROWSPEC,
					RowSpec.decode("fill:2dlu"),
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.NARROW_LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.NARROW_LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,}));
						
//			String currentLocation = WorkspaceController.getCurrentWorkspaceController().getWorkspaceLocation();
//			String currentLocation = "";
//			if (currentLocation != null && currentLocation.length()>0) {
//				literatureLocation.setText(currentLocation);
//			}
			{
				JLabel lblPdflocation = new JLabel(TextUtils.getText("literature_location"));
				mainPanel.add(lblPdflocation, "2, 2, fill, fill");
			}
			{
				{
					textField_2 = new JTextField();
					textField_2.setColumns(30);
					mainPanel.add(textField_2, "2, 4, fill, center");
				}
				{
					JButton btnBrowsePdf = new JButton(TextUtils.getText("browse"));
					btnBrowsePdf.addActionListener(new ActionListener() {						
						public void actionPerformed(ActionEvent e) {
							browsePdf();
						}						
					});
					mainPanel.add(btnBrowsePdf, "4, 4, right, center");
				}
				{
					JLabel lblBibtexFile = new JLabel(TextUtils.getText("bibtex_location"));
					mainPanel.add(lblBibtexFile, "2, 6, fill, fill");
				}
				{
					textField_1 = new JTextField();
					textField_1.setColumns(30);
					mainPanel.add(textField_1, "2, 8, fill, center");
				}
				{
					JButton btnBrowseBibtex = new JButton(TextUtils.getText("browse"));
					btnBrowseBibtex.addActionListener(new ActionListener() {						
						public void actionPerformed(ActionEvent e) {
							browseBibtex();
						}						
					});
					mainPanel.add(btnBrowseBibtex, "4, 8, right, center");
				}
				{
					JLabel lblProjectsLocation = new JLabel(TextUtils.getText("projects_location"));
					mainPanel.add(lblProjectsLocation, "2, 10, fill, fill");
				}
				{
					textField = new JTextField();
					textField.setColumns(30);
					mainPanel.add(textField, "2, 12, fill, center");
				}
				{
					JButton btnBrowseProjects = new JButton(TextUtils.getText("browse"));
					btnBrowseProjects.addActionListener(new ActionListener() {						
						public void actionPerformed(ActionEvent e) {
							browseProjects();
						}						
					});
					mainPanel.add(btnBrowseProjects, "4, 12, right, center");
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton(TextUtils.getText("ok"));
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						onOkButton();
					}
				});
				{
					JButton cancelButton = new JButton(TextUtils.getText("cancel"));
					cancelButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							onCancelButton();
						}
					});
					cancelButton.setActionCommand("cancel");
					buttonPane.add(cancelButton);
				}
				okButton.setActionCommand("ok");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}	
	}

	

}
