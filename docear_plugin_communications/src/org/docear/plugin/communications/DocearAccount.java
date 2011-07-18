package org.docear.plugin.communications;

import javax.ws.rs.core.MultivaluedMap;

import org.freeplane.plugin.accountmanager.Account;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class DocearAccount extends Account {
	public static final int STATUS_UNTESTED = 0;
	public static final int STATUS_VALID = 1;
	public static final int STATUS_INVALID = 2;
	
	private int status = STATUS_UNTESTED;
	private String anonymous_username;
	
	private Client client;
	
	final public static String VALIDATE = "docear_validate_credentials";
	
	public DocearAccount() {
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		this.client = Client.create();
		this.client.setReadTimeout(CommunicationsConfiguration.READ_TIMEOUT);
		this.client.setConnectTimeout(CommunicationsConfiguration.CONNECTION_TIMEOUT);		
		Thread.currentThread().setContextClassLoader(contextClassLoader);
		
		this.enableButton(DocearAccount.VALIDATE, DocearAccount.VALIDATE);
	}

	@Override
	public String getAccountName() {
		
		return "Docear";
	}

	public WebResource getWebresource() {
		return this.client.resource(this.getConnectionString());
	}
	
	
	// TODO: create anonymous user
	public void createAnounymousUser(String username) {
		MultivaluedMap<String,String> formParams = new MultivaluedMapImpl();
	    formParams.add(CommunicationsConfiguration.USER_NAME, username);
	    //TODO: neue methode zu mr dlib: formParams.add(CommunicationsConfiguration.USER_TYPE, 3);
	    try{
	        ClientResponse response = this.getWebresource().path("user/"+username).post(ClientResponse.class, formParams);
	        ClientResponse.Status status = response.getClientResponseStatus();
	        if(status != null){                                                                               
	            switch (status) {
	                case BAD_REQUEST:
	                    setStatus(STATUS_INVALID);
	                case OK:
	                    setStatus(STATUS_VALID);
	                default:
	                	setStatus(STATUS_VALID);
	            }
	        }
	        else{
	            setStatus(STATUS_INVALID);
	        }
	    }
	    catch(ClientHandlerException e){
	        String msg = "Could not create Anonymous User: Username: " +username+"; ";
	        System.err.println("ERROR: "+msg);
	    }
	    
	    if (getStatus() == STATUS_VALID) {
	    	this.anonymous_username = username;
	    }
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getAnonymous_username() {
		return anonymous_username;
	}

	public void setAnonymous_username(String anonymous_username) {
		this.anonymous_username = anonymous_username;
	}
	
	
}
