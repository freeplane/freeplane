package org.freeplane.core.ui.menubuilders.ribbon;

import java.awt.BorderLayout;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.util.ActionUtils;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.CommandButtonDisplayState;
import org.pushingpixels.flamingo.api.common.CommandButtonLayoutManager;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonPopupOrientationKind;
import org.pushingpixels.flamingo.api.common.JCommandButtonPanel;
import org.pushingpixels.flamingo.api.common.JScrollablePanel;
import org.pushingpixels.flamingo.api.common.JScrollablePanel.ScrollType;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.CommandButtonLayoutManagerMenuTileLevel2;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.JRibbonApplicationMenuPopupPanelSecondary;

public class RibbonLastOpenedMapsBuilder extends JRibbonApplicationMenuPrimaryBuilder {

	private PrimaryRolloverCallback rolloverCallback;
	
	public RibbonLastOpenedMapsBuilder(ResourceAccessor resourceAccessor, AcceleratebleActionProvider acceleratebleActionProvider, IAcceleratorMap accelerators) {
		super(resourceAccessor, acceleratebleActionProvider, accelerators);
	}
	
	@Override
	public void visit(Entry entry) {
		if(rolloverCallback == null) {
			RibbonApplicationMenuEntryPrimary component = this.createMenuEntry(entry);
			component.setRolloverCallback(getCallback(component, entry));
			entryAccessor.setComponent(entry, component);
			Object parent = entryAccessor.getAncestorComponent(entry);
			if(parent instanceof RibbonApplicationMenuContainer) {
				final RibbonApplicationMenuContainer container = (RibbonApplicationMenuContainer) parent; 
				container.add(component);
			}
		}
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return true;
	}
	
	private PrimaryRolloverCallback getCallback(final RibbonApplicationMenuEntryPrimary primeEntry, final Entry entry) {
		if (rolloverCallback == null) {
			rolloverCallback = new PrimaryRolloverCallback() {
				public void menuEntryActivated(JPanel targetPanel) {
					targetPanel.removeAll();
					targetPanel.setLayout(new BorderLayout());
					JCommandButtonPanel secondary = new JRibbonApplicationMenuPopupPanelSecondary(primeEntry);
					secondary.setToShowGroupLabels(false);
					String groupDesc = primeEntry.getText();
					secondary.addButtonGroup(groupDesc);
					for (Entry child : entry.children()) {
						AFreeplaneAction action = entryAccessor.getAction(child);
						String name = ActionUtils.getActionTitle(action);
						JCommandButton menuButton = new JCommandButton(name);
						menuButton.addActionListener(action);
						menuButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
						menuButton.setHorizontalAlignment(SwingUtilities.LEADING);
						menuButton.setPopupOrientationKind(CommandButtonPopupOrientationKind.SIDEWARD);
						menuButton.setEnabled(true);
						menuButton.setActionRichTooltip(new RichTooltip((String) action
						    .getValue(Action.SHORT_DESCRIPTION), name));
						secondary.addButtonToLastGroup(menuButton);
					}
					JScrollablePanel<JCommandButtonPanel> scrollPanel = new JScrollablePanel<JCommandButtonPanel>(
					    secondary, ScrollType.VERTICALLY);
					targetPanel.add(scrollPanel, BorderLayout.CENTER);
				}
			};
		}
		return rolloverCallback;
	}

}

class RibbonMenuLastOpenedMapsPanel extends JCommandButtonPanel {
	private static final long serialVersionUID = 1L;
	protected final static CommandButtonDisplayState MENU_TILE_LEVEL_2 = new CommandButtonDisplayState(
	    "Ribbon application menu tile level 2", 32) {
		@Override
		public CommandButtonLayoutManager createLayoutManager(AbstractCommandButton commandButton) {
			return new CommandButtonLayoutManagerMenuTileLevel2();
		}
	};

	public RibbonMenuLastOpenedMapsPanel() {
		super(MENU_TILE_LEVEL_2);
		this.setMaxButtonColumns(1);
	}
}
