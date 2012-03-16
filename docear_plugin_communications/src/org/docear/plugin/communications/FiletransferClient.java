package org.docear.plugin.communications;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.freeplane.core.util.LogUtils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;

public class FiletransferClient {
	private final static Client client;
	static {
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(FiletransferClient.class.getClassLoader());
		client = Client.create();
//		this.client.setReadTimeout(CommunicationsConfiguration.READ_TIMEOUT);
//		this.client.setConnectTimeout(CommunicationsConfiguration.CONNECTION_TIMEOUT);		
		Thread.currentThread().setContextClassLoader(contextClassLoader); 
	}
	
	private final File[] files;
	private final String restFulPath;
	
	public FiletransferClient(String restPath, File... files) {
		assert(files != null && files.length > 0);
		
		this.files = files;
		this.restFulPath = restPath;
	}
	
	
	public boolean send(boolean deleteIfTransferred) {
		FormDataMultiPart formDataMultiPart;
		FileInputStream inStream;
		byte[] data;
		for(File file : files) {
			try {
				formDataMultiPart = new FormDataMultiPart();
				inStream = new FileInputStream(file);
				int size = inStream.available();
				data = new byte[size];
				if(inStream.read(data) == size) { 
					formDataMultiPart.field("file", data, MediaType.APPLICATION_OCTET_STREAM_TYPE);
					
					WebResource res = client.resource(CommunicationsController.getController().getServiceUri());
					res = res.path("user/" + CommunicationsController.getController().getUserName() + "/" + this.restFulPath);
					ClientResponse response = res.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(ClientResponse.class, formDataMultiPart);
					if(!response.getClientResponseStatus().equals(ClientResponse.Status.ACCEPTED)) {
						throw new IOException("file upload not accepted ("+ response.getClientResponseStatus()+")");
					}
					else if (deleteIfTransferred) {
						file.delete();
					}
				}
				else {
					throw new IOException("incomplete read");
				}
			} 
			catch (Exception ex) {
				LogUtils.warn("Could not upload "+ file.getPath(), ex);
				return false;
			}
		}
		return true;
		
	}
		
//	public synchronized void copyFileToServer(File file) {
//        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
//        Thread.currentThread().setContextClassLoader(FiletransferClient.class.getClassLoader());
//		MapModel map = Controller.getCurrentController().getMap();
//		copyFileToServer(config, map);
//        Thread.currentThread().setContextClassLoader(contextClassLoader);
//	}
//
//	public static synchronized void copyFileToServer(MapModel map) {		
//		ModeController modeController = Controller.getCurrentModeController();
//		Controller controller = Controller.getCurrentController();
//		StringWriter sw = new StringWriter();
//		try {
//			modeController.getMapController().getFilteredXml(map, sw, Mode.EXPORT, true);
//		}
//		catch (IOException e1) {
//			e1.printStackTrace();
//		}
//
//		String filename = "";
//		// TODO: if not saved with a file name then return --> refactor
//		try {
//			filename = controller.getMap().getFile().getName();
//		}
//		catch (Exception e) {
//			return;
//		}
//
//		String xml = sw.toString();
//		try {
//			insertOrUpdateMindmap(config, xml, filename);
//
//		}
//		catch (Exception e) {
//			String msg = "Could not copy Mindmap to server: filename: " + filename + "; Username: "
//					+ config.getAccount().getUsername() + "; ";
//			System.err.println("Error: " + msg);
//			System.err.println(e.getMessage());
//		}
//	}
//
//	private static void insertOrUpdateMindmap(String mindmap, String filename)
//			throws Exception {
//        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
//        Thread.currentThread().setContextClassLoader(FiletransferClient.class.getClassLoader());
//		byte[] data = Tools.zip(mindmap);
//		FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
//
//		formDataMultiPart.field("file", data, MediaType.APPLICATION_OCTET_STREAM_TYPE);
//		String username = config.getAccount().getUsername();
//		if (username != null) {
//			formDataMultiPart.field(CommunicationsConfiguration.USER_NAME, username);
//		}
//		formDataMultiPart.field(CommunicationsConfiguration.ALLOW_INFORMATION_RETRIEVAL,
//				config.isAllowInformationRetrieval() ? "true" : "false");
//		formDataMultiPart.field(CommunicationsConfiguration.BACKUP, config.isBackup() ? "true" : "false");
//		formDataMultiPart.field(CommunicationsConfiguration.RECOMMENDATIONS, config.isAllowRecommendations() ? "true" : "false");
//		formDataMultiPart.field(CommunicationsConfiguration.FILENAME, filename);
//		// if (UserDataState.getCurrentState() ==
//		// UserDataState.SPLMM_USER_DATA_NEEDED) {
//		// formDataMultiPart.field(PASSWORD,
//		// SplmmPreferences.getPassword(),
//		// MediaType.TEXT_PLAIN_TYPE);
//		// }
//		formDataMultiPart.field(CommunicationsConfiguration.PASSWORD, config.getAccount().getPassword(),
//				MediaType.TEXT_PLAIN_TYPE);
//
//		Controller controller = Controller.getCurrentController();
//		final MapStyleModel styleModel = MapStyleModel.getExtension(controller.getMap());		
//		String mindmapId = styleModel.getProperty(MINDMAP_ID);
//		if (mindmapId == null || mindmapId.trim().length() == 0) {
//			insertMindmap(config, formDataMultiPart, username);
//		}
//		else {
//			System.out.println("mindmapID: "+mindmapId);
//			updateMindmap(config, formDataMultiPart, mindmapId, username);
//		}
//
//		// if (response.getClientResponseStatus() ==
//		// ClientResponse.Status.UNAUTHORIZED) {
//		// config.validateUserData();
//		// }
//        Thread.currentThread().setContextClassLoader(contextClassLoader);
//
//	}
//
//	private static void insertMindmap(CommunicationsConfiguration config, FormDataMultiPart formDataMultiPart, String username)
//			throws IOException {
//        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
//        Thread.currentThread().setContextClassLoader(FiletransferClient.class.getClassLoader());
//		LogUtils.info("org.docear.plugin.communications: insert new mindmap on Docear Server");
//		ModeController modeController = Controller.getCurrentModeController();
//		Controller controller = Controller.getCurrentController();
//
//		ClientResponse response = config.getAccount().getWebresource().path("user/" + username + "/mindmaps")
//				.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(ClientResponse.class, formDataMultiPart);
//		String mindmapId = response.getEntity(String.class);
//
//		MapStyle mapStyle = (MapStyle) modeController.getExtension(MapStyle.class);
//		mapStyle.setProperty(controller.getMap(), MINDMAP_ID, mindmapId);
//
//		if (response.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
//			throw new IOException(response.getClientResponseStatus().getStatusCode() + ": "
//					+ response.getClientResponseStatus().getReasonPhrase());
//		}
//        Thread.currentThread().setContextClassLoader(contextClassLoader);
//		
//	}
//
//	private static void updateMindmap(CommunicationsConfiguration config, FormDataMultiPart formDataMultiPart, String mindmapId,
//			String username) throws IOException {
//        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
//        Thread.currentThread().setContextClassLoader(FiletransferClient.class.getClassLoader());
//		LogUtils.info("org.docear.plugin.communications: update existing mindmap on Docear Server");
//		ClientResponse response = config.getAccount().getWebresource().path("user/" + username + "/mindmaps/" + mindmapId)
//				.type(MediaType.MULTIPART_FORM_DATA_TYPE).put(ClientResponse.class, formDataMultiPart);
//
//		if (response.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
//			throw new IOException(response.getClientResponseStatus().getStatusCode() + ": "
//					+ response.getClientResponseStatus().getReasonPhrase());
//		}
//        Thread.currentThread().setContextClassLoader(contextClassLoader);
//	}

}
