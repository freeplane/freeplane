package org.freeplane.core.ui.menubuilders.menu;

import java.awt.event.ActionListener;
import java.util.List;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.util.ActionUtils;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryFooter;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntrySecondary;

public class JRibbonApplicationMenuPrimaryBuilder implements EntryVisitor {
	private AcceleratebleActionProvider acceleratebleActionProvider;
	private EntryAccessor entryAccessor;
	
	public JRibbonApplicationMenuPrimaryBuilder(ResourceAccessor resourceAccessor, AcceleratebleActionProvider acceleratebleActionProvider) {
		super();
		this.acceleratebleActionProvider = acceleratebleActionProvider;
		this.entryAccessor = new EntryAccessor(resourceAccessor);
	}
	@Override
	public void visit(Entry entry) {
		CustomRibbonApplicationMenuEntryPrimary component = createMenuEntry(entry);
		entryAccessor.setComponent(entry, new RibbonApplicationMenuPrimaryContainer(component));
		Object parent = entryAccessor.getAncestorComponent(entry);
		if(parent instanceof RibbonApplicationMenuContainer) {
			final RibbonApplicationMenuContainer container = (RibbonApplicationMenuContainer) parent; 
			container.add(component);
		}
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}
	
	private CustomRibbonApplicationMenuEntryPrimary createMenuEntry(Entry entry) {
		AFreeplaneAction action = entryAccessor.getAction(entry);
		CommandButtonKind kind = CommandButtonKind.POPUP_ONLY;
		ActionListener listener = null;
		if(action == null) {
			if(!entry.hasChildren()) throw new RuntimeException("invalid entry state for RibbonApplicationMenuEntryPrimary create!");
			action = ActionUtils.getDummyAction(entry.getName());
		} else {
			kind = entry.hasChildren() ? CommandButtonKind.ACTION_AND_POPUP_MAIN_ACTION : CommandButtonKind.ACTION_ONLY;
			listener = acceleratebleActionProvider.acceleratableAction(action);
		}
		String title = ActionUtils.getActionTitle(action);
		ResizableIcon icon = ActionUtils.getActionIcon(action);

	 
		return new CustomRibbonApplicationMenuEntryPrimary(icon, title, listener, kind);
	}
}

class SecondaryGroupEntry {
	private final String groupTitle;
	private final RibbonApplicationMenuEntrySecondary entry;
	
	public SecondaryGroupEntry(String title, RibbonApplicationMenuEntrySecondary entry) {
		this.groupTitle = title;
		this.entry = entry;
	}
	
	public RibbonApplicationMenuEntrySecondary getEntry() {
		return entry;
	}
	
	public String getTitle() {
		return groupTitle;
	}
}

class RibbonApplicationMenuPrimaryContainer implements RibbonApplicationMenuContainer {
	final private CustomRibbonApplicationMenuEntryPrimary primary;

	public RibbonApplicationMenuPrimaryContainer(CustomRibbonApplicationMenuEntryPrimary primary) {
		this.primary = primary;
	}
	
	public RibbonApplicationMenuEntryPrimary getPrimary() {
		return primary;
	}
	
	@Override
	public void add(RibbonApplicationMenuEntryPrimary comp) {
		throw new RuntimeException("not supported!");
	}

	public void add(SecondaryGroupEntry group) {
		primary.addToSecondaryMenuGroup(group.getTitle(), group.getEntry());
	}

	@Override
	public void add(RibbonApplicationMenuEntryFooter comp) {
		throw new RuntimeException("not supported!");
	}	
}

class CustomRibbonApplicationMenuEntryPrimary extends RibbonApplicationMenuEntryPrimary {

	public CustomRibbonApplicationMenuEntryPrimary(ResizableIcon icon, String text, ActionListener mainActionListener, CommandButtonKind entryKind) {
		super(icon, text, mainActionListener, entryKind);
	}
	
	public synchronized void addToSecondaryMenuGroup(String groupTitle, RibbonApplicationMenuEntrySecondary... entries) {
		int index = this.groupTitles.indexOf(groupTitle);
		if(index == -1) {
			super.addSecondaryMenuGroup(groupTitle, entries);
		} else {
			List<RibbonApplicationMenuEntrySecondary> entryList = this.groupEntries.get(index);
			for (RibbonApplicationMenuEntrySecondary entry : entries) {
				entryList.add(entry);
			}
		}
	}
	
	
	
}
