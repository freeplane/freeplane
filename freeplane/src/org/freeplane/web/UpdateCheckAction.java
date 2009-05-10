package org.freeplane.web;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.FreeplaneVersion;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogTool;

/**
 * Checks for updates.
 * 
 * @author robert ladstaetter
 */
public class UpdateCheckAction extends AFreeplaneAction {
	private static boolean autorunEnabled  = true;
	private static Timer autorunTimer = null;
	/**
	 * the sid.
	 */
	private static final long serialVersionUID = 7910922464393515103L;
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
	public UpdateCheckAction(final Controller controller) {
		super("UpdateCheckAction", controller);
		if(autorunEnabled == false){
			return;
		}
		autorunEnabled = ResourceController.getResourceController().getBooleanProperty("check_updates_automatically");
		if(autorunEnabled == false){
			return;
		}
		autorunEnabled = false;
		autorunTimer = new Timer(30000, this);
		autorunTimer.setRepeats(false);
		autorunTimer.start();
	}

	public void actionPerformed(final ActionEvent e) {
		final boolean autoRun = e.getSource().equals(autorunTimer);
		if(autorunTimer != null){
			autorunTimer.stop();
			autorunTimer = null;
		}
		new Thread(new Runnable(){
			public void run() {
				checkForUpdates(autoRun);
	            
            }}, "checkForUpdates").start();
	}

	private void checkForUpdates(final boolean autoRun) {
	    final Locale defaultLocale = Locale.getDefault();
		final String language = defaultLocale.getLanguage();
		final String DEFAULT_LANGUAGE="en";
		final String webUpdateUrl = ResourceController.getResourceController().getProperty(
			WEB_UPDATE_LOCATION_KEY);
		final String translatedWebUpdate = webUpdateUrl + "history_" + language +".txt";
		final FreeplaneVersion localVersion = FreeplaneVersion.getVersion();
		final HttpVersionClient translatedVersionClient = new HttpVersionClient(translatedWebUpdate, localVersion);
		FreeplaneVersion lastTranslatedVersion = translatedVersionClient.getRemoteVersion();
		if(lastTranslatedVersion == null){
			lastTranslatedVersion = localVersion;
		}
		
		final String history;
		final FreeplaneVersion lastVersion;
		if(! language.equals(DEFAULT_LANGUAGE)){
			final String defaultWebUpdate = webUpdateUrl + "history_" + DEFAULT_LANGUAGE +".txt";
			final HttpVersionClient defaultVersionClient = new HttpVersionClient(defaultWebUpdate, lastTranslatedVersion);
			lastVersion= defaultVersionClient.getRemoteVersion();
			history = defaultVersionClient.getHistory() + translatedVersionClient.getHistory();
		}
		else{
			lastVersion = lastTranslatedVersion;
			history = translatedVersionClient.getHistory();
		}
		EventQueue.invokeLater(new Runnable(){

			public void run() {
				showUpdateDialog(autoRun, localVersion, lastVersion, history);
            }});
    }

	private void showUpdateDialog(final boolean autoRun, final FreeplaneVersion localVersion,
                                  final FreeplaneVersion lastVersion, String history) {
	    if (lastVersion == null) {
			if(! autoRun){
				showUpdateDialog("can_not_connect_to_update_server", null, null);
			}
			LogTool.warn("Couldn't determine current version. Ignoring update request.");
			return;
		}
		if (localVersion.compareTo(lastVersion) < 0) {
			final int choice = showUpdateDialog("new_version_available", lastVersion, history);
			if (0 != choice){
				return;
			}
			// go to download page
			try {
				getController().getViewController().openDocument(
				    new URL(ResourceController.getResourceController().getProperty(WEB_DOWNLOAD_LOCATION_KEY)));
			}
			catch (final MalformedURLException ex) {
				getController().errorMessage(ResourceBundles.getText("url_error") + "\n" + ex);
			}
			catch (final Exception ex) {
				getController().errorMessage(ex);
			}
		}
		else if(! autoRun){
			showUpdateDialog("version_up_to_date", null, null);
		}
    }

	private int showUpdateDialog(final String info, final FreeplaneVersion lastVersion, String history) {
	    final Box messagePane = Box.createVerticalBox();
	    final JLabel messageLabel;
	    if(lastVersion != null){
	    	messageLabel = new JLabel(FpStringUtils.formatText(info, lastVersion.toString()));
	    }
	    else{
	    	messageLabel = new JLabel(ResourceBundles.getText(info));
	    }
	    messageLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
	    messagePane.add(messageLabel);
	    final JTextArea historyArea = new JTextArea(history);
	    historyArea.setEditable(false);
	    final JScrollPane historyPane = new JScrollPane(historyArea);
	    historyPane.setPreferredSize(new Dimension(500, 300));
	    historyPane.setAlignmentX(JLabel.LEFT_ALIGNMENT);
	    messagePane.add(historyPane);			
	    final JCheckBox updateAutomatically = new JCheckBox(ResourceBundles.getText("OptionPanel.check_updates_automatically"),
		    ResourceController.getResourceController().getBooleanProperty("check_updates_automatically"));
	    updateAutomatically.setAlignmentX(JLabel.LEFT_ALIGNMENT);
	    messagePane.add(updateAutomatically);
	    Object[] options = new Object[]{ResourceBundles.getText("download"), 
	    		FpStringUtils.removeMnemonic(ResourceBundles.getText("cancel"))};
	    final int choice = JOptionPane.showOptionDialog(getController().getViewController().getFrame(), 
	    	messagePane, ResourceBundles.getText("updatecheckdialog"), JOptionPane.DEFAULT_OPTION, 
	    	JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
	    ResourceController.getResourceController().setProperty("check_updates_automatically",
		    Boolean.toString(updateAutomatically.isSelected()));
	    return choice;
    }
}
