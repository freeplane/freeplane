package org.docear.plugin.services;

import java.net.URL;
import java.util.Collection;

import javax.swing.SwingUtilities;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.logging.DocearLogger;
import org.docear.plugin.services.actions.DocearAllowUploadChooserAction;
import org.docear.plugin.services.actions.DocearCheckForUpdatesAction;
import org.docear.plugin.services.actions.DocearClearUserDataAction;
import org.docear.plugin.services.communications.CommunicationsController;
import org.docear.plugin.services.features.UpdateCheck;
import org.docear.plugin.services.features.elements.Application;
import org.docear.plugin.services.listeners.DocearEventListener;
import org.docear.plugin.services.listeners.MapLifeCycleListener;
import org.docear.plugin.services.listeners.ServiceWindowListener;
import org.docear.plugin.services.recommendations.RecommendationEntry;
import org.docear.plugin.services.recommendations.actions.ShowRecommendationsAction;
import org.docear.plugin.services.recommendations.mode.DocearRecommendationsMapController;
import org.docear.plugin.services.recommendations.mode.DocearRecommendationsModeController;
import org.docear.plugin.services.recommendations.workspace.ShowRecommendationsCreator;
import org.docear.plugin.services.recommendations.workspace.ShowRecommendationsNode;
import org.docear.plugin.services.upload.UploadController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceConfiguration;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.event.IWorkspaceEventListener;
import org.freeplane.plugin.workspace.event.WorkspaceEvent;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.WorkspaceRoot;

public class ServiceController extends UploadController {
	public static final String DOCEAR_INFORMATION_RETRIEVAL = "docear_information_retrieval";

	public static final String DOCEAR_SAVE_BACKUP = "docear_save_backup";
	public static final long RECOMMENDATIONS_AUTOSHOW_INTERVAL = 1000*60*60*24*7; // every 7 days in milliseconds


	private static ServiceController serviceController;

	private final IMapLifeCycleListener mapLifeCycleListener = new MapLifeCycleListener();
	public static final int ALLOW_RECOMMENDATIONS = 8;
	public static final int ALLOW_USAGE_MINING = 4;
	public static final int ALLOW_INFORMATION_RETRIEVAL = 2;
	public static final int ALLOW_RESEARCH = 1;
	

	private Application application;
	private DocearRecommendationsModeController modeController;
	private Collection<RecommendationEntry> autoRecommendations;
	private Boolean AUTO_RECOMMENDATIONS_LOCK = false;
	

	

	private ServiceController(ModeController modeController) {
		LogUtils.info("starting DocearBackupStarter()");
		initListeners(modeController);

		new ServiceConfiguration(modeController);
		new ServicePreferences(modeController);

		addPluginDefaults();
		addMenuEntries(modeController);
		Controller.getCurrentController().addAction(new DocearClearUserDataAction());
		Controller.getCurrentController().addAction(new DocearAllowUploadChooserAction());
		Controller.getCurrentController().addAction(new DocearCheckForUpdatesAction());
		Controller.getCurrentController().addAction(new ShowRecommendationsAction());
		
		startRecommendationsMode();
	}

	protected static void initialize(ModeController modeController) {
		if (serviceController == null) {

			serviceController = new ServiceController(modeController);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					serviceController.getUploader().start();
					serviceController.getPacker().start();
				}
			});
			new Thread() {
				public void run() {
					new UpdateCheck();
				}
			}.start();
		}
	}

	private void initListeners(ModeController modeController) {
		DocearController.getController().addDocearEventListener(new DocearEventListener());
		modeController.getMapController().addMapLifeCycleListener(mapLifeCycleListener);
	}

	public static ServiceController getController() {
		return serviceController;
	}

//	public ServiceRunner getBackupRunner() {
//		return backupRunner;
//	}

	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null) throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);

		this.modeController = (DocearRecommendationsModeController) Controller.getCurrentController().getModeController(
				DocearRecommendationsModeController.MODENAME);

		WorkspaceController.getController().addWorkspaceListener(new IWorkspaceEventListener() {

			private boolean workspacePrepared;

			public void workspaceReady(WorkspaceEvent event) {}
			
			public void workspaceChanged(WorkspaceEvent event) {}
			
			public void toolBarChanged(WorkspaceEvent event) {}
			
			public void openWorkspace(WorkspaceEvent event) {}
			
			public void configurationLoaded(WorkspaceEvent event) {
				AWorkspaceTreeNode parent = (AWorkspaceTreeNode) WorkspaceUtils.getModel().getRoot();
				AWorkspaceTreeNode node = WorkspaceUtils.getNodeForPath(((WorkspaceRoot) parent).getName()+"/"+TextUtils.getText("recommendations.workspace.node"));
				if(node == null) {
					node = new ShowRecommendationsNode();
					WorkspaceUtils.getModel().insertNodeTo(node, parent, 0, false);
				}
				else {					
					int index = parent.getChildIndex(node);
					if(index != 0) {
						if(index > 0) { 
							WorkspaceUtils.getModel().removeNodeFromParent(node);
						}
						WorkspaceUtils.getModel().insertNodeTo(node, parent, 0, false);
						parent.refresh();
					}
					
				}
			}
			
			public void closeWorkspace(WorkspaceEvent event) {}
			
			public void configurationBeforeLoading(WorkspaceEvent event) {
				if (!workspacePrepared) {
					WorkspaceController controller = WorkspaceController.getController();
					controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_ACTION, ShowRecommendationsCreator.NODE_TYPE,
							new ShowRecommendationsCreator());
					// modifyContextMenus();
				}
				workspacePrepared = true;

			} 

		});
		
		
	}

	public DocearRecommendationsModeController getRecommenationMode() {
		return this.modeController;
	}

	public boolean isBackupEnabled() {
		return ResourceController.getResourceController().getBooleanProperty(DOCEAR_SAVE_BACKUP);
	}

	public void setBackupEnabled(boolean b) {
		ResourceController.getResourceController().setProperty(DOCEAR_SAVE_BACKUP, b);
	}

	public int getInformationRetrievalCode() {
		return Integer.parseInt(ResourceController.getResourceController().getProperty(DOCEAR_INFORMATION_RETRIEVAL, "0"));
	}

	public boolean isResearchAllowed() {
		return (getInformationRetrievalCode() & ALLOW_RESEARCH) > 0;
	}

	public boolean isInformationRetrievalSelected() {
		return (getInformationRetrievalCode() & ALLOW_INFORMATION_RETRIEVAL) > 0;
	}

	public boolean isUsageMiningAllowed() {
		return (getInformationRetrievalCode() & ALLOW_USAGE_MINING) > 0;
	}

	public boolean isRecommendationsAllowed() {
		return (getInformationRetrievalCode() & ALLOW_RECOMMENDATIONS) > 0;
	}

	public void setInformationRetrievalCode(int code) {
		ResourceController.getResourceController().setProperty(DOCEAR_INFORMATION_RETRIEVAL, "" + code);
	}

	

	public boolean isBackupAllowed() {
		CommunicationsController commCtrl = CommunicationsController.getController();
		return isBackupEnabled() && commCtrl.allowTransmission() && !isEmpty(commCtrl.getRegisteredAccessToken()) && !isEmpty(commCtrl.getRegisteredUserName());
	}

	public boolean isInformationRetrievalAllowed() {
		CommunicationsController commCtrl = CommunicationsController.getController();
		boolean needUser = getInformationRetrievalCode() > 0 && commCtrl.allowTransmission();

		return needUser && (!isEmpty(commCtrl.getAccessToken()) || !isEmpty(commCtrl.getUserName()));
	}

	private boolean isEmpty(String s) {
		return s == null || s.trim().length() == 0;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	private void addMenuEntries(ModeController modeController) {

		// modeController.addMenuContributor(new IMenuContributor() {
		// public void updateMenus(ModeController modeController, MenuBuilder
		// builder) { // /EditDetailsInDialogAction
		// builder.addMenuItem("/menu_bar/extras",new
		// JMenu(TextUtils.getText("docear.recommendations.menu")),
		// "/menu_bar/recommendations", MenuBuilder.BEFORE);
		// builder.addAction("/menu_bar/recommendations", new
		// ShowRecommendationsAction(), MenuBuilder.AS_CHILD);
		// builder.addMenuItem("/node_popup",new
		// JMenu(TextUtils.getText("docear.recommendations.menu")),
		// "/node_popup/recommendations", MenuBuilder.PREPEND);
		// builder.addAction("/node_popup/recommendations", new
		// ShowRecommendationsAction(), MenuBuilder.AS_CHILD);
		// }
		// });
	}

	@Override
	public int getUploadInterval() {
		final ResourceController resourceCtrl = Controller.getCurrentController().getResourceController();
		int backupMinutes = resourceCtrl.getIntProperty("save_backup_automcatically", 0);
		if (backupMinutes <= 0) {
			backupMinutes = 30;
		}
		return backupMinutes;
	}
	
	private void startRecommendationsMode() {
		long lastShowTime = Controller.getCurrentController().getResourceController().getLongProperty("docear.recommendations.last_auto_show", 0);
		
		if(((System.currentTimeMillis()-lastShowTime) > RECOMMENDATIONS_AUTOSHOW_INTERVAL) 
				&& isRecommendationsAllowed()
				&& !isEmpty(CommunicationsController.getController().getUserName())) {
			LogUtils.info("automatically requesting recommendations");
			UITools.getFrame().addWindowListener(new ServiceWindowListener());
						
			
			synchronized (AUTO_RECOMMENDATIONS_LOCK) {
				AUTO_RECOMMENDATIONS_LOCK = true;
			}
			new Thread() {
				public void run() {	
					try {
						Collection<RecommendationEntry> recommendations = DocearRecommendationsMapController.getNewRecommendations(false);	
						if(recommendations.isEmpty()) {
							setAutoRecommendations(null);
						}
						else {
							setAutoRecommendations(recommendations);
						}						
						Controller.getCurrentController().getResourceController().setProperty("docear.recommendations.last_auto_show", Long.toString(System.currentTimeMillis()));
					
					} 
					catch (Exception e) {				
						DocearLogger.warn("org.docear.plugin.services.ServiceController.startRecommendationsMode(): " + e.getMessage());
						setAutoRecommendations(null);
					}
					synchronized (AUTO_RECOMMENDATIONS_LOCK) {
						AUTO_RECOMMENDATIONS_LOCK = false;
					}					
				}
			}.start();
		} 
		else {
			setAutoRecommendations(null);
		}
	}
	
	public void setAutoRecommendations(Collection<RecommendationEntry> autoRecommendations) {
		this.autoRecommendations = autoRecommendations;
	}

	public Collection<RecommendationEntry> getAutoRecommendations() {		
			while(isLocked()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}		
			return autoRecommendations;
	}

	private boolean isLocked() {
		synchronized (AUTO_RECOMMENDATIONS_LOCK ) {
			return AUTO_RECOMMENDATIONS_LOCK;
		}
	}

	public boolean isAutoRecommending() {
		synchronized (AUTO_RECOMMENDATIONS_LOCK ) {
			return AUTO_RECOMMENDATIONS_LOCK;
		}
	}

	
}
