package org.docear.plugin.services.features;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.docear.plugin.services.ServiceController;
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
import org.freeplane.core.io.IElementHandler;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.util.LogUtils;

public class UpdateCheck {	
	final private ReadManager readManager;
	
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
		
		load(this.getClass().getResourceAsStream("/app.xml"));
		Application application = ServiceController.getController().getApplication();
		System.out.println("test");
	}
	
	
	public void load(final InputStream xmlStream) {		
		final TreeXmlReader reader = new TreeXmlReader(readManager);
		
		try { 
			reader.load(new InputStreamReader(xmlStream, "UTF-8"));			
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
}
