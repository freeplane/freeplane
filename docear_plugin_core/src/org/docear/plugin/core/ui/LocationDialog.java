package org.docear.plugin.core.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FileUtils;
import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.io.ReplacingInputStream;
import org.docear.plugin.core.workspace.node.config.NodeAttributeObserver;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.swingplus.JHyperlink;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class LocationDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();

	private JPanel mainPanel = new JPanel();
	private LocationDialogPanel projectsLocation;
	private LocationDialogPanel bibtexLocation;
	private LocationDialogPanel literatureLocation;

	private final static String LITERATURE_REPOSITORY_INIT_PATH = "workspace:/literature_repository";
	private static String BIBTEX_PATH_INIT;
	private static String PROJECTS_PATH_INIT;

	private JCheckBox chckbxUseDefaults;
	private boolean useDemo = false;
	private JCheckBox chkBoxUseDemo;

	/**
	 * Create the dialog.
	 */

	public static void showWorkspaceChooserDialog() {
		showWorkspaceChooserDialog(true, true);
	}

	public static void showWorkspaceChooserDialog(boolean useDefaults, boolean showDemoSelector) {
		LocationDialog dialog = new LocationDialog(useDefaults, showDemoSelector);

		if(useDefaults) {
			JOptionPane.showMessageDialog(UITools.getFrame(), dialog, TextUtils.getRawText("docear_initialization"), JOptionPane.PLAIN_MESSAGE);
			dialog.onOkButton();
		}
		else {
			if(JOptionPane.showOptionDialog(UITools.getFrame(), dialog, TextUtils.getRawText("docear_initialization"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION) {
				dialog.onOkButton();
			}
		}
	}

	public static boolean allVariablesSet() {
		boolean variablesSet = true;
		variablesSet = variablesSet && CoreConfiguration.repositoryPathObserver.getUri() != null;
		variablesSet = variablesSet && CoreConfiguration.referencePathObserver.getUri() != null;
		variablesSet = variablesSet && CoreConfiguration.projectPathObserver.getUri() != null;

		return variablesSet;
	}

	private void onOkButton() {
		String bibPath = "";
		if (chckbxUseDefaults.isSelected()) {
			setLiteratureLocation(WorkspaceUtils.resolveURI(URI.create(LITERATURE_REPOSITORY_INIT_PATH)).getPath());
			setProjectsLocation(WorkspaceUtils.resolveURI(URI.create(PROJECTS_PATH_INIT)).getPath());
			bibPath = WorkspaceUtils.resolveURI(URI.create(BIBTEX_PATH_INIT)).getPath();
		} else {
			setLiteratureLocation(literatureLocation.getText());
			setProjectsLocation(projectsLocation.getText());			
			bibPath = (bibtexLocation.getText());
		}
		
		if(demoEnabled()) {
			copyDemoFiles(new File(bibPath));
		}
		
		setBibtexLocation(bibPath);
		
		WorkspaceController.getController().refreshWorkspace();
		// TODO: DOCEAR: create Docear-Workspace

	}

	public LocationDialog(boolean useDefaults, boolean showDemoSelector) {
		WorkspaceController workspaceController = WorkspaceController.getController();

		BIBTEX_PATH_INIT = "workspace:/" + workspaceController.getPreferences().getWorkspaceProfileHome() + "/docear.bib";
		PROJECTS_PATH_INIT = "workspace:/projects";

		this.setLayout(new BorderLayout());
		this.add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			mainPanel = new JPanel();
			contentPanel.add(mainPanel);
			mainPanel.setLayout(new FormLayout(new ColumnSpec[] {
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.PREF_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("pref:grow"),},
				new RowSpec[] {
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					RowSpec.decode("default:grow"),
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,}));

			{
				JPanel chkcbxPanel = new JPanel();
				chkcbxPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC },
						new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, }));			
				
				chckbxUseDefaults = new JCheckBox(TextUtils.getText("library_path_use_defaults"), useDefaults);
				chckbxUseDefaults.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						if (chckbxUseDefaults.isSelected()) {
							setPathsEnabled(false);
						} else {
							setPathsEnabled(true);
						}
					}
				});
				mainPanel.add(chckbxUseDefaults, "2, 2, fill, center");
				
				JHyperlink hyperlink = new JHyperlink(TextUtils.getText("library_paths_help"), TextUtils.getText("library_paths_help_uri"));
				chkcbxPanel.add(hyperlink, "2, 1, fill, center");				
				mainPanel.add(chkcbxPanel, "4, 2, fill, center");
			}
			{
				JPanel borderedPanel = new JPanel();
				borderedPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				mainPanel.add(borderedPanel, "2, 4, 3, 1, fill, fill");
				borderedPanel.setLayout(new FormLayout(new ColumnSpec[] {
						FormFactory.RELATED_GAP_COLSPEC,
						FormFactory.PREF_COLSPEC,
						FormFactory.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("pref:grow"),},
					new RowSpec[] {
						RowSpec.decode("top:default"),
						RowSpec.decode("top:default"),
						RowSpec.decode("top:default"),
						FormFactory.DEFAULT_ROWSPEC,}));
				{
					JLabel lblLiteraturelocation = new JLabel(TextUtils.getText("literature_location"));
					borderedPanel.add(lblLiteraturelocation, "2, 1, fill, fill");
				
					literatureLocation = new LocationDialogPanel(getLiteratureLocation(), true);
					borderedPanel.add(literatureLocation, "4, 1, fill, center");
				}
				{
					JLabel lblBibtexFile = new JLabel(TextUtils.getText("bibtex_location"));
					borderedPanel.add(lblBibtexFile, "2, 3, fill, fill");
				
					FileFilter bibFilter = new FileFilter() {
						public String getDescription() {
							return "*.bib (" + TextUtils.getText("locationdialog.filefilter.bib") + ")";
						}
	
						public boolean accept(File f) {
							return (f.isDirectory() || f.getName().endsWith(".bib"));
						}
					};
					bibtexLocation = new LocationDialogPanel(getBibtexLocation(), false, bibFilter);
					bibtexLocation.setExplanation(new JHyperlink(TextUtils.getText("bibtex_mendeley_help"), TextUtils.getText("bibtex_mendeley_help_uri")));
					borderedPanel.add(bibtexLocation, "4, 3, fill, center");
				}
				{
					JLabel lblProjectsLocation = new JLabel(TextUtils.getText("projects_location"));
					borderedPanel.add(lblProjectsLocation, "2, 4, fill, fill");
				
					projectsLocation = new LocationDialogPanel(getProjectsLocation(), true);
					borderedPanel.add(projectsLocation, "4, 4, fill, center");
				}
			
				{				
					if (showDemoSelector) {
						setDemoEnabled(useDefaults);
						chkBoxUseDemo = new JCheckBox(TextUtils.getText("library.paths.demo"), demoEnabled());
						chkBoxUseDemo.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								if (chkBoxUseDemo.isSelected()) {
									setDemoEnabled(true);
								} else {
									setDemoEnabled(false);
								}
							}
						});
						mainPanel.add(chkBoxUseDemo, "2, 6, 3, 1, fill, center");
					}
	
					if (useDefaults) {
						setPathsEnabled(false);
					}
				}
			}
		}

	}

	private void setDemoEnabled(boolean enabled) {
		this.useDemo = enabled;
	}

	public boolean demoEnabled() {
		return /*chckbxUseDefaults.isSelected() &&*/ this.useDemo;
	}

	private void setPathsEnabled(boolean b) {
		literatureLocation.setEnabled(b);
		bibtexLocation.setEnabled(b);
		projectsLocation.setEnabled(b);
		//chkBoxUseDemo.setEnabled(!b);
	}

	private URI getLiteratureLocation() {
		return getPropertyLocation(CoreConfiguration.repositoryPathObserver, LITERATURE_REPOSITORY_INIT_PATH);
	}

	private void setLiteratureLocation(String location) {
		URI uri = WorkspaceUtils.getWorkspaceRelativeURI(new File(location));
		CoreConfiguration.repositoryPathObserver.setUri(uri);
	}

	private URI getBibtexLocation() {
		return getPropertyLocation(CoreConfiguration.referencePathObserver, BIBTEX_PATH_INIT);
	}

	private void setBibtexLocation(String location) {
		URI uri = WorkspaceUtils.getWorkspaceRelativeURI(new File(location));
		CoreConfiguration.referencePathObserver.setUri(uri);
	}

	private URI getProjectsLocation() {
		return getPropertyLocation(CoreConfiguration.projectPathObserver, PROJECTS_PATH_INIT);
	}

	private void setProjectsLocation(String location) {
		URI uri = WorkspaceUtils.getWorkspaceRelativeURI(new File(location));
		CoreConfiguration.projectPathObserver.setUri(uri);
	}

	private URI getPropertyLocation(NodeAttributeObserver nodeAttributeObserver, String init) {
		URI uri = nodeAttributeObserver.getUri();
		if (uri == null) {
			try {
				uri = new URI(init);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}

		return uri;
	}
	
	private void copyDemoFiles(File bibPath) {
		Map<String, String> replaceMapping = new HashMap<String, String>();
		replaceMapping.put("@LITERATURE_REPO_DEMO@", CoreConfiguration.repositoryPathObserver.getUri().toString()+"/Example%20PDFs");
		URI relativeBibURI = LinkController.toLinkTypeDependantURI(WorkspaceUtils.getProfileBaseFile(), WorkspaceUtils.resolveURI(CoreConfiguration.repositoryPathObserver.getUri()), LinkController.LINK_RELATIVE_TO_MINDMAP);
		replaceMapping.put("@LITERATURE_BIB_DEMO@", relativeBibURI.toString()+"/Example PDFs");
		
		boolean created = createAndCopy(new File(WorkspaceUtils.getProfileBaseFile(),"library/incoming.mm"), "/demo/template_incoming.mm", replaceMapping);
		createAndCopy(new File(WorkspaceUtils.getProfileBaseFile(),"library/literature_and_annotations.mm"), "/demo/template_litandan.mm", replaceMapping);
		createAndCopy(new File(WorkspaceUtils.getProfileBaseFile(),"library/my_publications.mm"), "/demo/template_mypubs.mm", replaceMapping);
		createAndCopy(new File(WorkspaceUtils.getProfileBaseFile(),"library/temp.mm"), "/demo/template_temp.mm", created, replaceMapping);
		createAndCopy(new File(WorkspaceUtils.getProfileBaseFile(),"library/trash.mm"), "/demo/template_trash.mm", created, replaceMapping);
		if(!bibPath.exists()) {
			createAndCopy(bibPath, "/demo/docear_example.bib", replaceMapping);
		}
		
		createAndCopy(new File(WorkspaceUtils.resolveURI(CoreConfiguration.projectPathObserver.getUri()), "Example Project/My New Paper.mm"), "/demo/docear_example_project/My New Paper.mm",replaceMapping);
		
		createAndCopy(new File(WorkspaceUtils.resolveURI(CoreConfiguration.repositoryPathObserver.getUri()), "Example PDFs/Academic Search Engine Optimization (ASEO) -- Optimizing Scholarly Literature for Google Scholar and Co.pdf"), "/demo/docear_example_pdfs/Academic Search Engine Optimization (ASEO) -- Optimizing Scholarly Literature for Google Scholar and Co.pdf");
		createAndCopy(new File(WorkspaceUtils.resolveURI(CoreConfiguration.repositoryPathObserver.getUri()), "Example PDFs/Academic search engine spam and Google Scholars resilience against it.pdf"), "/demo/docear_example_pdfs/Academic search engine spam and Google Scholars resilience against it.pdf");
		createAndCopy(new File(WorkspaceUtils.resolveURI(CoreConfiguration.repositoryPathObserver.getUri()), "Example PDFs/An Exploratory Analysis of Mind Maps.pdf"), "/demo/docear_example_pdfs/An Exploratory Analysis of Mind Maps.pdf");
		createAndCopy(new File(WorkspaceUtils.resolveURI(CoreConfiguration.repositoryPathObserver.getUri()), "Example PDFs/Docear -- An Academic Literature Suite.pdf"), "/demo/docear_example_pdfs/Docear -- An Academic Literature Suite.pdf");
		createAndCopy(new File(WorkspaceUtils.resolveURI(CoreConfiguration.repositoryPathObserver.getUri()), "Example PDFs/Google Scholar's Ranking Algorithm -- An Introductory Overview.pdf"), "/demo/docear_example_pdfs/Google Scholar's Ranking Algorithm -- An Introductory Overview.pdf");
		createAndCopy(new File(WorkspaceUtils.resolveURI(CoreConfiguration.repositoryPathObserver.getUri()), "Example PDFs/Google Scholar's Ranking Algorithm -- The Impact of Citation Counts.pdf"), "/demo/docear_example_pdfs/Google Scholar's Ranking Algorithm -- The Impact of Citation Counts.pdf");
		createAndCopy(new File(WorkspaceUtils.resolveURI(CoreConfiguration.repositoryPathObserver.getUri()), "Example PDFs/Information Retrieval on Mind Maps -- What could it be good for.pdf"), "/demo/docear_example_pdfs/Information Retrieval on Mind Maps -- What could it be good for.pdf");
		createAndCopy(new File(WorkspaceUtils.resolveURI(CoreConfiguration.repositoryPathObserver.getUri()), "Example PDFs/Mr. DLib -- A Machine Readable Digital Library.pdf"), "/demo/docear_example_pdfs/Mr. DLib -- A Machine Readable Digital Library.pdf");
	}
	
	private boolean createAndCopy(File file, String resourcePath) {
		return createAndCopy(file, resourcePath, false, null);
	}
	
	private boolean createAndCopy(File file, String resourcePath,final Map<String, String> replaceMap) {
		return createAndCopy(file, resourcePath, false, replaceMap);
	}
	
	private boolean createAndCopy(File file, String resourcePath, boolean force,final Map<String, String> replaceMap) {
		try {
			if(!file.exists() || force) {
				createFile(file);
				InputStream is = CoreConfiguration.class.getResourceAsStream(resourcePath);
				if(replaceMap == null) {
					FileUtils.copyInputStreamToFile(is, file);
				}
				else {
					FileUtils.copyInputStreamToFile(new ReplacingInputStream(replaceMap, is), file);
				}
				return true;
			}			
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}	
		return false;
	}

	/**
	 * @param file
	 * @throws IOException
	 */
	private void createFile(File file) throws IOException {
		if(!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
			return;
		}
		file.createNewFile();
	}

}
