package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.dpolivaev.mnemonicsetter.MnemonicSetter;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionEnabler;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.MenuSplitter;
import org.freeplane.core.ui.MenuSplitterConfiguration;
import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.ui.svgicons.FreeplaneIconFactory;
import org.freeplane.core.ui.textchanger.TranslatedElement;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;

public class JMenuItemBuilder implements EntryVisitor{

	final private EntryPopupListener popupListener;
	final ResourceAccessor resourceAccessor;
	final private MenuSplitter menuSplitter;
	final private EntryAccessor entryAccessor;
	final private ComponentProvider menuActionComponentProvider;

	public JMenuItemBuilder(EntryPopupListener popupListener, IAcceleratorMap accelerators,
	                        AcceleratebleActionProvider acceleratebleActionProvider, ResourceAccessor resourceAccessor) {
		this(popupListener,
		    new MenuActionComponentProvider(accelerators, acceleratebleActionProvider, resourceAccessor),
		    resourceAccessor);
	}

	public JMenuItemBuilder(EntryPopupListener popupListener, ComponentProvider menuActionComponentProvider,
	                        ResourceAccessor resourceAccessor) {
		this.popupListener = popupListener;
		this.resourceAccessor = resourceAccessor;
		this.entryAccessor = new EntryAccessor(resourceAccessor);
		menuSplitter = new MenuSplitter(resourceAccessor.getIntProperty(
		    MenuSplitterConfiguration.MAX_MENU_ITEM_COUNT_KEY, 10));
		this.menuActionComponentProvider = menuActionComponentProvider;
	}

	@Override
	public void visit(Entry entry) {
	    if(Boolean.FALSE.equals(entry.getAttribute("allowed")))
	        return;
		if (containsSubmenu(entry))
			addSubmenu(entry);
		else
			addActionItem(entry);
	}

    public boolean containsSubmenu(Entry entry) {
        return (entry.hasChildren() || entryAccessor.getAction(entry) == null) && !entryAccessor.getText(entry).isEmpty();
    }

	private void addActionItem(Entry entry) {
		final Component actionComponent = createActionComponent(entry);
		if(actionComponent != null){
			setTranslationKey(entry, actionComponent);
			addComponent(entry, actionComponent);
		}
	}

	private Component createActionComponent(Entry entry) {
		
		// FIXME actually not possible
		final Object alreadyExistingComponent = entryAccessor.getComponent(entry);
		if(alreadyExistingComponent != null) {
			LogUtils.severe("BUG : component already exists at " + entry.getPath());
			return null;
		}
		
	    final Component component = menuActionComponentProvider.createComponent(entry);
		final AFreeplaneAction action = entryAccessor.getAction(entry);
		if (action != null) {
			final ActionEnabler actionEnabler = new ActionEnabler(component);
			action.addPropertyChangeListener(actionEnabler);
			entry.setAttribute(actionEnabler.getClass(), actionEnabler);
		}
		return component;
    }

	private void addComponent(Entry entry, final Component component) {
		entryAccessor.setComponent(entry, component);
		final Container container = (Container) entryAccessor.getAncestorComponent(entry);
		if (container != null)
			menuSplitter.addComponent(container, component);
    }

	private void addSubmenu(final Entry entry) {
		final Component actionComponent = createActionComponent(entry);
		JMenu menu = new JMenu();
		final String rawText = entryAccessor.getText(entry);
		LabelAndMnemonicSetter.setLabelAndMnemonic(menu, rawText);
		if(! entryAccessor.removeMenuIcon(entry)) {
		    final Icon icon = entryAccessor.getIcon(entry);
		    if (icon != null) {
		        menu.setIcon(FreeplaneIconFactory.toImageIcon(icon));
		    }
		}
		addComponent(entry, menu);
		if(actionComponent != null){
			menuSplitter.addMenuComponent(menu, actionComponent);
		}
		PopupMenuListenerForEntry popupMenuListener = new PopupMenuListenerForEntry(entry, popupListener, resourceAccessor);
		if(Compat.isMacOsX()) {
			addPopupMenuListenersForMacOsX(menu, popupMenuListener);
		}
		else {
			final JPopupMenu popupMenu = menu.getPopupMenu();
			popupMenu.addPopupMenuListener(MnemonicSetter.INSTANCE);
			popupMenu.addPopupMenuListener(popupMenuListener);
		}
		setTranslationKey(entry, menu);

	}

	private void addPopupMenuListenersForMacOsX(JMenu menu, PopupMenuListener popupMenuListener) {
		menu.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				PopupMenuEvent popupMenuEvent = new PopupMenuEvent(e.getSource());
				if(e.getStateChange() == ItemEvent.SELECTED) {
				    popupMenuListener.popupMenuWillBecomeVisible(popupMenuEvent);
				}
				if(e.getStateChange() == ItemEvent.DESELECTED) {
				    popupMenuListener.popupMenuWillBecomeInvisible(popupMenuEvent);
				}
			}
		});
	}

	private void setTranslationKey(final Entry entry, Component actionComponent) {
		if(actionComponent instanceof JComponent) {
			final String textKey = entryAccessor.getTextKey(entry);
			if (textKey != null)
				TranslatedElement.TEXT.setKey((JComponent) actionComponent, textKey);
			String tooltipKey = entryAccessor.getTooltipKey(entry);
			if (textKey != null)
				TranslatedElement.TOOLTIP.setKey((JComponent) actionComponent, tooltipKey);
		}
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return Boolean.FALSE.equals(entry.getAttribute("allowed")) || containsSubmenu(entry);
	}

}
