package org.freeplane.core.ui.ribbon.special;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.freeplane.core.resources.SetBooleanPropertyAction;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ribbon.ARibbonContributor;
import org.freeplane.core.ui.ribbon.CurrentState;
import org.freeplane.core.ui.ribbon.IChangeObserver;
import org.freeplane.core.ui.ribbon.IRibbonContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonBuildContext;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.AttributeViewTypeAction;
import org.freeplane.features.note.mindmapmode.SetNoteWindowPosition;
import org.freeplane.features.styles.mindmapmode.SetBooleanMapPropertyAction;
import org.freeplane.view.swing.map.ShowNotesInMapAction;
import org.pushingpixels.flamingo.api.common.CommandButtonDisplayState;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandMenuButton;
import org.pushingpixels.flamingo.api.common.JCommandToggleMenuButton;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;

public class ViewSettingsContributorFactory implements IRibbonContributorFactory {	

	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {

			public String getKey() {
				return attributes.getProperty("name");
			}
		

			public void contribute(final RibbonBuildContext context, ARibbonContributor parent) {
				if (parent == null) {
					return;
				}				
				JRibbonBand band = new JRibbonBand(TextUtils.removeTranslateComment(TextUtils.getText("ribbon.band.viewsettings")), null, null);
								
				createAttributeViewMenu(context, band);
				createNoteViewMenu(context, band);
				createToolTipMenu(context, band);
				createToolbarsMenu(context, band);
				
				List<RibbonBandResizePolicy> policies = new ArrayList<RibbonBandResizePolicy>();				
				policies.add(new CoreRibbonResizePolicies.Mirror(band.getControlPanel()));				
				policies.add(new CoreRibbonResizePolicies.High2Low(band.getControlPanel()));
				band.setResizePolicies(policies);			
				
				parent.addChild(band, new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", ""))));		    	
			}

			private void createToolbarsMenu(final RibbonBuildContext context, JRibbonBand band) {
				JCommandButton button = new JCommandButton(TextUtils.removeTranslateComment(TextUtils.getText("menu_toolbars")));
				button.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
				button.setPopupCallback(new PopupPanelCallback() {
					public JPopupPanel getPopupPanel(JCommandButton commandButton) {
						JCommandPopupMenu popupmenu = new JCommandPopupMenu();					
    					
    					final AFreeplaneAction toggleFBarAction = context.getBuilder().getMode().getAction("ToggleFBarAction");
    					final JCommandToggleMenuButton toggleFBarButton = RibbonActionContributorFactory.createCommandToggleMenuButton(toggleFBarAction);
    					toggleFBarAction.setSelected();
    					toggleFBarButton.getActionModel().setSelected(toggleFBarAction.isSelected());
    					popupmenu.addMenuButton(toggleFBarButton);
    					
    					final AFreeplaneAction toggleLeftToolbarAction = context.getBuilder().getMode().getAction("ToggleLeftToolbarAction");
    					final JCommandToggleMenuButton toggleLeftToolbarButton = RibbonActionContributorFactory.createCommandToggleMenuButton(toggleLeftToolbarAction);
    					toggleLeftToolbarAction.setSelected();
    					toggleLeftToolbarButton.getActionModel().setSelected(toggleLeftToolbarAction.isSelected());
    					popupmenu.addMenuButton(toggleLeftToolbarButton);
    					
    					final AFreeplaneAction toggleStatusAction = context.getBuilder().getMode().getAction("ToggleStatusAction");
    					final JCommandToggleMenuButton toggleStatusButton = RibbonActionContributorFactory.createCommandToggleMenuButton(toggleStatusAction);
    					toggleStatusAction.setSelected();
    					toggleStatusButton.getActionModel().setSelected(toggleStatusAction.isSelected());
    					popupmenu.addMenuButton(toggleStatusButton);
    					
    					final AFreeplaneAction toggleScrollbarsAction = context.getBuilder().getMode().getAction("ToggleScrollbarsAction");
    					final JCommandToggleMenuButton toggleScrollbarsButton = RibbonActionContributorFactory.createCommandToggleMenuButton(toggleScrollbarsAction);
    					toggleScrollbarsAction.setSelected();
    					toggleScrollbarsButton.getActionModel().setSelected(toggleScrollbarsAction.isSelected());
    					popupmenu.addMenuButton(toggleScrollbarsButton);
    					
						return popupmenu;
					}
				});
				band.addCommandButton(button, RibbonElementPriority.MEDIUM);
				
				AFreeplaneAction action = context.getBuilder().getMode().getAction("SetShortenerStateAction");
				button = RibbonActionContributorFactory.createCommandButton(action);
				band.addCommandButton(button, RibbonElementPriority.MEDIUM);
				
				action = context.getBuilder().getMode().getAction("ToggleDetailsAction");				
				button = RibbonActionContributorFactory.createCommandButton(action);
				band.addCommandButton(button, RibbonElementPriority.MEDIUM);				
			}


			private void createToolTipMenu(final RibbonBuildContext context, final JRibbonBand band) {
				JCommandButton button = new JCommandButton(TextUtils.removeTranslateComment(TextUtils.getText("menu_hoverView")));
				button.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
				button.setPopupCallback(new PopupPanelCallback() {
					public JPopupPanel getPopupPanel(JCommandButton commandButton) {
						JCommandPopupMenu popupmenu = new JCommandPopupMenu();
						
						final SetBooleanPropertyAction showAction = (SetBooleanPropertyAction) context.getBuilder().getMode().getAction("SetBooleanPropertyAction.show_node_tooltips");
    					final JCommandToggleMenuButton showButton = RibbonActionContributorFactory.createCommandToggleMenuButton(showAction);
    					showAction.setSelected();
    					showButton.getActionModel().setSelected(showAction.isSelected());
    					popupmenu.addMenuButton(showButton);
    					
    					final SetBooleanPropertyAction showStylesAction = (SetBooleanPropertyAction) context.getBuilder().getMode().getAction("SetBooleanPropertyAction.show_styles_in_tooltip");
    					final JCommandToggleMenuButton showStylesButton = RibbonActionContributorFactory.createCommandToggleMenuButton(showStylesAction);
    					showStylesAction.setSelected();
    					showStylesButton.getActionModel().setSelected(showStylesAction.isSelected());
    					popupmenu.addMenuButton(showStylesButton);
    					
    					final AFreeplaneAction modificationAction = context.getBuilder().getMode().getAction("CreationModificationPluginAction");
    					final JCommandToggleMenuButton modificationButton = RibbonActionContributorFactory.createCommandToggleMenuButton(modificationAction);
    					modificationAction.setSelected();
    					modificationButton.getActionModel().setSelected(modificationAction.isSelected());
    					popupmenu.addMenuButton(modificationButton);
    					
						return popupmenu;
					}
				});
				band.addCommandButton(button, RibbonElementPriority.MEDIUM);
			}


			private void createNoteViewMenu(final RibbonBuildContext context, final JRibbonBand band) {				
				JCommandButton displayNotesButton = new JCommandButton(TextUtils.removeTranslateComment(TextUtils.getText("menu_noteView")));
				displayNotesButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
				displayNotesButton.setPopupCallback(new PopupPanelCallback() {
					public JPopupPanel getPopupPanel(JCommandButton commandButton) {
						JCommandPopupMenu popupmenu = new JCommandPopupMenu();
						
						final ShowNotesInMapAction showNotesInMapAction = (ShowNotesInMapAction) context.getBuilder().getMode().getAction("ShowNotesInMapAction");
						final JCommandToggleMenuButton showNotedsInMapButton = RibbonActionContributorFactory.createCommandToggleMenuButton(showNotesInMapAction);
						showNotesInMapAction.setSelected();
						showNotedsInMapButton.getActionModel().setSelected(showNotesInMapAction.isSelected());
    					popupmenu.addMenuButton(showNotedsInMapButton);
    					
						final SetBooleanMapPropertyAction showIconAction = (SetBooleanMapPropertyAction) context.getBuilder().getMode().getAction("SetBooleanMapPropertyAction.show_note_icons");
    					final JCommandToggleMenuButton toggleButton = RibbonActionContributorFactory.createCommandToggleMenuButton(showIconAction);
    					showIconAction.setSelected();
    					toggleButton.getActionModel().setSelected(showIconAction.isSelected());
    					popupmenu.addMenuButton(toggleButton);
    					
    					JCommandMenuButton button = new JCommandMenuButton(TextUtils.removeTranslateComment(TextUtils.getText("note_window_location")), null);
    					button.setDisplayState(CommandButtonDisplayState.MEDIUM);
    					button.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
    					button.setPopupCallback(new PopupPanelCallback() {
    						public JPopupPanel getPopupPanel(JCommandButton commandButton) {
    							JCommandPopupMenu popupmenu = new JCommandPopupMenu();
    						
    							final SetNoteWindowPosition posTopAction = (SetNoteWindowPosition) context.getBuilder().getMode().getAction("SetNoteWindowPosition.top");
    							final JCommandToggleMenuButton posTopButton = RibbonActionContributorFactory.createCommandToggleMenuButton(posTopAction);							
    							popupmenu.addMenuButton(posTopButton);
    							posTopAction.setSelected();
    							posTopButton.getActionModel().setSelected(posTopAction.isSelected());
    							
    							final SetNoteWindowPosition posLeftAction = (SetNoteWindowPosition) context.getBuilder().getMode().getAction("SetNoteWindowPosition.left");
    							final JCommandToggleMenuButton posLeftButton = RibbonActionContributorFactory.createCommandToggleMenuButton(posLeftAction);							
    							popupmenu.addMenuButton(posLeftButton);
    							posLeftAction.setSelected();
    							posLeftButton.getActionModel().setSelected(posLeftAction.isSelected());
    							
    							final SetNoteWindowPosition posRightAction = (SetNoteWindowPosition) context.getBuilder().getMode().getAction("SetNoteWindowPosition.right");
    							final JCommandToggleMenuButton posRightButton = RibbonActionContributorFactory.createCommandToggleMenuButton(posRightAction);							
    							popupmenu.addMenuButton(posRightButton);
    							posRightAction.setSelected();
    							posRightButton.getActionModel().setSelected(posRightAction.isSelected());
    							
    							final SetNoteWindowPosition posBottomAction = (SetNoteWindowPosition) context.getBuilder().getMode().getAction("SetNoteWindowPosition.bottom");
    							final JCommandToggleMenuButton posBottomButton = RibbonActionContributorFactory.createCommandToggleMenuButton(posBottomAction);							
    							popupmenu.addMenuButton(posBottomButton);
    							posBottomAction.setSelected();
    							posBottomButton.getActionModel().setSelected(posBottomAction.isSelected());
    							
    							return popupmenu;
    						}
    					});    					
    					popupmenu.addMenuButton(button);
    					
						return popupmenu;
					}
				});
				band.addCommandButton(displayNotesButton, RibbonElementPriority.MEDIUM);
			}

			private void createAttributeViewMenu(final RibbonBuildContext context, JRibbonBand band) {
				JCommandButton button = new JCommandButton(TextUtils.removeTranslateComment(TextUtils.getText("menu_displayAttributes")));
				button.setDisplayState(CommandButtonDisplayState.MEDIUM);
				button.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
				button.setPopupCallback(new PopupPanelCallback() {
					public JPopupPanel getPopupPanel(JCommandButton commandButton) {
						JCommandPopupMenu popupmenu = new JCommandPopupMenu();
						
						final AttributeViewTypeAction showSelectedAttributesAction = (AttributeViewTypeAction) context.getBuilder().getMode().getAction("ShowSelectedAttributesAction");
						final JCommandToggleMenuButton showSelectedAttributesButton = RibbonActionContributorFactory.createCommandToggleMenuButton(showSelectedAttributesAction);							
						popupmenu.addMenuButton(showSelectedAttributesButton);
						showSelectedAttributesAction.setSelected();
						showSelectedAttributesButton.getActionModel().setSelected(showSelectedAttributesAction.isSelected());
						
						final AttributeViewTypeAction showAllAttributesAction = (AttributeViewTypeAction) context.getBuilder().getMode().getAction("ShowAllAttributesAction");
						final JCommandToggleMenuButton showAllAttributesButton = RibbonActionContributorFactory.createCommandToggleMenuButton(showAllAttributesAction);						
						popupmenu.addMenuButton(showAllAttributesButton);
						showAllAttributesAction.setSelected();
						showAllAttributesButton.getActionModel().setSelected(showAllAttributesAction.isSelected());
						
						final AttributeViewTypeAction hideAllAttributesAction = (AttributeViewTypeAction) context.getBuilder().getMode().getAction("HideAllAttributesAction");						
						final JCommandToggleMenuButton hideAllAttributesButton = RibbonActionContributorFactory.createCommandToggleMenuButton(hideAllAttributesAction);						
						popupmenu.addMenuButton(hideAllAttributesButton);
						hideAllAttributesAction.setSelected();
						hideAllAttributesButton.getActionModel().setSelected(hideAllAttributesAction.isSelected());
												
    					final SetBooleanMapPropertyAction showIconAction = (SetBooleanMapPropertyAction) context.getBuilder().getMode().getAction("SetBooleanMapPropertyAction.show_icon_for_attributes");
    					final JCommandToggleMenuButton toggleButton = RibbonActionContributorFactory.createCommandToggleMenuButton(showIconAction);
    					showIconAction.setSelected();
    					toggleButton.getActionModel().setSelected(showIconAction.isSelected());
    					popupmenu.addMenuButton(toggleButton);
						
						context.getBuilder().getMapChangeAdapter().addListener(new IChangeObserver() {							
							public void updateState(CurrentState state) {
								showSelectedAttributesAction.setSelected();
								showSelectedAttributesButton.getActionModel().setSelected(showSelectedAttributesAction.isSelected());
								showAllAttributesAction.setSelected();
								showAllAttributesButton.getActionModel().setSelected(showAllAttributesAction.isSelected());
								hideAllAttributesAction.setSelected();
								hideAllAttributesButton.getActionModel().setSelected(hideAllAttributesAction.isSelected());
								showIconAction.setSelected();
								toggleButton.getActionModel().setSelected(showIconAction.isSelected());
							}
						});
						
						JCommandMenuButton button = RibbonActionContributorFactory.createCommandMenuButton(context.getBuilder().getMode().getAction("ShowAttributeDialogAction"));
						popupmenu.addMenuButton(button);
						
						return popupmenu;
					}
				});
				band.addCommandButton(button, RibbonElementPriority.MEDIUM);
			}

			public void addChild(Object child, ChildProperties properties) {
			}
		};
	}
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
