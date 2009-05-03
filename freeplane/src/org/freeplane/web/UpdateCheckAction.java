package org.freeplane.web;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.FreeplaneVersion;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogTool;
import org.freeplane.core.util.ResUtil;

/**
 * Checks for updates.
 * 
 * @author robert ladstaetter
 */
public class UpdateCheckAction extends AFreeplaneAction {
	/**
	 * the url where to download the newest version
	 */
	private static final String WEB_DOWNLOAD_LOCATION_KEY = "webDownloadLocation";
	/**
	 * the url to check the local version against
	 */
	private static final String WEB_UPDATE_LOCATION_KEY = "webUpdateLocation";
	/**
	 * the sid.
	 */
	private static final long serialVersionUID = 7910922464393515103L;
	/**
	 * the client which asks a remote repository for the current version of the program.
	 */
	private VersionClient versionClient;
	/**
	 * The property file which saves the installed version information.
	 */
	private Properties localProperties;

	public void setVersionClient(VersionClient versionClient) {
		this.versionClient = versionClient;
	}

	public UpdateCheckAction(Controller controller) {
		super("UpdateCheckAction", controller);
		setVersionClient(new HttpVersionClient(ResourceController.getResourceController().getProperty(
		    WEB_UPDATE_LOCATION_KEY)));
		localProperties = ResUtil.loadProperties(FreeplaneVersion.VERSION_PROPERTIES);
	}

	public void actionPerformed(ActionEvent e) {
		String currentVersion = versionClient.getCurrentVersion();
		if (currentVersion == null) {
			LogTool.warn("Couldn't determine current version. Ignoring update request.");
			return;
		}
		String localVersion = (String)localProperties.get(FreeplaneVersion.VERSION_KEY);
		if (!localVersion.equals(currentVersion)) {
			LogTool.info("You have an old version installed (" +localVersion + ") - opening download page.");
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
		} else {
			// show a dialog to indicate that the current version matches the latest release.
			LogTool.info("You have the lastest version of freeplane installed : " + localVersion);
		}
	}
}
