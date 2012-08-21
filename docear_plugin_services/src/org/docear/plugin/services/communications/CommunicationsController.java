package org.docear.plugin.services.communications;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.concurrent.CancellationException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.httpclient.auth.AuthScope;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.services.communications.components.WorkspaceDocearServiceConnectionBar;
import org.docear.plugin.services.communications.components.WorkspaceDocearServiceConnectionBar.CONNECTION_STATE;
import org.docear.plugin.services.communications.components.dialog.ConnectionWaitDialog;
import org.docear.plugin.services.communications.components.dialog.ProxyAuthenticationDialog;
import org.docear.plugin.services.communications.features.AccountRegisterer;
import org.docear.plugin.services.communications.features.DocearServiceException;
import org.docear.plugin.services.communications.features.DocearServiceException.DocearServiceExceptionType;
import org.docear.plugin.services.communications.features.DocearServiceResponse;
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
import org.jdesktop.swingworker.SwingWorker;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.core.util.StringKeyStringValueIgnoreCaseMultivaluedMap;
import com.sun.jersey.multipart.impl.MultiPartWriter;

public class CommunicationsController implements PropertyLoadListener, IWorkspaceEventListener, IFreeplanePropertyListener, IDocearEventListener {
	private static CommunicationsController communicationsController;
	private static Boolean PROXY_CREDENTIALS_CANCELED = false;

	public static final String DOCEAR_PROXY_PORT = "docear.proxy_port";
	public static final String DOCEAR_PROXY_HOST = "docear.proxy_host";
	public static final String DOCEAR_USE_PROXY = "docear.use_proxy";
	public static final String DOCEAR_PROXY_USERNAME = "docear.proxy_username";

	private static char[] password = null;

	private boolean proxyDialogOkSelected = false;

	private ProxyAuthenticationDialog dialog = new ProxyAuthenticationDialog();

	private static ApacheHttpClient client = ApacheHttpClient.create();

	static {
		updateClientConfiguration(true);
	}

	private final WorkspaceDocearServiceConnectionBar connectionBar = new WorkspaceDocearServiceConnectionBar();

	public final static String DOCEAR_CONNECTION_USERNAME_PROPERTY = "docear.service.connect.username";
	public final static String DOCEAR_CONNECTION_TOKEN_PROPERTY = "docear.service.connect.token";
	public final static String DOCEAR_CONNECTION_ANONYMOUS_USERNAME_PROPERTY = "docear.service.connect.anonyous.username";
	public final static String DOCEAR_CONNECTION_ANONYMOUS_TOKEN_PROPERTY = "docear.service.connect.anonymous.token";
	public static final String CONNECTION_BAR_CLICKED = "CONNECTION_BAR_CLICKED";
	public static final String CREATING_USER = "__DOCEAR_TRYING_TO_CREATE_ACCOUNT__";

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

	public static CommunicationsController initialize(ModeController modeController) {
		if (communicationsController == null) {
			communicationsController = new CommunicationsController(modeController);
		}
		return communicationsController;
	}

	public static CommunicationsController getController() {
		return communicationsController;
	}

	public static void updateClientConfiguration(boolean prefChanged) {
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(CommunicationsController.class.getClassLoader());
		try {
			DefaultApacheHttpClientConfig cc = new DefaultApacheHttpClientConfig();
			if (ResourceController.getResourceController().getBooleanProperty(DOCEAR_USE_PROXY)) {
				String host = ResourceController.getResourceController().getProperty(DOCEAR_PROXY_HOST, "");
				String port = ResourceController.getResourceController().getProperty(DOCEAR_PROXY_PORT, "");
				String username = ResourceController.getResourceController().getProperty(DOCEAR_PROXY_USERNAME, "");

				cc.getProperties().put(DefaultApacheHttpClientConfig.PROPERTY_PROXY_URI, "http://" + host + ":" + port + "/");

				if (username != null && username.length() > 0 && password != null) {
					try {
						cc.getState().setProxyCredentials(AuthScope.ANY_REALM, host, Integer.parseInt(port), username, new String(password));
					}
					catch (NumberFormatException e) {
						LogUtils.severe(e);
						// UITools.showMessage(TextUtils.getText("docear.proxy.connect.portNumberFormatError.msg"),
						// JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			synchronized (client) {
				if (prefChanged) {
					cc.getClasses().add(MultiPartWriter.class);
					client = ApacheHttpClient.create(cc);
					client.setConnectTimeout(10000);
					client.setReadTimeout(10000);
				}
				else {
					client.getProperties().put(DefaultApacheHttpClientConfig.PROPERTY_HTTP_STATE, cc.getState());
				}
			}
		}
		finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
	}

	public <T> T get(Builder builder, Class<T> c) throws Exception {
		if (SwingUtilities.isEventDispatchThread()) {
			throw new Exception("Never call the webservice from the event dispatch thread.");
		}
		synchronized (dialog) {
			try {
				String accessToken = getAccessToken();				
				if (accessToken != null) {
					builder = builder.header("accessToken", accessToken);
				}
				return builder.get(c);
			}
			catch (Exception e) {
				LogUtils.info(e.getCause().toString());
				if (raiseProxyCredentialsDialog(e)) {
					if (proxyDialogOkSelected) {
						return get(builder, c);
					}
					else {
						throw (e);
					}
				}
				else {
					throw (e);
				}
			}
		}
	}

	public <T> T get(WebResource webResource, Class<T> c) throws Exception {
		return get(webResource.getRequestBuilder(), c);
	}

	public ClientResponse post(WebResource webResource, Object requestEntity) throws Exception {
		return post(webResource.getRequestBuilder(), requestEntity);
	}

	public ClientResponse post(Builder builder, Object requestEntity) throws Exception {
		if (SwingUtilities.isEventDispatchThread()) {
			throw new Exception("Never call the webservice from the event dispatch thread.");
		}
		synchronized (dialog) {
			try {
				String accessToken = getAccessToken();				
				if (accessToken != null) {
					builder = builder.header("accessToken", accessToken);
				}
				return builder.post(ClientResponse.class, requestEntity);
			}
			catch (Exception e) {
				LogUtils.info(e.getMessage());
				if (raiseProxyCredentialsDialog(e)) {
					if (proxyDialogOkSelected) {
						return post(builder, requestEntity);
					}
					else {
						throw (e);
					}
				}
				else {
					throw (e);
				}
			}
		}
	}

	public WebResource getWebResource(URI uri) {
		synchronized (client) {
			WebResource resource = client.resource(uri);			
			return resource;
		}

	}

	private boolean raiseProxyCredentialsDialog(Exception e) {
		if (e instanceof ClientHandlerException && e.getCause() != null && e.getCause().getCause() != null && e.getCause().getCause() instanceof IOException
				&& ResourceController.getResourceController().getBooleanProperty(DOCEAR_USE_PROXY)) {
			proxyDialogOkSelected = false;
			try {
				DocearController.getController().dispatchDocearEvent(new DocearEvent(this, DocearEventType.SHOW_DIALOG, dialog));
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						dialog.showDialog();
						proxyDialogOkSelected = dialog.isOKselected();
						DocearController.getController().dispatchDocearEvent(new DocearEvent(this, DocearEventType.CLOSE_DIALOG, dialog));
					}
				});
			}
			catch (Exception e1) {
				LogUtils.warn(e1);
			}
			return true;
		}
		return false;
	}

	public ConnectionWaitDialog getWaitDialog() {
		if (waitDialog == null) {
			waitDialog = new ConnectionWaitDialog();
		}
		return waitDialog;
	}

	public void tryToConnect(final String username, final String password, final boolean registeredUser, final boolean silent) throws DocearServiceException,
			URISyntaxException, CancellationException {
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		try {

			SwingWorker<Boolean, Void> connectionWorker = new SwingWorker<Boolean, Void>() {

				@Override
				protected Boolean doInBackground() throws Exception {
					MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
					formParams.add("password", password);
					Status status = null;
					try {
						WebResource webRes = getServiceResource().path("/authenticate/" + username);

						ClientResponse response = post(webRes, formParams);
						status = response.getClientResponseStatus();
						boolean connectedSuccessfully = processResponse(username, registeredUser, silent, response, status);
						stopWaitDialog(silent);
						return connectedSuccessfully;
					}
					catch (Exception e) {
						if (this.isCancelled()) {
							throw new CancellationException();
						}
						try {
							stopWaitDialog(silent);
						}
						catch (Exception e1) {
							LogUtils.warn(e1);
						}
						;
						LogUtils.warn(e);
						throw (e);
					}
				}
			};
			getWaitDialog().setWorker(connectionWorker);
			connectionWorker.execute();
			startWaitDialog(silent);
			boolean connectedSuccessfully = connectionWorker.get();
			if (connectedSuccessfully && !silent) {
				JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.format("docear.service.connect.success", username),
						TextUtils.getText("docear.service.connect.success.title"), JOptionPane.PLAIN_MESSAGE);
			}
		}
		catch (Exception e) {
			if (e instanceof CancellationException) {
				throw ((CancellationException) e);
			}
			if (e instanceof URISyntaxException) {
				// DOCEAR: should not happen because the URI is hard coded for
				// now
				throw ((URISyntaxException) e);
			}
			if (e instanceof DocearServiceException) {
				throw ((DocearServiceException) e);
			}
			DocearController.getController().dispatchDocearEvent(new DocearEvent(FiletransferClient.class, FiletransferClient.NO_CONNECTION));
			throw (new DocearServiceException(TextUtils.getText("docear.no_connection"), DocearServiceExceptionType.NO_CONNECTION));
		}
		finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
	}

	public String getLatestVersionXml(String minStatus) throws Exception {
		if (minStatus == null) {
			return null;
		}

		ClientResponse response = get(getServiceResource().path("/applications/docear/versions/latest").queryParam("minStatus", minStatus).header("test", "test"),
				ClientResponse.class);
		return response.getEntity(String.class);
	}

	private boolean processResponse(final String username, final boolean registeredUser, final boolean silent, ClientResponse response, Status status)
			throws IOException, DocearServiceException, InterruptedException, InvocationTargetException {
		if (Status.OK.equals(status)) {
			String token = response.getHeaders().getFirst("accessToken");
			setConnectionProperties(registeredUser, username, token);
			readResponseContent(response.getEntityInputStream());
			return true;
		}
		else {
			setConnectionProperties(registeredUser, "", "");
			if (!silent) {
				throw new DocearServiceException(readResponseContent(response.getEntityInputStream()));
			}
		}
		return false;
	}

	private void setConnectionProperties(final boolean registeredUser, final String username, String token) {
		final String propertyUserName = registeredUser ? DOCEAR_CONNECTION_USERNAME_PROPERTY : DOCEAR_CONNECTION_ANONYMOUS_USERNAME_PROPERTY;
		final String propertyAccessToken = registeredUser ? DOCEAR_CONNECTION_TOKEN_PROPERTY : DOCEAR_CONNECTION_ANONYMOUS_TOKEN_PROPERTY;

		ResourceController.getResourceController().setProperty(propertyUserName, username);
		if (registeredUser) {
			IPropertyControl ctrl = Controller.getCurrentController().getOptionPanelController().getPropertyControl(propertyUserName);
			if (ctrl != null) {
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
		// client.setConnectTimeout(1000);
		// client.setReadTimeout(1000);
		try {
			MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
			formParams.add("password", "");
			ClientResponse response = post(getServiceResource().path("/authenticate/anonymous"), formParams);
			Status status = response.getClientResponseStatus();
			if (status != null) {
				return true;
			}
		}
		catch (Exception e) {
			if ((e.getCause() instanceof SocketTimeoutException) // no
																 // connection
																 // to server
					|| (e.getCause() instanceof ConnectException) // connection
																  // refused (no
																  // server
																  // running
					|| (e.getCause() instanceof UnknownHostException)) // maybe
																	   // no
																	   // connection
			{
				DocearController.getController().dispatchDocearEvent(new DocearEvent(FiletransferClient.class, FiletransferClient.NO_CONNECTION));
			}
		}
		finally {
			// client.setConnectTimeout(5000);
			// client.setReadTimeout(10000);
		}
		return false;
	}

	public WebResource getServiceResource() {		
		WebResource resource = client.resource(getServiceUri());		
		return resource;
	}

	public DocearServiceResponse get(String path) {
		return get(path, null);
	}

	public DocearServiceResponse get(String path, MultivaluedMap<String, String> params) {

		try {
			if (params == null) {
				params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
			}
			
			ClientResponse response = get(getServiceResource().path(path).queryParams(params), ClientResponse.class);
			Status status = response.getClientResponseStatus();
			if (status != null && status.equals(Status.OK)) {
				return new DocearServiceResponse(org.docear.plugin.services.communications.features.DocearServiceResponse.Status.OK,
						response.getEntityInputStream());
			}
			else if (status != null && status.equals(Status.NO_CONTENT)) {
				return new DocearServiceResponse(org.docear.plugin.services.communications.features.DocearServiceResponse.Status.NO_CONTENT,
						response.getEntityInputStream());
			}
			else {
				return new DocearServiceResponse(org.docear.plugin.services.communications.features.DocearServiceResponse.Status.FAILURE,
						response.getEntityInputStream());
			}
		}
		catch (ClientHandlerException e) {
			if (e.getCause() instanceof UnknownHostException || e.getCause() instanceof NoRouteToHostException
					|| e.getCause() instanceof SocketTimeoutException || e.getCause() instanceof ConnectException) {
				return new DocearServiceResponse(org.docear.plugin.services.communications.features.DocearServiceResponse.Status.UNKNOWN_HOST,
						new ByteArrayInputStream("error".getBytes()));
			}
			else {
				return new DocearServiceResponse(org.docear.plugin.services.communications.features.DocearServiceResponse.Status.FAILURE,
						new ByteArrayInputStream("error".getBytes()));
			}
		}
		catch (Exception e) {
			return new DocearServiceResponse(org.docear.plugin.services.communications.features.DocearServiceResponse.Status.FAILURE, new ByteArrayInputStream(
					"error".getBytes()));
		}

	}

	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null) throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
	}

	private void addPropertiesToOptionPanel(ModeController modeController) {
		final URL preferences = this.getClass().getResource("preferences.xml");
		if (preferences == null) throw new RuntimeException("cannot open preferences");
		if (modeController instanceof MModeController) {
			((MModeController) modeController).getOptionPanelBuilder().load(preferences);
		}
	}

	public File getCommunicationsQueuePath() {
		return new File(ResourceController.getResourceController().getFreeplaneUserDirectory(), "queue");
	}

	public void propertiesLoaded(Collection<IPropertyControl> properties) {
		Controller.getCurrentController().getOptionPanelController().getPropertyControl(DOCEAR_CONNECTION_USERNAME_PROPERTY).setEnabled(false);
	}

	// public boolean postFileToDocearService(String restPath, File file,
	// boolean deleteIfTransferred) {
	// if (!allowTransmission || file == null || isEmpty(getUserName()) ||
	// isEmpty(getAccessToken())) {
	// return false;
	// }
	// try {
	// return getFileTransferClient(restPath).sendFile(file,
	// deleteIfTransferred);
	// } catch (DocearServiceException e) {
	// return false;
	// }
	// }

	public boolean transmissionPrepared() {
		if (allowTransmission && !isEmpty(getUserName()) && !isEmpty(getAccessToken())) {
			return true;
		}
		return false;
	}

	public FiletransferClient getFileTransferClient(String restPath) {
		return new FiletransferClient(restPath);
		// if(this.fileTransferClient == null) {
		// this.fileTransferClient = new FiletransferClient(restPath);
		// }
		// return this.fileTransferClient;
	}

	public URI getServiceUri() {
		if (System.getProperty("org.docear.localhost", "false").equals("true")) {
			return URI.create("http://127.0.0.1:8080/");
		}
		return URI.create("https://api.docear.org/");
	}

	public String getRegisteredUserName() {
		return ResourceController.getResourceController().getProperty(DOCEAR_CONNECTION_USERNAME_PROPERTY);
	}

	public String getUserName() {
		if(DocearController.getController().getSemaphoreController().isLocked(CREATING_USER)) {
			return null;
		}
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
			DocearController.getController().getSemaphoreController().lock(CREATING_USER);
			AccountRegisterer ar = new AccountRegisterer();
			try {
				ar.createAnonymousUser();
			}
			catch (Exception e) {
				LogUtils.warn(e);
			}
			userName = ResourceController.getResourceController().getProperty(DOCEAR_CONNECTION_ANONYMOUS_USERNAME_PROPERTY);
			DocearController.getController().getSemaphoreController().unlock(CREATING_USER);
		}

		return userName;
	}

	public String getRegisteredAccessToken() {
		return ResourceController.getResourceController().getProperty(DOCEAR_CONNECTION_TOKEN_PROPERTY);
	}

	public String getAccessToken() {
		if(DocearController.getController().getSemaphoreController().isLocked(CREATING_USER)) {
			return null;
		}
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
			DocearController.getController().getSemaphoreController().lock(CREATING_USER);
			AccountRegisterer ar = new AccountRegisterer();
			try {
				ar.createAnonymousUser();
			}
			catch (Exception e) {
				LogUtils.warn(e);
			}
			accessToken = ResourceController.getResourceController().getProperty(DOCEAR_CONNECTION_ANONYMOUS_USERNAME_PROPERTY);
			DocearController.getController().getSemaphoreController().unlock(CREATING_USER);
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
		else if (DOCEAR_USE_PROXY.equals(propertyName)) {
			updateClientConfiguration(true);
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
		if (event.getSource().equals(connectionBar)
				&& WorkspaceDocearServiceConnectionBar.ACTION_COMMAND_TOGGLE_CONNECTION_STATE.equals(event.getEventObject())) {
			allowTransmission = !allowTransmission;
			connectionBar.allowTransmission(allowTransmission);
			adjustInfoBarConnectionState();
			return;
		}
		if (event.getSource().equals(FiletransferClient.class)) {
			if (FiletransferClient.START_UPLOAD.equals(event.getEventObject())) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						connectionBar.setConnectionState(CONNECTION_STATE.UPLOADING);
					}
				});

			}
			else if (FiletransferClient.STOP_UPLOAD.equals(event.getEventObject())) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						adjustInfoBarConnectionState();
					}
				});
			}
			else if (FiletransferClient.NO_CONNECTION.equals(event.getEventObject())) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						connectionBar.setConnectionState(CONNECTION_STATE.DISCONNECTED);
					}
				});
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
		new Thread() {
			public void run() {
				checkConnection();
			}
		}.start();
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

	public static void setPassword(char[] password) {
		CommunicationsController.password = password;
	}

	public static void setProxyCanceled(boolean b) {
		synchronized (PROXY_CREDENTIALS_CANCELED) {
			PROXY_CREDENTIALS_CANCELED = b;
		}
	}

	public static boolean isProxyCanceled() {
		synchronized (PROXY_CREDENTIALS_CANCELED) {
			return PROXY_CREDENTIALS_CANCELED;
		}
	}

}
