package org.freeplane.plugin.workspace.components.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.net.URI;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;

import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class NewProjectDialogPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private JTextField txtProjectName;
	private JTextField txtProjectPath;
	protected boolean manualChoice = false;	
	
	public NewProjectDialogPanel() {
		setPreferredSize(new Dimension(400, 160));
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(100dlu;min):grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:max(50dlu;pref)"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default"),}));
		
		JPanel panel = new JPanel();
		panel.setBorder(new MatteBorder(0, 0, 1, 0, (Color) new Color(0, 0, 0)));
		panel.setBackground(Color.WHITE);
		add(panel, "1, 1, 8, 2, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default:grow"),}));
		
		JLabel lblNewLabel = new JLabel(TextUtils.getText(NewProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".help"));
		lblNewLabel.setVerticalAlignment(SwingConstants.TOP);
		panel.add(lblNewLabel, "2, 2");
		
		JLabel lblProjectName = new JLabel(TextUtils.getText(NewProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".name.label"));
		lblProjectName.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblProjectName, "2, 4, right, default");
		
		txtProjectName = new JTextField();
		txtProjectName.setText(TextUtils.getText(NewProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".name.default"));
		add(txtProjectName, "4, 4, fill, default");
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
		add(lblProjectPath, "2, 6, right, default");
		
		txtProjectPath = new JTextField(getDefaultProjectPath(txtProjectName.getText()));
		setProjectPath(getDefaultProjectPath(getProjectName()));
		add(txtProjectPath, "4, 6, fill, default");
		txtProjectPath.setColumns(10);
		
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
		add(btnBrowse, "6, 6");
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

	public String getProjectName() {
		return txtProjectName.getText().trim();
	}
	
	public URI getProjectPath() {
		return new File(txtProjectPath.getText()).toURI();
	}

}
