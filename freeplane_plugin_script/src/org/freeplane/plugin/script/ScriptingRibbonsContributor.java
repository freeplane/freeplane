package org.freeplane.plugin.script;

import static org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode.ON_SELECTED_NODE;
import static org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode.ON_SELECTED_NODE_RECURSIVELY;
import static org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode.ON_SINGLE_NODE;
import static org.freeplane.plugin.script.ScriptingMenuUtils.LABEL_AVAILABLE_MODES_TOOLTIP;
import static org.freeplane.plugin.script.ScriptingMenuUtils.LABEL_SCRIPT;
import static org.freeplane.plugin.script.ScriptingMenuUtils.LABEL_SCRIPTS_MENU;
import static org.freeplane.plugin.script.ScriptingMenuUtils.noScriptsAvailableMessage;
import static org.freeplane.plugin.script.ScriptingMenuUtils.scriptNameToMenuItemTitle;

import java.awt.Dimension;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ribbon.ARibbonContributor;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonBuildContext;
import org.freeplane.core.util.ActionUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode;
import org.freeplane.plugin.script.ScriptingConfiguration.ScriptMetaData;
import org.pushingpixels.flamingo.api.common.CommandToggleButtonGroup;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandMenuButton;
import org.pushingpixels.flamingo.api.common.JCommandToggleButton;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;

class ScriptingRibbonsContributor extends ARibbonContributor {
    private final ScriptingConfiguration configuration;
    private ConcurrentHashMap<RibbonElementPriority, ChildProperties> defaultChildProperties = new ConcurrentHashMap<RibbonElementPriority, ChildProperties>();
    private JCommandToggleButton toggleOnSingleNode;
    private JCommandToggleButton toggleOnSelectedNodes;
    private JCommandToggleButton toggleOnSelectedNodesRecursive;
    private CommandToggleButtonGroup toggleGroup;

    // we don't seem to need the attributes
    ScriptingRibbonsContributor(Properties attributes, ScriptingConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getKey() {
        return LABEL_SCRIPTS_MENU;
    }

    @Override
    public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
        addToggleButtons(parent);
        addScriptsPopup(parent);
    }

    private void addToggleButtons(ARibbonContributor parent) {
        toggleGroup = new CommandToggleButtonGroup();
        toggleOnSingleNode = createAndAddToggleButton(parent, toggleGroup, ON_SINGLE_NODE);
        toggleOnSingleNode.getActionModel().setSelected(true);
        toggleOnSelectedNodes = createAndAddToggleButton(parent, toggleGroup, ON_SELECTED_NODE);
        toggleOnSelectedNodesRecursive = createAndAddToggleButton(parent, toggleGroup, ON_SELECTED_NODE_RECURSIVELY);
    }

    private JCommandToggleButton createAndAddToggleButton(ARibbonContributor parent,
                                                          CommandToggleButtonGroup alignGroup,
                                                          ExecutionMode executionMode) {
        JCommandToggleButton toggleButton = new JCommandToggleButton("", getIcon(makeIconFileName(executionMode)));
        toggleButton.setActionRichTooltip(createRichTooltip(getTitleForExecutionMode(executionMode)));
        toggleGroup.add(toggleButton);
        parent.addChild(toggleButton, getDefaultProps(RibbonElementPriority.MEDIUM));
        return toggleButton;
    }

    private RichTooltip createRichTooltip(final String title) {
        final RichTooltip tooltip = new RichTooltip();
        tooltip.setTitle(title);
        return tooltip;
    }

    private String getTitleForExecutionMode(ExecutionMode executionMode) {
        final String scriptLabel = TextUtils.getText(LABEL_SCRIPT);
        return TextUtils.format(ScriptingConfiguration.getExecutionModeKey(executionMode), scriptLabel);
    }

    private String makeIconFileName(ExecutionMode executionMode) {
        return executionMode.name().toLowerCase() + ".png";
    }

    private ResizableIcon getIcon(String fileName) {
        String path = "/images/" + fileName;
        URL url = ResourceController.getResourceController().getResource(path);
        if (url == null)
            throw new RuntimeException("cannot resolve icon " + path);
        return ImageWrapperResizableIcon.getIcon(url, new Dimension(16, 16));
    }

    private void addScriptsPopup(ARibbonContributor parent) {
        JCommandButton button = RibbonActionContributorFactory
            .createCommandButton(ActionUtils.getDummyAction(getKey()));
        button.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
        button.setPopupCallback(new PopupPanelCallback() {
            public JPopupPanel getPopupPanel(JCommandButton commandButton) {
                JCommandPopupMenu popupmenu = new JCommandPopupMenu();
                registerScripts(popupmenu);
                return popupmenu;
            }
        });
        parent.addChild(button, getDefaultProps(RibbonElementPriority.TOP));
    }

    protected void registerScripts(JCommandPopupMenu popupmenu) {
        if (configuration.getMenuTitleToPathMap().isEmpty()) {
            popupmenu.addMenuButton(createNoScriptsAvailableButton());
        }
        else {
            final ExecutionMode executionMode = getExecutionMode();
            for (final Entry<String, String> entry : configuration.getMenuTitleToPathMap().entrySet()) {
                popupmenu.addMenuButton(createScriptButton(entry.getKey(), entry.getValue(), executionMode));
            }
        }
    }

    private JCommandMenuButton createNoScriptsAvailableButton() {
        return new JCommandMenuButton(noScriptsAvailableMessage(), null);
    }

    private JCommandMenuButton createScriptButton(final String scriptName, final String scriptPath,
                                                  ExecutionMode executionMode) {
        final ScriptMetaData metaData = configuration.getMenuTitleToMetaDataMap().get(scriptName);
        final String title = scriptNameToMenuItemTitle(scriptName);
        AFreeplaneAction action = new ExecuteScriptAction(scriptName, title, scriptPath, executionMode,
            metaData.cacheContent(), metaData.getPermissions());
        ResizableIcon icon = ActionUtils.getActionIcon(action);
        final JCommandMenuButton scriptEntry = new JCommandMenuButton(title, icon);
        scriptEntry.setActionRichTooltip(createRichTooltip(title, metaData));
        scriptEntry.addActionListener(action);
        scriptEntry.setFocusable(false);
        scriptEntry.setEnabled(metaData.getExecutionModes().contains(executionMode));
        return scriptEntry;
    }

    private RichTooltip createRichTooltip(String title, ScriptMetaData metaData) {
        final RichTooltip tooltip = createRichTooltip(TextUtils.format(LABEL_AVAILABLE_MODES_TOOLTIP, title));
        for (ExecutionMode executionMode : metaData.getExecutionModes()) {
            tooltip.addDescriptionSection(getTitleForExecutionMode(executionMode));
        }
        return tooltip;
    }

    private ExecutionMode getExecutionMode() {
        if (toggleOnSingleNode.equals(toggleGroup.getSelected()))
            return ON_SINGLE_NODE;
        else if (toggleOnSelectedNodes.equals(toggleGroup.getSelected()))
            return ON_SELECTED_NODE;
        else if (toggleOnSelectedNodesRecursive.equals(toggleGroup.getSelected()))
            return ON_SELECTED_NODE_RECURSIVELY;
        throw new RuntimeException("unexpected execution mode " + toggleGroup.getSelected());
    }

    private ChildProperties getDefaultProps(RibbonElementPriority elementPriority) {
        ChildProperties props = defaultChildProperties.get(elementPriority);
        if (props == null) {
            props = new ChildProperties();
            props.set(RibbonElementPriority.class, elementPriority);
            defaultChildProperties.put(elementPriority, props);
        }
        return props;
    }

    @Override
    public void addChild(Object child, ChildProperties properties) {
        throw new RuntimeException("didn't expect addition of additional children to this");
    }
}
