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
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;

public class UpdateCheck {
			
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
			xml = CommunicationsController.getController().getLatestVersionXml();
			load(xml);
			application = getApplication();
			
			Version latestVersion = getLatestAvailableVersion();			
			Version runningVersion = getRunningVersion();
			if (latestVersion == null || runningVersion == null) {
				return;
			}
			
			int compCode = latestVersion.compareTo(runningVersion);
			
			UpdateCheckerDialogPanel dialogPanel = new UpdateCheckerDialogPanel("", getStringFromVersion(runningVersion), getStringFromVersion(latestVersion));
			
			JOptionPane.showMessageDialog(UITools.getFrame(), dialogPanel, TextUtils.getText("docear.new_version_available.title"), JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			LogUtils.warn(e.getMessage());
		}
		
	}
	
	public void load(final String xml) {		
		final TreeXmlReader reader = new TreeXmlReader(readManager);
		
		try { 
			reader.load(new StringReader(xml));			
		}
		catch (final Exception e) {
			LogUtils.warn(e);
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
	
	private String getStringFromVersion(Version version) {
		String versionString = ""+version.getMajorVersion()+"."+version.getMiddleVersion()+"."+version.getMinorVersion();
		String status = version.getStatus();
		if (status != null && status.length()>0) {
			versionString += " "+status;
		}
		Integer statusNumber = version.getStatusNumber();
		if (statusNumber != null && statusNumber > -1) {
			versionString += statusNumber;
		}
		versionString += " build";
		Integer buildNumber = version.getBuildNumber();
		if (buildNumber != null && buildNumber>-1) {
			versionString += buildNumber;
		}
		return versionString;
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
