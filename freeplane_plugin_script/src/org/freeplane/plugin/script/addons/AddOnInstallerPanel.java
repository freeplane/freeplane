package org.freeplane.plugin.script.addons;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.FreeplaneIconUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.main.addons.AddOnProperties;
import org.freeplane.main.addons.AddOnsController;
import org.freeplane.plugin.script.ScriptingEngine;
import org.freeplane.plugin.script.ScriptingPermissions;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class AddOnInstallerPanel extends JPanel {

	private ManageAddOnsPanel manageAddOnsPanel;
	private ManageAddOnsPanel manageThemesPanel;
	private JButton installButton;
	private JTextField urlField;

	public AddOnInstallerPanel(final ManageAddOnsPanel manageAddOnsPanel, ManageAddOnsPanel manageThemesPanel) {
		this.manageAddOnsPanel = manageAddOnsPanel;
		this.manageThemesPanel = manageThemesPanel;
		setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		//
		// Search
		//
		add(DefaultComponentFactory.getInstance().createSeparator(getTitleText("search")), "1, 2");
		add(createVisitAddOnPageButton(), "1, 4, left, default");
		add(Box.createVerticalStrut(20), "1, 6");
		//
		// Install from known location
		//
		add(DefaultComponentFactory.getInstance().createSeparator(getTitleText("install.from.known.location")), "1, 7");
		installButton = createInstallButton();
		urlField = createUrlField(installButton);
		final JButton selectFile = createFileChooser(urlField);
		installButton.addActionListener(createInstallActionListener());
		final Box box = Box.createHorizontalBox();
		box.add(urlField);
		box.add(selectFile);
		add(box, "1, 9");
		add(installButton, "1, 11, right, default");
//		setBackground(Color.WHITE);
	}

	private static String getText(String key, Object... parameters) {
		return ManageAddOnsDialog.getText(key, parameters);
	}

	private static String getTitleText(final String key) {
		final String titleStyle = "<html><b><font size='+1'>";
	    return titleStyle + getText(key);
    }

	private JButton createVisitAddOnPageButton() {
		try {
			final String addOnsUriString = TextUtils.removeTranslateComment(TextUtils.getText("addons.site"));
			// parse the URI on creation of the dialog to test the URI syntax early
			final URI addOnsUri = new URI(addOnsUriString);
			return UITools.createHtmlLinkStyleButton(addOnsUri, getText("visit.addon.page"));
		}
		catch (URISyntaxException ex) {
			// bad translation?
			throw new RuntimeException(ex);
		}
	}

	private JButton createInstallButton() {
	    final JButton installButton = new JButton();
		MenuBuilder.setLabelAndMnemonic(installButton, getText("install"));
		installButton.setEnabled(false);
		// FIXME: get rid of that
		installButton.setMargin(new Insets(0, 25, 0, 25));
		return installButton;
    }

	private ActionListener createInstallActionListener() {
	    return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final Controller controller = Controller.getCurrentController();
				try {
					LogUtils.info("installing add-on from " + urlField.getText());
					controller.getViewController().setWaitingCursor(true);
					final URL url = toURL(urlField.getText());
					setStatusInfo(getText("status.installing"));
					final ModeController modeController = controller.getModeController(MModeController.MODENAME);
					final MFileManager fileManager = (MFileManager) MFileManager.getController(modeController);
					MapModel newMap = new MMapModel();
					if (!fileManager.loadCatchExceptions(url, newMap)) {
					    LogUtils.warn("can not load " + url);
					    return;
					}
					controller.getModeController().getMapController().fireMapCreated(newMap);
					AddOnProperties addOn = (AddOnProperties) ScriptingEngine.executeScript(newMap.getRootNode(),
					    getInstallScriptSource(), ScriptingPermissions.getPermissiveScriptingPermissions());
					if (addOn != null) {
						setStatusInfo(getText("status.success", addOn.getName()));
						AddOnsController.getController().registerInstalledAddOn(addOn);
						final ManageAddOnsPanel managementPanel = addOn.isTheme() ? manageThemesPanel
						        : manageAddOnsPanel;
						managementPanel.getTableModel().addAddOn(addOn);
						urlField.setText("");
						((JTabbedPane)getParent()).setSelectedComponent(managementPanel);
						selectLastAddOn(managementPanel);
					}
				}
				catch (Exception ex) {
					UITools.errorMessage(getText("error", ex.toString()));
				}
				finally {
					controller.getViewController().setWaitingCursor(false);
				}
			}

			private String getInstallScriptSource() throws IOException {
				final ResourceController resourceController = ResourceController.getResourceController();
				final File scriptDir = new File(resourceController.getInstallationBaseDir(), "scripts");
				final File installScript = new File(scriptDir, "installScriptAddOn.groovy");
				if (!installScript.exists())
					throw new RuntimeException("internal error: installer not found at " + installScript);
				return FileUtils.slurpFile(installScript);
			}

			private URL toURL(String urlText) throws MalformedURLException {
				try {
					return new URL(urlText);
				}
				catch (Exception e2) {
					return new File(urlText).toURI().toURL();
				}
			}
		};
	}

	private void selectLastAddOn(JComponent managementPanel) {
		try {
			JTable table = findJTable(managementPanel);
			final int row = table.getModel().getRowCount() - 1;
			table.getSelectionModel().setSelectionInterval(row, row);
		}
		catch (Exception e) {
			LogUtils.warn("cannot select just installed add-on", e);
		}
	}

	private JTable findJTable(JComponent child) {
		for (Component component : child.getComponents()) {
			if (component instanceof JTable) {
				return (JTable) component;
			}
			else if (component instanceof JComponent) {
				final JTable findResult = findJTable((JComponent) component);
				if (findResult != null)
					return findResult;
			}
		}
		return null;
	}

	private JButton createFileChooser(final JTextField urlField) {
		final JButton selectFile = new JButton(getText("search.file"),
		    FreeplaneIconUtils.createImageIconByResourceKey("OpenAction.icon"));
		final JFileChooser fileChooser = new JFileChooser();
		selectFile.setToolTipText(getText("select.tooltip"));
		selectFile.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
		selectFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileChooser.showOpenDialog(urlField);
				final File selectedFile = fileChooser.getSelectedFile();
				if (selectedFile != null)
					urlField.setText(selectedFile.getAbsolutePath());
			}
		});
		return selectFile;
	}

	private JTextField createUrlField(final JButton install) {
		final JTextField urlField = new JTextField();
//		urlField.setColumns(100);
		urlField.setToolTipText(getText("install.tooltip"));
		urlField.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				updateImpl(e);
			}

			public void removeUpdate(DocumentEvent e) {
				updateImpl(e);
			}

			public void changedUpdate(DocumentEvent e) {
				updateImpl(e);
			}

			private void updateImpl(DocumentEvent e) {
				install.setEnabled(e.getDocument().getLength() > 0);
			}
		});
		urlField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					install.requestFocusInWindow();
					install.doClick();
				}
			}
		});
		return urlField;
	}
	
	JButton getInstallButton() {
    	return installButton;
    }

	JTextField getUrlField() {
    	return urlField;
    }

	private static void setStatusInfo(final String message) {
		Controller.getCurrentController().getViewController().out(message);
	}
}
