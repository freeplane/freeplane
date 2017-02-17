package org.freeplane.plugin.workspace.components.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;

import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class NewProjectDialogPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private JTextField txtProjectName;
	private JTextField txtProjectPath;
	protected boolean manualChoice = false;
	private JLabel lblWarn;
	private Component confirmButton;
	
	public NewProjectDialogPanel() {
		setPreferredSize(new Dimension(400, 160));
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
				RowSpec.decode("fill:max(30dlu;pref)"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default"),}));
		
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
		
		JLabel lblNewLabel = new JLabel(TextUtils.getText(NewProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".help"));
		lblNewLabel.setVerticalAlignment(SwingConstants.TOP);
		panel.add(lblNewLabel, "2, 2");
		
		lblWarn = new JLabel(TextUtils.getText(ImportProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".warn1"));
		add(lblWarn, "2, 4, 5, 1");
		URL url = this.getClass().getResource("/images/16x16/dialog-warning-4.png");
		if(url != null) {
			lblWarn.setIcon(new ImageIcon(url));
		}
		lblWarn.setVisible(false);
		
		JLabel lblProjectName = new JLabel(TextUtils.getText(NewProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".name.label"));
		lblProjectName.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblProjectName, "2, 6, right, default");
		
		txtProjectName = new JTextField();
		txtProjectName.setText(TextUtils.getText(NewProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".name.default"));
		add(txtProjectName, "4, 6, fill, default");
		txtProjectName.setColumns(10);
		txtProjectName.addKeyListener(new KeyListener() {			
			public void keyTyped(KeyEvent evt) {
				if(isBlackListed(evt.getKeyChar())) {
					evt.consume();
				}
			}
			
			public void keyReleased(KeyEvent evt) {
				if(isBlackListed(evt.getKeyChar())) {
					evt.consume();
				}
				else {
					if(!manualChoice) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								setProjectPath(getDefaultProjectPath(getProjectName()));
							}
						});
					} 
				}
			}
			
			public void keyPressed(KeyEvent evt) {
				if(isBlackListed(evt.getKeyChar())) {
					evt.consume();
				}
			}
		});
		
		JLabel lblProjectPath = new JLabel(TextUtils.getText(NewProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".path.label"));
		lblProjectPath.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblProjectPath, "2, 8, right, default");
		
		txtProjectPath = new JTextField(getDefaultProjectPath(txtProjectName.getText()));
		setProjectPath(getDefaultProjectPath(getProjectName()));
		add(txtProjectPath, "4, 8, fill, default");
		txtProjectPath.setColumns(10);
		txtProjectPath.addKeyListener(new KeyListener() {			
			public void keyTyped(KeyEvent evt) {
				if(isBlackListed(evt.getKeyChar())) {
					evt.consume();
				}
				else {
					manualChoice = true;
				}
			}
			
			public void keyReleased(KeyEvent evt) {
				if(isBlackListed(evt.getKeyChar())) {
					evt.consume();
				}
				else {
					manualChoice = true;
				}
				enableConfirmation();
			}
			
			public void keyPressed(KeyEvent evt) {
				if(isBlackListed(evt.getKeyChar())) {
					evt.consume();
				}
				else {
					manualChoice = true;
				}
			}
		});
		
		JButton btnBrowse = new JButton("...");
		btnBrowse.setToolTipText(TextUtils.getText(NewProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".button.tip"));
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File home = URIUtils.getAbsoluteFile(getProjectPath());
				while(home != null && !home.exists()) {
					home = home.getParentFile();
				}
				JFileChooser chooser = new JFileChooser(home == null ? getDefaultProjectPath(getProjectName()) : home.getAbsolutePath());
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileHidingEnabled(true);
				int response = chooser.showOpenDialog(NewProjectDialogPanel.this);
				if(response == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					setProjectPath(file.getAbsolutePath());
					manualChoice = true;
				}
			}
		});
		add(btnBrowse, "6, 8");
	}
	
	@Override
	public void paint(Graphics g) {
		enableConfirmation();
		super.paint(g);
	}
	
	public static boolean isBlackListed(char keyChar) {
		if(
			'%' == keyChar
			|| '!' == keyChar
			|| '$' == keyChar
			|| '§' == keyChar
			|| '&' == keyChar
			|| '\'' == keyChar
			|| '´' == keyChar
		) {
			return true;
		}
		return false;
	}

	protected void setProjectPath(String path) {
		txtProjectPath.setText(path);
	}

	protected String getDefaultProjectPath(String projectName) {
		File base = URIUtils.getAbsoluteFile(WorkspaceController.getDefaultProjectHome());
		if(projectName == null) {
			projectName = TextUtils.getText(NewProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".name.default");
		}
		File path = new File(base, projectName.trim());		
		int counter = 1;
		while(path.exists() && projectName.trim().length() > 0) {
			path = new File(base, projectName.trim()+" "+(counter++));
		}		
		return path.getAbsolutePath();
	}
	
	private void enableConfirmation() {
		if(confirmButton != null) {
			if(NameExistsInWorkspace(getProjectName())) {
				lblWarn.setText(TextUtils.getText(NewProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".warn1"));
				lblWarn.setVisible(true);
				confirmButton.setEnabled(false);					
			}
			else if(PathExistsInWorkspace(txtProjectPath.getText())) {
				lblWarn.setText(TextUtils.getText(NewProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".warn2"));
				lblWarn.setVisible(true);
				confirmButton.setEnabled(false);
			}
			else {
				confirmButton.setEnabled(true);
				lblWarn.setVisible(false);
			}	
		}
	}
	
	private boolean NameExistsInWorkspace(String name) {
		for(AWorkspaceProject project : WorkspaceController.getCurrentModel().getProjects()) {
			try {
				if(project.getProjectName().equals(name)) {
					return true;
				}
			} 
			catch (Exception e) {
				LogUtils.info(""+e.getMessage());
			}
		}
		return false;
	}
	
	private boolean PathExistsInWorkspace(String path) {
		for(AWorkspaceProject project : WorkspaceController.getCurrentModel().getProjects()) {
			try {
				if(URIUtils.getFile(project.getProjectHome()).getAbsolutePath().equals(new File(path).getAbsolutePath())) {
					return true;
				}
			} 
			catch (Exception e) {
				LogUtils.info(""+e.getMessage());
			}
		}
		return false;
	}

	public String getProjectName() {
		return txtProjectName.getText().trim();
	}
	
	public URI getProjectPath() {
		return new File(txtProjectPath.getText()).toURI();
	}
	
	public void setConfirmButton(Component comp) { 
		this.confirmButton = comp;
	}

}
