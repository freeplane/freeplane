package org.docear.plugin.communications.features;

import java.net.URISyntaxException;

import javax.ws.rs.core.MultivaluedMap;

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
		// this.client.setReadTimeout(CommunicationsConfiguration.READ_TIMEOUT);
		// this.client.setConnectTimeout(CommunicationsConfiguration.CONNECTION_TIMEOUT);
		Thread.currentThread().setContextClassLoader(contextClassLoader);
	}

	public AccountRegisterer() {

	}

	public void createAnonymousUser() throws DocearServiceException, URISyntaxException {
		String name = createAnonymousUserName();
		createUser(name, null, USER_TYPE_ANONYMOUS, null, null, false, null);
		CommunicationsController.getController().tryToConnect(name, null, false, true);
	}

	public void createRegisteredUser(String name, String password, String email, Integer birthYear, Boolean newsLetter, Boolean isMale) throws DocearServiceException, URISyntaxException {
		createUser(name, password, USER_TYPE_REGISTERED, email, birthYear, newsLetter, isMale);
		ResourceController.getResourceController().setProperty(CommunicationsController.DOCEAR_CONNECTION_USERNAME_PROPERTY, name);
		CommunicationsController.getController().tryToConnect(name, password, true, true);

	}

	private void createUser(String name, String password, Integer type, String email, Integer birthYear, Boolean newsLetter, Boolean isMale) throws DocearServiceException, URISyntaxException {
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
			queryParams.add("userName", name);
			queryParams.add("password", password);
			queryParams.add("retypedPassword", password);
			queryParams.add("userType", "" + type);
			queryParams.add("eMail", email);
			queryParams.add("firstName", null);
			queryParams.add("middleName", null);
			queryParams.add("lastName", null);
			queryParams.add("birthYear", birthYear == null ? null : birthYear.toString());
			queryParams.add("generalNewsLetter", newsLetter == null ? null : newsLetter.toString());
			queryParams.add("isMale", isMale == null ? null : isMale.toString());
	
			WebResource res = client.resource(CommunicationsController.getController().getServiceUri()).path("/user/" + name);
			ClientResponse response = res.post(ClientResponse.class, queryParams);
			
			if (response.getClientResponseStatus() != Status.OK) {
				throw new DocearServiceException(response.getEntity(String.class));
			}
		}
		finally{
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}

	}

	private String createAnonymousUserName() {
		return System.currentTimeMillis() + "_" + CoreUtils.createRandomString(5);
	}

	// public static void main(String[] args) {
	// AccountRegisterer registerer = new AccountRegisterer();
	//
	// //registerer.registerUser("stefan", "qvii-c", "", null, false);
	// response = registerer.createAnonymousUser();
	// }

}
