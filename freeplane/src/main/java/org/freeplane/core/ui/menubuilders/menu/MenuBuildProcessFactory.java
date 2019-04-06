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

	public PhaseProcessor getBuildProcessor() {
		return buildProcessor;
	}

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
		    new AcceleratorDestroyer(entries));

		final RecursiveMenuStructureProcessor uiBuilder = new RecursiveMenuStructureProcessor();
		uiBuilder.setDefaultBuilder(EntryVisitor.EMTPY);
		uiBuilder.addBuilder("skip", EntryVisitor.SKIP);
		
		childProcessor = new SubtreeProcessor();
		final ActionStatusUpdater actionSelectListener = new ActionStatusUpdater();
		EntryPopupListenerCollection entryPopupListenerCollection = new EntryPopupListenerCollection();
		entryPopupListenerCollection.addEntryPopupListener(childProcessor);
		entryPopupListenerCollection.addEntryPopupListener(actionSelectListener);

		
		acceleratorMap.addAcceleratorChangeListener(modeController, new MenuAcceleratorChangeListener(entries));
		
		uiBuilder.addBuilder("toolbar", new JToolbarBuilder(userInputListenerFactory));
		uiBuilder.setSubtreeDefaultBuilderPair("toolbar", "toolbar.action");
		uiBuilder.addBuilder("toolbar.action", new JToolbarComponentBuilder());

		uiBuilder.addBuilder("main_menu", new JMenubarBuilder(userInputListenerFactory));
		uiBuilder.setSubtreeDefaultBuilderPair("main_menu", "menu.action");
		
		uiBuilder.addBuilderPair("radio_button_group", //
		    new JMenuRadioGroupBuilder(entryPopupListenerCollection, acceleratorMap, new AcceleratebleActionProvider(),
		        resourceAccessor), new JComponentRemover());
		
		uiBuilder.addBuilder("map_popup", new PopupBuilder(userInputListenerFactory.getMapPopup(), entryPopupListenerCollection));
		uiBuilder.setSubtreeDefaultBuilderPair("map_popup", "menu.action");
		uiBuilder.addBuilder("node_popup", new PopupBuilder(userInputListenerFactory.getNodePopupMenu(), entryPopupListenerCollection));
		uiBuilder.setSubtreeDefaultBuilderPair("node_popup", "menu.action");

		actionBuilder.addBuilder("ignore", new ChildEntryFilter() {
			@Override
			public boolean shouldRemove(Entry entry) {
				return ! uiBuilder.containsOneOf(entry.builders());
			}
		});
		
		if(Compat.isMacOsX()){
			actionBuilder.addBuilder("removeOnMac", new ChildEntryFilter() {
				@Override
				public boolean shouldRemove(Entry entry) {
					return true;
				}
			});
		}

		JMenuItemBuilder menuBuilder = new JMenuItemBuilder(entryPopupListenerCollection, acceleratorMap, new AcceleratebleActionProvider(),
		    resourceAccessor);
		JComponentRemover destroyer = new JComponentRemover();
		uiBuilder.addBuilderPair("menu", menuBuilder, destroyer);
		uiBuilder.addBuilderPair("menu.action", menuBuilder, destroyer);
		uiBuilder.addBuilderPair("noActions", new EmptyMenuItemBuilder(resourceAccessor), destroyer);


		buildProcessor = new PhaseProcessor(buildPhaseListeners)
								.withPhase(ACTIONS, actionBuilder) //
							    .withPhase(ACCELERATORS, acceleratorBuilder)
							    .withPhase(UI, uiBuilder);
		childProcessor.setProcessor(buildProcessor);
	}
}

