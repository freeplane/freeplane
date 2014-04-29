package org.freeplane.plugin.script;

import java.util.Properties;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ribbon.ARibbonContributor;
import org.freeplane.core.ui.ribbon.IRibbonContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonBuildContext;
import org.freeplane.core.util.ActionUtils;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.mode.Controller;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandMenuButton;
import org.pushingpixels.flamingo.api.common.JCommandToggleButton;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;

class ScriptingRibbonsContributorFactory implements IRibbonContributorFactory {
    @Override
    public ARibbonContributor getContributor(final Properties attributes) {
        return new ARibbonContributor() {
            @Override
            public String getKey() {
                return attributes.getProperty("name");
            }
            
            @Override
            public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
                
                final JCommandToggleButton toggle1 = RibbonActionContributorFactory.createCommandToggleButton(ActionUtils.getDummyAction("all_nodes"));
                
                ChildProperties togglerProps = new ChildProperties();
                togglerProps.set(RibbonElementPriority.class, RibbonElementPriority.MEDIUM);
                
                parent.addChild(toggle1, togglerProps);
                
                final JCommandToggleButton toggle2 = RibbonActionContributorFactory.createCommandToggleButton(ActionUtils.getDummyAction("all_nodes_recursive"));
                parent.addChild(toggle2, togglerProps);
                
                JCommandButton button = RibbonActionContributorFactory.createCommandButton(ActionUtils.getDummyAction(getKey()));
                
                ChildProperties childProps = new ChildProperties();
                childProps.set(RibbonElementPriority.class, RibbonElementPriority.TOP);
                
                button.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
                button.setPopupCallback(new PopupPanelCallback() {
                    
                    public JPopupPanel getPopupPanel(JCommandButton commandButton) {
                        IMapSelection selection = Controller.getCurrentController().getSelection();
                        boolean isMultiSelection = selection != null && selection.getSelection().size() > 1;
                        JCommandPopupMenu popupmenu = new JCommandPopupMenu();
                        // loop through all scripts and create a JCommandButton
                        {
                            //get respective action from somewhere
                            AFreeplaneAction action = null;
                            
                            String title = ActionUtils.getActionTitle(action);
                            ResizableIcon icon = ActionUtils.getActionIcon(action);
                            
                            final JCommandMenuButton scriptEntry = new JCommandMenuButton(title, icon);
                            
                            RibbonActionContributorFactory.updateRichTooltip(scriptEntry, action, null);
                            scriptEntry.addActionListener(action);
                            scriptEntry.setFocusable(false);
                            
                            //if(script allows Multi)
                            boolean isMultiAllowed = true; 
                            scriptEntry.setEnabled(isMultiSelection&&isMultiAllowed);                       
                            
                            popupmenu.addMenuButton(scriptEntry);
                        }
                        return popupmenu;
                    }
                });
                
                parent.addChild(button, childProps);                
            }
            
            @Override
            public void addChild(Object child, ChildProperties properties) {
                //we 
            }
        };
    }
}