package org.freeplane.core.ui.menubuilders.menu;

import static org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase.ACTIONS;
import static org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase.UI;

import org.freeplane.core.ui.IUserInputListenerFactory;
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
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase;
import org.freeplane.core.ui.menubuilders.generic.RecursiveMenuStructureProcessor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.features.mode.FreeplaneActions;

public class MenuBuildProcessFactory {

	public PhaseProcessor createBuildProcessor(IUserInputListenerFactory userInputListenerFactory, FreeplaneActions freeplaneActions,
	                                           ResourceAccessor resourceAccessor, IAcceleratorMap acceleratorMap, EntriesForAction entries) {
		final RecursiveMenuStructureProcessor actionBuilder = new RecursiveMenuStructureProcessor();
		actionBuilder.setDefaultBuilder(new ActionFinder(freeplaneActions));

		final RecursiveMenuStructureProcessor acceleratorBuilder = new RecursiveMenuStructureProcessor();
		acceleratorBuilder.setDefaultBuilder(new AcceleratorBuilder(acceleratorMap, entries));

		RecursiveMenuStructureProcessor recursiveMenuStructureBuilder = new RecursiveMenuStructureProcessor();
		recursiveMenuStructureBuilder.setDefaultBuilder(EntryVisitor.EMTPY);
		recursiveMenuStructureBuilder.addBuilder("ignore", EntryVisitor.CHILD_ENTRY_REMOVER);
		recursiveMenuStructureBuilder.addBuilder("toolbar", new JToolbarBuilder(userInputListenerFactory));
		recursiveMenuStructureBuilder.setSubtreeDefaultBuilderPair("toolbar", "toolbar.action");
		recursiveMenuStructureBuilder.addBuilder("toolbar.action", new JToolbarComponentBuilder());
		recursiveMenuStructureBuilder.addBuilder("main_menu", new JMenubarBuilder(userInputListenerFactory));
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
		    .withPhase(Phase.ACCELERATORS, acceleratorBuilder).withPhase(UI, recursiveMenuStructureBuilder);
		childBuilder.setProcessor(buildProcessor);
		return buildProcessor;
	}
}

