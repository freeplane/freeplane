package org.freeplane.plugin.workspace.components.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.IResultProcessor;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.plugin.workspace.model.project.ProjectLoader;
import org.freeplane.plugin.workspace.nodes.ProjectRootNode;

import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class ImportProjectDialogPanel extends JPanel {
	private static final SimpleDateFormat format = new SimpleDateFormat("M/d/yy HH:mm");
	private static final long serialVersionUID = 1L;
	private JTextField txtProjectName;
	private JTextField txtProjectPath;
	private JComboBox projectVersions;
	private ProjectVersionsModel versionModel;
	private JLabel lblWarn;
	private JLabel lblProjectName;
	private Component confirmButton;
	
	

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public ImportProjectDialogPanel() {
		setPreferredSize(new Dimension(400, 180));
		setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(100dlu;min):grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:max(50dlu;pref)"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		JPanel panel = new JPanel();
		panel.setBorder(new MatteBorder(0, 0, 1, 0, (Color) new Color(0, 0, 0)));
		panel.setBackground(Color.WHITE);
		add(panel, "1, 1, 8, 2, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default:grow"),}));
		
		JLabel lblNewLabel = new JLabel(TextUtils.getText(ImportProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".help"));
		lblNewLabel.setVerticalAlignment(SwingConstants.TOP);
		panel.add(lblNewLabel, "2, 2");
		
		lblWarn = new JLabel(TextUtils.getText(ImportProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".warn1"));
		add(lblWarn, "2, 4, 5, 1");
		URL url = this.getClass().getResource("/images/16x16/dialog-warning-4.png");
		if(url != null) {
			lblWarn.setIcon(new ImageIcon(url));
		}
		lblWarn.setVisible(false);
				
		JLabel lblProjectPath = new JLabel(TextUtils.getText(ImportProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".path.label"));
		lblProjectPath.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblProjectPath, "2, 6, right, default");
		
		txtProjectPath = new JTextField();
		add(txtProjectPath, "4, 6, fill, default");
		txtProjectPath.setColumns(10);
		txtProjectPath.setEditable(false);
		
		JButton btnBrowse = new JButton("...");
		btnBrowse.setToolTipText(TextUtils.getText(ImportProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".button.tip"));
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				File home = URIUtils.getAbsoluteFile(getProjectPath());
				while(home != null && !home.exists()) {
					home = home.getParentFile();
				}
				JFileChooser chooser = new JFileChooser(home == null ? getDefaultProjectPath() : home.getAbsolutePath());
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileHidingEnabled(true);
				int response = chooser.showOpenDialog(ImportProjectDialogPanel.this);
				if(response == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					setProjectPath(file.getAbsolutePath());
				}
			}
		});
		add(btnBrowse, "6, 6");
		
		lblProjectName = new JLabel(TextUtils.getText(ImportProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".name.label"));
		lblProjectName.setEnabled(false);
		add(lblProjectName, "2, 8, right, default");
		
		projectVersions = new JComboBox();
		projectVersions.setModel(getComboBoxModel());
		projectVersions.setEnabled(false);
		add(projectVersions, "4, 8, 3, 1, fill, default");
		projectVersions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableConfirmation();
			}
		});
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	
	protected void setProjectPath(String path) {
		txtProjectPath.setText(path);
		updateProjectVersions();
	}

	@Override
	public void paint(Graphics g) {
		enableConfirmation();
		super.paint(g);
	}

	private void updateProjectVersions() {
		File home = new File(txtProjectPath.getText());
		getComboBoxModel().clear();
		
		File _data = new File(home, "_data");
		if(_data.exists()) {
			readVersions(_data);		
		}
		enableControlls(getComboBoxModel().getSize() > 0);
	}

	private void enableControlls(boolean enabled) {
		if(getComboBoxModel().getSize() > 0) {
			projectVersions.setSelectedIndex(0); 
		}
		lblWarn.setText(TextUtils.getText(ImportProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".warn1"));
		lblWarn.setVisible(!enabled);
		projectVersions.setEnabled(enabled);
		lblProjectName.setEnabled(enabled);
		enableConfirmation();
	}
	
	private void enableConfirmation() {
		if(confirmButton != null) {
			if(getComboBoxModel().getSelectedItem() == null) {
				confirmButton.setEnabled(false);
				if(projectVersions.isEnabled()) {
					lblWarn.setText(TextUtils.getText(ImportProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".warn2"));
					lblWarn.setVisible(true);
				}
			}
			else {
				if(WorkspaceController.getCurrentModel().getProject(getComboBoxModel().getSelectedItem().getProject().getProjectID()) != null) {
					lblWarn.setText(TextUtils.getText(ImportProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".warn3"));
					lblWarn.setVisible(true);
					confirmButton.setEnabled(false);					
				}
				else {
					confirmButton.setEnabled(true);
					lblWarn.setVisible(false);
				}
			}
			
		}
	}
	

	private void readVersions(File home) {
		for(File folder : home.listFiles(new FileFilter() {			
			public boolean accept(File pathname) {
				if(pathname.isDirectory()) {
					return true;
				}
				return false;
			}
			})) {
			
			File settings = new File(folder, "settings.xml");
			if(settings.exists()) {
				AWorkspaceProject project = AWorkspaceProject.create(folder.getName(), home.getParentFile().toURI());
				String item = new TempProjectLoader().getMetaInfo(project);
				if(item == null) {
					continue;
				}
				getComboBoxModel().addItem(new VersionItem(project, item, new Date(settings.lastModified())));
			}
		}
	}
	
	private ProjectVersionsModel getComboBoxModel() {
		if(this.versionModel == null) {
			this.versionModel = new ProjectVersionsModel();
		}
		return this.versionModel;
	}

	protected String getDefaultProjectPath() {
		File base = URIUtils.getAbsoluteFile(WorkspaceController.getDefaultProjectHome());
		return base.getAbsolutePath();
	}

	public String getProjectName() {
		return txtProjectName.getText().trim();
	}
	
	public AWorkspaceProject getProject() {
		VersionItem item = (VersionItem) projectVersions.getSelectedItem();
		if(item == null) {
			return null;
		}
		return item.getProject();
	}
	
	private URI getProjectPath() {
		if(txtProjectPath.getText().length()==0) {
			return null;
		}
		return new File(txtProjectPath.getText()).toURI();
	}
	
	public void setConfirmButton(Component comp) { 
		this.confirmButton = comp;
	}


	/***********************************************************************************
	 * INTERNAL TYPES
	 **********************************************************************************/
	
	class TempProjectLoader extends ProjectLoader {
		StringBuilder versionString;
		
		public String getMetaInfo(AWorkspaceProject project) {
			try {
				versionString = new StringBuilder();
				LOAD_RETURN_TYPE retType = this.loadProject(project);
				if(LOAD_RETURN_TYPE.EXISTING_PROJECT.equals(retType)) {
					return versionString.length() == 0 ? null : versionString.toString();
				}
			} catch (IOException e) {
				LogUtils.warn(e);
			}
			return null;
		}

		@Override
		public IResultProcessor getDefaultResultProcessor() {
			return new IResultProcessor() {
				public void process(AWorkspaceTreeNode parent, AWorkspaceTreeNode node) {
					if(node == null) {
						return;
					}
					if(node instanceof ProjectRootNode) {
						versionString.append(node.getName());
					}
					if(parent == null) {
						return;
					}						
					parent.addChildNode(node);
				}

				public void setProject(AWorkspaceProject project) {
				}
			};
		}
		
		
		
	}
	
	class ProjectVersionsModel implements ComboBoxModel {
		
		private List<VersionItem> items = new ArrayList<VersionItem>();
		private List<ListDataListener> listeners = new ArrayList<ListDataListener>();
		private int selectedIndex = -1;
		
		/***********************************************************************************
		 * CONSTRUCTORS
		 **********************************************************************************/

		/***********************************************************************************
		 * METHODS
		 **********************************************************************************/

		/***********************************************************************************
		 * REQUIRED METHODS FOR INTERFACES
		 **********************************************************************************/
		
		public int getSize() {
			return items.size();
		}

		public Object getElementAt(int index) {
			return items.get(index);
		}

		public void addListDataListener(ListDataListener l) {
			listeners.add(l);
		}

		public void removeListDataListener(ListDataListener l) {
			listeners.remove(l);
		}

		public void setSelectedItem(Object anItem) {
			int idx = items.indexOf(anItem);
			if(idx == -1) {
				throw new NoSuchElementException();
			}
			this.selectedIndex  = idx;
		}

		public VersionItem getSelectedItem() {
			if(this.selectedIndex == -1) {
				return null;
			}
			return items.get(selectedIndex);			
		}
		
		public void clear() {
			int endIdx = items.size();
			this.items.clear();
			fireItemsRemoved(0, endIdx);
		}
		
		private void validateSelection() {
			if(selectedIndex >= items.size()) {
				selectedIndex = items.size()-1;
			}
			
		}

		public void setSelectedIndex(int index) {
			if(index < 0 || index >= items.size()) {
				throw new IndexOutOfBoundsException();
			}
			this.selectedIndex  = index;
		}
		
		public void addItem(VersionItem item) {
			for(VersionItem it : items) {
				if(it.compareTo(item) < 0) {
					insertItem(item, items.indexOf(it));
					return;
				}
			}
			insertItem(item, items.size());
		}
		
		private void insertItem(VersionItem item, int index) {
			if(item == null) {
				return;
			}
			if(index < 0 || index > items.size()) {
				throw new IndexOutOfBoundsException();
			}
			items.add(index, item);
			fireItemsAdded(index, index);
		}
		
		public void removeItem(VersionItem anItem) {
			if(anItem == null) {
				return;
			}
			int idx = items.indexOf(anItem);
			if(idx == -1) {
				return;
			}
			removeItem(idx);
		}
		
		public VersionItem removeItem(int index) {
			if(index < 0 || index >= items.size()) {
				throw new IndexOutOfBoundsException();
			}
			VersionItem obj = items.remove(index);
			fireItemsRemoved(index, index);
			return obj;
		}

		protected void fireItemsAdded(int startIndex, int endIndex) {
			ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, startIndex, endIndex);
			for (int i = items.size()-1; i >= 0; i--) {
				listeners.get(i).intervalAdded(event);
			}			
		}
		
		protected void fireItemsRemoved(int startIndex, int endIndex) {
			validateSelection();
			ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, startIndex, endIndex);
			for (int i = items.size()-1; i >= 0; i--) {
				listeners.get(i).intervalRemoved(event);
			}			
		}
		
		protected void fireItemsChanged(int startIndex, int endIndex) {
			ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, startIndex, endIndex);
			for (int i = items.size()-1; i >= 0; i--) {
				listeners.get(i).contentsChanged(event);
			}			
		}
		
	}
	
	public class VersionItem implements Comparable<VersionItem> {
		
		private final AWorkspaceProject project;
		private final Date latestUse;
		private final String name;

		/***********************************************************************************
		 * CONSTRUCTORS
		 **********************************************************************************/

		public VersionItem(AWorkspaceProject prj, String name, Date version) {
			if(prj == null || name == null || version == null) {
				throw new IllegalArgumentException("NULLPointer");
			}
			this.project = prj;
			this.latestUse = version;
			this.name = name;
		}
		/***********************************************************************************
		 * METHODS
		 **********************************************************************************/
		
		public AWorkspaceProject getProject() {
			return project;
		}
		
		public String toString() {
			return this.name + " [" +format.format(latestUse) + "]"; 
		}
		
		/***********************************************************************************
		 * REQUIRED METHODS FOR INTERFACES
		 **********************************************************************************/
		
		public int compareTo(VersionItem o) {
			return (int) (latestUse.getTime()-o.latestUse.getTime());
		}		
	}

}
