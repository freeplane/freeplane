package org.docear.plugin.communications;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

import javax.swing.JOptionPane;
import javax.ws.rs.core.MultivaluedMap;

import org.docear.plugin.communications.components.dialog.DocearServiceLoginPanel;
import org.docear.plugin.core.ALanguageController;
import org.freeplane.core.resources.OptionPanelController.PropertyLoadListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.StringProperty;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class CommunicationsController extends ALanguageController implements ActionListener, PropertyLoadListener{
	private final static CommunicationsController communicationsController = new CommunicationsController();

	public CommunicationsController() {
		super();
		
		addPluginDefaults();
		addPropertiesToOptionPanel();		
		
		Controller.getCurrentController().getOptionPanelController().addButtonListener(this);
		Controller.getCurrentController().getOptionPanelController().addPropertyLoadListener(this);
	}
	
	public static CommunicationsController getController() {
		return communicationsController;
	}
	
	
	public void actionPerformed(ActionEvent e) {		
		if("docear_connect".equals(e.getActionCommand())) {
			DocearServiceLoginPanel loginPanel = new DocearServiceLoginPanel();
			int choice = JOptionPane.showConfirmDialog(UITools.getFrame(), loginPanel, TextUtils.getOptionalText("docear.service.connect.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			
			if(choice == JOptionPane.OK_OPTION) {
				tryToConnect(loginPanel.getUsername(), loginPanel.getPassword());
			}
		}
	}
	
	private void tryToConnect(final String username, final String password) {
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		try {
			Client client = Client.create();
			//client.setReadTimeout(CommunicationsConfiguration.READ_TIMEOUT);
			//client.setConnectTimeout(CommunicationsConfiguration.CONNECTION_TIMEOUT);		
			MultivaluedMap<String,String> formParams = new MultivaluedMapImpl();
		    formParams.add("password", password);
			
			ClientResponse response = client.resource(new URI("https://api.docear.org/")).path("/authenticate/"+username).post(ClientResponse.class, formParams);
			Status status = response.getClientResponseStatus();
			
			if(status.equals(Status.OK)) {				
				String token = response.getHeaders().getFirst("accessToken");
				ResourceController.getResourceController().setProperty("docear.service.connect.username", username);
				((StringProperty)Controller.getCurrentController().getOptionPanelController().getPropertyControl("docear.service.connect.username")).setValue(username);
				ResourceController.getResourceController().setProperty("docear.service.connect.token", token);
				JOptionPane.showMessageDialog(UITools.getFrame(), "UserAccessToken: "+token);
				InputStream is = response.getEntityInputStream();
		        while (is.read() > -1);
		        is.close();
			} else {
				ResourceController.getResourceController().setProperty("docear.service.connect.username", "");
				((StringProperty)Controller.getCurrentController().getOptionPanelController().getPropertyControl("docear.service.connect.username")).setValue("");
				ResourceController.getResourceController().setProperty("docear.service.connect.token", "");
				InputStream is = response.getEntityInputStream();
				int chr;
				StringBuilder message = new StringBuilder();
		        while ((chr = is.read()) > -1) {
		        	message.append((char)chr);
		        }
		        is.close();
				JOptionPane.showMessageDialog(UITools.getFrame(), "failed to authenticate: ("+status+") "+message.toString());
			}
		    
		} 
		catch (Exception ex) {
			LogUtils.severe(ex);
		}
		finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
	}
	
	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
	}

	private void addPropertiesToOptionPanel() {
		final URL preferences = this.getClass().getResource("preferences.xml");
		if (preferences == null)
			throw new RuntimeException("cannot open preferences");
		MModeController modeController = (MModeController) Controller
				.getCurrentModeController();

		modeController.getOptionPanelBuilder().load(preferences);
	}

	public void propertiesLoaded(Collection<IPropertyControl> properties) {
		Controller.getCurrentController().getOptionPanelController().getPropertyControl("docear.service.connect.username").setEnabled(false);		
	}
	
	public boolean postFileToDocearService(String restPath, boolean deleteIfTransferred, File... files) {
		if(getUserName() == null || getUserName().trim().length() <= 0) {
			return false;
		}
		FiletransferClient client = new FiletransferClient(restPath, files);
		return client.send(deleteIfTransferred);
	}

	public String getUserName() {
		return ResourceController.getResourceController().getProperty("docear.service.connect.username");
	}

	public URI getServiceUri() throws URISyntaxException {
		return new URI("https://api.docear.org/");
	}

}
