package org.docear.plugin.communications;

import java.io.IOException;
import java.io.StringWriter;

import javax.ws.rs.core.MediaType;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapStyleModel;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.multipart.FormDataMultiPart;

public class Filetransfer {
	private final static String MINDMAP_ID = "mindmapId";
	
	public static synchronized void copyMindmapToServer(CommunicationsConfiguration config) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Filetransfer.class.getClassLoader());
		MapModel map = Controller.getCurrentController().getMap();
		copyMindmapToServer(config, map);
        Thread.currentThread().setContextClassLoader(contextClassLoader);
	}

	public static synchronized void copyMindmapToServer(CommunicationsConfiguration config, MapModel map) {
		System.out.println("insert or update 1");
		ModeController modeController = Controller.getCurrentModeController();
		Controller controller = Controller.getCurrentController();
		StringWriter sw = new StringWriter();
		try {
			modeController.getMapController().getFilteredXml(map, sw, Mode.EXPORT, true);
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}

		String filename = "";
		// TODO: if not saved with a file name then return --> refactor
		try {
			filename = controller.getMap().getFile().getName();
		}
		catch (Exception e) {
			return;
		}

		String xml = sw.toString();
		try {
			insertOrUpdateMindmap(config, xml, filename);

		}
		catch (Exception e) {
			String msg = "Could not copy Mindmap to server: filename: " + filename + "; Username: "
					+ config.getAccount().getUsername() + "; ";
			System.err.println("Error: " + msg);
			System.err.println(e.getMessage());
		}
	}

	private static void insertOrUpdateMindmap(CommunicationsConfiguration config, String mindmap, String filename)
			throws Exception {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Filetransfer.class.getClassLoader());
		byte[] data = Tools.zip(mindmap);
		FormDataMultiPart formDataMultiPart = new FormDataMultiPart();

		formDataMultiPart.field(CommunicationsConfiguration.MIND_MAP, data, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		String username = config.getAccount().getUsername();
		if (username != null) {
			formDataMultiPart.field(CommunicationsConfiguration.USER_NAME, username);
		}
		formDataMultiPart.field(CommunicationsConfiguration.ALLOW_INFORMATION_RETRIEVAL,
				config.isAllowInformationRetrieval() ? "true" : "false");
		formDataMultiPart.field(CommunicationsConfiguration.BACKUP, config.isBackup() ? "true" : "false");
		formDataMultiPart.field(CommunicationsConfiguration.RECOMMENDATIONS, config.isAllowRecommendations() ? "true" : "false");
		formDataMultiPart.field(CommunicationsConfiguration.FILENAME, filename);
		// if (UserDataState.getCurrentState() ==
		// UserDataState.SPLMM_USER_DATA_NEEDED) {
		// formDataMultiPart.field(PASSWORD,
		// SplmmPreferences.getPassword(),
		// MediaType.TEXT_PLAIN_TYPE);
		// }
		formDataMultiPart.field(CommunicationsConfiguration.PASSWORD, config.getAccount().getPassword(),
				MediaType.TEXT_PLAIN_TYPE);

		Controller controller = Controller.getCurrentController();
		final MapStyleModel styleModel = MapStyleModel.getExtension(controller.getMap());		
		String mindmapId = styleModel.getProperty(MINDMAP_ID);
		if (mindmapId == null || mindmapId.trim().length() == 0) {
			insertMindmap(config, formDataMultiPart, username);
		}
		else {
			System.out.println("mindmapID: "+mindmapId);
			updateMindmap(config, formDataMultiPart, mindmapId, username);
		}

		// if (response.getClientResponseStatus() ==
		// ClientResponse.Status.UNAUTHORIZED) {
		// config.validateUserData();
		// }
        Thread.currentThread().setContextClassLoader(contextClassLoader);

	}

	private static void insertMindmap(CommunicationsConfiguration config, FormDataMultiPart formDataMultiPart, String username)
			throws IOException {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Filetransfer.class.getClassLoader());
		LogUtils.info("org.docear.plugin.communications: insert new mindmap on Docear Server");
		ModeController modeController = Controller.getCurrentModeController();
		Controller controller = Controller.getCurrentController();

		ClientResponse response = config.getAccount().getWebresource().path("user/" + username + "/mindmaps")
				.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(ClientResponse.class, formDataMultiPart);
		String mindmapId = response.getEntity(String.class);

		MapStyle mapStyle = (MapStyle) modeController.getExtension(MapStyle.class);
		mapStyle.setProperty(controller.getMap(), MINDMAP_ID, mindmapId);

		if (response.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
			throw new IOException(response.getClientResponseStatus().getStatusCode() + ": "
					+ response.getClientResponseStatus().getReasonPhrase());
		}
        Thread.currentThread().setContextClassLoader(contextClassLoader);
		
	}

	private static void updateMindmap(CommunicationsConfiguration config, FormDataMultiPart formDataMultiPart, String mindmapId,
			String username) throws IOException {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Filetransfer.class.getClassLoader());
		LogUtils.info("org.docear.plugin.communications: update existing mindmap on Docear Server");
		ClientResponse response = config.getAccount().getWebresource().path("user/" + username + "/mindmaps/" + mindmapId)
				.type(MediaType.MULTIPART_FORM_DATA_TYPE).put(ClientResponse.class, formDataMultiPart);

		if (response.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
			throw new IOException(response.getClientResponseStatus().getStatusCode() + ": "
					+ response.getClientResponseStatus().getReasonPhrase());
		}
        Thread.currentThread().setContextClassLoader(contextClassLoader);
	}

}
