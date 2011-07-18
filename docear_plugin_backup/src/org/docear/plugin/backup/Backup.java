package org.docear.plugin.backup;

import java.awt.PageAttributes.MediaType;
import java.util.logging.Level;

public class Backup {
//	public static String postMindMap(String mindmap, String fileName) {
//
//		try {
//			byte[] data = Tools.zip(mindmap);
//			FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
//			formDataMultiPart.field(MIND_MAP, data,
//					MediaType.APPLICATION_OCTET_STREAM_TYPE);
//			String username = getUserName();
//			if (username != null) {
//				formDataMultiPart.field(USER_NAME, username);
//			} else {
//				return null;
//			}
//			formDataMultiPart.field(ALLOW_IR, Tools
//					.parseBoolean(SplmmPreferences.getAllowIRonMM()));
//			formDataMultiPart.field(BACKUP, Tools
//					.parseBoolean(SplmmPreferences.getAllowBackup()));
//			formDataMultiPart.field(REC, Tools
//					.parseBoolean(SplmmPreferences.getAllowRecommendations()));
//			formDataMultiPart.field(FILENAME, fileName);
//			if (UserDataState.getCurrentState() == UserDataState.SPLMM_USER_DATA_NEEDED) {
//				formDataMultiPart.field(PASSWORD,
//						SplmmPreferences.getPassword(),
//						MediaType.TEXT_PLAIN_TYPE);
//			}
//			ClientResponse response = WEBRESOURCE
//					.path("user/" + username + "/mindmaps")
//					.type(MediaType.MULTIPART_FORM_DATA_TYPE)
//					.post(ClientResponse.class, formDataMultiPart);
//
//			String mindmapID = response.getEntity(String.class);
//			if (response.getClientResponseStatus() == ClientResponse.Status.OK
//					&& mindmapID
//							.matches("[\\w]{8}-[\\w]{4}-[\\w]{4}-[\\w]{4}-[\\w]{12}")) {
//				return mindmapID;
//			}
//			if (response.getClientResponseStatus() == ClientResponse.Status.UNAUTHORIZED) {
//				if (UserDataState.getCurrentState() == UserDataState.SPLMM_USER_DATA_NEEDED) {
//					SciPloreWebClient.validateUserData(getUserName(),
//							SplmmPreferences.getPassword());
//				}
//			}
//		} catch (ClientHandlerException e) {
//
//			// JOptionPane.showMessageDialog(null,
//			// LocalizationSupport.message("could.not.connect.to.server.to.backup.the.mindmap"),
//			// LocalizationSupport.message("problem"), JOptionPane.OK_OPTION);
//			String msg = "Could not post Mindmap: filename: " + fileName
//					+ "; Username: " + SplmmPreferences.getUserName() + "; ";
//			Tools.log(SciPloreWebClient.class.getName(), msg,
//					Level.WARNING);
//		}
//		return null;
//	}
//
//	public static void putMindMap(String mindmap, String mindmapID,
//			String fileName) {
//		if (SplmmPreferences.getUserName() == null
//				|| SplmmPreferences.getUserName().isEmpty()
//				|| SplmmPreferences.getUserName().equalsIgnoreCase(
//						LocalizationSupport.message("anonymous")))
//			return;
//		try {
//			byte[] data = Tools.zip(mindmap);
//			FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
//			formDataMultiPart.field(MIND_MAP, data,
//					MediaType.APPLICATION_OCTET_STREAM_TYPE);
//			String username = getUserName();
//			if (username != null) {
//				formDataMultiPart.field(USER_NAME, username);
//			} else {
//				return;
//			}
//			formDataMultiPart.field(ALLOW_IR, Tools
//					.parseBoolean(SplmmPreferences.getAllowIRonMM()));
//			formDataMultiPart.field(BACKUP, Tools
//					.parseBoolean(SplmmPreferences.getAllowBackup()));
//			formDataMultiPart.field(REC, Tools
//					.parseBoolean(SplmmPreferences.getAllowRecommendations()));
//			formDataMultiPart.field(FILENAME, fileName);
//			if (UserDataState.getCurrentState() == UserDataState.SPLMM_USER_DATA_NEEDED) {
//				formDataMultiPart.field(PASSWORD,
//						SplmmPreferences.getPassword(),
//						MediaType.TEXT_PLAIN_TYPE);
//			}
//			ClientResponse response = WEBRESOURCE
//					.path("user/" + username + "/mindmaps/" + mindmapID)
//					.type(MediaType.MULTIPART_FORM_DATA_TYPE)
//					.put(ClientResponse.class, formDataMultiPart);
//
//			String string = response.getEntity(String.class);
//			if (response.getClientResponseStatus() == ClientResponse.Status.UNAUTHORIZED) {
//				if (UserDataState.getCurrentState() == UserDataState.SPLMM_USER_DATA_NEEDED) {
//					SciPloreWebClient.validateUserData(getUserName(),
//							SplmmPreferences.getPassword());
//				}
//			}
//		} catch (ClientHandlerException e) {
//			// JOptionPane.showMessageDialog(null,
//			// LocalizationSupport.message("could.not.connect.to.server.to.backup.the.mindmap"),
//			// LocalizationSupport.message("problem"), JOptionPane.OK_OPTION);
//			String msg = "Could not put Mindmap: filename: " + fileName
//					+ "; Username: " + SplmmPreferences.getUserName() + "; ";
//			Tools.log(SciPloreWebClient.class.getName(), msg,
//					Level.WARNING);
//		}
//	}
}
