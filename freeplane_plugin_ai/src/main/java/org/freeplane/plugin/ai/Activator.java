package org.freeplane.plugin.ai;

import java.net.URL;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Properties;
import javax.swing.JTabbedPane;
import org.freeplane.api.ai.AiRequestService;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.SetBooleanPropertyAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.ai.code.AiChatAttachmentService;
import org.freeplane.features.ai.code.AiCodeHostService;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeIterator;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.main.application.CommandLineOptions;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.freeplane.plugin.ai.chat.request.ScriptAiRequestService;
import org.freeplane.plugin.ai.tools.availability.ToolAvailabilityLevelSettings;
import org.freeplane.plugin.ai.chat.ui.AIChatPanel;
import org.freeplane.plugin.ai.tools.code.AiCodeOperationAuthorizer;
import org.freeplane.plugin.ai.tools.formula.FormulaEditingSettings;
import org.freeplane.plugin.ai.code.AttachedEditorChatModeSettings;
import org.freeplane.plugin.ai.code.RoutingAiCodeHostService;
import org.freeplane.plugin.ai.code.SingleEditorAttachmentService;
import org.freeplane.plugin.ai.edits.AIEdits;
import org.freeplane.plugin.ai.edits.AiEditsPersistenceBuilder;
import org.freeplane.plugin.ai.edits.AiEditsSettings;
import org.freeplane.plugin.ai.edits.AiEditsStateIconProvider;
import org.freeplane.plugin.ai.edits.ClearAiMarkersInMapAction;
import org.freeplane.plugin.ai.edits.ClearAiMarkersInSelectionAction;
import org.freeplane.plugin.ai.maps.AvailableMaps;
import org.freeplane.plugin.ai.maps.ControllerMapModelProvider;
import org.freeplane.plugin.ai.mcpserver.ModelContextProtocolAiCodeHostService;
import org.freeplane.plugin.ai.model.AIModelListPreferenceMigration;
import org.freeplane.plugin.ai.mcpserver.ModelContextProtocolServer;
import org.freeplane.plugin.ai.prompt.AiPromptActionRegistry;
import org.freeplane.plugin.ai.prompt.AiPromptMenuInstaller;
import org.freeplane.plugin.ai.tools.AIToolSetBuilder;
import org.freeplane.plugin.ai.tools.MessageBuilder;
import org.freeplane.plugin.ai.tools.documentation.ApiDocumentationMapLoader;
import org.freeplane.plugin.ai.tools.utilities.ToolCaller;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    private static final String PREFERENCES_RESOURCE = "preferences.xml";
    private static final String SYSTEM_MESSAGE_PROPERTY = MessageBuilder.SYSTEM_MESSAGE_PROPERTY;
    private static final String OPENAI_KEY_PROPERTY = "ai_openai_key";
    private static final String OPENROUTER_KEY_PROPERTY = "ai_openrouter_key";
    private static final String REQUESTY_KEY_PROPERTY = "ai_requesty_key";
    private static final String CUSTOM_KEY_PROPERTY = "ai_custom_key";
    private static final String GEMINI_KEY_PROPERTY = "ai_gemini_key";
    private static final String OLLAMA_API_KEY_PROPERTY = "ai_ollama_api_key";
    private static final String MCP_TOKEN_PROPERTY = "ai_mcp_token";
    private ModelContextProtocolServer modelContextProtocolServer;
    private AIChatPanel aiChatPanel;
    private AiPromptActionRegistry promptActionRegistry;
    private ServiceRegistration<?> aiRequestServiceRegistration;
    private ServiceRegistration<?> aiChatAttachmentServiceRegistration;

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
                    MMapController mapController = (MMapController) modeController.getMapController();
                    AvailableMaps availableMaps = new AvailableMaps(new ControllerMapModelProvider(),
                        new ApiDocumentationMapLoader(mapController));
                    mapController.addMapLifeCycleListener(availableMaps);
                    final JTabbedPane tabs = UITools.getFreeplaneTabbedPanel();
                    aiChatPanel = new AIChatPanel(availableMaps);
                    SingleEditorAttachmentService attachmentService =
                        new SingleEditorAttachmentService(aiChatPanel, new AttachedEditorChatModeSettings());
                    RoutingAiCodeHostService codeHostService = new RoutingAiCodeHostService(
                        attachmentService,
                        () -> currentAiOwnedScriptHostService(context));
                    aiChatPanel.setCodeHostService(codeHostService);
                    registerAiChatAttachmentService(context, attachmentService);
                    tabs.addTab("",
                        ResourceController.getResourceController().getIcon(
                            "/images/panelTabs/aiTab.svg?useAccentColor=true"),
                        aiChatPanel,
                        TextUtils.getText("ai_panel"));
                    promptActionRegistry = AiPromptMenuInstaller.install(modeController, aiChatPanel);
                    aiChatPanel.setPromptActionRegistry(promptActionRegistry);
                    registerAiRequestService(context, promptActionRegistry);
                    startModelContextProtocolServer(aiChatPanel, codeHostService, modeController, availableMaps);
                    addPreferencesToOptionPanel();
                }

                private void addPreferencesToOptionPanel() {
                    final URL preferences = this.getClass().getResource(PREFERENCES_RESOURCE);
                    if (preferences == null) {
                        throw new RuntimeException("cannot open preferences");
                    }
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
                    AIModelListPreferenceMigration.migrate(resourceController);
                    properties.keySet()
                        .stream()
                        .map(String.class::cast)
                        .forEach(key -> {
                        if (AiEditsSettings.containsProperty(key))
                            resourceController.securePropertyForModification(key);
                        else
                            resourceController.securePropertyForReadingAndModification(key);
                        });
                    markSecretsForSeparatePersistence(resourceController);
                    setSystemMessageDefault(resourceController);
                }

                private void markSecretsForSeparatePersistence(ResourceController resourceController) {
                    resourceController.persistPropertyInSecretsFile(OPENAI_KEY_PROPERTY);
                    resourceController.persistPropertyInSecretsFile(OPENROUTER_KEY_PROPERTY);
                    resourceController.persistPropertyInSecretsFile(REQUESTY_KEY_PROPERTY);
                    resourceController.persistPropertyInSecretsFile(CUSTOM_KEY_PROPERTY);
                    resourceController.persistPropertyInSecretsFile(GEMINI_KEY_PROPERTY);
                    resourceController.persistPropertyInSecretsFile(OLLAMA_API_KEY_PROPERTY);
                    resourceController.persistPropertyInSecretsFile(MCP_TOKEN_PROPERTY);
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

                private void startModelContextProtocolServer(AIChatPanel aiChatPanel,
                                                             RoutingAiCodeHostService codeHostService,
                                                             ModeController modeController,
                                                             AvailableMaps availableMaps) {
                    if (modelContextProtocolServer == null) {
                        Controller controller = modeController.getController();
                        if (controller == null || controller.getViewController() == null) {
                            LogUtils.severe("Cannot start MCP server: view controller is not available.");
                            return;
                        }
                        ModelContextProtocolAiCodeHostService mcpCodeHostService = new ModelContextProtocolAiCodeHostService(
                            codeHostService,
                            aiChatPanel::clearPendingAiOwnedUserRunFollowup);
                        AiCodeOperationAuthorizer aiCodeOperationAuthorizer = new AiCodeOperationAuthorizer(
                            ToolCaller.MCP,
                            new ToolAvailabilityLevelSettings()::getToolAvailability,
                            null,
                            () -> Boolean.valueOf(new FormulaEditingSettings().isEnabled()),
                            mcpCodeHostService);
                        AIToolSetBuilder toolSetBuilder = new AIToolSetBuilder(availableMaps)
                            .toolCallSummaryHandler(aiChatPanel.toolCallSummaryHandler())
                            .toolCaller(ToolCaller.MCP)
                            .codeHostService(mcpCodeHostService)
                            .aiCodeOperationAuthorizer(aiCodeOperationAuthorizer);
                        modelContextProtocolServer = new ModelContextProtocolServer(
                            toolSetBuilder.buildToolObjects(),
                            aiCodeOperationAuthorizer,
                            controller.getViewController(),
                            mcpCodeHostService,
                            aiChatPanel.toolCallSummaryHandler());
                        ResourceController resourceController = ResourceController.getResourceController();
                        resourceController.addPropertyChangeListener(modelContextProtocolServer);
                    }
                }

                private AiCodeHostService currentAiOwnedScriptHostService(BundleContext context) {
                    if (context == null) {
                        return null;
                    }
                    try {
                        java.util.Collection<ServiceReference<AiCodeHostService>> references =
                            context.getServiceReferences(AiCodeHostService.class, "(scriptHost=AI)");
                        if (references == null || references.isEmpty()) {
                            return null;
                        }
                        ServiceReference<AiCodeHostService> reference = references.iterator().next();
                        return context.getService(reference);
                    } catch (Exception error) {
                        LogUtils.warn(error);
                        return null;
                    }
                }
            },
            properties);
    }

    private void registerAiRequestService(BundleContext context, AiPromptActionRegistry promptActionRegistry) {
        if (aiRequestServiceRegistration != null) {
            aiRequestServiceRegistration.unregister();
        }
        aiRequestServiceRegistration = context.registerService(
            AiRequestService.class.getName(),
            new ScriptAiRequestService(aiChatPanel, promptActionRegistry),
            new Hashtable<String, String[]>());
    }

    private void registerAiChatAttachmentService(BundleContext context,
                                                 SingleEditorAttachmentService attachmentService) {
        if (aiChatAttachmentServiceRegistration != null) {
            aiChatAttachmentServiceRegistration.unregister();
        }
        aiChatAttachmentServiceRegistration = context.registerService(
            AiChatAttachmentService.class.getName(),
            attachmentService,
            new Hashtable<String, String[]>());
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        if (aiRequestServiceRegistration != null) {
            aiRequestServiceRegistration.unregister();
            aiRequestServiceRegistration = null;
        }
        if (aiChatAttachmentServiceRegistration != null) {
            aiChatAttachmentServiceRegistration.unregister();
            aiChatAttachmentServiceRegistration = null;
        }
        if (aiChatPanel != null) {
            aiChatPanel.persistCurrentChatIfNeeded();
        }
        if (promptActionRegistry != null) {
            promptActionRegistry.persistStateIfChanged();
        }
        if (modelContextProtocolServer != null) {
            modelContextProtocolServer.stop();
        }
    }
}
