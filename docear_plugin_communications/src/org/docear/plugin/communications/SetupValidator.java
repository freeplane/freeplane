package org.docear.plugin.communications;

import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.ws.rs.core.MultivaluedMap;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.mode.Controller;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;


public class SetupValidator {
	private static Client CLIENT = Client.create();
    static{
        CLIENT.setConnectTimeout(1000);
        CLIENT.setReadTimeout(7000);
    }
	
	private static final String PASSWORD = "password";
    private static final String USER_NAME = "userName";
    
    public enum ValidationState {
    	VALID, NOT_VALID, EXCEPTION, CONNECTION_PROBLEM, SERVICE_DOWN;
    }
	
    public static ValidationState validate() {
    	MultivaluedMap<String,String> formParams = new MultivaluedMap();
	    formParams.add(USER_NAME, username);
	    formParams.add(PASSWORD, password);
        try{
        	ResourceController resController = Controller.getCurrentModeController().getController().getResourceController()formParams;
        	resController.getProperty(key)
        	//System.out.println("docear_save_backup="+this.modecontroller.getController().getResourceController().getProperty("docear_save_backup"));
            ClientResponse response = WEBRESOURCE.path("user").put(ClientResponse.class, formParams);
            switch (response.getClientResponseStatus()) {

                case BAD_REQUEST:
                    JOptionPane.showMessageDialog(null, LocalizationSupport.message("user.name.is.not.allowed.to.be.empty"), LocalizationSupport.message("error"), JOptionPane.OK_OPTION);
                    return ValidationState.NOT_VALID;
                case UNAUTHORIZED:
                    JOptionPane.showMessageDialog(null, LocalizationSupport.message("user.name.or.password.wrong"), LocalizationSupport.message("error"), JOptionPane.OK_OPTION);
                    return ValidationState.NOT_VALID;
                case OK:
                    username = response.getEntity(String.class);
                    SplmmPreferences.setCredentialsValidated(true);
                    return ValidationState.VALID;
                default:                    
                    return ValidationState.EXCEPTION;
            }
        }
        catch(ClientHandlerException e){
            String msg = "Could not validate Userdata:  Username: " +username+"; ";
            SciPloreUtils.log(SciPloreWebClient.class.getName(), msg, Level.WARNING);
            if(isInternetReachable()){
                return ValidationState.SERVICE_DOWN;
            }
            else{
                return ValidationState.CONNECTION_PROBLEM;
            }
        }
    }

}
