package org.freeplane.core.ui.menubuilders.menu;

import static org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase.ACCELERATORS;
import static org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase.ACTIONS;
import static org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase.UI;

import java.util.List;

import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.ui.menubuilders.action.AcceleratorBuilder;
import org.freeplane.core.ui.menubuilders.action.AcceleratorDestroyer;
import org.freeplane.core.ui.menubuilders.action.ActionFinder;
import org.freeplane.core.ui.menubuilders.action.ActionStatusUpdater;
import org.freeplane.core.ui.menubuilders.action.EntriesForAction;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.BuildPhaseListener;
import org.freeplane.core.ui.menubuilders.generic.BuildProcessFactory;
import org.freeplane.core.ui.menubuilders.generic.ChildEntryFilter;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListenerCollection;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor;
import org.freeplane.core.ui.menubuilders.generic.RecursiveMenuStructureProcessor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.ui.menubuilders.generic.SubtreeProcessor;
import org.freeplane.core.util.Compat;
import org.freeplane.features.mode.FreeplaneActions;

public class MenuBuildProcessFactory implements BuildProcessFactory {

	private PhaseProcessor buildProcessor;
	
	private SubtreeProcessor childProcessor;

	@Override
    public PhaseProcessor getBuildProcessor() {
		return buildProcessor;
	}

	@Override
    public SubtreeProcessor getChildProcessor() {
		return childProcessor;
	}

	public MenuBuildProcessFactory(IUserInputListenerFactory userInputListenerFactory,
	                               FreeplaneActions modeController,
	                                           ResourceAccessor resourceAccessor, IAcceleratorMap acceleratorMap, EntriesForAction entries, List<BuildPhaseListener> buildPhaseListeners) {
		final RecursiveMenuStructureProcessor actionBuilder = new RecursiveMenuStructureProcessor();
		actionBuilder.setDefaultBuilder(new ActionFinder(modeController));
		actionBuilder.addBuilder("conditionalActionBuilder", new ConditionalActionBuilder(modeController));

		final RecursiveMenuStructureProcessor acceleratorBuilder = new RecursiveMenuStructureProcessor();
		acceleratorBuilder.setDefaultBuilderPair(new AcceleratorBuilder(acceleratorMap, entries),
		    new AcceleratorDestroyer(modeController, acceleratorMap, entries));

        
        childProcessor = new SubtreeProcessor(RecursiveMenuStructureProcessor::shouldProcessOnEvent);
        final ActionStatusUpdater actionSelectListener = new ActionStatusUpdater();
        EntryPopupListenerCollection entryPopupListenerCollection = new EntryPopupListenerCollection();
        entryPopupListenerCollection.addEntryPopupListener(childProcessor);
        
        acceleratorMap.addAcceleratorChangeListener(modeController, new MenuAcceleratorChangeListener(entries));

        final RecursiveMenuStructureProcessor uiBuilder = new RecursiveMenuStructureProcessor();
		uiBuilder.setDefaultBuilderPair(EntryVisitor.EMTPY, EntryVisitor.EMTPY);
		uiBuilder.addBuilderPair("skip", EntryVisitor.SKIP, EntryVisitor.SKIP);
		
		uiBuilder.addBuilder("toolbar", new JToolbarBuilder(userInputListenerFactory));
		uiBuilder.setSubtreeDefaultBuilderPair("toolbar", "toolbar.action");
		uiBuilder.addBuilder("toolbar.action", new JToolbarComponentBuilder());

		uiBuilder.addBuilder("main_menu", new JMenubarBuilder(userInputListenerFactory));
        uiBuilder.setSubtreeDefaultBuilderPair("main_menu", "menu");
        uiBuilder.setSubtreeDefaultBuilderPair("menu", "skip");
		
		uiBuilder.addBuilder("map_popup", new PopupBuilder(userInputListenerFactory.getMapPopup(), entryPopupListenerCollection, resourceAccessor));
		uiBuilder.setSubtreeDefaultBuilderPair("map_popup", "skip");
		uiBuilder.addBuilder("node_popup", new PopupBuilder(userInputListenerFactory.getNodePopupMenu(), entryPopupListenerCollection, resourceAccessor));
		uiBuilder.setSubtreeDefaultBuilderPair("node_popup", "skip");

		AcceleratebleActionProvider acceleratebleActionProvider = new AcceleratebleActionProvider();
		JMenuItemBuilder menuBuilder = new JMenuItemBuilder(entryPopupListenerCollection, acceleratorMap, acceleratebleActionProvider,
		        resourceAccessor);
        JComponentRemover destroyer = JComponentRemover.INSTANCE;
		uiBuilder.addBuilder("menu", menuBuilder);
        uiBuilder.addBuilderPair("radio_button_group", EntryVisitor.SKIP, EntryVisitor.SKIP);

        final RecursiveMenuStructureProcessor menuItemBuilder = new RecursiveMenuStructureProcessor();
        menuItemBuilder.setDefaultBuilderPair(menuBuilder, destroyer);
        menuItemBuilder.addBuilderPair("skip", EntryVisitor.SKIP, EntryVisitor.SKIP);
        menuItemBuilder.addBuilderPair("noActions", new EmptyMenuItemBuilder(resourceAccessor), destroyer);

        menuItemBuilder.addBuilderPair("radio_button_group", //
            new JMenuRadioGroupBuilder(entryPopupListenerCollection, acceleratorMap, acceleratebleActionProvider,
                resourceAccessor), 
                new JRadioButtonGroupComponentRemover()
                );

        actionBuilder.addBuilderPair("ignore", new ChildEntryFilter() {
			@Override
			public boolean shouldRemove(Entry entry) {
				return ! uiBuilder.containsOneOf(entry.builders());
			}
		}, EntryVisitor.EMTPY);
		
		if(Compat.isMacOsX()){
			actionBuilder.addBuilderPair("removeOnMac", new ChildEntryFilter() {
				@Override
				public boolean shouldRemove(Entry entry) {
					return true;
				}
			}, EntryVisitor.EMTPY);
		}

		buildProcessor = new PhaseProcessor(buildPhaseListeners)
								.withPhase(ACTIONS, actionBuilder) //
							    .withPhase(ACCELERATORS, acceleratorBuilder)
							    .withPhase(UI, uiBuilder);
		childProcessor.setProcessor(buildProcessor);
		
		PhaseProcessor menuItemProcessor = new PhaseProcessor(buildPhaseListeners)
		.withPhase(UI, menuItemBuilder);
		
        SubtreeProcessor menuProcessor = new SubtreeProcessor(e -> menuBuilder.containsSubmenu(e) && ! e.builders().contains("radio_button_group"));
        entryPopupListenerCollection.addEntryPopupListener(menuProcessor);
        entryPopupListenerCollection.addEntryPopupListener(actionSelectListener);
		menuProcessor.setProcessor(menuItemProcessor);
	}
}

