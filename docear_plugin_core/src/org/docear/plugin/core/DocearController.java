/**
 * author: Marcel Genzmehr
 * 19.08.2011
 */
package org.docear.plugin.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.core.logger.DocearEventLogger;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;

/**
 * 
 */
public class DocearController implements IDocearEventListener {

	private final static String PLACEHOLDER_PROFILENAME = "@@PROFILENAME@@";
	private static final String DEFAULT_LIBRARY_PATH = "workspace:/"+PLACEHOLDER_PROFILENAME+"/library";
	private final static Pattern PATTERN = Pattern.compile(PLACEHOLDER_PROFILENAME);
	
	private String applicationName;
	private String applicationVersion;
	private String applicationStatus;
	private String applicationStatusVersion;
	private int applicationBuildNumber;
	private String applicationBuildDate;
	
	
	private final DocearEventLogger docearEventLogger = new DocearEventLogger();
	
	private final Vector<IDocearEventListener> docearListeners = new Vector<IDocearEventListener>();		
	private final static DocearController docearController = new DocearController();
	
	private IDocearLibrary currentLibrary = null;	
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	protected DocearController() {
		setApplicationIdentifiers();
		addDocearEventListener(this);
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public static DocearController getController() {
		return docearController;
	}
	
	public void setApplicationIdentifiers() {
		final Properties versionProperties = new Properties();
		InputStream in = null;
		try {
			in = this.getClass().getResource("/version.properties").openStream();
			versionProperties.load(in);
		}
		catch (final IOException e) {
			
		}
		
		final Properties buildProperties = new Properties();
		in = null;
		try {
			in = this.getClass().getResource("/build.number").openStream();
			buildProperties.load(in);
		}
		catch (final IOException e) {
			
		}
		
		setApplicationName("Docear");
		setApplicationVersion(versionProperties.getProperty("docear_version"));
		setApplicationStatus(versionProperties.getProperty("docear_version_status"));		
		setApplicationStatusVersion(versionProperties.getProperty("docear_version_status_number"));
		setApplicationBuildDate(versionProperties.getProperty("build_date"));
		setApplicationBuildNumber(Integer.parseInt(buildProperties.getProperty("build.number")) -1);
	}
		
	public void addDocearEventListener(IDocearEventListener listener) {
		if(this.docearListeners.contains(listener)) {
			return;
		}
		this.docearListeners.add(listener);
	}
	
	public void removeDocearEventListener(IDocearEventListener listener) {
		this.docearListeners.remove(listener);
	}
	
	public void removeAllDocearEventListeners() {
		this.docearListeners.removeAllElements();
	}
	
	public void dispatchDocearEvent(DocearEvent event) {
		//LogUtils.info("DOCEAR: dispatchEvent: "+ event);
		for(IDocearEventListener listener : this.docearListeners) {
			listener.handleEvent(event);
		}
	}
	
	public IDocearLibrary getLibrary() {
		return currentLibrary;
	}
	
	public URI getLibraryPath() {		
		Matcher mainMatcher = PATTERN.matcher(DEFAULT_LIBRARY_PATH);
		String ret = mainMatcher.replaceAll(WorkspaceController.getController().getPreferences().getWorkspaceProfileHome());
		return WorkspaceUtils.absoluteURI(URI.create(ret));
	}
	
	public DocearEventLogger getDocearEventLogger() {
		return this.docearEventLogger;
	}
	
//	public String getApplicationVersion() {
//		String version	= ResourceController.getResourceController().getProperty("docear_version");
//		String status	= ResourceController.getResourceController().getProperty("docear_status");
//		
//		return version+" "+status;
//	}
	
	public String getApplicationName() {
		return this.applicationName;
	}
	
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	
	public String getApplicationVersion() {
		return applicationVersion;
	}
	public void setApplicationVersion(String applicationVersion) {
		this.applicationVersion = applicationVersion;
	}
	public String getApplicationStatus() {
		return applicationStatus;
	}
	public void setApplicationStatus(String applicationStatus) {
		this.applicationStatus = applicationStatus;
	}
	public String getApplicationStatusVersion() {
		return applicationStatusVersion;
	}
	public void setApplicationStatusVersion(String applicationStatusVersion) {
		this.applicationStatusVersion = applicationStatusVersion;
	}
	public int getApplicationBuildNumber() {
		return applicationBuildNumber;
	}
	public void setApplicationBuildNumber(int i) {
		this.applicationBuildNumber = i;
	}
	public String getApplicationBuildDate() {
		return applicationBuildDate;
	}
	public void setApplicationBuildDate(String applicationBuildDate) {
		this.applicationBuildDate = applicationBuildDate;
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public void handleEvent(DocearEvent event) {
		if(event.getType() == DocearEventType.NEW_LIBRARY && event.getSource() instanceof IDocearLibrary) {
			this.currentLibrary = (IDocearLibrary) event.getSource();
			LogUtils.info("DOCEAR: new DocearLibrary set");
		}	
	}
	

	
}
