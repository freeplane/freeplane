package org.docear.plugin.core;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.url.UrlManager;
import org.freeplane.plugin.workspace.WorkspaceController;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class LocationDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
		
	private JPanel mainPanel = new JPanel();
	private JTextField projectsLocation;
	private JTextField bibtexLocation;
	private JTextField literatureLocation;
	
	private final static String DOCUMENT_REPOSITORY_INIT_PATH = "workspace:/document_repository";	
	//TODO: DOCEAR: profile name
	private static String BIBTEX_PATH_INIT;
	private static String PROJECTS_PATH_INIT;
	
	
	
	private File workspaceLocation;
	/**
	 * Create the dialog.
	 */
	
	public static boolean allVariablesSet() {
		boolean variablesSet = true;		
		variablesSet = variablesSet && CoreConfiguration.repositoryPathObserver.getValue() != null;
		variablesSet = variablesSet && CoreConfiguration.referencePathObserver.getValue() != null;
		variablesSet = variablesSet && CoreConfiguration.projectPathObserver.getValue() != null;
		
		System.out.println("DOCEAR: allVariablesSet: "+variablesSet);
		
		return variablesSet;
	}
	
	private void browseLiterature() {
		JFileChooser fileChooser = UrlManager.getController().getFileChooser(null, true, true);
		if (literatureLocation != null) {
			File file = new File(this.literatureLocation.getText());
			if (file.exists()) {
				fileChooser.setSelectedFile(file);
			}
			else {
				fileChooser.setSelectedFile(this.workspaceLocation);
			}
		}

		int retVal = fileChooser.showOpenDialog(UITools.getFrame());
		if (retVal == JFileChooser.APPROVE_OPTION) {			
			File selectedfile = fileChooser.getSelectedFile();
			this.literatureLocation.setText(selectedfile.getPath());						
		}		
	}
	
	private void browseBibtex() {
		JFileChooser fileChooser = UrlManager.getController().getFileChooser(null, false, true);
		if (bibtexLocation != null) {
			File file = new File(this.bibtexLocation.getText());
			if (file.exists()) {
				fileChooser.setSelectedFile(file);
			}
			else {
				fileChooser.setSelectedFile(this.workspaceLocation);
			}
		}

		int retVal = fileChooser.showOpenDialog(UITools.getFrame());
		if (retVal == JFileChooser.APPROVE_OPTION) {			
			File selectedfile = fileChooser.getSelectedFile();
			this.bibtexLocation.setText(selectedfile.getPath());						
		}
	}
	
	private void browseProjects() {
		JFileChooser fileChooser = UrlManager.getController().getFileChooser(null, true, true);
		if (projectsLocation != null) {
			File file = new File(this.projectsLocation.getText());
			if (file.exists()) {
				fileChooser.setSelectedFile(file);
			}
			else {
				fileChooser.setSelectedFile(this.workspaceLocation);
			}
		}

		int retVal = fileChooser.showOpenDialog(UITools.getFrame());
		if (retVal == JFileChooser.APPROVE_OPTION) {			
			File selectedfile = fileChooser.getSelectedFile();
			this.projectsLocation.setText(selectedfile.getPath());						
		}
	}
	
	private void onCancelButton() {
		this.dispose();
	}
	
	private void onOkButton() {
		
		setLiteratureLocation(literatureLocation.getText());
		setBibtexLocation(bibtexLocation.getText());
		setProjectsLocation(projectsLocation.getText());
		WorkspaceController.getController().refreshWorkspace();
		//TODO: DOCEAR: create Docear-Workspace
		
		this.dispose();
	}
	
	
	public LocationDialog() {
		WorkspaceController workspaceController = WorkspaceController.getController();
		this.workspaceLocation = new File(workspaceController.getPreferences().getWorkspaceLocation());
		
		BIBTEX_PATH_INIT = "workspace:/."+workspaceController.getPreferences().getWorkspaceProfile()+"/bibtex.bib";
		PROJECTS_PATH_INIT = "workspace:/."+workspaceController.getPreferences().getWorkspaceProfile()+"/projects";
		
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
			
			{
				JLabel lblLiteraturelocation = new JLabel(TextUtils.getText("literature_location"));
				mainPanel.add(lblLiteraturelocation, "2, 2, fill, fill");
			}
			{
				{					
					literatureLocation = new JTextField();
					literatureLocation.setColumns(30);
					literatureLocation.setText(getLiteratureLocation());
					mainPanel.add(literatureLocation, "2, 4, fill, center");
				}
				{
					JButton btnBrowseLiterature = new JButton(TextUtils.getText("browse"));
					btnBrowseLiterature.addActionListener(new ActionListener() {						
						public void actionPerformed(ActionEvent e) {
							browseLiterature();
						}						
					});
					mainPanel.add(btnBrowseLiterature, "4, 4, right, center");
				}
				{
					JLabel lblBibtexFile = new JLabel(TextUtils.getText("bibtex_location"));
					mainPanel.add(lblBibtexFile, "2, 6, fill, fill");
				}
				{
					bibtexLocation = new JTextField();
					bibtexLocation.setColumns(30);
					bibtexLocation.setText(getBibtexLocation());
					mainPanel.add(bibtexLocation, "2, 8, fill, center");
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
					projectsLocation = new JTextField();
					projectsLocation.setColumns(30);
					projectsLocation.setText(getProjectsLocation());
					mainPanel.add(projectsLocation, "2, 12, fill, center");
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
		
	private String getLiteratureLocation() {	
		String location = (String) CoreConfiguration.repositoryPathObserver.getValue();
//		if (location == null) {
//			location = CoreConfiguration.
//		}
		return getPropertyLocation(DocearController.DOCUMENT_REPOSITORY_PATH_PROPERTY, DOCUMENT_REPOSITORY_INIT_PATH);
	}
	
	private void setLiteratureLocation(String location) {
		CoreConfiguration.repositoryPathObserver.setValue(location);
	}
	
	private String getBibtexLocation() {
		return getPropertyLocation(DocearController.BIBTEX_PATH_PROPERTY, BIBTEX_PATH_INIT);
	}
	
	private void setBibtexLocation(String location) {
		CoreConfiguration.referencePathObserver.setValue(location);
	}
	
	private String getProjectsLocation() {
		return getPropertyLocation(DocearController.PROJECTS_PATH_PROPERTY, PROJECTS_PATH_INIT);
	}
	
	private void setProjectsLocation(String location) {
		CoreConfiguration.projectPathObserver.setValue(location);
	}


	private String getPropertyLocation(String property, String init) {
		String location = ResourceController.getResourceController().getProperty(property, "");		
		if (location.length() > 0) {
			return location;
		}
		
		try {
			File f = new File(new URL(init).openConnection().getURL().toURI());
			return f.getPath();
		}
		catch (Exception e) {					
			e.printStackTrace();
		}
		
		return "";
	}

}
