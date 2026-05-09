package org.freeplane.plugin.ai;

import java.net.URL;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Properties;

import javax.swing.JTabbedPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.SetBooleanPropertyAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeIterator;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.main.application.CommandLineOptions;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.freeplane.plugin.ai.chat.AIChatPanel;
import org.freeplane.plugin.ai.edits.AIEdits;
import org.freeplane.plugin.ai.edits.AiEditsPersistenceBuilder;
import org.freeplane.plugin.ai.edits.AiEditsSettings;
import org.freeplane.plugin.ai.edits.AiEditsStateIconProvider;
import org.freeplane.plugin.ai.edits.ClearAiMarkersInMapAction;
import org.freeplane.plugin.ai.edits.ClearAiMarkersInSelectionAction;
import org.freeplane.plugin.ai.maps.AvailableMaps;
import org.freeplane.plugin.ai.maps.ControllerMapModelProvider;
import org.freeplane.plugin.ai.mcpserver.ModelContextProtocolServer;
import org.freeplane.plugin.ai.restapi.RestApiServer;
import org.freeplane.plugin.ai.tools.AIToolSetBuilder;
import org.freeplane.plugin.ai.tools.MessageBuilder;
import org.freeplane.plugin.ai.tools.utilities.ToolCaller;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static final String PREFERENCES_RESOURCE = "preferences.xml";
	private static final String SYSTEM_MESSAGE_PROPERTY = MessageBuilder.SYSTEM_MESSAGE_PROPERTY;
	private static final String OPENROUTER_KEY_PROPERTY = "ai_openrouter_key";
	private static final String GEMINI_KEY_PROPERTY = "ai_gemini_key";
	private static final String OLLAMA_API_KEY_PROPERTY = "ai_ollama_api_key";
	private static final String MCP_TOKEN_PROPERTY = "ai_mcp_token";
	private static final String DASHSCOPE_KEY_PROPERTY = "ai_dashscope_key";
	private static final String ERNIE_KEY_PROPERTY = "ai_ernie_key";
	private ModelContextProtocolServer modelContextProtocolServer;
	private RestApiServer restApiServer;
	private AIChatPanel aiChatPanel;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		registerMindMapModeExtension(context);
	}

	private void registerMindMapModeExtension(final BundleContext context) {
		final Hashtable<String, String[]> properties = new Hashtable<String, String[]>();
		properties.put("mode", new String[] { MModeController.MODENAME });
		context.registerService(IModeControllerExtensionProvider.class.getName(),
		    new IModeControllerExtensionProvider() {
			    @Override
				public void installExtension(final ModeController modeController, CommandLineOptions options) {
				    addPluginDefaults();
				    registerAiEditsFeatures(modeController);
				    final JTabbedPane tabs = UITools.getFreeplaneTabbedPanel();
				    aiChatPanel = new AIChatPanel();
				    tabs.addTab("", ResourceController.getResourceController().getIcon("/images/panelTabs/aiTab.svg?useAccentColor=true"),
				        aiChatPanel, TextUtils.getText("ai_panel"));
				    startModelContextProtocolServer(aiChatPanel, modeController);
				    startRestApiServer(aiChatPanel, modeController);
				    addPreferencesToOptionPanel();
				}

				private void addPreferencesToOptionPanel() {
					final URL preferences = this.getClass().getResource(PREFERENCES_RESOURCE);
					if (preferences == null)
						throw new RuntimeException("cannot open preferences");
					final Controller controller = Controller.getCurrentController();
					MModeController modeController = (MModeController) controller.getModeController();
					modeController.getOptionPanelBuilder().load(preferences);
				}

				private void addPluginDefaults() {
					final URL defaults = this.getClass().getResource("defaults.properties");
					Objects.requireNonNull(defaults, "cannot open defaults");
					Properties properties = new Properties();
					ResourceController.loadProperties(properties, defaults);
					ResourceController resourceController = ResourceController.getResourceController();
					resourceController.addDefaults(properties);
					properties.keySet()
					.stream()
					.filter(key -> ! AiEditsSettings.AI_EDITS_STATE_ICON_VISIBLE_PROPERTY.equals(key))
					.forEach(key -> resourceController.securePropertyForReadingAndModification((String) key));
					markSecretsForSeparatePersistence(resourceController);
					setSystemMessageDefault(resourceController);
				}

				private void markSecretsForSeparatePersistence(ResourceController resourceController) {
					resourceController.persistPropertyInSecretsFile(OPENROUTER_KEY_PROPERTY);
					resourceController.persistPropertyInSecretsFile(GEMINI_KEY_PROPERTY);
					resourceController.persistPropertyInSecretsFile(OLLAMA_API_KEY_PROPERTY);
					resourceController.persistPropertyInSecretsFile(MCP_TOKEN_PROPERTY);
					resourceController.persistPropertyInSecretsFile(DASHSCOPE_KEY_PROPERTY);
					resourceController.persistPropertyInSecretsFile(ERNIE_KEY_PROPERTY);
				}

				private void setSystemMessageDefault(ResourceController resourceController) {
					resourceController.securePropertyForReadingAndModification(SYSTEM_MESSAGE_PROPERTY);
				}

				private void registerAiEditsFeatures(ModeController modeController) {
					AiEditsSettings aiEditsSettings = new AiEditsSettings();
					IconController iconController = modeController.getExtension(IconController.class);
					if (iconController != null) {
						iconController.addStateIconProvider(new AiEditsStateIconProvider(aiEditsSettings));
					}
					MapController mapController = modeController.getMapController();
					if (mapController != null) {
						AiEditsPersistenceBuilder persistenceBuilder = new AiEditsPersistenceBuilder(aiEditsSettings);
						persistenceBuilder.registerBy(mapController.getReadManager(), mapController.getWriteManager());
					}
					modeController.addAction(new ClearAiMarkersInMapAction());
					modeController.addAction(new ClearAiMarkersInSelectionAction());
					modeController.addAction(new SetBooleanPropertyAction(
						AiEditsSettings.AI_EDITS_STATE_ICON_VISIBLE_PROPERTY));
					registerAiEditsIconRefreshListener();
				}

				private void registerAiEditsIconRefreshListener() {
					ResourceController.getResourceController().addPropertyChangeListener(
						new IFreeplanePropertyListener() {
							@Override
							public void propertyChanged(String propertyName, String newValue, String oldValue) {
								if (!AiEditsSettings.AI_EDITS_STATE_ICON_VISIBLE_PROPERTY.equals(propertyName)) {
									return;
								}
								refreshAiEditsStateIcons();
							}
						});
				}

				private void refreshAiEditsStateIcons() {
					Controller controller = Controller.getCurrentController();
					if (controller == null || controller.getModeController() == null) {
						return;
					}
					if (controller.getMap() == null || controller.getMap().getRootNode() == null) {
						return;
					}
					IconController iconController = controller.getModeController().getExtension(IconController.class);
					if (iconController == null) {
						return;
					}
					MapController mapController = controller.getModeController().getMapController();
					NodeModel rootNode = controller.getMap().getRootNode();
					NodeIterator<NodeModel> iterator = NodeIterator.of(rootNode, NodeModel::getChildren);
					while (iterator.hasNext()) {
						NodeModel node = iterator.next();
						mapController.nodeRefresh(node, AIEdits.class, null, null);
					}
				}

					private void startModelContextProtocolServer(AIChatPanel aiChatPanel, ModeController modeController) {
						if (modelContextProtocolServer == null) {
							Controller controller = modeController.getController();
							if (controller == null || controller.getViewController() == null) {
								LogUtils.severe("Cannot start MCP server: view controller is not available.");
								return;
							}
							modelContextProtocolServer = new ModelContextProtocolServer(new AIToolSetBuilder()
							    .toolCallSummaryHandler(aiChatPanel.toolCallSummaryHandler())
							    .toolCaller(ToolCaller.MCP)
							    .build(),
								controller.getViewController());
							ResourceController resourceController = ResourceController.getResourceController();
							resourceController.addPropertyChangeListener(modelContextProtocolServer);
						}
					}

				private void startRestApiServer(AIChatPanel aiChatPanel, ModeController modeController) {
					if (restApiServer == null) {
						try {
							AvailableMaps availableMaps = new AvailableMaps(new ControllerMapModelProvider());
							restApiServer = new RestApiServer(availableMaps, aiChatPanel);
							restApiServer.start();
						} catch (Exception e) {
							LogUtils.warn("Cannot start REST API server", e);
						}
					}
				}
		    }, properties);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		if (aiChatPanel != null) {
			aiChatPanel.persistCurrentChatIfNeeded();
		}
		if (modelContextProtocolServer != null) {
			modelContextProtocolServer.stop();
		}
		if (restApiServer != null) {
			restApiServer.stop();
		}
	}
}
