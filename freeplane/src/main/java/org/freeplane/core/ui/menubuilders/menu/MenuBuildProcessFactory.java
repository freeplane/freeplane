package org.freeplane.core.ui.menubuilders.menu;

import static org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phases.ACTIONS;
import static org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phases.UI;

import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.ui.menubuilders.action.AcceleratorBuilder;
import org.freeplane.core.ui.menubuilders.action.ActionFinder;
import org.freeplane.core.ui.menubuilders.action.ActionSelectListener;
import org.freeplane.core.ui.menubuilders.action.EntriesForAction;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.ChildProcessor;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListenerCollection;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phases;
import org.freeplane.core.ui.menubuilders.generic.RecursiveMenuStructureProcessor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.features.mode.FreeplaneActions;

public class MenuBuildProcessFactory {

	public PhaseProcessor createBuildProcessor(FreeplaneActions freeplaneActions, ResourceAccessor resourceAccessor,
	                                           IAcceleratorMap acceleratorMap) {
		final RecursiveMenuStructureProcessor actionBuilder = new RecursiveMenuStructureProcessor();
		actionBuilder.setDefaultBuilder(new ActionFinder(freeplaneActions));
		final RecursiveMenuStructureProcessor acceleratorBuilder = new RecursiveMenuStructureProcessor();
		final EntriesForAction entries = new EntriesForAction();
		acceleratorBuilder.setDefaultBuilder(new AcceleratorBuilder(acceleratorMap, entries));
		acceleratorMap.addAcceleratorChangeListener(new MenuAcceleratorChangeListener(entries));
		RecursiveMenuStructureProcessor recursiveMenuStructureBuilder = new RecursiveMenuStructureProcessor();
		recursiveMenuStructureBuilder.setDefaultBuilder(EntryVisitor.EMTPY);
		recursiveMenuStructureBuilder.addBuilder("ribbon_taskbar", EntryVisitor.SKIP);
		recursiveMenuStructureBuilder.addBuilder("ribbon_menu", EntryVisitor.SKIP);
		recursiveMenuStructureBuilder.addBuilder("toolbar", new JToolbarBuilder());
		recursiveMenuStructureBuilder.setSubtreeDefaultBuilderPair("toolbar", "toolbar.action");
		recursiveMenuStructureBuilder.addBuilder("toolbar.action", new JToolbarActionBuilder());
		recursiveMenuStructureBuilder.addBuilder("main_menu", new JMenubarBuilder());
		recursiveMenuStructureBuilder.setSubtreeDefaultBuilderPair("main_menu", "menu.action");
		final ChildProcessor childBuilder = new ChildProcessor();
		final ActionSelectListener actionSelectListener = new ActionSelectListener();
		EntryPopupListenerCollection entryPopupListenerCollection = new EntryPopupListenerCollection();
		entryPopupListenerCollection.addEntryPopupListener(childBuilder);
		entryPopupListenerCollection.addEntryPopupListener(actionSelectListener);
		recursiveMenuStructureBuilder.addBuilderPair("menu.action", //
		    new JMenuItemBuilder(entryPopupListenerCollection, acceleratorMap, new AcceleratebleActionProvider(),
		        resourceAccessor), new JComponentRemover());

		final PhaseProcessor buildProcessor = new PhaseProcessor().withPhase(ACTIONS, actionBuilder) //
		    .withPhase(Phases.ACCELERATORS, acceleratorBuilder).withPhase(UI, recursiveMenuStructureBuilder);
		childBuilder.setProcessor(buildProcessor);
		return buildProcessor;
	}
}

