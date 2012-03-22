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
		FileInputStream inStream = null;
		byte[] data;
		for(File file : files) {
			if(!CommunicationsController.getController().allowTransmission()) {
				break;
			}
			try {
				formDataMultiPart = new FormDataMultiPart();
				inStream = new FileInputStream(file);
				int size = inStream.available();
				data = new byte[size];
				if(inStream.read(data) == size) { 
					formDataMultiPart.field("file", data, MediaType.APPLICATION_OCTET_STREAM_TYPE);
					
					WebResource res = client.resource(CommunicationsController.getController().getServiceUri());
					res = res.path("/user/" + CommunicationsController.getController().getUserName() + "/" + this.restFulPath);
					ClientResponse response = res.type(MediaType.MULTIPART_FORM_DATA_TYPE).header("accessToken", CommunicationsController.getController().getAccessToken()).post(ClientResponse.class, formDataMultiPart);					
					if(response==null || !response.getClientResponseStatus().equals(ClientResponse.Status.OK)) {
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
			catch (Exception ex) {
				LogUtils.warn("Could not upload "+ file.getPath(), ex);
				return false;
			}
			finally {
				try {
					inStream.close();
				} 
				catch (Exception e) {				
				}
			}
		}
		return true;		
	}
}
