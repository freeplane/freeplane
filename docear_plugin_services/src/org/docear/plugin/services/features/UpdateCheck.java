package org.docear.plugin.services.features;

import java.io.StringReader;

import javax.swing.JOptionPane;

import org.docear.plugin.communications.CommunicationsController;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.components.dialog.UpdateCheckerDialogPanel;
import org.docear.plugin.services.features.creators.ApplicationCreator;
import org.docear.plugin.services.features.creators.BuildNumberCreator;
import org.docear.plugin.services.features.creators.MajorVersionCreator;
import org.docear.plugin.services.features.creators.MiddleVersionCreator;
import org.docear.plugin.services.features.creators.MinorVersionCreator;
import org.docear.plugin.services.features.creators.ReleaseDateCreator;
import org.docear.plugin.services.features.creators.ReleaseNotesCreator;
import org.docear.plugin.services.features.creators.StatusCreator;
import org.docear.plugin.services.features.creators.StatusNumberCreator;
import org.docear.plugin.services.features.creators.VersionCreator;
import org.docear.plugin.services.features.creators.VersionsCreator;
import org.docear.plugin.services.features.elements.Application;
import org.docear.plugin.services.features.elements.Version;
import org.freeplane.core.io.IElementHandler;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;

public class UpdateCheck {
	public static final String DOCEAR_UPDATE_CHECKER_DISABLE = "docear.update_checker.disable";
	public static final String DOCEAR_UPDATE_CHECKER_ALL = "docear.update_checker.all";
	public static final String DOCEAR_UPDATE_CHECKER_BETA = "docear.update_checker.beta";
	public static final String DOCEAR_UPDATE_CHECKER_MINOR = "docear.update_checker.minor";
	public static final String DOCEAR_UPDATE_CHECKER_MIDDLE = "docear.update_checker.middle";
	public static final String DOCEAR_UPDATE_CHECKER_MAJOR = "docear.update_checker.major";
			
	final private ReadManager readManager;
	
	private Application application;
	
	ApplicationCreator applicationCreator;
	VersionsCreator versionsCreator;
	VersionCreator versionCreator;
	
	ReleaseDateCreator releaseDateCreator;
	BuildNumberCreator buildNumberCreator;
	MajorVersionCreator majorVersionCreator;
	MiddleVersionCreator middleVersionCreator;
	MinorVersionCreator minorVersionCreator;
	StatusCreator statusCreator;
	StatusNumberCreator statusNumberCreator;
	ReleaseNotesCreator releaseNotesCreator;
	
	
	
	public UpdateCheck() {
		this.readManager = new ReadManager();
		initReadManager();
		
		String xml;
		try {
			String choice = ResourceController.getResourceController().getProperty("docear.update_checker.options");
			if (choice == null || DOCEAR_UPDATE_CHECKER_DISABLE.equals(choice)) {
				return;
			}
			
			String minStatus = null;
			if (choice.equals(DOCEAR_UPDATE_CHECKER_ALL)) {
				minStatus = Version.StatusName.devel.name();
			}
			else if (choice.equals(DOCEAR_UPDATE_CHECKER_BETA)) {
				minStatus = Version.StatusName.beta.name();
			}
			else {
				minStatus = Version.StatusName.stable.name();
			}			
			
			xml = CommunicationsController.getController().getLatestVersionXml(minStatus);
			load(xml);
			application = getApplication();
			
			Version latestVersion = getLatestAvailableVersion();			
			Version runningVersion = getRunningVersion();
			if (latestVersion == null || runningVersion == null) {
				return;
			}
						
			int compCode = latestVersion.compareTo(runningVersion);
			
			if (showUpdateCheckerDialog(compCode, choice)) {
				// don't show Dialog again if latestVersionFromServer was already announced to the user
				String lastLatestVersionString = ResourceController.getResourceController().getProperty("docer.update_checker.savedLatestVersion", "");
				String latestVersionString = latestVersion.toString();
				if (lastLatestVersionString.equals(latestVersionString)) {
					return;
				}
				ResourceController.getResourceController().setProperty("docer.update_checker.savedLatestVersion", latestVersionString);
				
				UpdateCheckerDialogPanel dialogPanel = new UpdateCheckerDialogPanel("", runningVersion.toString(), latestVersionString);
				JOptionPane.showMessageDialog(UITools.getFrame(), dialogPanel, TextUtils.getText("docear.new_version_available.title"), JOptionPane.INFORMATION_MESSAGE);
				ResourceController.getResourceController().setProperty("docear.update_checker.options", dialogPanel.getChoice());
			}
			
		} catch (Exception e) {
			LogUtils.warn(e.getMessage());
		}		
	}
	
	private boolean showUpdateCheckerDialog(int compCode, String choice) {
		if (choice.equals(DOCEAR_UPDATE_CHECKER_ALL) && compCode >= Version.CompareCode.DEVEL.code) {
			return true;
		}
		else if (choice.equals(DOCEAR_UPDATE_CHECKER_BETA) && compCode >= Version.CompareCode.BETA.code) {
			return true;
		}
		else if (choice.equals(DOCEAR_UPDATE_CHECKER_MINOR) && compCode >= Version.CompareCode.MINOR.code) {
			return true;
		}
		else if (choice.equals(DOCEAR_UPDATE_CHECKER_MIDDLE) && compCode >= Version.CompareCode.MIDDLE.code) {
			return true;
		}
		else if (choice.equals(DOCEAR_UPDATE_CHECKER_MAJOR) && compCode >= Version.CompareCode.MAJOR.code) {
			return true;
		}
		
		return false;
	}
	
	public void load(final String xml) {		
		final TreeXmlReader reader = new TreeXmlReader(readManager);
		
		try { 
			reader.load(new StringReader(xml));			
		}
		catch (final Exception e) {
			LogUtils.warn(e.getMessage());
		}		
	}
	
	private void initReadManager() {
		readManager.addElementHandler("application", getApplicationCreator());
		readManager.addElementHandler("versions", getVersionsCreator());
		readManager.addElementHandler("version", getVersionCreator());
		
		readManager.addElementHandler("release_date", getReleaseDateCreator());
		readManager.addElementHandler("build", getBuildNumberCreator());
		readManager.addElementHandler("major", getMajorVersionCreator());
		readManager.addElementHandler("middle", getMiddleVersionCreator());
		readManager.addElementHandler("minor", getMinorVersionCreator());
		readManager.addElementHandler("status", getStatusCreator());
		readManager.addElementHandler("status_number", getStatusNumberCreator());
		readManager.addElementHandler("release_notes", getReleaseNotesCreator());
	}
	
	public Version getRunningVersion() {
		try {
			DocearController docearController = DocearController.getController();
			Version version = new Version();		
			String[] versionStrings = docearController.getApplicationVersion().split("\\.");
			version.setMajorVersion(Integer.parseInt(versionStrings[0]));
			version.setMiddleVersion(Integer.parseInt(versionStrings[1]));
			version.setMinorVersion(Integer.parseInt(versionStrings[2]));
			
			version.setStatus(docearController.getApplicationStatus());
			version.setStatusNumber(Integer.parseInt(docearController.getApplicationStatusVersion()));
			version.setBuildNumber(docearController.getApplicationBuildNumber());
			
			return version;
		}
		catch(Exception e) {
			LogUtils.warn(e);
			return null;
		}
	}
	
	public Version getLatestAvailableVersion() {
		return ServiceController.getController().getApplication().getVersions().entrySet().iterator().next().getValue();		
	}
	
	private IElementHandler getReleaseNotesCreator() {		
		if (this.releaseNotesCreator == null) {
			this.releaseNotesCreator = new ReleaseNotesCreator();
		}
		
		return this.releaseNotesCreator;
	}
	
	private IElementHandler getStatusNumberCreator() {		
		if (this.statusNumberCreator == null) {
			this.statusNumberCreator = new StatusNumberCreator();
		}
		
		return this.statusNumberCreator;
	}
	
	private IElementHandler getStatusCreator() {		
		if (this.statusCreator == null) {
			this.statusCreator = new StatusCreator();
		}
		
		return this.statusCreator;
	}
	
	private IElementHandler getMinorVersionCreator() {		
		if (this.minorVersionCreator == null) {
			this.minorVersionCreator = new MinorVersionCreator();
		}
		
		return this.minorVersionCreator;
	}
	
	private IElementHandler getMiddleVersionCreator() {		
		if (this.middleVersionCreator == null) {
			this.middleVersionCreator = new MiddleVersionCreator();
		}
		
		return this.middleVersionCreator;
	}
	
	private IElementHandler getMajorVersionCreator() {		
		if (this.majorVersionCreator == null) {
			this.majorVersionCreator = new MajorVersionCreator();
		}
		
		return this.majorVersionCreator;
	}
	
	private IElementHandler getBuildNumberCreator() {		
		if (this.buildNumberCreator == null) {
			this.buildNumberCreator = new BuildNumberCreator();
		}
		
		return this.buildNumberCreator;
	}
	
	private IElementHandler getReleaseDateCreator() {		
		if (this.releaseDateCreator == null) {
			this.releaseDateCreator = new ReleaseDateCreator();
		}
		
		return this.releaseDateCreator;
	}
	
	private IElementHandler getVersionCreator() {		
		if (this.versionCreator == null) {
			this.versionCreator = new VersionCreator();
		}
		
		return this.versionCreator;
	}
	
	private IElementHandler getVersionsCreator() {		
		if (this.versionsCreator == null) {
			this.versionsCreator = new VersionsCreator();
		}
		
		return this.versionsCreator;
	}
	
	private IElementHandler getApplicationCreator() {
		if (this.applicationCreator == null) {
			this.applicationCreator = new ApplicationCreator();
		}
		
		return this.applicationCreator; 
	}


	public Application getApplication() {
		return application;
	}
}
