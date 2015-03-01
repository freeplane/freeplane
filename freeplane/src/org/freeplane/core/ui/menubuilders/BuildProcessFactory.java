package org.freeplane.core.ui.menubuilders;

import org.freeplane.features.mode.FreeplaneActions;

public class BuildProcessFactory {

	public PhaseProcessor createBuildProcessor(FreeplaneActions freeplaneActions, MenuEntryBuilder menuEntryBuilder) {
		RecursiveMenuStructureProcessor recursiveMenuStructureBuilder = new RecursiveMenuStructureProcessor();

		recursiveMenuStructureBuilder.setDefaultBuilder(EntryVisitor.EMTPY_VISITOR);

		recursiveMenuStructureBuilder.addBuilder("toolbar", new JToolbarBuilder());
		recursiveMenuStructureBuilder.addSubtreeDefaultBuilder("toolbar", "toolbar.action");
		recursiveMenuStructureBuilder.addBuilder("toolbar.action", new JToolbarActionBuilder());

		recursiveMenuStructureBuilder.addBuilder("main_menu", new JMenubarBuilder());
		recursiveMenuStructureBuilder.addSubtreeDefaultBuilder("main_menu", "menu.action");
		final EntryPopupListenerImplementation popupListener = new EntryPopupListenerImplementation();
		recursiveMenuStructureBuilder.addBuilder("menu.action", new JMenuItemBuilder(popupListener, menuEntryBuilder));

		final RecursiveMenuStructureProcessor actionBuilder = new RecursiveMenuStructureProcessor();
		
		actionBuilder.setDefaultBuilder(new ActionFinder(freeplaneActions ));
		final PhaseProcessor buildProcessor = new PhaseProcessor(actionBuilder,recursiveMenuStructureBuilder);
		popupListener.setProcessor(buildProcessor);
		return buildProcessor;
	}
}

class EntryPopupListenerImplementation implements
EntryPopupListener {
	private Processor processor;

	@Override
	public void childEntriesWillBecomeVisible(Entry entry) {
		processor.process(entry);
	}

	public void setProcessor(Processor processor) {
		this.processor = processor;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void childEntriesWillBecomeInvisible(Entry entry) {
	}
}

