package org.docear.plugin.communications.features;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.docear.plugin.communications.CommunicationsController;
import org.docear.plugin.communications.FiletransferClient;
import org.docear.plugin.core.util.CoreUtils;
import org.freeplane.core.resources.ResourceController;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class AccountRegisterer {
	private static final int USER_TYPE_REGISTERED = 2;
	private static final int USER_TYPE_ANONYMOUS = 3;
	
	private final static Client client;
	
	static {
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(FiletransferClient.class.getClassLoader());
		client = Client.create();
//		this.client.setReadTimeout(CommunicationsConfiguration.READ_TIMEOUT);
//		this.client.setConnectTimeout(CommunicationsConfiguration.CONNECTION_TIMEOUT);		
		Thread.currentThread().setContextClassLoader(contextClassLoader); 
	}
	
	public AccountRegisterer() {
		
	}
	
	public boolean createAnonymousUser() {
		String name = createAnonymousUserName();		
		try {
			if (createUser(name, null, USER_TYPE_ANONYMOUS, null, null, false)) {
				//ResourceController.getResourceController().setProperty(CommunicationsController.DOCEAR_CONNECTION_ANONYMOUS_USERNAME_PROPERTY, name);
				CommunicationsController.getController().tryToConnect(name, null, false, true);
				return true;
			}
			else {
				return false;
			}
		} 
		catch (Exception e) {		
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean createRegisteredUser(String name, String password, String email, Integer birthYear, Boolean newsLetter) {		
		try {
			if (createUser(name, password, USER_TYPE_REGISTERED, email, birthYear, newsLetter)) {
				ResourceController.getResourceController().setProperty(CommunicationsController.DOCEAR_CONNECTION_USERNAME_PROPERTY, name);
				CommunicationsController.getController().tryToConnect(name, password, true, true);
				return true;
			}
			else {
				return false;
			}
		} 
		catch (Exception e) {		
			e.printStackTrace();
			return false;
		}
	}
	
	
	private boolean createUser(String name, String password, Integer type, String email, Integer birthYear, Boolean newsLetter) throws Exception {	
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();				
		queryParams.add("userName", name);
		queryParams.add("password", password);
		queryParams.add("retypedPassword", password);
		queryParams.add("userType", ""+type);
		queryParams.add("eMail", email);
		queryParams.add("firstName", null);
		queryParams.add("middleName", null);
		queryParams.add("lastName", null);
		queryParams.add("birthYear", birthYear==null ? null : birthYear.toString());
		queryParams.add("generalNewsLetter", newsLetter.toString());
		queryParams.add("recommenderNewsLetter", newsLetter.toString());
		queryParams.add("mindmappingNewsLetter", newsLetter.toString());

		WebResource res = client.resource(CommunicationsController.getController().getServiceUri()).path("/user/"+name);
		ClientResponse response = res.post(ClientResponse.class, queryParams);
		
		if (response.getClientResponseStatus() == Status.OK) {
			return true;			
		}
		
		Thread.currentThread().setContextClassLoader(contextClassLoader);
		return false;
	}
	
	private String createAnonymousUserName() {
		return System.currentTimeMillis()+"_"+CoreUtils.createRandomString(5);
	}
	
//	public static void main(String[] args) {
//		AccountRegisterer registerer = new AccountRegisterer();
//		
//		//registerer.registerUser("stefan", "qvii-c", "", null, false);
//		response = registerer.createAnonymousUser();
//	}

}
