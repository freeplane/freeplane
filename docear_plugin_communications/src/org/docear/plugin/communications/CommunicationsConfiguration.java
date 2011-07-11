package org.docear.plugin.communications;

import java.net.URL;
import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.ws.rs.core.MultivaluedMap;

import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.accountmanager.AccountManager;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;



public class CommunicationsConfiguration {
	public enum ValidationState {
        VALID, NOT_VALID, EXCEPTION, CONNECTION_PROBLEM, SERVICE_DOWN;
    }
	
	public static String USERNAME = "userName";
	private static final String PASSWORD = "password";
	private WebResource webresource = null;
	
	private DocearAccount account;
	
	public CommunicationsConfiguration() {
		this.account = new DocearAccount();
						
		AccountManager.registerAccount(account);
		this.validateUserData();		
		
	}
	
	private ValidationState validateUserData(){
        MultivaluedMap<String,String> formParams = new MultivaluedMapImpl();
	    formParams.add(USERNAME, this.account.getUsername());
	    formParams.add(PASSWORD, this.account.getPassword());
        try{
        	Client client = Client.create();
    		client.setConnectTimeout(10000);
    		client.setReadTimeout(70000);
        	this.webresource = client.resource(this.account.getConnectionString());
            ClientResponse response = this.webresource.path("user").put(ClientResponse.class, formParams);
            switch (response.getClientResponseStatus()) {

                case BAD_REQUEST:
                    JOptionPane.showMessageDialog(null, "user.name.is.not.allowed.to.be.empty", "error", JOptionPane.OK_OPTION);
                    return ValidationState.NOT_VALID;
                case UNAUTHORIZED:
                    JOptionPane.showMessageDialog(null, "user.name.or.password.wrong", "error", JOptionPane.OK_OPTION);
                    return ValidationState.NOT_VALID;
                case OK:
                    String username = response.getEntity(String.class);
                    System.out.println("debug username: "+username);
                    
                    return ValidationState.VALID;
                default:                    
                    return ValidationState.EXCEPTION;
            }
        }
        catch(Exception e){
        	JOptionPane.showMessageDialog(null, "could not connect to webservice", "error", JOptionPane.OK_OPTION);
        	return ValidationState.EXCEPTION;
//            String msg = "Could not validate Userdata:  Username: " +username+"; ";
//            SciPloreUtils.log(SciPloreWebClient.class.getName(), msg, Level.WARNING);
//            if(isInternetReachable()){
//                return ValidationState.SERVICE_DOWN;
//            }
//            else{
//                return ValidationState.CONNECTION_PROBLEM;
//            }
        }
    }
	
	private void addLanguageResources() {
		ResourceBundles resBundle = ((ResourceBundles)Controller.getCurrentModeController().getController().getResourceController().getResources());
		String lang = resBundle.getLanguageCode();
		if (lang == null || lang.equals(ResourceBundles.LANGUAGE_AUTOMATIC)) {
			lang = "en";
		}
		
		final URL res = this.getClass().getResource("/translations/Resources_"+resBundle.getLanguageCode()+".properties");
		System.out.println("DOCEAR res: "+res);
		//resBundle.addResources(resBundle.getLanguageCode(), res);
	}
		
}
