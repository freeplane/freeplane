package org.docear.plugin.communications;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.docear.plugin.communications.features.DocearServiceException;
import org.docear.plugin.communications.features.DocearServiceException.DocearServiceExceptionType;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.freeplane.core.util.LogUtils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.multipart.FormDataMultiPart;

public class FiletransferClient {
	private final static Client client;
	public static final String START_UPLOAD = "docear.service.upload.start";
	public static final String STOP_UPLOAD = "docear.service.upload.stop";
	public static final String NO_CONNECTION = "docear.service.connection.problem";
	
	
	static {
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(FiletransferClient.class.getClassLoader());
		client = Client.create();
//		this.client.setReadTimeout(CommunicationsConfiguration.READ_TIMEOUT);
//		this.client.setConnectTimeout(CommunicationsConfiguration.CONNECTION_TIMEOUT);		
		Thread.currentThread().setContextClassLoader(contextClassLoader); 
	}

	private WebResource serviceResource;
	
	public FiletransferClient(String restPath) {
		serviceResource = client.resource(CommunicationsController.getController().getServiceUri());
		serviceResource = serviceResource.path("/user/" + CommunicationsController.getController().getUserName() + "/" + restPath);
	}
	
	public boolean sendFile(File file, boolean deleteIfTransferred) throws DocearServiceException {
		if (!CommunicationsController.getController().transmissionPrepared() || file == null) {
			return false;
		}
		DocearController.getController().dispatchDocearEvent(new DocearEvent(this.getClass(), START_UPLOAD));
		FileInputStream inStream = null;
		try {
			inStream = new FileInputStream(file);
			int size = inStream.available();
			byte[] data = new byte[size];
			if(inStream.read(data) == size) {
				FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
				formDataMultiPart.field("file", data, MediaType.APPLICATION_OCTET_STREAM_TYPE);					
				
				String accessToken = CommunicationsController.getController().getAccessToken();
				Builder requestBuilder = serviceResource.type(MediaType.MULTIPART_FORM_DATA_TYPE).header("accessToken", accessToken);
				
				ClientResponse response = requestBuilder.post(ClientResponse.class, formDataMultiPart);					
				if(response==null || !response.getClientResponseStatus().equals(ClientResponse.Status.OK)) {
					//System.out.println(response.getEntity(String.class));
					throw new IOException("file upload not accepted ("+ response+")");
				}
				else if (deleteIfTransferred) {
					inStream.close();
					System.gc();
					file.delete();
				}
			}
			else {
				throw new IOException("incomplete read ("+file.getPath()+")");
			}
		}
		catch(ClientHandlerException ex) {
			DocearController.getController().dispatchDocearEvent(new DocearEvent(this.getClass(), NO_CONNECTION));
			throw new DocearServiceException("no connection to the server", DocearServiceExceptionType.NO_CONNECTION);
		}
		catch (Exception ex) {
			LogUtils.warn("Could not upload "+ file.getPath(), ex);
			DocearController.getController().dispatchDocearEvent(new DocearEvent(this.getClass(), STOP_UPLOAD));
			return false;
		}
		finally {
			try {
				inStream.close();
			} 
			catch (Exception e) {				
			}
		}
		DocearController.getController().dispatchDocearEvent(new DocearEvent(this.getClass(), STOP_UPLOAD));
		return true;
	}
	
	public boolean sendFiles(File[] files, boolean deleteIfTransferred) throws DocearServiceException {
		for(File file : files) {
			if(!CommunicationsController.getController().allowTransmission()) {
				break;
			}
			if(!sendFile(file, deleteIfTransferred)) {
				return false;
			}
		}		
		return true;		
	}
}
