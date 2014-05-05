package org.freeplane.plugin.script;

import static org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode.ON_SELECTED_NODE;
import static org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode.ON_SELECTED_NODE_RECURSIVELY;
import static org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode.ON_SINGLE_NODE;
import static org.freeplane.plugin.script.ScriptingMenuUtils.getTitle;
import static org.freeplane.plugin.script.ScriptingMenuUtils.makeMenuTitle;
import static org.freeplane.plugin.script.ScriptingMenuUtils.noScriptsAvailableMessage;

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
import org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode;
import org.freeplane.plugin.script.ScriptingConfiguration.ScriptMetaData;
import org.pushingpixels.flamingo.api.common.CommandButtonDisplayState;
import org.pushingpixels.flamingo.api.common.CommandToggleButtonGroup;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandButtonStrip;
import org.pushingpixels.flamingo.api.common.JCommandMenuButton;
import org.pushingpixels.flamingo.api.common.JCommandToggleButton;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;

class ScriptingRibbonsContributor extends ARibbonContributor {
    private final Properties attributes;
    private final ScriptingConfiguration configuration;
    private ConcurrentHashMap<RibbonElementPriority, ChildProperties> defaultChildProperties = new ConcurrentHashMap<RibbonElementPriority, ChildProperties>();
    private JCommandToggleButton toggleOnSingleNode;
    private JCommandToggleButton toggleOnSelectedNodes;
    private JCommandToggleButton toggleOnSelectedNodesRecursive;
    private CommandToggleButtonGroup execModes;

    ScriptingRibbonsContributor(Properties attributes, ScriptingConfiguration configuration) {
        this.attributes = attributes;
        this.configuration = configuration;
    }

    @Override
    public String getKey() {
        return attributes.getProperty("name");
    }

    @Override
    public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
        addToggleButtons(parent);
        addScriptsPopup(parent);
    }

    private void addToggleButtons(ARibbonContributor parent) {
        JCommandButtonStrip alignStrip = new JCommandButtonStrip(JCommandButtonStrip.StripOrientation.HORIZONTAL);
        alignStrip.setDisplayState(CommandButtonDisplayState.MEDIUM);
        
        execModes = new CommandToggleButtonGroup();
        toggleOnSingleNode = createAndAddToggleButton(alignStrip, execModes, "on_single_node");
        toggleOnSingleNode.getActionModel().setSelected(true);
        parent.addChild(toggleOnSingleNode, getDefaultProps(RibbonElementPriority.MEDIUM));
        toggleOnSelectedNodes = createAndAddToggleButton(alignStrip, execModes, "on_selected_nodes");
        parent.addChild(toggleOnSelectedNodes, getDefaultProps(RibbonElementPriority.MEDIUM));
        toggleOnSelectedNodesRecursive = createAndAddToggleButton(alignStrip, execModes, "on_selected_nodes_recursive");
        parent.addChild(toggleOnSelectedNodesRecursive, getDefaultProps(RibbonElementPriority.MEDIUM));
        //seems not to work
//        parent.addChild(alignStrip, getDefaultProps(RibbonElementPriority.TOP));

    }

    private JCommandToggleButton createAndAddToggleButton(JCommandButtonStrip alignStrip,
                                                          CommandToggleButtonGroup alignGroup, String name) {
        JCommandToggleButton toggleButton = new JCommandToggleButton("", getIcon(name + ".png"));
        execModes.add(toggleButton);
        alignStrip.add(toggleButton);
        return toggleButton;
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
            popupmenu.add(createNoScriptsAvailableButton());
        }
        else {
            for (final Entry<String, String> entry : configuration.getMenuTitleToPathMap().entrySet()) {
                popupmenu.add(createScriptButton(entry.getKey(), entry.getValue()));
            }
        }
    }

    private JCommandMenuButton createNoScriptsAvailableButton() {
        return new JCommandMenuButton(noScriptsAvailableMessage(), null);
    }

    private JCommandMenuButton createScriptButton(final String scriptName, final String scriptPath) {
        final ScriptMetaData metaData = configuration.getMenuTitleToMetaDataMap().get(scriptName);
        ExecutionMode executionMode = getExecutionMode();
        final String title = getTitle(metaData, executionMode);
        final String titleKey = metaData.getTitleKey(executionMode);
        final String menuTitle = makeMenuTitle(scriptName, titleKey);
        AFreeplaneAction action = new ExecuteScriptAction(scriptName, menuTitle, scriptPath, executionMode,
            metaData.cacheContent(), metaData.getPermissions());
        ResizableIcon icon = ActionUtils.getActionIcon(action);
        final JCommandMenuButton scriptEntry = new JCommandMenuButton(title, icon);
        // FIXME: add list of available execution modes + shortcut to tooltip
        RibbonActionContributorFactory.updateRichTooltip(scriptEntry, action, null);
        scriptEntry.addActionListener(action);
        scriptEntry.setFocusable(false);
        scriptEntry.setEnabled(metaData.getExecutionModes().contains(executionMode));
        return scriptEntry;
    }

    private ExecutionMode getExecutionMode() {
        if (toggleOnSingleNode.equals(execModes.getSelected()))
            return ON_SINGLE_NODE;
        else if (toggleOnSelectedNodes.equals(execModes.getSelected()))
            return ON_SELECTED_NODE;
        else if (toggleOnSelectedNodesRecursive.equals(execModes.getSelected()))
            return ON_SELECTED_NODE_RECURSIVELY;
        throw new RuntimeException("unexpected execution mode " + execModes.getSelected());
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
