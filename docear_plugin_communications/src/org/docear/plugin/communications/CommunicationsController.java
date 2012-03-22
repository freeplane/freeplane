package org.docear.plugin.communications;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

import javax.swing.JOptionPane;
import javax.ws.rs.core.MultivaluedMap;

import org.docear.plugin.communications.components.WorkspaceDocearServiceConnectionBar;
import org.docear.plugin.communications.components.WorkspaceDocearServiceConnectionBar.CONNECTION_STATE;
import org.docear.plugin.communications.components.dialog.DocearServiceLoginPanel;
import org.docear.plugin.core.ALanguageController;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.IDocearEventListener;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.OptionPanelController.PropertyLoadListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.StringProperty;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.event.IWorkspaceEventListener;
import org.freeplane.plugin.workspace.event.WorkspaceEvent;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class CommunicationsController extends ALanguageController implements ActionListener, PropertyLoadListener, IWorkspaceEventListener, IFreeplanePropertyListener, IDocearEventListener {
	private final static CommunicationsController communicationsController = new CommunicationsController();
	
	private final WorkspaceDocearServiceConnectionBar connectionBar= new WorkspaceDocearServiceConnectionBar();
	
	public final static String DOCEAR_CONNECTION_USERNAME_PROPERTY = "docear.service.connect.username";
	public final static String DOCEAR_CONNECTION_TOKEN_PROPERTY = "docear.service.connect.token";
	
	private boolean allowTransmission = true; 

	public CommunicationsController() {
		super();
		
		addPluginDefaults();
		addPropertiesToOptionPanel();		
		
		Controller.getCurrentController().getOptionPanelController().addButtonListener(this);
		Controller.getCurrentController().getOptionPanelController().addPropertyLoadListener(this);
		Controller.getCurrentController().getResourceController().addPropertyChangeListener(this);
		WorkspaceController.getController().addWorkspaceListener(this);
		DocearController.getController().addDocearEventListener(this);
		
		WorkspaceController.getController().addToolBar(connectionBar);
		propertyChanged(DOCEAR_CONNECTION_TOKEN_PROPERTY, getAccessToken(), null);		
	}
	
	public static CommunicationsController getController() {
		return communicationsController;
	}
	
	
	public void actionPerformed(ActionEvent e) {		
		if("docear_connect".equals(e.getActionCommand())) {
			DocearServiceLoginPanel loginPanel = new DocearServiceLoginPanel();
			int choice = JOptionPane.showConfirmDialog(UITools.getFrame(), loginPanel, TextUtils.getOptionalText("docear.service.connect.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			
			if(choice == JOptionPane.OK_OPTION) {
				tryToConnect(loginPanel.getUsername(), loginPanel.getPassword());
			}
		}
	}
	
	private void tryToConnect(final String username, final String password) {
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		try {
			Client client = Client.create();
			//client.setReadTimeout(CommunicationsConfiguration.READ_TIMEOUT);
			//client.setConnectTimeout(CommunicationsConfiguration.CONNECTION_TIMEOUT);		
			MultivaluedMap<String,String> formParams = new MultivaluedMapImpl();
		    formParams.add("password", password);
			
			ClientResponse response = client.resource(getServiceUri()).path("/authenticate/"+username).post(ClientResponse.class, formParams);
			Status status = response.getClientResponseStatus();
			
			if(status.equals(Status.OK)) {				
				String token = response.getHeaders().getFirst("accessToken");
				ResourceController.getResourceController().setProperty("docear.service.connect.username", username);
				((StringProperty)Controller.getCurrentController().getOptionPanelController().getPropertyControl(DOCEAR_CONNECTION_USERNAME_PROPERTY)).setValue(username);
				ResourceController.getResourceController().setProperty("docear.service.connect.token", token);
				JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.format("docear.service.connect.success", username));
				InputStream is = response.getEntityInputStream();
		        while (is.read() > -1);
		        is.close();
			} else {
				//ResourceController.getResourceController().setProperty("docear.service.connect.username", "");
				//((StringProperty)Controller.getCurrentController().getOptionPanelController().getPropertyControl(DOCEAR_CONNECTION_USERNAME_PROPERTY)).setValue("");
				ResourceController.getResourceController().setProperty("docear.service.connect.token", "");
				InputStream is = response.getEntityInputStream();
				int chr;
				StringBuilder message = new StringBuilder();
		        while ((chr = is.read()) > -1) {
		        	message.append((char)chr);
		        }
		        is.close();
				JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.format("docear.service.connect.failure", status, message.toString()));
			}
		    
		} 
		catch (Exception ex) {
			LogUtils.severe(ex);
		}
		finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
	}
	
	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
	}

	private void addPropertiesToOptionPanel() {
		final URL preferences = this.getClass().getResource("preferences.xml");
		if (preferences == null)
			throw new RuntimeException("cannot open preferences");
		MModeController modeController = (MModeController) Controller
				.getCurrentModeController();

		modeController.getOptionPanelBuilder().load(preferences);
	}

	public void propertiesLoaded(Collection<IPropertyControl> properties) {
		Controller.getCurrentController().getOptionPanelController().getPropertyControl(DOCEAR_CONNECTION_USERNAME_PROPERTY).setEnabled(false);
	}
	
	public boolean postFileToDocearService(String restPath, boolean deleteIfTransferred, File... files) {
		if(!allowTransmission || getAccessToken() == null || getAccessToken().trim().length() <= 0 || getUserName() == null || getUserName().trim().length() <= 0) {
			return false;
		}
		FiletransferClient client = new FiletransferClient(restPath, files);
		return client.send(deleteIfTransferred);
	}

	public String getUserName() {
		return ResourceController.getResourceController().getProperty(DOCEAR_CONNECTION_USERNAME_PROPERTY);
	}

	public URI getServiceUri() throws URISyntaxException {
		return new URI("https://api.docear.org/");
		//return new URI("http://141.44.30.58:8080/");
	}

	public String getAccessToken() {
		return ResourceController.getResourceController().getProperty(DOCEAR_CONNECTION_TOKEN_PROPERTY);
	}

	
	public void propertyChanged(String propertyName, String newValue, String oldValue) {
		if(DOCEAR_CONNECTION_USERNAME_PROPERTY.equals(propertyName)) {
			connectionBar.setUsername(newValue);
		} 
		else if(DOCEAR_CONNECTION_TOKEN_PROPERTY.equals(propertyName)) {
			if(newValue != null && newValue.trim().length() > 0) {
				connectionBar.setUsername(getUserName());
				setTransmissionStatus();
				connectionBar.setEnabled(true);
			}
			else {
				connectionBar.setUsername("");
				connectionBar.setConnectionState(CONNECTION_STATE.DISCONNECTED);
				connectionBar.setEnabled(false);
			}
		}
		
		
	}
	
	public void handleEvent(DocearEvent event) {
		if(event.getSource().equals(connectionBar) && 
			WorkspaceDocearServiceConnectionBar.ACTION_COMMAND_TOGGLE_CONNECTION_STATE.equals(event.getEventObject())) {
			allowTransmission = !allowTransmission;
			connectionBar.allowTransmission(allowTransmission);
			setTransmissionStatus();
		}
		if(event.getSource().equals(FiletransferClient.class)) {
			if(FiletransferClient.START_UPLOAD.equals(event.getEventObject())) {
				connectionBar.setConnectionState(CONNECTION_STATE.UPLOADING);
			}
			else if(FiletransferClient.STOP_UPLOAD.equals(event.getEventObject())) {
				setTransmissionStatus();
			}
		}
		
	}

	private void setTransmissionStatus() {
		if(allowTransmission) {
			connectionBar.setConnectionState(CONNECTION_STATE.CONNECTED);
		}
		else {
			connectionBar.setConnectionState(CONNECTION_STATE.INTERRUPTED);
		}
	}
	
	public boolean allowTransmission() {
		return allowTransmission;
	}
	
	public void workspaceChanged(WorkspaceEvent event) {
		WorkspaceController.getController().addToolBar(connectionBar);		
	}
	
	
	
	public void openWorkspace(WorkspaceEvent event) {}

	public void closeWorkspace(WorkspaceEvent event) {}

	public void workspaceReady(WorkspaceEvent event) {}	

	public void toolBarChanged(WorkspaceEvent event) {}

	public void configurationLoaded(WorkspaceEvent event) {}

	public void configurationBeforeLoading(WorkspaceEvent event) {}
}
