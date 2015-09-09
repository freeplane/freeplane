package org.freeplane.core.ui.menubuilders.menu;

import static org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase.ACCELERATORS;
import static org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase.ACTIONS;
import static org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase.UI;

import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.ui.menubuilders.action.AcceleratorBuilder;
import org.freeplane.core.ui.menubuilders.action.AcceleratorDestroyer;
import org.freeplane.core.ui.menubuilders.action.ActionFinder;
import org.freeplane.core.ui.menubuilders.action.ActionSelectListener;
import org.freeplane.core.ui.menubuilders.action.EntriesForAction;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListenerCollection;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor;
import org.freeplane.core.ui.menubuilders.generic.RecursiveMenuStructureProcessor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.ui.menubuilders.generic.SubtreeProcessor;
import org.freeplane.features.mode.FreeplaneActions;

public class MenuBuildProcessFactory {

	private PhaseProcessor buildProcessor;

	public PhaseProcessor getBuildProcessor() {
		return buildProcessor;
	}

	public SubtreeProcessor getChildProcessor() {
		return childProcessor;
	}

	private SubtreeProcessor childProcessor;

	public MenuBuildProcessFactory createBuildProcessor(IUserInputListenerFactory userInputListenerFactory,
	                                                    FreeplaneActions freeplaneActions,
	                                           ResourceAccessor resourceAccessor, IAcceleratorMap acceleratorMap, EntriesForAction entries) {
		final RecursiveMenuStructureProcessor actionBuilder = new RecursiveMenuStructureProcessor();
		actionBuilder.setDefaultBuilder(new ActionFinder(freeplaneActions));

		final RecursiveMenuStructureProcessor acceleratorBuilder = new RecursiveMenuStructureProcessor();
		acceleratorBuilder.setDefaultBuilderPair(new AcceleratorBuilder(acceleratorMap, entries),
		    new AcceleratorDestroyer(acceleratorMap, entries));

		RecursiveMenuStructureProcessor uiBuilder = new RecursiveMenuStructureProcessor();
		uiBuilder.setDefaultBuilder(EntryVisitor.EMTPY);
		uiBuilder.addBuilder("ignore", EntryVisitor.CHILD_ENTRY_REMOVER);
		uiBuilder.addBuilder("skip", EntryVisitor.SKIP);
		
		childProcessor = new SubtreeProcessor();
		final ActionSelectListener actionSelectListener = new ActionSelectListener();
		EntryPopupListenerCollection entryPopupListenerCollection = new EntryPopupListenerCollection();
		entryPopupListenerCollection.addEntryPopupListener(childProcessor);
		entryPopupListenerCollection.addEntryPopupListener(actionSelectListener);

		if (userInputListenerFactory.useRibbonMenu()) {
			acceleratorMap.addAcceleratorChangeListener(new RibbonAcceleratorChangeListener(resourceAccessor, acceleratorMap, entries));
			
			uiBuilder.addBuilderPair("main_menu", new JRibbonBuilder(userInputListenerFactory), new JRibbonComponentRemover());
			uiBuilder.setSubtreeDefaultBuilderPair("main_menu", "ribbon.action");
			
			uiBuilder.addBuilderPair("ribbon_taskbar", new JRibbonTaskbarBuilder(), new JRibbonTaskbarDestroyer());
			uiBuilder.addBuilder("ribbon_menu", new JRibbonApplicationMenuBuilder(resourceAccessor));
			uiBuilder.addBuilder("primary_entry", new JRibbonApplicationMenuPrimaryBuilder(resourceAccessor, new AcceleratebleActionProvider(), acceleratorMap));
			uiBuilder.addBuilder("entry_group", new JRibbonApplicationMenuGroupBuilder(resourceAccessor, acceleratorMap));
			uiBuilder.addBuilder("footer_entry", new JRibbonApplicationMenuFooterBuilder(resourceAccessor, acceleratorMap));
			uiBuilder.addBuilderPair("ribbon_task", new JRibbonTaskBuilder(resourceAccessor), new JRibbonComponentRemover());
			uiBuilder.addBuilderPair("ribbon_band", new JRibbonBandBuilder(resourceAccessor), new JRibbonComponentRemover());	
			
			uiBuilder.addBuilderPair("ribbon.action", 
				new JRibbonActionBuilder(entryPopupListenerCollection, acceleratorMap, new AcceleratebleActionProvider(),
				    resourceAccessor), new JRibbonComponentRemover());
			
//			LastOpenedList lastOpenedList = ((ApplicationResourceController)ResourceController.getResourceController()).getLastOpenedList();
//			LastOpenedMapsRibbonContributorFactory lastOpenedMapsRibbonContributorFactory = lastOpenedList.getLastOpenedMapsRibbonContributorFactory();
//			RibbonBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder(RibbonBuilder.class);
//			menuBuilder.registerContributorFactory("lastOpenedMaps", lastOpenedMapsRibbonContributorFactory);
			
			
		}
		else {
			acceleratorMap.addAcceleratorChangeListener(new MenuAcceleratorChangeListener(entries));
			
			uiBuilder.addBuilder("toolbar", new JToolbarBuilder(userInputListenerFactory));
			uiBuilder.setSubtreeDefaultBuilderPair("toolbar", "toolbar.action");
			uiBuilder.addBuilder("toolbar.action", new JToolbarComponentBuilder());

			uiBuilder.addBuilder("main_menu", new JMenubarBuilder(userInputListenerFactory));
			uiBuilder.setSubtreeDefaultBuilderPair("main_menu", "menu.action");
			
			uiBuilder.addBuilderPair("radio_button_group", //
			    new JMenuRadioGroupBuilder(entryPopupListenerCollection, acceleratorMap, new AcceleratebleActionProvider(),
			        resourceAccessor), new JComponentRemover());
		}
		
		uiBuilder.addBuilder("map_popup", new MapPopupBuilder(userInputListenerFactory));
		uiBuilder.setSubtreeDefaultBuilderPair("map_popup", "menu.action");
		uiBuilder.addBuilder("node_popup", new NodePopupBuilder(userInputListenerFactory));
		uiBuilder.setSubtreeDefaultBuilderPair("node_popup", "menu.action");

		uiBuilder.addBuilderPair("menu.action", //
		    new JMenuItemBuilder(entryPopupListenerCollection, acceleratorMap, new AcceleratebleActionProvider(),
		        resourceAccessor), new JComponentRemover());

		buildProcessor = new PhaseProcessor()
								.withPhase(ACTIONS, actionBuilder) //
							    .withPhase(ACCELERATORS, acceleratorBuilder)
							    .withPhase(UI, uiBuilder);
		childProcessor.setProcessor(buildProcessor);
		return this;
	}
}

