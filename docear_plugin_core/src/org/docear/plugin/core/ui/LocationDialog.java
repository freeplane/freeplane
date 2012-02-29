package org.docear.plugin.core.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.workspace.node.config.NodeAttributeObserver;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
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

	/**
	 * Create the dialog.
	 */

	public static void showWorkspaceChooserDialog() {
		showWorkspaceChooserDialog(true);
	}

	public static void showWorkspaceChooserDialog(boolean useDefaults) {
		LocationDialog dialog = new LocationDialog(useDefaults);

		JOptionPane.showMessageDialog(UITools.getFrame(), dialog,
				TextUtils.getRawText("docear_initialization"),
				JOptionPane.PLAIN_MESSAGE);

		dialog.onOkButton();
	}

	public static boolean allVariablesSet() {
		boolean variablesSet = true;
		variablesSet = variablesSet
				&& CoreConfiguration.repositoryPathObserver.getUri() != null;
		variablesSet = variablesSet
				&& CoreConfiguration.referencePathObserver.getUri() != null;
		variablesSet = variablesSet
				&& CoreConfiguration.projectPathObserver.getUri() != null;

		return variablesSet;
	}

	private void onOkButton() {
		if (chckbxUseDefaults.isSelected()) {
			setLiteratureLocation(WorkspaceUtils.resolveURI(
					URI.create(LITERATURE_REPOSITORY_INIT_PATH)).getPath());
			setBibtexLocation(WorkspaceUtils.resolveURI(
					URI.create(BIBTEX_PATH_INIT)).getPath());
			setProjectsLocation(WorkspaceUtils.resolveURI(
					URI.create(PROJECTS_PATH_INIT)).getPath());
		} else {
			setLiteratureLocation(literatureLocation.getText());
			setBibtexLocation(bibtexLocation.getText());
			setProjectsLocation(projectsLocation.getText());
		}
		WorkspaceController.getController().refreshWorkspace();
		// TODO: DOCEAR: create Docear-Workspace

	}

	public LocationDialog(boolean useDefaults) {
		WorkspaceController workspaceController = WorkspaceController
				.getController();

		BIBTEX_PATH_INIT = "workspace:/"
				+ workspaceController.getPreferences()
						.getWorkspaceProfileHome() + "/docear.bib";
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
					FormFactory.PREF_COLSPEC,},
				new RowSpec[] {
					RowSpec.decode("fill:4dlu"),
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					RowSpec.decode("fill:2dlu"),
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,}));

			{
				JLabel lblLiteraturelocation = new JLabel(
						TextUtils.getText("literature_location"));
				mainPanel.add(lblLiteraturelocation, "2, 4, fill, fill");
			}
			{
				{
					literatureLocation = new LocationDialogPanel(
							getLiteratureLocation(), true);
					mainPanel.add(literatureLocation, "4, 4, fill, center");
				}
				{
					JLabel lblBibtexFile = new JLabel(
							TextUtils.getText("bibtex_location"));
					mainPanel.add(lblBibtexFile, "2, 6, fill, fill");
				}
				{
					FileFilter bibFilter = new FileFilter() {
						public String getDescription() {
							return "*.bib ("
									+ TextUtils
											.getText("locationdialog.filefilter.bib")
									+ ")";
						}

						public boolean accept(File f) {
							return (f.isDirectory() || f.getName().endsWith(
									".bib"));
						}
					};
					bibtexLocation = new LocationDialogPanel(
							getBibtexLocation(), false, bibFilter);
					bibtexLocation.setExplanation(new JHyperlink(TextUtils
							.getText("bibtex_mendeley_help"), TextUtils
							.getText("bibtex_mendeley_help_uri")));
					mainPanel.add(bibtexLocation, "4, 6, fill, center");
				}
				{
					JLabel lblProjectsLocation = new JLabel(
							TextUtils.getText("projects_location"));
					mainPanel.add(lblProjectsLocation, "2, 8, fill, fill");
				}
				{
					projectsLocation = new LocationDialogPanel(
							getProjectsLocation(), true);
					mainPanel.add(projectsLocation, "4, 8, fill, center");
				}
			}
			{
				chckbxUseDefaults = new JCheckBox(
						TextUtils.getText("library_path_use_defaults"),
						useDefaults);
				chckbxUseDefaults.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						if (chckbxUseDefaults.isSelected()) {
							setPathsEnabled(false);
						} else {
							setPathsEnabled(true);
						}

					}
				});				
				JPanel chkcbxPanel = new JPanel();
				chkcbxPanel.setLayout(new FormLayout(new ColumnSpec[] {
						FormFactory.RELATED_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC}, new RowSpec[] {						
						FormFactory.DEFAULT_ROWSPEC,
						}));
				
				JHyperlink hyperlink = new JHyperlink(TextUtils.getText("library_paths_help"), TextUtils.getText("library_paths_help_uri"));
				chkcbxPanel.add(hyperlink, "2, 1, fill, center");
				mainPanel.add(chckbxUseDefaults, "2, 2, fill, center");
				mainPanel.add(chkcbxPanel, "4, 2, fill, center");

				if (useDefaults) {
					setPathsEnabled(false);
				}
			}
		}

	}

	private void setPathsEnabled(boolean b) {
		literatureLocation.setEnabled(b);
		bibtexLocation.setEnabled(b);
		projectsLocation.setEnabled(b);
	}

	private URI getLiteratureLocation() {
		return getPropertyLocation(CoreConfiguration.repositoryPathObserver,
				LITERATURE_REPOSITORY_INIT_PATH);
	}

	private void setLiteratureLocation(String location) {
		URI uri = WorkspaceUtils.getWorkspaceRelativeURI(new File(location));
		CoreConfiguration.repositoryPathObserver.setUri(uri);
	}

	private URI getBibtexLocation() {
		return getPropertyLocation(CoreConfiguration.referencePathObserver,
				BIBTEX_PATH_INIT);
	}

	private void setBibtexLocation(String location) {
		URI uri = WorkspaceUtils.getWorkspaceRelativeURI(new File(location));
		CoreConfiguration.referencePathObserver.setUri(uri);
	}

	private URI getProjectsLocation() {
		return getPropertyLocation(CoreConfiguration.projectPathObserver,
				PROJECTS_PATH_INIT);
	}

	private void setProjectsLocation(String location) {
		URI uri = WorkspaceUtils.getWorkspaceRelativeURI(new File(location));
		CoreConfiguration.projectPathObserver.setUri(uri);
	}

	private URI getPropertyLocation(
			NodeAttributeObserver nodeAttributeObserver, String init) {
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

}
