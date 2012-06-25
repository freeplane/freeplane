/**
 * author: Marcel Genzmehr
 * 19.08.2011
 */
package org.docear.plugin.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.core.features.DocearProgressObserver;
import org.docear.plugin.core.io.IOTools;
import org.docear.plugin.core.logger.DocearEventLogger;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;

/**
 * 
 */
public class DocearController implements IDocearEventListener {

	private final static String PLACEHOLDER_PROFILENAME = "@@PROFILENAME@@";
	private static final String DEFAULT_LIBRARY_PATH = "workspace:/"+PLACEHOLDER_PROFILENAME+"/library";
	private final static Pattern PATTERN = Pattern.compile(PLACEHOLDER_PROFILENAME);
	static final String DOCEAR_FIRST_RUN_PROPERTY = "docear.already_initialized";
	
	private final static String DOCEAR_VERSION_NUMBER = "docear.version.number";
	
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
	
	private final Set<String> workingThreads = new HashSet<String>();
	private final boolean firstRun;
	private boolean applicationShutdownAborted = false;
	private Map<Class<?>, Set<DocearProgressObserver>> progressObservers = new TreeMap<Class<?>, Set<DocearProgressObserver>>(new Comparator<Class<?>>() {
		public int compare(Class<?> c1, Class<?> c2) {
			if(c1.equals(c2)) {
				return 0;
			}
			return c1.getName().compareTo(c2.getName());
		}
	});
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	protected DocearController() {
		firstRun = !ResourceController.getResourceController().getBooleanProperty(DOCEAR_FIRST_RUN_PROPERTY);
		setApplicationIdentifiers();
		addDocearEventListener(this);
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public boolean isDocearFirstStart() {
		return firstRun;
	}
	
	public boolean isLicenseDialogNecessary() {		
		int storedBuildNumber = Integer.parseInt(ResourceController.getResourceController().getProperty(DOCEAR_VERSION_NUMBER, "0"));
		if (storedBuildNumber == 0) {
			ResourceController.getResourceController().setProperty(DOCEAR_VERSION_NUMBER, ""+this.applicationBuildNumber);
			return true;
		}
		else {
			return false;
		}
	}
	
	public static DocearController getController() {
		return docearController;
	}
	
	public synchronized void addWorkingThreadHandle(String handleId) {
		if(handleId == null) {
			return;
		}
		workingThreads.add(handleId);		
	}
	
	public synchronized void removeWorkingThreadHandle(String handleId) {
		if(handleId == null) {
			return;
		}
		workingThreads.remove(handleId);		
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
	
	public Version getVersion() {
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
	private void setApplicationVersion(String applicationVersion) {
		this.applicationVersion = applicationVersion;
	}
	public String getApplicationStatus() {
		return applicationStatus;
	}
	private void setApplicationStatus(String applicationStatus) {
		this.applicationStatus = applicationStatus;
	}
	public String getApplicationStatusVersion() {
		return applicationStatusVersion;
	}
	private void setApplicationStatusVersion(String applicationStatusVersion) {
		this.applicationStatusVersion = applicationStatusVersion;
	}
	public int getApplicationBuildNumber() {
		return applicationBuildNumber;
	}
	private void setApplicationBuildNumber(int i) {
		this.applicationBuildNumber = i;
	}
	public String getApplicationBuildDate() {
		return applicationBuildDate;
	}
	private void setApplicationBuildDate(String applicationBuildDate) {
		this.applicationBuildDate = applicationBuildDate;
	}
	
	private synchronized boolean hasWorkingThreads() {
		return !workingThreads.isEmpty();
	}
	
	public boolean shutdown() {	
		dispatchDocearEvent(new DocearEvent(this, DocearEventType.APPLICATION_CLOSING));
		
		Controller.getCurrentController().getViewController().saveProperties();
		ResourceController.getResourceController().saveProperties();		
		if(!waitThreadsReady()){
			return false;
		}
		if(Controller.getCurrentController().getViewController().quit()) {
			if(!waitThreadsReady()){
				return false;
			}
		}
		else {
			return false;
		}		
		return true;
	}
	
	public void addProgressObserver(Class<?> clazz, DocearProgressObserver observer) {
		Set<DocearProgressObserver> observers = progressObservers.get(clazz);
		if(observers == null) {
			observers = new HashSet<DocearProgressObserver>();
			progressObservers.put(clazz, observers);
		}
		observers.add(observer);
	}
	
	public Collection<DocearProgressObserver> getProgressObservers(Class<?> clazz) {
		Collection<DocearProgressObserver> observers = progressObservers.get(clazz);
		if(observers == null) {
			observers = Collections.emptySet();
		}
		return observers;
	}
	
	private boolean waitThreadsReady() {
		while(hasWorkingThreads()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
			}				
		}	
		if(this.applicationShutdownAborted){
			this.applicationShutdownAborted = false;
			return false;
		}
		return true;
	}
	
	
	public String getDataProcessingTerms() {
		try {
			return IOTools.getStringFromStream(DocearController.class.getResourceAsStream("/Docear_data_processing.txt"),"UTF-8");
		}
		catch (IOException e) {
			LogUtils.warn(e);
			return "Data Processing";
		}
	}
	
	public String getDataPrivacyTerms() {
		try {
			return IOTools.getStringFromStream(DocearController.class.getResourceAsStream("/Docear_data_privacy.txt"), "UTF-8");
		}
		catch (IOException e) {
			LogUtils.warn(e);
			return "Data Privacy";
		}
	}
	
	public String getTermsOfUse() {
		try {
			return IOTools.getStringFromStream(DocearController.class.getResourceAsStream("/Docear_terms_of_use.txt"),"UTF-8");
		}
		catch (IOException e) {
			LogUtils.warn(e);
			return "Terms of Use";
		}
	}
	
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public void handleEvent(DocearEvent event) {
		if(event.getType() == DocearEventType.APPLICATION_CLOSING) {
			WorkspaceUtils.saveCurrentConfiguration();
		}
		else if(event.getType() == DocearEventType.APPLICATION_CLOSING_ABORTED){
			this.applicationShutdownAborted = true;
		}
		else if(event.getType() == DocearEventType.NEW_LIBRARY && event.getSource() instanceof IDocearLibrary) {
			this.currentLibrary = (IDocearLibrary) event.getSource();
			LogUtils.info("DOCEAR: new DocearLibrary set");
		} 
			
	}
	
	

	
}
