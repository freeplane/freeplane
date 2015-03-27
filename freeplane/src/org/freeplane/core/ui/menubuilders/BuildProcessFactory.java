package org.freeplane.core.ui.menubuilders;

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

