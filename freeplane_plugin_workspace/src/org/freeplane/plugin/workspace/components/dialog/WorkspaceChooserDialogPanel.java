package org.freeplane.plugin.workspace.components.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
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
	private JLabel label;
	private JLabel label_1;
	
	
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
				fileChooser.setCurrentDirectory(file.getParentFile());
				fileChooser.setSelectedFile(file);
				//fileChooser.setSelectedFile(file);
			}
			else {
				while((file = file.getParentFile()) != null && !file.exists()) {
				}
				if(file == null) {
					fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
				}
				else {
					fileChooser.setCurrentDirectory(file);
				}
			}
		}

		int retVal = fileChooser.showOpenDialog(UITools.getFrame());
		if (retVal == JFileChooser.APPROVE_OPTION) {
			File selectedfile = fileChooser.getSelectedFile();
			this.location.setText(selectedfile.getPath());
			workspaceChange(this.location.getText());
		}
	}

	public WorkspaceChooserDialogPanel() {
		new WorkspaceChooserDialogPanel("");
	}
	
	public WorkspaceChooserDialogPanel(String defaultLocation) {
		{	
			this.setLayout(new FormLayout(new ColumnSpec[] {
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("default:grow"),
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC,},
				new RowSpec[] {
					FormFactory.RELATED_GAP_ROWSPEC,
					RowSpec.decode("bottom:default"),
					FormFactory.RELATED_GAP_ROWSPEC,
					RowSpec.decode("top:20dlu"),
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,}));
			{
				label = new JLabel(TextUtils.getText("workspace_chooser_help"));
				add(label, "2, 2, 3, 1, default, top");
			}
			{
				String text = TextUtils.format("workspace_chooser_help_2", System.getProperty("user.home")+File.separator+ResourceController.getResourceController().getProperty("ApplicationName","Freeplane").toLowerCase());
				label_1 = new JLabel((Compat.isWindowsOS() ? text: text.replace("\\", "/")));
				add(label_1, "2, 4");
			}
			location = new JTextField();
			this.add(location, "2, 6, fill, fill");

			String currentLocation = WorkspaceController.getController().getPreferences().getWorkspaceLocation();
			if (currentLocation != null && currentLocation.length() > 0) {
				location.setText(currentLocation);
			}
			else {
				location.setText(defaultLocation);
			}
			location.setColumns(30);
			{
				JButton btnBrowse = new JButton(TextUtils.getText("browse"));
				this.add(btnBrowse, "4, 6");

				{
					profileComboBox = new JComboBox();
					this.add(profileComboBox, "2, 8, fill, default");
					profileComboBox.setModel(new WorkspaceProfileListModel(currentLocation == null ? defaultLocation : currentLocation));					
					
				}
				{
					this.btnCreateNew = new JButton(TextUtils.getText("workspace.profile.new"));
					btnCreateNew.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							onCreateNewProfile();
						}
					});
					this.add(btnCreateNew, "4, 8");
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

	private class WorkspaceProfileListModel extends DefaultComboBoxModel {
		

		private static final long serialVersionUID = 1L;
		private final FileFilter profileFilter = new FileFilter() {
			public boolean accept(File pathname) {
				if (pathname.isDirectory() 
						&& Arrays.asList(pathname.list()).contains("workspace.xml")
						&& !pathname.getName().equals(WorkspacePreferences.WORKSPACE_PROFILE_DEFAULT)
						//&& pathname.getParentFile().getPath().endsWith(WorkspaceController.getController().getPreferences().getWorkspaceProfilesRoot()) 
						) {
					return true;
				}
				return false;
			}
		};
		private Object selectedObject;

		public WorkspaceProfileListModel(String currentLocation) {
			reload(currentLocation);
		}

		private void initProfileList(File workspaceBase) {
			if (workspaceBase.isDirectory()) {
				File profileDir = new File(workspaceBase, WorkspaceController.getController().getPreferences().getWorkspaceProfilesRoot());
				if(profileDir == null || (!profileDir.exists()&&!profileDir.mkdirs())) {
					return;
				}
				for (File folder : profileDir.listFiles(profileFilter)) {
					addElement(new ProfileListObject(folder.getName(), folder.getName()));
					if (WorkspaceController.getController().getPreferences().getWorkspaceProfile().endsWith(folder.getName())) {
						selectedObject = getElementAt(getSize() - 1);
					}
				}
			}
		}

		public void reload(String path) {
			reload(path, null);
		}

		public void reload(String path, String newProfileName) {
			while(getSize() > 0) {
				this.removeElementAt(0);
			}
			addElement(new ProfileListObject(WorkspacePreferences.WORKSPACE_PROFILE_DEFAULT, "<"
					+ WorkspacePreferences.WORKSPACE_PROFILE_DEFAULT + "> profile"));
			selectedObject = getElementAt(0);
			if (newProfileName != null) {
				addElement(new ProfileListObject(newProfileName, newProfileName));				
			}

			if (path != null) {
				File file = new File(path);
				if (file.exists()) {
					initProfileList(file);
				}
				else {
					// TODO: DOCEAR - do sth.
				}
			}
			if (newProfileName != null) {
				selectedObject = getElementAt(1);
			}
			setSelectedItem(selectedObject);
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
