package org.freeplane.core.ui.menubuilders;

import org.freeplane.features.mode.FreeplaneActions;

public class BuildProcessFactory {

	public PhaseProcessor createBuildProcessor(FreeplaneActions freeplaneActions, ResourceAccessor menuEntryBuilder) {
		RecursiveMenuStructureProcessor recursiveMenuStructureBuilder = new RecursiveMenuStructureProcessor();

		recursiveMenuStructureBuilder.setDefaultBuilder(EntryVisitor.EMTPY_VISITOR);

		recursiveMenuStructureBuilder.addBuilder("toolbar", new JToolbarBuilder());
		recursiveMenuStructureBuilder.addSubtreeDefaultBuilder("toolbar", "toolbar.action");
		recursiveMenuStructureBuilder.addBuilder("toolbar.action", new JToolbarActionBuilder());

		recursiveMenuStructureBuilder.addBuilder("main_menu", new JMenubarBuilder());
		recursiveMenuStructureBuilder.addSubtreeDefaultBuilder("main_menu", "menu.action");
		final ChildProcessor popupListener = new ChildProcessor();
		recursiveMenuStructureBuilder.addBuilder("menu.action", new JMenuItemBuilder(popupListener, menuEntryBuilder));

		final RecursiveMenuStructureProcessor actionBuilder = new RecursiveMenuStructureProcessor();
		
		actionBuilder.setDefaultBuilder(new ActionFinder(freeplaneActions ));
		final PhaseProcessor buildProcessor = new PhaseProcessor(actionBuilder,recursiveMenuStructureBuilder);
		popupListener.setProcessor(buildProcessor);
		return buildProcessor;
	}
}

