package org.docear.plugin.communications;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.ws.rs.core.MultivaluedMap;

import org.docear.plugin.communications.components.WorkspaceDocearServiceConnectionBar;
import org.docear.plugin.communications.components.WorkspaceDocearServiceConnectionBar.CONNECTION_STATE;
import org.docear.plugin.communications.components.dialog.ConnectionWaitDialog;
import org.docear.plugin.communications.features.AccountRegisterer;
import org.docear.plugin.communications.features.DocearServiceException;
import org.docear.plugin.communications.features.DocearServiceException.DocearServiceExceptionType;
import org.docear.plugin.communications.features.DocearServiceResponse;
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
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.event.IWorkspaceEventListener;
import org.freeplane.plugin.workspace.event.WorkspaceEvent;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class CommunicationsController extends ALanguageController implements PropertyLoadListener, IWorkspaceEventListener, IFreeplanePropertyListener, IDocearEventListener {
	private static CommunicationsController communicationsController;

	private static final Client client;
	static {
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(CommunicationsController.class.getClassLoader());
		client = Client.create();
		Thread.currentThread().setContextClassLoader(contextClassLoader);
	}

	private final WorkspaceDocearServiceConnectionBar connectionBar = new WorkspaceDocearServiceConnectionBar();

	public final static String DOCEAR_CONNECTION_USERNAME_PROPERTY = "docear.service.connect.username";
	public final static String DOCEAR_CONNECTION_TOKEN_PROPERTY = "docear.service.connect.token";
	public final static String DOCEAR_CONNECTION_ANONYMOUS_USERNAME_PROPERTY = "docear.service.connect.anonyous.username";
	public final static String DOCEAR_CONNECTION_ANONYMOUS_TOKEN_PROPERTY = "docear.service.connect.anonymous.token";
	public static final String CONNECTION_BAR_CLICKED = "CONNECTION_BAR_CLICKED";

	private boolean allowTransmission = true;

	private ConnectionWaitDialog waitDialog;

	public CommunicationsController(ModeController modeController) {
		super();

		addPluginDefaults();
		addPropertiesToOptionPanel(modeController);

		Controller.getCurrentController().getOptionPanelController().addPropertyLoadListener(this);
		Controller.getCurrentController().getResourceController().addPropertyChangeListener(this);
		WorkspaceController.getController().addWorkspaceListener(this);
		DocearController.getController().addDocearEventListener(this);

		WorkspaceController.getController().addToolBar(connectionBar);
		propertyChanged(DOCEAR_CONNECTION_TOKEN_PROPERTY, getRegisteredAccessToken(), null);
	}

	protected static CommunicationsController initialize(ModeController modeController) {
		if(communicationsController == null) {
			communicationsController = new CommunicationsController(modeController);
		}
		return communicationsController;
	}
	
	public static CommunicationsController getController() {
		return communicationsController;
	}
	

	public ConnectionWaitDialog getWaitDialog() {
		if(waitDialog == null) {
			waitDialog = new ConnectionWaitDialog();
		}
		return waitDialog;
	}
	
	
	public void tryToConnect(final String username, final String password, final boolean registeredUser, final boolean silent) throws DocearServiceException, URISyntaxException {
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		try {
			startWaitDialog(silent);			
			MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
			formParams.add("password", password);
			
			try {
				WebResource webRes = client.resource(getServiceUri()).path("/authenticate/" + username);
		
				ClientResponse response = webRes.post(ClientResponse.class, formParams);
				Status status = response.getClientResponseStatus();
				stopWaitDialog(silent);
				processResponse(username, registeredUser, silent, response, status);
			}
			catch (URISyntaxException ex) {				
				// DOCEAR: should not happen because the URI is hard coded for now
				stopWaitDialog(silent);
				throw(ex);
			}
			catch (Exception e) {				
				stopWaitDialog(silent);
				if(e instanceof DocearServiceException) {
					throw ((DocearServiceException)e);
				}
				DocearController.getController().dispatchDocearEvent(new DocearEvent(FiletransferClient.class, FiletransferClient.NO_CONNECTION));
				throw(new DocearServiceException(TextUtils.getText("docear.no_connection"), DocearServiceExceptionType.NO_CONNECTION));				
			}

		} finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
	}	
	
	public String getLatestVersionXml(String minStatus) throws Exception {
		if (minStatus == null) {
			return null;
		}
		
		ClientResponse response = client.resource(getServiceUri()).path("/applications/docear/versions/latest").queryParam("minStatus", minStatus).get(ClientResponse.class);
		return response.getEntity(String.class);
	}

	private void processResponse(final String username, final boolean registeredUser, final boolean silent, ClientResponse response, Status status) throws IOException, DocearServiceException {
		if (Status.OK.equals(status)) {
			String token = response.getHeaders().getFirst("accessToken");
			if (!silent) {
				JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.format("docear.service.connect.success", username), TextUtils.getText("docear.service.connect.success.title"), JOptionPane.PLAIN_MESSAGE);
			}
			setConnectionProperties(registeredUser, username, token);
			readResponseContent(response.getEntityInputStream());
		}
		else {
			if (!silent) {						
				throw new DocearServiceException(readResponseContent(response.getEntityInputStream()));
			}
			setConnectionProperties(registeredUser, "", "");
		}
	}

	private void setConnectionProperties(final boolean registeredUser, final String username,  String token) {
		final String propertyUserName = registeredUser ? DOCEAR_CONNECTION_USERNAME_PROPERTY : DOCEAR_CONNECTION_ANONYMOUS_USERNAME_PROPERTY;
		final String propertyAccessToken = registeredUser ? DOCEAR_CONNECTION_TOKEN_PROPERTY : DOCEAR_CONNECTION_ANONYMOUS_TOKEN_PROPERTY;
		
		ResourceController.getResourceController().setProperty(propertyUserName, username);
		if (registeredUser) {
			IPropertyControl ctrl = Controller.getCurrentController().getOptionPanelController().getPropertyControl(propertyUserName);
			if(ctrl != null) {
				((StringProperty) ctrl).setValue(username);
			}
		}
		ResourceController.getResourceController().setProperty(propertyAccessToken, token);
	}
	
	private String readResponseContent(InputStream is) throws IOException {
		int chr;
		StringBuilder message = new StringBuilder();
		while ((chr = is.read()) > -1) {
			message.append((char) chr);
		}
		is.close();
		return message.toString();
	}
	
	public void startWaitDialog(final boolean silent) {
		if (!silent) {
			getWaitDialog().start();
		}
	}

	public void stopWaitDialog(final boolean silent) {
		if (!silent) {
			getWaitDialog().stop();
		}
	}

	public boolean checkConnection() {
		client.setConnectTimeout(1000);
		client.setReadTimeout(1000);
		try {
			MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
			formParams.add("password", "");
			ClientResponse response = client.resource(getServiceUri()).path("/authenticate/anonymous").post(ClientResponse.class, formParams);
			Status status = response.getClientResponseStatus();
			if (status != null) {
				return true;
			}
		} catch (Exception e) {
			if ((e.getCause() instanceof SocketTimeoutException)	// no connection to server
				|| (e.getCause() instanceof ConnectException) // connection refused (no server running
				|| (e.getCause() instanceof UnknownHostException)) // maybe no connection 
			{
				DocearController.getController().dispatchDocearEvent(new DocearEvent(FiletransferClient.class, FiletransferClient.NO_CONNECTION));
			}
		} finally {
			client.setConnectTimeout(5000);
			client.setReadTimeout(10000);
		}
		return false;
	}
	
	public WebResource getServiceResource() {
		try {
			return client.resource(getServiceUri());
		} catch (URISyntaxException e) {
			return null;
		}
	}
	
	public DocearServiceResponse get(String path) {
		try {
			ClientResponse response = client.resource(getServiceUri()).path(path).get(ClientResponse.class);
			Status status = response.getClientResponseStatus();
			if (status != null && status.equals(Status.OK)) {
				return new DocearServiceResponse(org.docear.plugin.communications.features.DocearServiceResponse.Status.OK, response.getEntityInputStream());
			} 
			else if (status != null && status.equals(Status.NO_CONTENT)) {
				return new DocearServiceResponse(org.docear.plugin.communications.features.DocearServiceResponse.Status.NO_CONTENT, response.getEntityInputStream());
			}
			else {
				return new DocearServiceResponse(org.docear.plugin.communications.features.DocearServiceResponse.Status.FAILURE, response.getEntityInputStream());
			}
		}
		catch (ClientHandlerException e) {
			if(e.getCause() instanceof UnknownHostException || e.getCause() instanceof NoRouteToHostException || e.getCause() instanceof SocketTimeoutException || e.getCause() instanceof ConnectException) {
				return new DocearServiceResponse(org.docear.plugin.communications.features.DocearServiceResponse.Status.UNKNOWN_HOST, new ByteArrayInputStream("error".getBytes()));
			}
			else {
				return new DocearServiceResponse(org.docear.plugin.communications.features.DocearServiceResponse.Status.FAILURE, new ByteArrayInputStream("error".getBytes()));
			}
		}
		catch (Exception e) {
			return new DocearServiceResponse(org.docear.plugin.communications.features.DocearServiceResponse.Status.FAILURE, new ByteArrayInputStream("error".getBytes()));
		} 
		
	}

	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
	}

	private void addPropertiesToOptionPanel(ModeController modeController) {
		final URL preferences = this.getClass().getResource("preferences.xml");
		if (preferences == null)
			throw new RuntimeException("cannot open preferences");
		if(modeController instanceof MModeController) {
			((MModeController) modeController).getOptionPanelBuilder().load(preferences);
		}
	}

	public File getCommunicationsQueuePath() {
		return new File(ResourceController.getResourceController().getFreeplaneUserDirectory(), "queue");
	}

	public void propertiesLoaded(Collection<IPropertyControl> properties) {
		Controller.getCurrentController().getOptionPanelController().getPropertyControl(DOCEAR_CONNECTION_USERNAME_PROPERTY).setEnabled(false);
	}

	public boolean postFileToDocearService(String restPath, boolean deleteIfTransferred, File... files) {
		if (!allowTransmission || files.length == 0 || isEmpty(getUserName()) || isEmpty(getAccessToken())) {
			return false;
		}
		FiletransferClient client = new FiletransferClient(restPath, files);
		return client.send(deleteIfTransferred);
	}

	public URI getServiceUri() throws URISyntaxException {
		return new URI("https://api.docear.org/");
		//return new URI("http://127.0.0.1:8080/");
	}

	public String getRegisteredUserName() {
		return ResourceController.getResourceController().getProperty(DOCEAR_CONNECTION_USERNAME_PROPERTY);
	}

	public String getUserName() {
		if (isEmpty(getRegisteredUserName()) || isEmpty(getRegisteredAccessToken())) {
			return getAnonymousUserName();
		}
		else {
			return getRegisteredUserName();
		}
	}

	private String getAnonymousUserName() {
		String userName = ResourceController.getResourceController().getProperty(DOCEAR_CONNECTION_ANONYMOUS_USERNAME_PROPERTY);
		if (isEmpty(userName)) {
			AccountRegisterer ar = new AccountRegisterer();
			try {
				ar.createAnonymousUser();
			}
			catch(Exception e) {
				LogUtils.warn(e);
			}
			userName = ResourceController.getResourceController().getProperty(DOCEAR_CONNECTION_ANONYMOUS_USERNAME_PROPERTY);
		}

		return userName;
	}

	public String getRegisteredAccessToken() {
		return ResourceController.getResourceController().getProperty(DOCEAR_CONNECTION_TOKEN_PROPERTY);
	}

	public String getAccessToken() {
		if (isEmpty(getRegisteredUserName()) || isEmpty(getRegisteredAccessToken())) {
			return getAnonymousAccessToken();
		}
		else {
			return getRegisteredAccessToken();
		}
	}
	
	public void resetRegisteredUser() {
		setConnectionProperties(true, "", "");		
	}

	private String getAnonymousAccessToken() {
		String accessToken = ResourceController.getResourceController().getProperty(DOCEAR_CONNECTION_ANONYMOUS_TOKEN_PROPERTY);
		if (isEmpty(accessToken)) {
			AccountRegisterer ar = new AccountRegisterer();
			try {
				ar.createAnonymousUser();
			}
			catch(Exception e) {
				LogUtils.warn(e);
			}
			accessToken = ResourceController.getResourceController().getProperty(DOCEAR_CONNECTION_ANONYMOUS_USERNAME_PROPERTY);
		}
		return accessToken;
	}

	public void propertyChanged(String propertyName, String newValue, String oldValue) {
		if (DOCEAR_CONNECTION_USERNAME_PROPERTY.equals(propertyName)) {
			connectionBar.setUsername(newValue);
		}
		else if (DOCEAR_CONNECTION_TOKEN_PROPERTY.equals(propertyName)) {
			adjustInfoBarConnectionState();

		}

	}

	private void adjustInfoBarConnectionState() {
		if (getRegisteredAccessToken() != null && getRegisteredAccessToken().trim().length() > 0) {
			connectionBar.setUsername(getRegisteredUserName());
			connectionBar.setEnabled(true);
			if (allowTransmission) {
				connectionBar.setConnectionState(CONNECTION_STATE.CONNECTED);
			}
			else {
				connectionBar.setConnectionState(CONNECTION_STATE.DISABLED);
			}
		}
		else {
			connectionBar.setUsername("");
			connectionBar.setConnectionState(CONNECTION_STATE.NO_CREDENTIALS);
			connectionBar.setEnabled(false);
		}
	}

	public void handleEvent(DocearEvent event) {
		if (event.getSource().equals(connectionBar) &&
				WorkspaceDocearServiceConnectionBar.ACTION_COMMAND_TOGGLE_CONNECTION_STATE.equals(event.getEventObject())) {
			allowTransmission = !allowTransmission;
			connectionBar.allowTransmission(allowTransmission);
			adjustInfoBarConnectionState();
			return;
		}
		if (event.getSource().equals(FiletransferClient.class)) {
			if (FiletransferClient.START_UPLOAD.equals(event.getEventObject())) {
				connectionBar.setConnectionState(CONNECTION_STATE.UPLOADING);
			}
			else if (FiletransferClient.STOP_UPLOAD.equals(event.getEventObject())) {
				adjustInfoBarConnectionState();
			}
			else if (FiletransferClient.NO_CONNECTION.equals(event.getEventObject())) {
				connectionBar.setConnectionState(CONNECTION_STATE.DISCONNECTED);
			}
			return;
		}

	}

	private boolean isEmpty(String s) {
		return s == null || s.trim().length() == 0;
	}

	public boolean allowTransmission() {
		return allowTransmission;
	}

	public void workspaceChanged(WorkspaceEvent event) {
		WorkspaceController.getController().addToolBar(connectionBar);
		SwingUtilities.invokeLater(new Runnable() {			
			public void run() {
				checkConnection();
			}
		});
	}

	public void openWorkspace(WorkspaceEvent event) {
	}

	public void closeWorkspace(WorkspaceEvent event) {							
	}

	public void workspaceReady(WorkspaceEvent event) {
	}

	public void toolBarChanged(WorkspaceEvent event) {
	}

	public void configurationLoaded(WorkspaceEvent event) {
	}

	public void configurationBeforeLoading(WorkspaceEvent event) {
	}

	
}
