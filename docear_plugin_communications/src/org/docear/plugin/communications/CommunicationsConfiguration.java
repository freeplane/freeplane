package org.docear.plugin.communications;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.ws.rs.core.MultivaluedMap;

import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.IPropertyControlCreator;
import org.freeplane.core.resources.components.PropertyBean;
import org.freeplane.core.ui.OptionPanelButtonListener;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.accountmanager.AccountManager;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class CommunicationsConfiguration implements ActionListener {
	public enum ValidationState {
		VALID, NOT_VALID, EXCEPTION, CONNECTION_PROBLEM, SERVICE_DOWN;
	}

	public static String USERNAME = "userName";
	public static com.sun.jersey.multipart.impl.MultiPartConfigProvider multipartconfig;
	private static final String PASSWORD = "password";
	private WebResource webresource = null;

	private DocearAccount account;

	public CommunicationsConfiguration() {
		System.out.println(multipartconfig==null? "null":multipartconfig.toString());
		addPropertiesToOptionPanel();
		
		OptionPanelButtonListener.addButtonListener(this);
		this.account = new DocearAccount();
		AccountManager.registerAccount(account);
	}
	
	private void addPropertiesToOptionPanel() {
		ModeController modeController = Controller.getCurrentModeController();
		ResourceBundles resBundle = ((ResourceBundles)modeController.getController().getResourceController().getResources());
		
		String lang = resBundle.getLanguageCode();
		if (lang == null || lang.equals(ResourceBundles.LANGUAGE_AUTOMATIC)) {
			lang = "en";
		}
		
		final URL res = this.getClass().getResource("/translations/Resources_"+lang+".properties");
		resBundle.addResources(resBundle.getLanguageCode(), res);	
		
	}

	public ValidationState validateUserData() {
		MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
		formParams.add(USERNAME, this.account.getUsername());
		formParams.add(PASSWORD, this.account.getPassword());
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			Client client = Client.create();
			client.setConnectTimeout(10000);
			client.setReadTimeout(70000);
			this.webresource = client.resource(this.account.getConnectionString().trim());
			
			ClientResponse response = this.webresource.path("user").put(
					ClientResponse.class, formParams);
			switch (response.getClientResponseStatus()) {

			case BAD_REQUEST:
				JOptionPane.showMessageDialog(null,
						"user.name.is.not.allowed.to.be.empty", "error",
						JOptionPane.OK_OPTION);
				return ValidationState.NOT_VALID;
			case UNAUTHORIZED:
				JOptionPane.showMessageDialog(null,
						"user.name.or.password.wrong", "error",
						JOptionPane.OK_OPTION);
				return ValidationState.NOT_VALID;
			case OK:
				String username = response.getEntity(String.class);
				System.out.println("debug username: " + username);

				return ValidationState.VALID;
			default:
				return ValidationState.EXCEPTION;
			}
		} catch (Exception e) {			
			return ValidationState.EXCEPTION;
			// String msg = "Could not validate Userdata:  Username: "
			// +username+"; ";
			// SciPloreUtils.log(SciPloreWebClient.class.getName(), msg,
			// Level.WARNING);
			// if(isInternetReachable()){
			// return ValidationState.SERVICE_DOWN;
			// }
			// else{
			// return ValidationState.CONNECTION_PROBLEM;
			// }
		}
		finally{
			Thread.currentThread().setContextClassLoader(contextClassLoader);			
		}
	}

	private void addLanguageResources() {
		ResourceBundles resBundle = ((ResourceBundles) Controller
				.getCurrentModeController().getController()
				.getResourceController().getResources());
		String lang = resBundle.getLanguageCode();
		if (lang == null || lang.equals(ResourceBundles.LANGUAGE_AUTOMATIC)) {
			lang = "en";
		}

		final URL res = this.getClass().getResource(
				"/translations/Resources_" + resBundle.getLanguageCode()
						+ ".properties");
		System.out.println("DOCEAR res: " + res);
		// resBundle.addResources(resBundle.getLanguageCode(), res);
	}

	@Override
	public void actionPerformed(ActionEvent e) {		
		//ResourceController.getResourceController().getPropertyChangeListeners().ge
		System.out.println("DOCEAR actionPerformed: " + e.toString());
		if (e.getActionCommand().equals("docear_validate_credentials")) {			
			ValidationState state = this.validateUserData();
			if (state == ValidationState.VALID) {
				JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getFrame(),
						"account_credentials are valid", "information",
						JOptionPane.INFORMATION_MESSAGE);
			}
			else {
				JOptionPane.showMessageDialog(null,
						"could not connect to webservice", "error",
						JOptionPane.ERROR_MESSAGE);
			}
			
				
		}
	}
	
}
