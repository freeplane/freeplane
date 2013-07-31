package org.freeplane.main.application;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.Border;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.freeplane.main.addons.AddOnProperties;
import org.freeplane.main.addons.AddOnsController;

/**
 * Checks for updates.
 * 
 * @author robert ladstaetter
 */
class UpdateCheckAction extends AFreeplaneAction {
	private static boolean autorunEnabled = true;
	private static Timer autorunTimer = null;
	private static final int CHECK_TIME = 30 * 1000;
	private static final String CHECK_UPDATES_AUTOMATICALLY = "check_updates_automatically";
	private static final String LAST_UPDATE_CHECK_TIME = "last_update_check_time";
	private static final String LAST_UPDATE_VERSION = "last_update_verson";
	/**
	 * the sid.
	 */
	private static final long serialVersionUID = 1L;
	private static final int TWO_DAYS = 1 * 24 * 60 * 60 * 1000;
	private static final String UPDATE_BUTTON_LOCATION = "main_toolbar_update";
	private static final String UPDATE_BUTTON_PATH = UPDATE_BUTTON_LOCATION + "/checkUpdate";
	/**
	 * the url where to download the newest version
	 */
	private static final String WEB_DOWNLOAD_LOCATION_KEY = "webDownloadLocation";
	/**
	 * the url to check the local version against
	 */
	private static final String WEB_UPDATE_LOCATION_KEY = "webUpdateLocation";

	/**
	 * the client which asks a remote repository for the current version of the program.
	 */
	public UpdateCheckAction() {
		super("UpdateCheckAction");
		final Controller controller = Controller.getCurrentController();
		controller.getMapViewManager().addMapViewChangeListener(new IMapViewChangeListener() {
			public void afterViewChange(final Component oldView, final Component newView) {
				if (newView == null) {
					return;
				}
				controller.getViewController().invokeLater(new Runnable() {
					public void run() {
						removeMe();
					}
				});
				setTimer();
			}

			public void afterViewClose(final Component oldView) {
			}

			public void afterViewCreated(final Component mapView) {
			}

			public void beforeViewChange(final Component oldView, final Component newView) {
			}

			private void removeMe() {
				controller.getMapViewManager().removeMapViewChangeListener(this);
			}
		});
	}

	public void actionPerformed(final ActionEvent e) {
		final boolean autoRun = e.getSource().equals(autorunTimer);
		if (autorunTimer != null) {
			autorunTimer.stop();
			autorunTimer = null;
		}
		new Thread(new Runnable() {
			public void run() {
				checkForUpdates(autoRun);
			}
		}, "checkForUpdates").start();
	}

	private void addUpdateButton(final FreeplaneVersion lastVersion) {
		Controller controller = Controller.getCurrentController();
		final Set<String> modes = controller.getModes();
		for (final String mode : modes) {
			final MenuBuilder menuBuilder = controller.getModeController(mode).getUserInputListenerFactory()
			    .getMenuBuilder();
			if (lastVersion == null || lastVersion.compareTo(FreeplaneVersion.getVersion()) <= 0) {
				ResourceController.getResourceController().setProperty(LAST_UPDATE_VERSION, "");
				if (menuBuilder.get(UPDATE_BUTTON_PATH) != null) {
					menuBuilder.removeElement(UPDATE_BUTTON_PATH);
				}
				continue;
			}
			ResourceController.getResourceController().setProperty(LAST_UPDATE_VERSION, lastVersion.toString());
			final String updateAvailable = TextUtils.format("new_version_available", lastVersion.toString());
			controller.getViewController().out(updateAvailable);
			putValue(SHORT_DESCRIPTION, updateAvailable);
			putValue(LONG_DESCRIPTION, updateAvailable);
			if (menuBuilder.get(UPDATE_BUTTON_PATH) == null) {
				menuBuilder.addAction(UPDATE_BUTTON_LOCATION, UPDATE_BUTTON_PATH, UpdateCheckAction.this,
				    MenuBuilder.AS_CHILD);
			}
		}
	}

	private void checkForUpdates(final boolean autoRun) {
		final Date now = new Date();
		ResourceController.getResourceController().setProperty(LAST_UPDATE_CHECK_TIME, Long.toString(now.getTime()));
		final Locale defaultLocale = Locale.getDefault();
		final String language = defaultLocale.getLanguage();
		final String DEFAULT_LANGUAGE = "en";
		final String translatedWebUpdate = getWebUpdateUrl(language);
		final FreeplaneVersion localVersion = FreeplaneVersion.getVersion();
		final HttpVersionClient translatedVersionClient = new HttpVersionClient(translatedWebUpdate, localVersion);
		FreeplaneVersion lastTranslatedVersion = translatedVersionClient.getRemoteVersion();
		if (lastTranslatedVersion == null) {
			lastTranslatedVersion = localVersion;
		}
		final String history;
		final FreeplaneVersion lastVersion;
		final boolean connectSuccesfull;
		if (!language.equals(DEFAULT_LANGUAGE)) {
			final String defaultWebUpdate = getWebUpdateUrl(DEFAULT_LANGUAGE);
			final HttpVersionClient defaultVersionClient = new HttpVersionClient(defaultWebUpdate,
			    lastTranslatedVersion);
			lastVersion = defaultVersionClient.getRemoteVersion();
			history = defaultVersionClient.getHistory() + translatedVersionClient.getHistory();
			connectSuccesfull = defaultVersionClient.isSuccessful();
		}
		else {
			lastVersion = lastTranslatedVersion;
			history = translatedVersionClient.getHistory();
			connectSuccesfull = translatedVersionClient.isSuccessful();
		}
		
		checkForAddonsUpdates();
		Controller.getCurrentController().getViewController().invokeLater(new Runnable() {
			public void run() {
				addUpdateButton(lastVersion);
				if (autoRun) {
					return;
				}
				showUpdateDialog(connectSuccesfull, localVersion, lastVersion, history);
			}
		});
	}
	
	private void checkForAddonsUpdates() {
		List<AddOnProperties> installedAddOns = AddOnsController.getController().getInstalledAddOns();
		for (AddOnProperties addOnProperties : installedAddOns) {
			FreeplaneVersion addOnLocalVersion;
			try {
				addOnLocalVersion = FreeplaneVersion.getVersion((addOnProperties.getVersion().replace("v", "")));
			} catch (IllegalArgumentException e) {
				addOnLocalVersion = FreeplaneVersion.getVersion("0.0.0");
			}
			final String addOnUpdateUrl = addOnProperties.getUpdateUrl().toString() + "?v=" + addOnLocalVersion.toString();
			final HttpVersionClient versionClient = new HttpVersionClient(addOnUpdateUrl, addOnLocalVersion);
			final boolean connectSuccesfull;
			FreeplaneVersion lastVersion = versionClient.getRemoteVersion();

			connectSuccesfull = versionClient.isSuccessful();
			if (connectSuccesfull) {
				addOnProperties.setLastVersion(lastVersion.toString());
			}
        }
	}

	private String getWebUpdateUrl(final String language) {
		{
			final String webUpdateUrl = ResourceController.getResourceController().getProperty(WEB_UPDATE_LOCATION_KEY);
			final FreeplaneVersion localVersion = FreeplaneVersion.getVersion();
			final StringBuilder sb = new StringBuilder(webUpdateUrl);
			final String type = localVersion.getType();
			if(! type.equals("")){
				sb.append(type);
				sb.append('/');
			}
			sb.append("history_");
			sb.append(language);
			sb.append(".txt");
			return sb.toString();
		}
	}

	private FreeplaneVersion getKnownNewVersion() {
		final FreeplaneVersion localVersion = FreeplaneVersion.getVersion();
		final String property = ResourceController.getResourceController().getProperty(LAST_UPDATE_VERSION);
		if (property.equals("")) {
			return null;
		}
		
		FreeplaneVersion lastVersion = FreeplaneVersion.getVersion(property);
		if (lastVersion.compareTo(localVersion) <= 0) {
			lastVersion = null;
		}
		return lastVersion;
	}

	private void setTimer() {
		if (autorunEnabled == false) {
			return;
		}
		autorunEnabled = ResourceController.getResourceController().getBooleanProperty(CHECK_UPDATES_AUTOMATICALLY);
		if (autorunEnabled == false) {
			return;
		}
		autorunEnabled = false;
		final Date now = new Date();
		final long nextCheckMillis = ResourceController.getResourceController().getLongProperty(LAST_UPDATE_CHECK_TIME,
		    0)
		        + TWO_DAYS;
		final Date nextCheckDate = new Date(nextCheckMillis);
		if (now.before(nextCheckDate)) {
			final FreeplaneVersion knownNewVersion = getKnownNewVersion();
			addUpdateButton(knownNewVersion);
			return;
		}
		autorunTimer = new Timer(CHECK_TIME, this);
		autorunTimer.setRepeats(false);
		autorunTimer.start();
	}


	private void showUpdateDialog(final boolean connectSuccesfull, final FreeplaneVersion localVersion,
	                              final FreeplaneVersion newVersion, final String history) {
		if (connectSuccesfull == false) {
			showUpdateDialog("can_not_connect_to_info_server", newVersion, history);
		} else {
			showUpdateDialog("new_version_available", newVersion, history);
		}
		
		return;
	}

	private int showUpdateDialog(final String info, final FreeplaneVersion freeplaneLastVersion, final String history) {
		final Box messagePane = Box.createVerticalBox();
		JLabel emptyLabel = new JLabel("");
		
		// grid setup
		JPanel gridPane = new JPanel(new GridBagLayout());
		gridPane.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		Border paddingBorder = BorderFactory.createEmptyBorder(0, 10, 10, 0);
		gridPane.setBorder(BorderFactory.createCompoundBorder(paddingBorder,paddingBorder));
		
		GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        int gridRow = 0;
        c.weightx = 0.5;
        
		
		// table headers
		JLabel componentHeader = new JLabel(TextUtils.getText("updater.component"), SwingConstants.CENTER);
		JLabel installedVersionHeader = new JLabel(TextUtils.getText("updater.version.installed"), SwingConstants.CENTER);
		JLabel latestVersionHeader = new JLabel(TextUtils.getText("updater.version.latest"), SwingConstants.CENTER);
		
		Font boldFont = new Font(componentHeader.getFont().getName(),Font.BOLD,componentHeader.getFont().getSize());
		componentHeader.setFont(boldFont);
		installedVersionHeader.setFont(boldFont);
		latestVersionHeader.setFont(boldFont);
		
		componentHeader.setBorder(paddingBorder);
		installedVersionHeader.setBorder(paddingBorder);
		latestVersionHeader.setBorder(paddingBorder);

		// adding headers
        c.gridy = 0;
        
        c.gridx = 0;
		gridPane.add(componentHeader, c);
		c.gridx = 1;
		gridPane.add(installedVersionHeader, c);
		c.gridx = 2;
		gridPane.add(latestVersionHeader, c);
		c.gridx = 3;
		gridPane.add(emptyLabel, c);
		c.gridx = 4;
		gridPane.add(emptyLabel, c);
        
		
		// first row : freeplane
        c.gridy = 1;
		
        
		final JLabel freeplaneLabel = new JLabel("Freeplane");
		FreeplaneVersion freeplaneLocalVersion = FreeplaneVersion.getVersion();
		JLabel freeplaneInstalledVersionLabel = new JLabel(freeplaneLocalVersion.toString(), SwingConstants.CENTER);
		JLabel freeplaneLatestVersionLabel;

		JButton updateButton, changelogButton;
		final Locale defaultLocale = Locale.getDefault();
		final String language = defaultLocale.getLanguage();
		final String translatedWebUpdate = getWebUpdateUrl(language);
		
		changelogButton = new JButton(TextUtils.getText("updater.viewChangelog"));
		changelogButton.addActionListener(openUrlListener);
		changelogButton.setActionCommand(translatedWebUpdate);
		
		updateButton = new JButton(TextUtils.getText("updater.goToDownload"));
		updateButton.addActionListener(openUrlListener);
		updateButton.setActionCommand("http://freeplane.sourceforge.net");

		Boolean needsUpdate = Boolean.FALSE;
		if (freeplaneLastVersion != null) {
			if (freeplaneLocalVersion.compareTo(freeplaneLastVersion) < 0) {
				needsUpdate = Boolean.TRUE;
			} else {
				needsUpdate = Boolean.FALSE;
			}

			freeplaneLatestVersionLabel = new JLabel(freeplaneLastVersion.toString(), SwingConstants.CENTER);
		} else {
			freeplaneLatestVersionLabel = new JLabel(TextUtils.getText("updater.version.unknown"), SwingConstants.CENTER);
			freeplaneLatestVersionLabel.setToolTipText(TextUtils.getText(info));
		}
		changelogButton.setEnabled(needsUpdate);
		updateButton.setEnabled(needsUpdate);


		
		c.gridx = 0;
		gridPane.add(freeplaneLabel, c);
		c.gridx = 1;
		gridPane.add(freeplaneInstalledVersionLabel, c);
		c.gridx = 2;
		gridPane.add(freeplaneLatestVersionLabel, c);
		c.gridx = 3;
		gridPane.add(changelogButton, c);
		c.gridx = 4;
		gridPane.add(updateButton, c);
		
		
/*				
		if (history != "") {
			final JTextArea historyArea = new JTextArea(history);
			historyArea.setEditable(false);
			final JScrollPane historyPane = new JScrollPane(historyArea);
			historyPane.setPreferredSize(new Dimension(500, 300));
			historyPane.setAlignmentX(JLabel.LEFT_ALIGNMENT);
			c.weightx = 1.0;
			c.gridx = 0;
			c.gridy = 2;
			gridPane.add(historyPane, c);
		}
*/
		List<AddOnProperties> installedAddOns = AddOnsController.getController().getInstalledAddOns();
		gridRow = 3;
		for (AddOnProperties addOnProperties : installedAddOns) {
			FreeplaneVersion addOnLocalVersion;
			FreeplaneVersion addOnLastVersion;
			try {
				addOnLocalVersion = FreeplaneVersion.getVersion((addOnProperties.getVersion().replace("v", "")));
			} catch (IllegalArgumentException e) {
				addOnLocalVersion = FreeplaneVersion.getVersion("0.0.0");
			}
			try {
				addOnLastVersion = FreeplaneVersion.getVersion((addOnProperties.getLastVersion()));
			} catch (IllegalArgumentException e) {
				addOnLastVersion = FreeplaneVersion.getVersion("0.0.0");
			}

			JLabel addOnLabel, addOnInstalledVersionLabel, addOnLatestVersionLabel;
			
			
			addOnInstalledVersionLabel = new JLabel(addOnLocalVersion.toString(), SwingConstants.CENTER);
			
			needsUpdate = Boolean.FALSE;
			if (addOnLastVersion != null) {
				if (addOnLocalVersion.compareTo(addOnLastVersion) < 0) {
					needsUpdate = Boolean.TRUE;
				} else {
					
				}
				addOnLatestVersionLabel = new JLabel(addOnLastVersion.toString(), SwingConstants.CENTER);
			} else {
				addOnLatestVersionLabel = new JLabel(TextUtils.getText("updater.version.unknown"), SwingConstants.CENTER);
				addOnLatestVersionLabel.setToolTipText(TextUtils.getText("updater.version.unreachable") + " " + addOnProperties.getUpdateUrl().toString());
			}
			
			addOnLabel = new JLabel(TextUtils.getText("addons." + addOnProperties.getName()));
			c.gridx = 0;
			c.gridy = gridRow;
			gridPane.add(addOnLabel, c);
			
			c.gridx = 1;
			c.gridy = gridRow;
			gridPane.add(addOnInstalledVersionLabel, c);
			
			c.gridx = 2;
			c.gridy = gridRow;
			gridPane.add(addOnLatestVersionLabel, c);
			
			c.gridx = 3;
			c.gridy = gridRow;
			changelogButton = new JButton(TextUtils.getText("updater.viewChangelog"));
			changelogButton.addActionListener(openUrlListener);
			changelogButton.setActionCommand(addOnProperties.getHomepage().toString());
			gridPane.add(changelogButton,c );
			changelogButton.setEnabled(needsUpdate);
			
			c.gridx = 4;
			c.gridy = gridRow;
			updateButton = new JButton(TextUtils.getText("updater.goToDownload"));
			updateButton.addActionListener(openUrlListener);
			updateButton.setActionCommand(addOnProperties.getUpdateUrl().toString());
			gridPane.add(updateButton, c);
			updateButton.setEnabled(needsUpdate);

			gridRow++;
		}
		messagePane.add(gridPane);
		
		
		JLabel confLabel = new JLabel(TextUtils.getText("preferences"));
		confLabel.setFont(boldFont);
		messagePane.add(confLabel);
		final JCheckBox updateAutomatically = new JCheckBox(TextUtils
			    .getText("OptionPanel.check_updates_automatically"), ResourceController.getResourceController()
			    .getBooleanProperty(CHECK_UPDATES_AUTOMATICALLY));
			updateAutomatically.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		messagePane.add(updateAutomatically);
		final Object[] options;
		options = new Object[] { TextUtils.getText("simplyhtml.closeBtnName") };

		
		final int choice = JOptionPane.showOptionDialog(Controller.getCurrentController().getViewController().getFrame(), messagePane,
		    TextUtils.getText("updatecheckdialog"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
		    options, options[0]);
		ResourceController.getResourceController().setProperty(CHECK_UPDATES_AUTOMATICALLY,
		    Boolean.toString(updateAutomatically.isSelected()));
		return choice;
	}


	private ActionListener openUrlListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			try {
				Controller.getCurrentController().getViewController().openDocument(
						new URL( ((JButton) e.getSource()).getActionCommand()) );
			}
			catch (final MalformedURLException ex) {
				UITools.errorMessage(TextUtils.getText("url_error") + "\n" + ex);
			}
			catch (final Exception ex) {
				UITools.errorMessage(ex);
			}
		}
	};

}