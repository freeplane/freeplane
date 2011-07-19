package org.docear.plugin.communications;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.multipart.FormDataMultiPart;

public class Filetransfer {
	public static String insertMindmap(CommunicationsConfiguration config, String mindmap, String fileName) throws Exception {
		try {
			
			//TODO: Logic for Anonymous Users
			byte[] data = Tools.zip(mindmap);
			FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
			
			System.out.println("debug docear: TEST");
			
			formDataMultiPart.field(CommunicationsConfiguration.MIND_MAP, data,
					MediaType.APPLICATION_OCTET_STREAM_TYPE);
			String username = config.getAccount().getUsername();
			
			if (username != null) {
				formDataMultiPart.field(CommunicationsConfiguration.USER_NAME, username);
			} 
			
			formDataMultiPart.field(CommunicationsConfiguration.ALLOW_INFORMATION_RETRIEVAL, 
					config.isAllowInformationRetrieval() ? "true":"false");
			formDataMultiPart.field(CommunicationsConfiguration.BACKUP, 
					config.isBackup() ? "true":"false");
			formDataMultiPart.field(CommunicationsConfiguration.RECOMMENDATIONS, 
					config.isAllowRecommendations() ? "true":"false");
			
			formDataMultiPart.field(CommunicationsConfiguration.FILENAME, fileName);
//			if (UserDataState.getCurrentState() == UserDataState.SPLMM_USER_DATA_NEEDED) {
//				formDataMultiPart.field(PASSWORD,
//						SplmmPreferences.getPassword(),
//						MediaType.TEXT_PLAIN_TYPE);
//			}
			formDataMultiPart.field(CommunicationsConfiguration.PASSWORD, config.getAccount().getPassword(), 
					MediaType.TEXT_PLAIN_TYPE);
			ClientResponse response = config.getAccount().getWebresource()
					.path("user/" + username + "/mindmaps")
					.type(MediaType.MULTIPART_FORM_DATA_TYPE)
					.post(ClientResponse.class, formDataMultiPart);

			String mindmapID = response.getEntity(String.class);
			if (response.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
				throw new IOException(response.getClientResponseStatus().getStatusCode()+": "+
						response.getClientResponseStatus().getReasonPhrase());
			}
			if (response.getClientResponseStatus() == ClientResponse.Status.OK
					&& mindmapID
							.matches("[\\w]{8}-[\\w]{4}-[\\w]{4}-[\\w]{4}-[\\w]{12}")) {
				return mindmapID;
			}
			
//			if (response.getClientResponseStatus() == ClientResponse.Status.UNAUTHORIZED) {
//				config.validateUserData();
//			}
		} 
		catch (Exception e) {
			String msg = "Could not post Mindmap: filename: " + fileName
					+ "; Username: " + config.getAccount().getUsername() + "; ";
			System.err.println("Error: "+msg);
			System.err.println(e.getMessage());
			
			throw e;
		}
		return null;
	}

	public static void updateMindmap(CommunicationsConfiguration config, String mindmap, String mindmapID, String fileName) throws Exception {
//		if (SplmmPreferences.getUserName() == null
//				|| SplmmPreferences.getUserName().isEmpty()
//				|| SplmmPreferences.getUserName().equalsIgnoreCase(
//						LocalizationSupport.message("anonymous")))
//			return;
		try {			
			byte[] data = Tools.zip(mindmap);
			FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
			formDataMultiPart.field(CommunicationsConfiguration.MIND_MAP, data,	MediaType.APPLICATION_OCTET_STREAM_TYPE);
			
			String username = config.getAccount().getUsername();
			if (username != null) {
				formDataMultiPart.field(CommunicationsConfiguration.USER_NAME, username);
			} 
			formDataMultiPart.field(CommunicationsConfiguration.ALLOW_INFORMATION_RETRIEVAL, 
					config.isAllowInformationRetrieval() ? "true":"false");
			formDataMultiPart.field(CommunicationsConfiguration.BACKUP, 
					config.isBackup() ? "true":"false");
			formDataMultiPart.field(CommunicationsConfiguration.RECOMMENDATIONS, 
					config.isAllowRecommendations() ? "true":"false");
			formDataMultiPart.field(CommunicationsConfiguration.FILENAME, fileName);
			
//			if (UserDataState.getCurrentState() == UserDataState.SPLMM_USER_DATA_NEEDED) {
//				formDataMultiPart.field(PASSWORD,
//						SplmmPreferences.getPassword(),
//						MediaType.TEXT_PLAIN_TYPE);
//			}
			formDataMultiPart.field(CommunicationsConfiguration.PASSWORD, config.getAccount().getPassword(),
					MediaType.TEXT_PLAIN_TYPE);

			ClientResponse response = config.getAccount().getWebresource()
					.path("user/" + username + "/mindmaps/" + mindmapID)
					.type(MediaType.MULTIPART_FORM_DATA_TYPE)
					.put(ClientResponse.class, formDataMultiPart);
			
			if (response.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
				throw new IOException(response.getClientResponseStatus().getStatusCode()+": "+
						response.getClientResponseStatus().getReasonPhrase());
			}
			
//			String string = response.getEntity(String.class);
//			if (response.getClientResponseStatus() == ClientResponse.Status.UNAUTHORIZED) {
//				if (UserDataState.getCurrentState() == UserDataState.SPLMM_USER_DATA_NEEDED) {
//					SciPloreWebClient.validateUserData(getUserName(),
//							SplmmPreferences.getPassword());
//				}
//			}
		} catch (Exception e) {
			// JOptionPane.showMessageDialog(null,
			// LocalizationSupport.message("could.not.connect.to.server.to.backup.the.mindmap"),
			// LocalizationSupport.message("problem"), JOptionPane.OK_OPTION);
			String msg = "Could not post Mindmap: filename: " + fileName
					+ "; Username: " + config.getAccount().getUsername() + "; ";
			System.err.println("Error: "+msg);
			System.err.println("Reason: "+e.getMessage());
			e.printStackTrace();
			
			throw(e);
		}
	}
}
