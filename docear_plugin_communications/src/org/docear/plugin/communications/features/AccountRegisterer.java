package org.docear.plugin.communications.features;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.docear.plugin.communications.CommunicationsController;
import org.docear.plugin.communications.FiletransferClient;
import org.docear.plugin.core.util.CoreUtils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
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
	
	public String createAnonymousUser() {
		String name = createAnonymousUserName();		
		try {
			if (createUser(name, null, USER_TYPE_ANONYMOUS, null, null, false)) {
				return name;
			}
			else {
				return null;
			}
		} 
		catch (Exception e) {		
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean createRegisteredUser(String name, String password, String email, Integer birthYear, Boolean newsLetter) {		
		try {
			return createUser(name, password, USER_TYPE_REGISTERED, email, birthYear, newsLetter);
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
		
		if (response.getClientResponseStatus().equals(Response.Status.OK)) {
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
