package org.freeplane.plugin.workspace.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ListDataListener;

import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspacePreferences;
import org.freeplane.plugin.workspace.WorkspaceUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class WorkspaceChooserDialogPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;	
	private JTextField location;
	
	private JComboBox profileComboBox;
	private JButton btnCreateNew;

	
	public String getLocationPath() {
		return location.getText();
	}

	public String getProfileName() {
		return ((ProfileListObject) profileComboBox.getSelectedItem()).getName();
	}
	
	/**
	 * Create the dialog.
	 */
	private void onCreateNewProfile() {
		String profileName = JOptionPane.showInputDialog(this, TextUtils.getText("new_profile_name"), "");
		profileName = WorkspaceUtils.stripIllegalChars(profileName);

		if (profileName != null && profileName.length() > 0) {
			workspaceChange(this.location.getText(), profileName);
		}
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

		int retVal = fileChooser.showOpenDialog(this);
		if (retVal == JFileChooser.APPROVE_OPTION) {
			File selectedfile = fileChooser.getSelectedFile();
			this.location.setText(selectedfile.getPath());
			workspaceChange(this.location.getText());
		}
	}

	public WorkspaceChooserDialogPanel() {	
		{	
			this.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, },
					new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
			location = new JTextField();
			this.add(location, "2, 2, fill, fill");

			String currentLocation = WorkspaceController.getController().getPreferences().getWorkspaceLocation();
			if (currentLocation != null && currentLocation.length() > 0) {
				location.setText(currentLocation);
			}
			location.setColumns(30);
			{
				JButton btnBrowse = new JButton(TextUtils.getText("browse"));
				this.add(btnBrowse, "4, 2");

				{
					profileComboBox = new JComboBox();
					this.add(profileComboBox, "2, 4, fill, default");
					profileComboBox.setModel(new WorkspaceProfileListModel());					
					
				}
				{
					this.btnCreateNew = new JButton(TextUtils.getText("workspace.profile.new"));
					btnCreateNew.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							onCreateNewProfile();
						}
					});
					this.add(btnCreateNew, "4, 4");
				}
				if (currentLocation != null && currentLocation.length() > 0) {
					workspaceChange(this.location.getText());
				}
				if (location.getText().trim().length() == 0) {
					this.profileComboBox.setEnabled(false);
					this.btnCreateNew.setEnabled(false);
				}
				else {
					this.profileComboBox.setEnabled(true);
					this.btnCreateNew.setEnabled(true);
				}

				btnBrowse.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						onShowButton();
					}
				});
			}
		}
		
	}

	public void addDirectoryOption(String text, JButton button) {
		JTextField position = new JTextField();
		position.setText(text);
		position.setColumns(30);
		this.add(position, "2, 4, fill, fill");

		this.add(button, "4, 4, fill, fill");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onShowButton();
			}
		});

	}

	private void workspaceChange(final String newPath) {
		workspaceChange(newPath, null);
	}

	private void workspaceChange(final String newPath, final String newProfileName) {
		if (newPath != null && newPath.trim().length() > 0) {
			((WorkspaceProfileListModel) profileComboBox.getModel()).reload(newPath, newProfileName);
			this.btnCreateNew.setEnabled(true);
			this.profileComboBox.setEnabled(true);
		}
	}

	private class WorkspaceProfileListModel implements MutableComboBoxModel, Serializable {
		DefaultComboBoxModel internalModel;
		Vector<ProfileListObject> itemList;

		private static final long serialVersionUID = 1L;
		private final FileFilter profileFilter = new FileFilter() {
			public boolean accept(File pathname) {
				if (pathname.isDirectory() && !pathname.getName().equals("." + WorkspacePreferences.WORKSPACE_PROFILE_DEFAULT)
						&& pathname.getName().startsWith(".") && Arrays.asList(pathname.list()).contains("workspace.xml")) {
					return true;
				}
				return false;
			}
		};
		private Object selectedObject;

		public WorkspaceProfileListModel() {
			reload(null);
		}

		private void initProfileList(File workspaceBase) {
			if (workspaceBase.isDirectory()) {
				for (File folder : workspaceBase.listFiles(profileFilter)) {
					itemList.add(new ProfileListObject(folder.getName().substring(1), folder.getName().substring(1)));
					if (WorkspaceController.getController().getPreferences().getWorkspaceProfile()
							.equals(folder.getName().substring(1))) {
						selectedObject = itemList.elementAt(itemList.size() - 1);
					}
				}
			}
		}

		public void reload(String path) {
			reload(path, null);
		}

		public void reload(String path, String newProfileName) {
			itemList = new Vector<WorkspaceChooserDialogPanel.ProfileListObject>();
			itemList.add(new ProfileListObject(WorkspacePreferences.WORKSPACE_PROFILE_DEFAULT, "<"
					+ WorkspacePreferences.WORKSPACE_PROFILE_DEFAULT + "> profile"));
			selectedObject = itemList.elementAt(0);
			if (newProfileName != null) {
				itemList.add(new ProfileListObject(newProfileName, newProfileName));				
			}

			if (path != null) {
				File file = new File(path);
				if (file.exists()) {
					initProfileList(file);
				}
				else {
					// TODO: DOCEAR> do sth.
				}
			}
			this.internalModel = new DefaultComboBoxModel(itemList.toArray());
			if (newProfileName != null) {
				selectedObject = itemList.elementAt(1);
			}
			setSelectedItem(selectedObject);
		}

		public int getSize() {
			return internalModel.getSize();
		}

		public Object getElementAt(int index) {
			return internalModel.getElementAt(index);
		}

		public Object getSelectedItem() {
			if (internalModel.getSelectedItem() == null) {
				return internalModel.getElementAt(0);
			}
			return internalModel.getSelectedItem();
		}

		public void setSelectedItem(Object anItem) {
			if (anItem == null) {
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
