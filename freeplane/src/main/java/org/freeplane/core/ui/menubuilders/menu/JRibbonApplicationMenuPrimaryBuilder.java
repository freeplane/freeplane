package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
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
	private ResourceAccessor resourceAccessor;
	private AcceleratebleActionProvider acceleratebleActionProvider;
	private EntryAccessor entryAccessor;
	
	public JRibbonApplicationMenuPrimaryBuilder(ResourceAccessor resourceAccessor, AcceleratebleActionProvider acceleratebleActionProvider) {
		super();
		this.resourceAccessor = resourceAccessor;
		this.acceleratebleActionProvider = acceleratebleActionProvider;
		this.entryAccessor = new EntryAccessor(resourceAccessor);
	}
	@Override
	public void visit(Entry entry) {
		RibbonApplicationMenuEntryPrimary component = createMenuEntry(entry);
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
	
	private RibbonApplicationMenuEntryPrimary createMenuEntry(Entry entry) {
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

	 
		return new RibbonApplicationMenuEntryPrimary(icon, title, listener, kind);
	}
}

class SecondaryEntryGroup {
	private final String groupTitle;
	private List<RibbonApplicationMenuEntrySecondary> entries = new ArrayList<RibbonApplicationMenuEntrySecondary>();
	
	public SecondaryEntryGroup(String title) {
		this.groupTitle = title;
	}
	
	public void addEntry(RibbonApplicationMenuEntrySecondary entry) {
		entries.add(entry);
	}
	
	public List<RibbonApplicationMenuEntrySecondary> getEntries() {
		return Collections.unmodifiableList(entries);
	}
	
	public String getTitle() {
		return groupTitle;
	}
}

class RibbonApplicationMenuPrimaryContainer implements RibbonApplicationMenuContainer {
	final private RibbonApplicationMenuEntryPrimary primary;

	public RibbonApplicationMenuPrimaryContainer(RibbonApplicationMenuEntryPrimary primary) {
		this.primary = primary;
	}
	
	public RibbonApplicationMenuEntryPrimary getPrimary() {
		return primary;
	}
	
	@Override
	public void add(RibbonApplicationMenuEntryPrimary comp) {
		throw new RuntimeException("not supported!");
	}

	@Override
	public void add(SecondaryEntryGroup group) {
		primary.addSecondaryMenuGroup(group.getTitle(), group.getEntries().toArray(new RibbonApplicationMenuEntrySecondary[0]));
	}

	@Override
	public void add(RibbonApplicationMenuEntryFooter comp) {
		throw new RuntimeException("not supported!");
	}	
}
