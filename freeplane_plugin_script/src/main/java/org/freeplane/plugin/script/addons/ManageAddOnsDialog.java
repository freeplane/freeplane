package org.freeplane.plugin.script.addons;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.main.addons.AddOnProperties;

public class ManageAddOnsDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private AddOnInstallerPanel addOnInstallerPanel;
	private JTabbedPane tabbedPane;

	public ManageAddOnsDialog(final List<AddOnProperties> addOns) {
		super((Frame) UITools.getMenuComponent(), TextUtils.getText("ManageAddOnsAction.text"), true);
		// stolen from FileRevisionsDialog - no idea if actually needed
		if (getOwner() != null) {
			final Window[] ownedWindows = getOwner().getOwnedWindows();
			for (Window w : ownedWindows) {
				if (w.isVisible()) {
					w.toBack();
				}
			}
		}
		tabbedPane = new JTabbedPane();
		tabbedPane.setPreferredSize(getPreferredSizeForWindow());
		final ManageAddOnsPanel manageAddOnsPanel = new ManageAddOnsPanel(filterNonThemes(addOns));
		final ManageAddOnsPanel manageThemesPanel = new ManageAddOnsPanel(filterThemes(addOns));
		addOnInstallerPanel = new AddOnInstallerPanel(manageAddOnsPanel, manageThemesPanel);
		tabbedPane.addTab(getText("tab.install"), createIcon("/images/install_addons.svg"), addOnInstallerPanel,
		    getText("tab.install.tooltip"));
		tabbedPane.addTab(getText("tab.manage"), createIcon("/images/manage_addons.png"), manageAddOnsPanel,
		    getText("tab.manage.tooltip"));
		tabbedPane.addTab(getText("tab.manage.themes"), createIcon("/images/manage_themes.png"), manageThemesPanel,
		    getText("tab.manage.themes.tooltip"));
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton closeButton = new JButton();
				LabelAndMnemonicSetter.setLabelAndMnemonic(closeButton, TextUtils.getRawText("close_btn"));
				closeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				buttonPane.add(closeButton);
			}
		}
		pack();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		UITools.addEscapeActionToDialog(this);
	}

	private List<AddOnProperties> filterThemes(final List<AddOnProperties> addOns) {
		final ArrayList<AddOnProperties> result = new ArrayList<AddOnProperties>(addOns.size());
		for (AddOnProperties addOn : addOns) {
			if (addOn.isTheme())
				result.add(addOn);
		}
		return result;
	}

	private List<AddOnProperties> filterNonThemes(final List<AddOnProperties> addOns) {
		final ArrayList<AddOnProperties> result = new ArrayList<AddOnProperties>(addOns.size());
		for (AddOnProperties addOn : addOns) {
			if (!addOn.isTheme())
				result.add(addOn);
		}
		return result;
	}

	private ImageIcon createIcon(String resource) {
		return new ImageIcon(ResourceController.getResourceController().getResource(resource));
	}

	private Dimension getPreferredSizeForWindow() {
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		return new Dimension((int) screenSize.getWidth() * 4 / 5, (int) screenSize.getHeight() * 2 / 3);
	}

	private static String getResourceKey(final String key) {
		return "ManageAddOnsDialog." + key;
	}

	static String getText(String key, Object... parameters) {
		if (parameters.length == 0)
			return TextUtils.getText(getResourceKey(key));
		else
			return TextUtils.format(getResourceKey(key), parameters);
	}

	public void install(final URL url) {
		if (addOnInstallerPanel.isShowing()) {
			addOnInstallerPanel.getUrlField().setText(url.toString());
			tabbedPane.paintImmediately(0, 0, tabbedPane.getWidth(), tabbedPane.getHeight());
			addOnInstallerPanel.getInstallButton().doClick();
		}
		else {
			addOnInstallerPanel.addHierarchyListener(new HierarchyListener() {
				public void hierarchyChanged(HierarchyEvent e) {
					if (addOnInstallerPanel.isShowing()) {
						addOnInstallerPanel.removeHierarchyListener(this);
						install(url);
					}
				}
			});
			tabbedPane.setSelectedComponent(addOnInstallerPanel);
			if (!isVisible())
				setVisible(true);
		}
	}
}
