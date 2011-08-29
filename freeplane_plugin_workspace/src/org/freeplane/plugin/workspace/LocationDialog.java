package org.freeplane.plugin.workspace;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.io.Serializable;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.MutableComboBoxModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataListener;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class LocationDialog extends JDialog implements VetoableChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField location;

	private JPanel mainPanel = new JPanel();

	/**
	 * Create the dialog.
	 */
	private void onCancelButton() {
		WorkspaceController.getController().setWorkspaceLocation("");
		WorkspaceController.getController().showWorkspace(false);
		this.dispose();
	}

	private void onOkButton() {
		WorkspaceController.getController().setWorkspaceLocation(location.getText());
		this.dispose();
	}

	private void onShowButton() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (location != null) {
			File file = new File(this.location.getText());
			if (file.exists()) {
				fileChooser.setSelectedFile(file);
			}
		}

		int retVal = fileChooser.showOpenDialog(UITools.getFrame());
		if (retVal == JFileChooser.APPROVE_OPTION) {
			File selectedfile = fileChooser.getSelectedFile();
			this.location.setText(selectedfile.getPath());
		}
	}

	public LocationDialog() {
		this.setModal(true);
		setTitle(TextUtils.getText("no_location_set"));
		setBounds(100, 100, 484, 200);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("114px:grow"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("106px"),
					FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
					FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("25px"), FormFactory.RELATED_GAP_ROWSPEC,
					RowSpec.decode("default:grow"), }));
		}
		{
			mainPanel = new JPanel();
			contentPanel.add(mainPanel, "1, 1, 12, 4, fill, fill");
			mainPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, },
					new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
			location = new JTextField();
			mainPanel.add(location, "2, 2, fill, fill");

			String currentLocation = WorkspaceController.getController().getWorkspaceLocation();
			if (currentLocation != null && currentLocation.length() > 0) {
				location.setText(currentLocation);
			}
			location.setColumns(30);
			location.addVetoableChangeListener(this);
			{
				JButton btnBrowse = new JButton(TextUtils.getText("browse"));
				mainPanel.add(btnBrowse, "4, 2");
				{
					JComboBox comboBox = new JComboBox();
					mainPanel.add(comboBox, "2, 4, fill, default");
					comboBox.setModel(new WorkspaceProfileListModel());
				}
				{
					JButton btnCreateNew = new JButton(TextUtils.getText("workspace.profile.new"));
					mainPanel.add(btnCreateNew, "4, 4");
				}
				btnBrowse.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						onShowButton();
					}
				});
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
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
			{
				JButton okButton = new JButton(TextUtils.getText("ok"));
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						onOkButton();
					}
				});
				okButton.setActionCommand("ok");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}

	public void addDirectoryOption(String text, JButton button) {
		JTextField position = new JTextField();
		position.setText(text);
		position.setColumns(30);
		mainPanel.add(position, "2, 4, fill, fill");

		// JButton btnPdf = new JButton("PDF");
		mainPanel.add(button, "4, 4, fill, fill");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onShowButton();
			}
		});

	}

	public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
		evt.getNewValue();
	}

	private class WorkspaceProfileListModel implements MutableComboBoxModel, Serializable {
		DefaultComboBoxModel internalModel;
		Vector<ProfileListObject> itemList;

		private static final long serialVersionUID = 1L;
		
		public WorkspaceProfileListModel() {
			itemList = new Vector<LocationDialog.ProfileListObject>();
			itemList.add(new ProfileListObject(WorkspacePreferences.WORKSPACE_PROFILE_DEFAULT, "<"+WorkspacePreferences.WORKSPACE_PROFILE_DEFAULT+"> profile"));
		}
		
		public void reload(String path) {
			this.internalModel = new DefaultComboBoxModel(itemList.toArray());
		}

		public int getSize() {
			return internalModel.getSize();
		}

		public Object getElementAt(int index) {
			return internalModel.getElementAt(index);
		}
		
		public Object getSelectedItem() {
	        if(internalModel.getSelectedItem() == null) {
	        	return internalModel.getElementAt(0); 
	        }
			return internalModel.getSelectedItem();
	    }

		public void setSelectedItem(Object anItem) {
			if(anItem == null) {
				internalModel.setSelectedItem(internalModel.getElementAt(0));
			}
			internalModel.setSelectedItem(anItem);
		}

		public void addListDataListener(ListDataListener l) {
			internalModel.addListDataListener(l);	
		}

		public void removeListDataListener(ListDataListener l) {
			internalModel.removeListDataListener(l);
		}

		public void addElement(Object obj) {
			internalModel.addElement(obj);			
		}

		public void removeElement(Object obj) {
			internalModel.removeElement(obj);			
		}

		public void insertElementAt(Object obj, int index) {
			internalModel.insertElementAt(obj, index);
		}

		public void removeElementAt(int index) {
			internalModel.removeElementAt(index);			
		}

	}

	public class ProfileListObject {
		private final String name;
		private final String displayName;
		
		public ProfileListObject(final String name, final String displayName) {
			this.name = name;
			this.displayName = displayName;
		}
		
		public String getName() {
			return this.name;
		}
		
		public String toString() {
			return displayName;
		}

	}
}
