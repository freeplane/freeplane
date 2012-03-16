package org.docear.plugin.communications;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;

import javax.swing.JOptionPane;
import javax.ws.rs.core.MultivaluedMap;

import org.docear.plugin.communications.components.dialog.DocearServiceLoginPanel;
import org.docear.plugin.core.ALanguageController;
import org.freeplane.core.resources.OptionPanelController.PropertyLoadListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.StringProperty;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.accountmanager.AccountManager;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class CommunicationsConfiguration extends ALanguageController implements ActionListener, PropertyLoadListener {
	public static final String PASSWORD = "password";
	public static final String USER_NAME = "userName";
	public static final String ALLOW_INFORMATION_RETRIEVAL = "allowIR"; // allow
																		// information
																		// retrieval
	public static final String BACKUP = "backup";
	public static final String RECOMMENDATIONS = "rec"; // recommendations
	public static final String USAGE_MINING = "useage_mining";
	public static final String MIND_MAP = "mindMap";
	public static final String USER_TYPE = "userType";
	public static final String ANONYMOUS_USER_TYPE = "3";
	public static final String FILENAME = "filename";
	
	public static final String SAVE_BACKUP_PROPERTY = "docear_save_backup";
		
	public static final int READ_TIMEOUT = 10000;
	public static final int CONNECTION_TIMEOUT = 70000;

//	private boolean backup = false;
	private boolean allowInformationRetrieval = false;
	private boolean allowRecommendations = false;
	private boolean allowUsageMining = false;
	
	//private Client client;

	// private static final ResourceController resourceController
	// Controller.getCurrentController().getResourceController();

	public static String USERNAME = "userName";
	public static com.sun.jersey.multipart.impl.MultiPartConfigProvider multipartconfig;

	public enum ValidationState {
		VALID, NOT_VALID, EXCEPTION, CONNECTION_PROBLEM, SERVICE_DOWN;
	}

	private DocearAccount account;

	public CommunicationsConfiguration() {
		super();
		
		addPluginDefaults();
		addPropertiesToOptionPanel();		
		
		Controller.getCurrentController().getOptionPanelController().addButtonListener(this);
		Controller.getCurrentController().getOptionPanelController().addPropertyLoadListener(this);
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		this.account = new DocearAccount();
		AccountManager.registerAccount(account);
        Thread.currentThread().setContextClassLoader(contextClassLoader);

//		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
//		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
//		this.client = Client.create();
//		this.client.setConnectTimeout(CONNECTION_TIMEOUT);
//		this.client.setReadTimeout(READ_TIMEOUT);
//		Thread.currentThread().setContextClassLoader(contextClassLoader);
	}
	
	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
	}

	public DocearAccount getAccount() {
		return account;
	}

//	public void setBackup(boolean backup) {
//		this.backup = backup;
//	}
//
	public boolean isBackup() {
		return Controller.getCurrentController().getResourceController().getBooleanProperty(SAVE_BACKUP_PROPERTY);
	}

	public void setAccount(DocearAccount account) {
		this.account = account;
	}

	public boolean isAllowInformationRetrieval() {
		return allowInformationRetrieval;
	}

	public void setAllowInformationRetrieval(boolean allowInformationRetrieval) {
		this.allowInformationRetrieval = allowInformationRetrieval;
	}

	public boolean isAllowRecommendations() {
		return allowRecommendations;
	}

	public void setAllowRecommendations(boolean allowRecommendations) {
		this.allowRecommendations = allowRecommendations;
	}

	public boolean isAllowUsageMining() {
		return allowUsageMining;
	}

	public void setAllowUsageMining(boolean allowUsageMining) {
		this.allowUsageMining = allowUsageMining;
	}

	public boolean isDocearAccountNeeded() {
		return isBackup();
	}

	private void addPropertiesToOptionPanel() {
		final URL preferences = this.getClass().getResource("preferences.xml");
		if (preferences == null)
			throw new RuntimeException("cannot open preferences");
		MModeController modeController = (MModeController) Controller
				.getCurrentModeController();

		modeController.getOptionPanelBuilder().load(preferences);
	}

	private ValidationState getValidationState() {
		MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
		formParams.add(USERNAME, this.account.getUsername());
		formParams.add(PASSWORD, this.account.getPassword());		
		try {
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		    
			ClientResponse response = this.getAccount().getWebresource()
					.path("user").put(ClientResponse.class, formParams);
            Thread.currentThread().setContextClassLoader(contextClassLoader);
			switch (response.getClientResponseStatus()) {

			case BAD_REQUEST:
				return ValidationState.NOT_VALID;
			case UNAUTHORIZED:
				return ValidationState.NOT_VALID;
			case OK:
				return ValidationState.VALID;
			default:
				return ValidationState.EXCEPTION;
			}
		} catch (Exception e) {
			return ValidationState.EXCEPTION;
		}
	}

	public void validateUserData() {
		this.validateUserData(false);
	}

	public void validateUserData(boolean showValid) {
		ValidationState state = this.getValidationState();
		if (state == ValidationState.VALID) {
			if (showValid) {
				JOptionPane.showMessageDialog(Controller.getCurrentController()
						.getViewController().getFrame(),
						TextUtils.getText("account_credentials_valid"),
						"information", JOptionPane.INFORMATION_MESSAGE);
			}
		} else if (state == ValidationState.NOT_VALID) {
			JOptionPane.showMessageDialog(null,
					TextUtils.getText("account_credentials_invalid"), "error",
					JOptionPane.ERROR_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null,
					TextUtils.getText("webservice_unreachable"), "error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void propertiesLoaded(Collection<IPropertyControl> properties) {
		Controller.getCurrentController().getOptionPanelController().getPropertyControl("docear.service.connect.username").setEnabled(false);
		
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(DocearAccount.VALIDATE)) {
			this.validateUserData(true);
		}
		else if("docear_connect".equals(e.getActionCommand())) {
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
			
			ClientResponse response = client.resource(new URI("https://api.docear.org/")).path("/authenticate/"+username).post(ClientResponse.class, formParams);
			Status status = response.getClientResponseStatus();
			
			if(status.equals(Status.OK)) {				
				String token = response.getHeaders().getFirst("accessToken");
				ResourceController.getResourceController().setProperty("docear.service.connect.username", username);
				((StringProperty)Controller.getCurrentController().getOptionPanelController().getPropertyControl("docear.service.connect.username")).setValue(username);
				ResourceController.getResourceController().setProperty("docear.service.connect.token", token);
				JOptionPane.showMessageDialog(UITools.getFrame(), "UserAccessToken: "+token);
				InputStream is = response.getEntityInputStream();
		        while (is.read() > -1);
		        is.close();
			} else {
				ResourceController.getResourceController().setProperty("docear.service.connect.username", "");
				((StringProperty)Controller.getCurrentController().getOptionPanelController().getPropertyControl("docear.service.connect.username")).setValue("");
				ResourceController.getResourceController().setProperty("docear.service.connect.token", "");
				InputStream is = response.getEntityInputStream();
				int chr;
				StringBuilder message = new StringBuilder();
		        while ((chr = is.read()) > -1) {
		        	message.append((char)chr);
		        }
		        is.close();
				JOptionPane.showMessageDialog(UITools.getFrame(), "failed to authenticate: ("+status+") "+message.toString());
			}
		    
		} 
		catch (Exception ex) {
			LogUtils.severe(ex);
		}
		finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
	}

}
