package org.docear.plugin.communications;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.ws.rs.core.MultivaluedMap;

import org.docear.plugin.core.ALanguageController;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.accountmanager.AccountManager;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class CommunicationsConfiguration extends ALanguageController implements ActionListener {
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
		
	public static final int READ_TIMEOUT = 10000;
	public static final int CONNECTION_TIMEOUT = 70000;

	private boolean backup = false;
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
		this.account = new DocearAccount();
		AccountManager.registerAccount(account);

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

	public void setBackup(boolean backup) {
		this.backup = backup;
	}

	public boolean isBackup() {
		return backup;
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
		return this.isBackup();
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
			ClientResponse response = this.getAccount().getWebresource()
					.path("user").put(ClientResponse.class, formParams);
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

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(DocearAccount.VALIDATE)) {
			this.validateUserData(true);
		}
	}

}
