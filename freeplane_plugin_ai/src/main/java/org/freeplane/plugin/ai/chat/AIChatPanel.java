package org.freeplane.plugin.ai.chat;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.SetBooleanPropertyAction;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.JAutoCheckBoxMenuItem;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.components.html.ScaledEditorKit;
import org.freeplane.core.ui.textchanger.TranslatedElement;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.core.util.MenuUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.text.TextController;
import org.freeplane.plugin.ai.edits.AiEditsSettings;
import org.freeplane.plugin.ai.edits.ClearAiMarkersInMapAction;
import org.freeplane.plugin.ai.edits.ClearAiMarkersInSelectionAction;
import org.freeplane.plugin.ai.maps.AvailableMaps;
import org.freeplane.plugin.ai.maps.ControllerMapModelProvider;
import org.freeplane.plugin.ai.tools.AIToolSetBuilder;
import org.freeplane.plugin.ai.tools.utilities.ToolCallSummaryHandler;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AIChatPanel extends JPanel {
    private static final int TOP_BAR_HORIZONTAL_GAP = 2;

    /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
    private final JEditorPane messageHistoryPane;
    private final HTMLEditorKit messageHistoryEditorKit;
    private final JScrollPane scrollPane;
    private final JTextArea inputArea;
    private final JButton undoButton;
    private final JButton redoButton;
    private final JButton sendButton;
    private final Icon sendIcon;
    private final Icon stopIcon;
    private final Icon preferencesIcon;
    private final Icon assistantProfileIcon;
    private String sendTooltipText;
    private String cancelTooltipText;
    private String undoTooltipText;
    private String redoTooltipText;
    private String preferencesTooltipText;
    private String noProviderConfiguredText;
    private AIChatService chatService;
    private final JPopupMenu menuPopup;
    private final AIProviderConfiguration configuration;
    private final ChatDisplaySettings chatDisplaySettings;
    private final AIModelSelectionController modelSelectionController;
    private ChatMemory chatMemory;
    private final ChatTokenUsageTracker chatTokenUsageTracker;
    private final JLabel tokenUsageLabel;
    private final ChatMessageRenderer messageRenderer;
    private final ChatMessageHistory messageHistory;
    private final ChatMemoryHistoryRenderer chatMemoryHistoryRenderer;
    private final AvailableMaps availableMaps;
    private final DateTimeFormatter chatNameFormatter;
    private final LiveChatController liveChatController;
    private final ChatRequestFlow chatRequestFlow;
    private final AssistantProfileSelectionSync assistantProfileSelectionSync;
    private final AssistantProfilePaneBuilder assistantProfilePaneBuilder;

    public AIChatPanel() {
        setLayout(new BorderLayout());
        messageHistoryPane = new JEditorPane();
        messageHistoryPane.setContentType("text/html");
        messageHistoryEditorKit = ScaledEditorKit.create();
        messageHistoryPane.setEditorKit(messageHistoryEditorKit);
        messageHistoryPane.setEditable(false);
        messageHistoryPane.setOpaque(true);
        messageHistoryPane.setBackground(Color.WHITE);
        inputArea = new JTextArea(3, 20);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        applyChatMessageStyles();
        resetMessageHistory();
        messageHistory = new ChatMessageHistory(messageHistoryPane, messageHistoryEditorKit);
        messageHistoryPane.setTransferHandler(new ChatMessageTransferHandler(messageHistoryPane, messageHistory));
        messageHistoryPane.setDragEnabled(true);
        messageHistoryPane.addHyperlinkListener(
            new ChatHistoryHyperlinkHandler(
                ChatHistoryHyperlinkHandler.defaultLinkControllerAdapter()).createListener());
        configureEmptyHistoryFocusTransfer();
        scrollPane = new JScrollPane(messageHistoryPane);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        undoButton = new JButton("\u21B6");
        redoButton = new JButton("\u21B7");
        sendButton = new JButton();
        sendButton.setIcon(ResourceController.getResourceController()
            .getImageIcon("/images/ai_send_arrow_up.svg?useAccentColor=true"));
        sendIcon = sendButton.getIcon();
        stopIcon = ResourceController.getResourceController()
            .getImageIcon("/images/ai_stop.svg?useAccentColor=true");
        preferencesIcon = ResourceController.getResourceController()
            .getImageIcon("/images/generic_settings.svg?useAccentColor=true");
        assistantProfileIcon = ResourceController.getResourceController()
            .getImageIcon("/images/EggheadCB.svg?useAccentColor=true");
        Dimension sendButtonSize = sendButton.getPreferredSize();
        Dimension sideButtonSize = new Dimension(sendButtonSize.width, Math.max(1, sendButtonSize.height / 2));
        Dimension tallSendButtonSize = new Dimension(sendButtonSize.width, sideButtonSize.height * 2);
        sendButton.setPreferredSize(tallSendButtonSize);
        sendButton.setMinimumSize(tallSendButtonSize);
        sendButton.setMaximumSize(tallSendButtonSize);
        undoButton.setPreferredSize(sideButtonSize);
        undoButton.setMinimumSize(sideButtonSize);
        undoButton.setMaximumSize(sideButtonSize);
        redoButton.setPreferredSize(sideButtonSize);
        redoButton.setMinimumSize(sideButtonSize);
        redoButton.setMaximumSize(sideButtonSize);
        menuPopup = buildMenuPopup();
        configuration = new AIProviderConfiguration();
        chatDisplaySettings = new ChatDisplaySettings();
        modelSelectionController = new AIModelSelectionController(configuration, new AIModelCatalog(configuration));
        modelSelectionController.setModelSelectionChangeListener(modelDescriptor -> chatService = null);
        AssistantProfileSelectionModel assistantProfileSelectionModel = new AssistantProfileSelectionModel();
        chatMemory = createChatMemory();
        tokenUsageLabel = new JLabel();
        tokenUsageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        chatTokenUsageTracker = new ChatTokenUsageTracker(this::updateTokenUsageLabel);
        messageRenderer = new ChatMessageRenderer();
        chatMemoryHistoryRenderer = new ChatMemoryHistoryRenderer(messageHistory, messageRenderer);
        availableMaps = new AvailableMaps(new ControllerMapModelProvider());
        chatNameFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        liveChatController = new LiveChatController(
            this,
            availableMaps,
            requireTextController(),
            chatNameFormatter,
            this::activateSession,
            chatTokenUsageTracker::snapshotState
        );
        assistantProfileSelectionSync = new AssistantProfileSelectionSync(
            assistantProfileSelectionModel,
            liveChatController);
        assistantProfileSelectionSync.setChatMemory(chatMemory);
        assistantProfileSelectionSync.setProfileMessageConsumer(this::appendProfileMessage);
        assistantProfilePaneBuilder = new AssistantProfilePaneBuilder(
            assistantProfileSelectionModel,
            assistantProfileSelectionSync,
            assistantProfileIcon);
        chatRequestFlow = new ChatRequestFlow(new ChatRequestFlow.RequestCallbacks() {
            @Override
            public void onRequestStarted() {
                inputArea.setEditable(false);
                setSendButtonStopState();
                updateUndoRedoButtonState();
            }

            @Override
            public void onRequestFinished() {
                liveChatController.synchronizeTranscriptWithMemory();
                updateInputState();
            }

            @Override
            public void onUserTextRestored(String userText) {
                inputArea.setText(userText == null ? "" : userText);
                inputArea.setCaretPosition(inputArea.getText().length());
            }

            @Override
            public void onRequestFailed(String userText, String errorMessage) {
                appendFailureMessages(userText, errorMessage);
            }

            @Override
            public void onAssistantResponse(String text) {
                appendChatMessage(text, ChatMessageCategory.ASSISTANT);
                refreshTokenCounters();
            }

            @Override
            public void onAssistantError(String text) {
            }

            @Override
            public int snapshotMemorySize() {
                return getMemorySize();
            }

            @Override
            public void truncateMemoryToSize(int size) {
                AIChatPanel.this.truncateMemoryToSize(size);
            }

            @Override
            public void synchronizeTranscriptWithMemory() {
                liveChatController.synchronizeTranscriptWithMemory();
            }

            @Override
            public void rebuildHistoryFromTranscript() {
                AIChatPanel.this.rebuildHistoryFromMemory();
            }

            @Override
            public boolean evictOldestTurn() {
                AssistantProfileChatMemory memory = activeAssistantProfileChatMemory();
                if (memory == null || !memory.evictOldestTurn()) {
                    return false;
                }
                return true;
            }

            @Override
            public void onPostResponseEviction() {
                liveChatController.synchronizeTranscriptWithMemory();
                rebuildHistoryFromMemory();
                updateInputState();
            }

            @Override
            public void refreshTokenCounters() {
                AIChatPanel.this.refreshTokenCounters();
            }

            @Override
            public boolean isToolCallHistoryVisible() {
                return chatDisplaySettings.isToolCallHistoryVisible();
            }

            @Override
            public void onToolSummaryAppended(ChatMemoryRenderEntry entry) {
                AIChatPanel.this.appendHistoryEntry(entry);
            }
        }, chatTokenUsageTracker);
        chatRequestFlow.updateChatMemory(activeAssistantProfileChatMemory());
        liveChatController.initialize(chatMemory);
        assistantProfilePaneBuilder.initialize();

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
        JPanel actionButtonsPanel = new JPanel(new BorderLayout(4, 0));
        JPanel undoRedoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        undoRedoPanel.add(undoButton);
        undoRedoPanel.add(redoButton);
        actionButtonsPanel.add(sendButton, BorderLayout.WEST);
        actionButtonsPanel.add(undoRedoPanel, BorderLayout.EAST);
        inputPanel.add(actionButtonsPanel, BorderLayout.EAST);
        JPanel inputContainer = new JPanel(new BorderLayout());
        inputContainer.add(assistantProfilePaneBuilder.buildPanel(), BorderLayout.NORTH);
        inputContainer.add(inputPanel, BorderLayout.CENTER);
        JPanel tokenUsagePanel = new JPanel(new BorderLayout());
        tokenUsagePanel.add(tokenUsageLabel, BorderLayout.EAST);
        inputContainer.add(tokenUsagePanel, BorderLayout.SOUTH);

        JPanel topBarContainer = buildTopBarPanel();

        add(scrollPane, BorderLayout.CENTER);
        add(inputContainer, BorderLayout.SOUTH);
        add(topBarContainer, BorderLayout.NORTH);

        sendButton.addActionListener(event -> {
            if (isRequestActive()) {
                cancelActiveRequest();
            } else if (!isProviderConfigured()) {
                openPreferences();
            } else {
                sendMessage();
            }
        });
        undoButton.addActionListener(event -> undoLastTurn());
        redoButton.addActionListener(event -> redoLastTurn());
        int shortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        KeyStroke sendKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, shortcutMask);
        KeyStroke undoKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_UP, shortcutMask);
        KeyStroke redoKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, shortcutMask);
        sendTooltipText = TextUtils.format("ai_chat_send.tooltip", MenuUtils.formatKeyStroke(sendKeyStroke));
        KeyStroke cancelKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        cancelTooltipText = TextUtils.format("ai_chat_cancel.tooltip", MenuUtils.formatKeyStroke(cancelKeyStroke));
        undoTooltipText = TextUtils.getText("simplyhtml.undoLabel")
            + " (" + MenuUtils.formatKeyStroke(undoKeyStroke) + ")";
        redoTooltipText = TextUtils.getText("simplyhtml.redoLabel")
            + " (" + MenuUtils.formatKeyStroke(redoKeyStroke) + ")";
        preferencesTooltipText = TextUtils.getText("preferences");
        noProviderConfiguredText = TextUtils.getText("ai_chat_no_provider_configured");
        sendButton.setToolTipText(sendTooltipText);
        undoButton.setToolTipText(undoTooltipText);
        redoButton.setToolTipText(redoTooltipText);
        messageHistoryPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_A, shortcutMask), "selectAllMessages");
        messageHistoryPane.getActionMap().put("selectAllMessages", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                messageHistoryPane.selectAll();
            }
        });
        inputArea.getInputMap().put(sendKeyStroke, "sendMessage");
        inputArea.getInputMap().put(undoKeyStroke, "undoTurn");
        inputArea.getInputMap().put(redoKeyStroke, "redoTurn");
        inputArea.getActionMap().put("sendMessage", new AbstractAction() {
            /**
			 * Comment for <code>serialVersionUID</code>
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent event) {
                if (isRequestActive()) {
                    cancelActiveRequest();
                } else if (!isProviderConfigured()) {
                    return;
                } else {
                    sendMessage();
                }
            }
        });
        inputContainer.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(cancelKeyStroke, "cancelRequest");
        inputContainer.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(undoKeyStroke, "undoTurn");
        inputContainer.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(redoKeyStroke, "redoTurn");
        inputContainer.getActionMap().put("cancelRequest", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                cancelActiveRequest();
            }
        });
        inputContainer.getActionMap().put("undoTurn", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                undoLastTurn();
            }
        });
        inputContainer.getActionMap().put("redoTurn", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                redoLastTurn();
            }
        });
        modelSelectionController.loadInitialModelSelectionList();
        registerProviderConfigurationListener();
        registerModelSelectionRefreshListener();
        registerTokenCounterModeListener();
        registerChatFontScalingListener();
        refreshTokenCounterMode();
        updateInputState();
    }

    private JPanel buildTopBarPanel() {
        JPanel topBar = new JPanel(new BorderLayout(TOP_BAR_HORIZONTAL_GAP, 0));
        JButton menuButton = new JButton("\u2261");
        TranslatedElementFactory.createTooltip(menuButton, "preferences");
        menuButton.addActionListener(event -> menuPopup.show(menuButton, 0, menuButton.getHeight()));
        topBar.add(menuButton, BorderLayout.WEST);
        topBar.add(modelSelectionController.getModelSelectionComboBox(), BorderLayout.CENTER);
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, TOP_BAR_HORIZONTAL_GAP, 0));
        String historyIconPath = "/images/ai_history.svg?useAccentColor=true";
        JButton chatsButton = TranslatedElementFactory.createButtonWithIcon(historyIconPath, "ai_chat_chats");
        chatsButton.addActionListener(event -> {
            cancelActiveRequest();
            liveChatController.openLiveChats();
        });
        rightButtons.add(chatsButton);
        String clearIconPath = "/images/ai_new_chat.svg?useAccentColor=true";
        JButton newChatButton = TranslatedElementFactory.createButtonWithIcon(clearIconPath, "ai_chat_new_chat");
        newChatButton.addActionListener(event -> {
            cancelActiveRequest();
            liveChatController.startNewChat();
        });
        rightButtons.add(newChatButton);
        topBar.add(rightButtons, BorderLayout.EAST);
        return topBar;
    }

    private JPopupMenu buildMenuPopup() {
        JPopupMenu menuPopup = new JPopupMenu();
        Action openPreferencesAction = new AbstractAction("Preferences") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                openPreferences();
            }
        };
        JMenuItem preferencesMenuItem = TranslatedElementFactory.createMenuItem(openPreferencesAction, "preferences");
        preferencesMenuItem.setIcon(preferencesIcon);
        menuPopup.add(preferencesMenuItem);
        Action manageProfilesAction = new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                assistantProfilePaneBuilder.openAssistantProfileManager();
            }
        };
        JMenuItem manageProfilesMenuItem = TranslatedElementFactory.createMenuItem(
            manageProfilesAction,
            AssistantProfilePaneBuilder.MANAGE_PROFILES_TEXT_KEY);
        manageProfilesMenuItem.setIcon(assistantProfileIcon);
        menuPopup.add(manageProfilesMenuItem);
        addAiEditsMenuItems(menuPopup);
        return menuPopup;
    }

    private void addAiEditsMenuItems(JPopupMenu menuPopup) {
        if (Controller.getCurrentModeController() == null) {
            return;
        }
        AFreeplaneAction clearMapAction = Controller.getCurrentModeController()
            .getAction(ClearAiMarkersInMapAction.ACTION_KEY);
        AFreeplaneAction clearSelectionAction = Controller.getCurrentModeController()
            .getAction(ClearAiMarkersInSelectionAction.ACTION_KEY);
        AFreeplaneAction showIconAction = Controller.getCurrentModeController()
            .getAction(SetBooleanPropertyAction.actionKey(AiEditsSettings.AI_EDITS_STATE_ICON_VISIBLE_PROPERTY));
        if (clearMapAction == null && clearSelectionAction == null && showIconAction == null) {
            return;
        }
        menuPopup.addSeparator();
        addMenuItem(menuPopup, clearMapAction);
        addMenuItem(menuPopup, clearSelectionAction);
        addToggleMenuItem(menuPopup, showIconAction);
    }

    private void addMenuItem(JPopupMenu menuPopup, AFreeplaneAction action) {
        if (action == null) {
            return;
        }
        menuPopup.add(TranslatedElementFactory.createMenuItem(action, action.getTextKey()));
    }

    private void addToggleMenuItem(JPopupMenu menuPopup, AFreeplaneAction action) {
        if (action == null) {
            return;
        }
        String labelKey = action.getTextKey();
        JCheckBoxMenuItem menuItem = new JAutoCheckBoxMenuItem(action);
        LabelAndMnemonicSetter.setLabelAndMnemonic(menuItem, TextUtils.getRawText(labelKey));
        TranslatedElement.TEXT.setKey(menuItem, labelKey);
        TranslatedElementFactory.createTooltip(menuItem, action.getTooltipKey());
        menuPopup.add(menuItem);
    }

    private void openPreferences() {
        Controller controller = Controller.getCurrentController();
        MModeController modeController = (MModeController) controller.getModeController(MModeController.MODENAME);
        modeController.showPreferences("plugins", "ai");
    }

    private void registerModelSelectionRefreshListener() {
        ResourceController.getResourceController().addPropertyChangeListener(
            new IFreeplanePropertyListener() {
                @Override
                public void propertyChanged(String propertyName, String newValue, String oldValue) {
                    if (!isModelSelectionRefreshProperty(propertyName)) {
                        return;
                    }
                    SwingUtilities.invokeLater(() -> modelSelectionController.loadInitialModelSelectionList());
                }
            });
    }

    private void registerProviderConfigurationListener() {
        ResourceController.getResourceController().addPropertyChangeListener(
            new IFreeplanePropertyListener() {
                @Override
                public void propertyChanged(String propertyName, String newValue, String oldValue) {
                    if (!isProviderConfigurationProperty(propertyName)) {
                        return;
                    }
                    SwingUtilities.invokeLater(() -> updateInputState());
                }
            });
    }

    private void registerTokenCounterModeListener() {
        ResourceController.getResourceController().addPropertyChangeListener(
            new IFreeplanePropertyListener() {
                @Override
                public void propertyChanged(String propertyName, String newValue, String oldValue) {
                    if (!ChatTokenCounterSettings.CHAT_TOKEN_COUNTER_MODE_PROPERTY.equals(propertyName)) {
                        return;
                    }
                    SwingUtilities.invokeLater(() -> refreshTokenCounterMode());
                }
            });
    }

    private void registerChatFontScalingListener() {
        ResourceController.getResourceController().addPropertyChangeListener(
            new IFreeplanePropertyListener() {
                @Override
                public void propertyChanged(String propertyName, String newValue, String oldValue) {
                    if (!AIChatMessageStyleSettings.CHAT_FONT_SCALING_PROPERTY.equals(propertyName)) {
                        return;
                    }
                    SwingUtilities.invokeLater(() -> refreshChatMessageStyles());
                }
            });
    }

    private void refreshChatMessageStyles() {
        applyChatMessageStyles();
        rebuildHistoryFromMemory();
    }

    private void applyChatMessageStyles() {
        AIChatMessageStyleSettings aiChatMessageStyleSettings = new AIChatMessageStyleSettings();
        Font font = inputArea.getFont();
		float baseFontSize = font != null ? font.getSize2D() / UITools.FONT_SCALE_FACTOR : 10;
		new ChatMessageStyleApplier().apply(
            messageHistoryPane,
            messageHistoryEditorKit,
            baseFontSize,
            aiChatMessageStyleSettings.getChatFontScaling());
    }

    private boolean isModelSelectionRefreshProperty(String propertyName) {
        return "ai_openrouter_model_allowlist".equals(propertyName)
            || "ai_gemini_model_list".equals(propertyName)
            || "ai_ollama_model_allowlist".equals(propertyName)
            || "ai_provider_name".equals(propertyName)
            || "ai_model_name".equals(propertyName)
            || "ai_selected_model".equals(propertyName)
            || "ai_openrouter_key".equals(propertyName)
            || "ai_openrouter_service_address".equals(propertyName)
            || "ai_gemini_key".equals(propertyName)
            || "ai_gemini_service_address".equals(propertyName)
            || "ai_ollama_api_key".equals(propertyName)
            || "ai_ollama_service_address".equals(propertyName);
    }

    private boolean isProviderConfigurationProperty(String propertyName) {
        return "ai_openrouter_key".equals(propertyName)
            || "ai_gemini_key".equals(propertyName)
            || "ai_ollama_service_address".equals(propertyName);
    }

    private void sendMessage() {
        String userMessage = inputArea.getText().trim();
        if (userMessage.isEmpty()) {
            return;
        }
        chatRequestFlow.beginRequest(userMessage);
        assistantProfileSelectionSync.maybeInjectBeforeUserMessage();
        chatRequestFlow.captureChatSnapshot();
        appendChatMessage(userMessage, ChatMessageCategory.USER);
        chatRequestFlow.refreshTokenCounters();
        liveChatController.updateSessionNameFromFirstUserMessage(userMessage);
        inputArea.setText("");
        ensureChatService();
        if (chatService == null) {
            chatRequestFlow.restoreChatSnapshot();
            return;
        }
        chatRequestFlow.submitRequest(chatService);
    }

    /**
     * Initiates a streaming chat request for the web SSE endpoint.
     * Calls {@link AIChatService#chatStream} if the current chat service supports streaming;
     * otherwise reports an error to the handler immediately.
     */
    public void chatStream(String userMessage, StreamingChatResponseHandler handler) {
        ensureChatService();
        if (chatService == null) {
            handler.onError(new IllegalStateException("Chat service not available"));
            return;
        }
        chatService.chatStream(userMessage, handler);
    }

    private boolean isRequestActive() {
        return chatRequestFlow.isRequestActive();
    }

    private void cancelActiveRequest() {
        chatRequestFlow.cancelActiveRequest();
    }

    private AssistantProfileChatMemory activeAssistantProfileChatMemory() {
        if (chatMemory instanceof AssistantProfileChatMemory) {
            return (AssistantProfileChatMemory) chatMemory;
        }
        return null;
    }

    private void setSendButtonStopState() {
        sendButton.setText(null);
        sendButton.setIcon(stopIcon);
        sendButton.setToolTipText(cancelTooltipText);
    }

    private void setSendButtonSendState() {
        sendButton.setText(null);
        sendButton.setIcon(sendIcon);
        sendButton.setToolTipText(sendTooltipText);
    }

    private void setSendButtonPreferencesState() {
        sendButton.setText(null);
        sendButton.setIcon(preferencesIcon);
        sendButton.setToolTipText(preferencesTooltipText);
    }

    private void updateInputState() {
        if (isRequestActive()) {
            updateUndoRedoButtonState();
            return;
        }
        if (isProviderConfigured()) {
            setProviderReadyState();
        } else {
            setNoProviderState();
        }
        updateUndoRedoButtonState();
    }

    private void configureEmptyHistoryFocusTransfer() {
        messageHistoryPane.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                if (messageHistory.size() != 0) {
                    return;
                }
                SwingUtilities.invokeLater(inputArea::requestFocusInWindow);
            }
        });
    }

    private void setProviderReadyState() {
        inputArea.setEditable(true);
        if (noProviderConfiguredText != null && noProviderConfiguredText.equals(inputArea.getText())) {
            inputArea.setText("");
        }
        setSendButtonSendState();
    }

    private void setNoProviderState() {
        inputArea.setEditable(false);
        inputArea.setText(noProviderConfiguredText);
        inputArea.setCaretPosition(0);
        setSendButtonPreferencesState();
    }

    private boolean isProviderConfigured() {
        return isNonEmptyText(configuration.getOpenRouterKey())
            || isNonEmptyText(configuration.getGeminiKey())
            || configuration.hasOllamaServiceAddress()
            || configuration.hasErnieKey()
            || configuration.hasDashScopeKey();
    }

    private boolean isNonEmptyText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void ensureChatService() {
        if (chatService != null) {
            return;
        }
        AIModelSelection selection = AIModelSelection.fromSelectionValue(configuration.getSelectedModelValue());
        if (selection == null) {
            appendChatMessage("Missing AI model selection.", ChatMessageCategory.ASSISTANT);
            return;
        }
        String providerName = selection.getProviderName();
        if (AIChatModelFactory.PROVIDER_NAME_OPENROUTER.equalsIgnoreCase(providerName)) {
            if (configuration.getOpenRouterKey() == null || configuration.getOpenRouterKey().isEmpty()) {
                appendChatMessage("Missing OpenRouter key setting.", ChatMessageCategory.ASSISTANT);
                return;
            }
        } else if (AIChatModelFactory.PROVIDER_NAME_GEMINI.equalsIgnoreCase(providerName)) {
            if (configuration.getGeminiKey() == null || configuration.getGeminiKey().isEmpty()) {
                appendChatMessage("Missing Gemini key setting.", ChatMessageCategory.ASSISTANT);
                return;
            }
        } else if (AIChatModelFactory.PROVIDER_NAME_OLLAMA.equalsIgnoreCase(providerName)) {
            if (!configuration.hasOllamaServiceAddress()) {
                appendChatMessage("Missing Ollama service address setting.", ChatMessageCategory.ASSISTANT);
                return;
            }
        } else {
            appendChatMessage("Unknown AI provider selection.", ChatMessageCategory.ASSISTANT);
            return;
        }
        chatService = AIChatServiceFactory.createService(new AIToolSetBuilder()
                .toolCallSummaryHandler(chatRequestFlow::onToolCallSummary)
                .availableMaps(availableMaps)
                .mapAccessListener(liveChatController.mapAccessListener())
                .build(),
            chatMemory,
            chatTokenUsageTracker,
            chatRequestFlow::onToolCallSummary,
            chatRequestFlow.cancellationSupplier(),
            chatRequestFlow::onProviderUsage);
        StreamingChatModel streamingChatModel = AIChatModelFactory.createStreamingChatModel(configuration);
        if (streamingChatModel != null) {
            chatService.setStreamingChatModel(streamingChatModel);
        }
    }

    private void appendChatMessage(String text, ChatMessageCategory category) {
        if (SwingUtilities.isEventDispatchThread()) {
            appendChatMessageInternal(text, category);
        } else {
            SwingUtilities.invokeLater(() -> appendChatMessageInternal(text, category));
        }
    }

    private void appendChatMessageInternal(String text, ChatMessageCategory category) {
        if (text == null || category == null) {
            return;
        }
        String messageText = messageRenderer.renderMessage(text, category == ChatMessageCategory.ASSISTANT);
        messageHistory.appendMessage(text, messageText, category.getStyleClassName());
        if (category == ChatMessageCategory.USER) {
            liveChatController.recordUserMessage(text);
        } else if (category == ChatMessageCategory.ASSISTANT) {
            liveChatController.recordAssistantMessage(text);
        }
    }

    private void appendProfileMessage(String profileName) {
        String normalizedName = profileName == null ? "" : profileName.trim();
        String messageText = normalizedName.isEmpty()
            ? TextUtils.getText("ai_chat_profile_label")
            : TextUtils.format("ai_chat_profile_message", normalizedName);
        appendChatMessage(messageText, ChatMessageCategory.PROFILE);
    }

    private void appendFailureMessages(String userText, String errorMessage) {
        String normalizedUserMessage = userText == null ? "" : userText.trim();
        if (!normalizedUserMessage.isEmpty()) {
            appendTransientMessage(
                normalizedUserMessage,
                ChatMessageCategory.SYSTEM,
                false);
        }
        String normalizedErrorMessage = errorMessage == null ? "" : errorMessage.trim();
        String errorNotice = normalizedErrorMessage.isEmpty()
            ? "Request failed. Check model availability, account balance, or provider settings."
            : "Request failed: " + normalizedErrorMessage;
        appendFailureNotice(errorNotice);
    }

    private void appendTransientMessage(String sourceText, ChatMessageCategory category, boolean renderAsAssistant) {
        if (sourceText == null || category == null) {
            return;
        }
        String renderedText = messageRenderer.renderMessage(sourceText, renderAsAssistant);
        messageHistory.appendMessage(sourceText, renderedText, category.getStyleClassName());
    }

    private void appendFailureNotice(String sourceText) {
        if (sourceText == null) {
            return;
        }
        String renderedText = messageRenderer.renderFailureMessage(sourceText);
        messageHistory.appendMessage(sourceText, renderedText, ChatMessageCategory.ERROR.getStyleClassName());
    }

    public ToolCallSummaryHandler toolCallSummaryHandler() {
        return chatRequestFlow::onToolCallSummary;
    }

    public void persistCurrentChatIfNeeded() {
        liveChatController.persistCurrentSessionIfNeeded();
    }

    private void resetMessageHistory() {
        messageHistoryPane.setText("<html><body></body></html>");
        messageHistoryPane.setCaretPosition(0);
    }

    private void updateTokenUsageLabel(ChatUsageTotals totals) {
        SwingUtilities.invokeLater(() -> {
            tokenUsageLabel.setVisible(totals.isVisible());
            tokenUsageLabel.setText(totals.formatStatusLine());
        });
    }

    private void activateSession(ChatMemory sessionChatMemory, boolean fromTranscriptRestore) {
        chatMemory = sessionChatMemory;
        chatService = null;
        chatTokenUsageTracker.restoreState(liveChatController.getCurrentTokenUsageState());
        chatRequestFlow.updateChatMemory(activeAssistantProfileChatMemory());
        assistantProfileSelectionSync.setChatMemory(chatMemory);
        assistantProfilePaneBuilder.syncSelection(fromTranscriptRestore);
        chatRequestFlow.resetRequestState();
        rebuildHistoryFromMemory();
        refreshTokenCounters();
        updateInputState();
    }

    private void updateUndoRedoButtonState() {
        boolean enabled = !isRequestActive();
        undoButton.setEnabled(enabled && liveChatController.canUndo());
        redoButton.setEnabled(enabled && liveChatController.canRedo());
    }

    private void undoLastTurn() {
        if (isRequestActive()) {
            return;
        }
        boolean canUndo = liveChatController.canUndo();
        String userMessage = liveChatController.undoLastTurn();
        if (canUndo) {
            chatTokenUsageTracker.undoLastResponse();
        }
        if (!liveChatController.canRedo() && userMessage.isEmpty()) {
            updateUndoRedoButtonState();
            return;
        }
        rebuildHistoryFromMemory();
        refreshTokenCounters();
        inputArea.setText(userMessage);
        inputArea.setCaretPosition(inputArea.getText().length());
        updateInputState();
    }

    private void redoLastTurn() {
        if (isRequestActive()) {
            return;
        }
        if (!liveChatController.canRedo()) {
            updateUndoRedoButtonState();
            return;
        }
        liveChatController.redoLastTurn();
        chatTokenUsageTracker.redoLastResponse();
        rebuildHistoryFromMemory();
        refreshTokenCounters();
        inputArea.setText("");
        inputArea.setCaretPosition(0);
        updateInputState();
    }

    private int getMemorySize() {
        AssistantProfileChatMemory memory = activeAssistantProfileChatMemory();
        if (memory != null) {
            return memory.conversationMessageCount();
        }
        if (chatMemory == null) {
            return 0;
        }
        return chatMemory.messages().size();
    }

    private void truncateMemoryToSize(int size) {
        AssistantProfileChatMemory memory = activeAssistantProfileChatMemory();
        if (memory != null) {
            memory.truncateConversationMessagesTo(size);
            return;
        }
        if (chatMemory == null) {
            return;
        }
        List<ChatMessage> current = chatMemory.messages();
        int targetSize = Math.max(0, Math.min(size, current.size()));
        if (targetSize == current.size()) {
            return;
        }
        chatMemory.clear();
        for (int index = 0; index < targetSize; index++) {
            ChatMessage message = current.get(index);
            if (message != null) {
                chatMemory.add(message);
            }
        }
    }

    private void rebuildHistoryFromMemory() {
        chatMemoryHistoryRenderer.rebuildFromMessages(historyMessages());
    }

    private void appendHistoryEntry(ChatMemoryRenderEntry entry) {
        if (SwingUtilities.isEventDispatchThread()) {
            chatMemoryHistoryRenderer.appendEntry(entry);
        } else {
            SwingUtilities.invokeLater(() -> chatMemoryHistoryRenderer.appendEntry(entry));
        }
    }

    private List<ChatMemoryRenderEntry> historyMessages() {
        AssistantProfileChatMemory memory = activeAssistantProfileChatMemory();
        if (memory != null) {
            return memory.panelConversationRenderEntries();
        }
        if (chatMemory == null) {
            return Collections.emptyList();
        }
        List<ChatMessage> messages = chatMemory.messages();
        List<ChatMemoryRenderEntry> entries = new ArrayList<>();
        for (int index = 0; index < messages.size(); index++) {
            entries.add(ChatMemoryRenderEntry.forMessage(messages.get(index)));
        }
        return entries;
    }

    private ChatMemory createChatMemory() {
        ChatMemorySettings chatMemorySettings = new ChatMemorySettings();
        return AssistantProfileChatMemory.builder()
            .dynamicMaxTokens(ignored -> chatMemorySettings.getMaximumTokenCount())
            .tokenEstimatorModelNameProvider(this::currentModelNameForTokenEstimator)
            .build();
    }

    private void refreshTokenCounterMode() {
        ChatTokenCounterMode counterMode = new ChatTokenCounterSettings().getCounterMode();
        chatTokenUsageTracker.setCounterMode(counterMode, tokenCounterModeLabel(counterMode));
        refreshTokenCounters();
    }

    private String tokenCounterModeLabel(ChatTokenCounterMode counterMode) {
        if (counterMode == null) {
            return null;
        }
        String key = "OptionPanel.ai_chat_token_counter_mode." + counterMode.getPreferenceValue();
        return TextUtils.getOptionalText(key);
    }

    private void refreshTokenCounters() {
        chatTokenUsageTracker.refreshTotals(activeAssistantProfileChatMemory(),
            TextUtils.getOptionalText("ai_chat_token_counter.input"),
            TextUtils.getOptionalText("ai_chat_token_counter.output"));
    }

    private String currentModelNameForTokenEstimator() {
        AIModelSelection selection = AIModelSelection.fromSelectionValue(configuration.getSelectedModelValue());
        if (selection == null) {
            return null;
        }
        return selection.getModelName();
    }

    private TextController requireTextController() {
        ModeController modeController = Controller.getCurrentModeController();
        if (modeController == null) {
            throw new IllegalStateException("Current mode controller is not available.");
        }
        TextController textController = modeController.getExtension(TextController.class);
        if (textController == null) {
            throw new IllegalStateException("Text controller is not available.");
        }
        return textController;
    }

    private enum ChatMessageCategory {
        USER("message-user"),
        ASSISTANT("message-assistant"),
        TOOL_CALL("message-tool"),
        MCP_CALL("message-mcp-call"),
        PROFILE("message-profile"),
        ERROR("message-error"),
        SYSTEM("message-system");

        private final String styleClassName;

        ChatMessageCategory(String styleClassName) {
            this.styleClassName = styleClassName;
        }

        String getStyleClassName() {
            return styleClassName;
        }
    }

}
