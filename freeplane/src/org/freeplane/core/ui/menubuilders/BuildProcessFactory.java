package org.freeplane.core.ui.menubuilders;

import org.freeplane.core.ui.menubuilders.action.ActionFinder;
import org.freeplane.core.ui.menubuilders.action.ResourceAccessor;
import org.freeplane.core.ui.menubuilders.generic.BuilderDestroyerPair;
import org.freeplane.core.ui.menubuilders.generic.ChildProcessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor;
import org.freeplane.core.ui.menubuilders.generic.RecursiveMenuStructureProcessor;
import org.freeplane.core.ui.menubuilders.menu.JMenuItemBuilder;
import org.freeplane.core.ui.menubuilders.menu.JMenubarBuilder;
import org.freeplane.core.ui.menubuilders.menu.JToolbarActionBuilder;
import org.freeplane.core.ui.menubuilders.menu.JToolbarBuilder;
import org.freeplane.features.mode.FreeplaneActions;

public class BuildProcessFactory {

	public PhaseProcessor createBuildProcessor(FreeplaneActions freeplaneActions, ResourceAccessor menuEntryBuilder) {
		RecursiveMenuStructureProcessor recursiveMenuStructureBuilder = new RecursiveMenuStructureProcessor();

		recursiveMenuStructureBuilder.setDefaultBuilder(EntryVisitor.EMTPY_VISITOR);

		recursiveMenuStructureBuilder.addBuilderPair("toolbar", new BuilderDestroyerPair(new JToolbarBuilder(), null));
		recursiveMenuStructureBuilder.setSubtreeDefaultBuilderPair("toolbar", "toolbar.action");
		recursiveMenuStructureBuilder.addBuilderPair("toolbar.action", new BuilderDestroyerPair(new JToolbarActionBuilder(), null));

		recursiveMenuStructureBuilder.addBuilderPair("main_menu", new BuilderDestroyerPair(new JMenubarBuilder(), null));
		recursiveMenuStructureBuilder.setSubtreeDefaultBuilderPair("main_menu", "menu.action");
		final ChildProcessor popupListener = new ChildProcessor();
		recursiveMenuStructureBuilder.addBuilderPair("menu.action", new BuilderDestroyerPair(new JMenuItemBuilder(popupListener, menuEntryBuilder), null));

		final RecursiveMenuStructureProcessor actionBuilder = new RecursiveMenuStructureProcessor();
		
		actionBuilder.setDefaultBuilder(new ActionFinder(freeplaneActions ));
		final PhaseProcessor buildProcessor = new PhaseProcessor(actionBuilder,recursiveMenuStructureBuilder);
		popupListener.setProcessor(buildProcessor);
		return buildProcessor;
	}
}

