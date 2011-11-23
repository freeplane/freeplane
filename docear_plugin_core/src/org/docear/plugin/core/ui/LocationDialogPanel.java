/**
 * author: Marcel Genzmehr
 * 22.11.2011
 */
package org.docear.plugin.core.ui;

import java.io.File;
import java.net.URI;

import javax.swing.JPanel;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.url.UrlManager;
import org.freeplane.plugin.workspace.WorkspaceUtils;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class LocationDialogPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField txtLocationString;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public LocationDialogPanel(URI oldLocation, final boolean directoryOnly) {
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(150dlu;default):grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		txtLocationString = new JTextField();
		txtLocationString.setText("locationString");
		add(txtLocationString, "2, 2, fill, default");
		txtLocationString.setColumns(10);
		
		JButton button = new JButton("...");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = UrlManager.getController().getFileChooser(null, true, true);
				if (txtLocationString != null) {
					File file = new File(txtLocationString.getText());
					if (file.exists()) {
						fileChooser.setSelectedFile(file);
					}
					else {
						fileChooser.setSelectedFile(WorkspaceUtils.getWorkspaceBaseFile());
					}
				}

				fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
				if(directoryOnly) {
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				} 
				else {
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				}
				int retVal = fileChooser.showOpenDialog(UITools.getFrame());
				if (retVal == JFileChooser.APPROVE_OPTION) {			
					File selectedfile = fileChooser.getSelectedFile();
					txtLocationString.setText(selectedfile.getPath());						
				}
			}
		});
		add(button, "4, 2");
		setLocationUri(oldLocation);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public URI getLocationUri() {
		return WorkspaceUtils.getWorkspaceRelativeURI(new File(txtLocationString.getText()));
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void setLocationUri(URI location) {
		txtLocationString.setText(WorkspaceUtils.resolveURI(location).getPath());
	}
}
