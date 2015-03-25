package org.freeplane.core.ui.menubuilders;

import static java.lang.Boolean.TRUE;
import static org.freeplane.core.ui.menubuilders.RecursiveMenuStructureProcessor.PROCESS_ON_POPUP;

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
		if(TRUE.equals(entry.getAttribute(PROCESS_ON_POPUP)))
			for (Entry child:entry.children())
				processor.build(child);
	}

	public void setProcessor(Processor processor) {
		this.processor = processor;
		
	}

	@Override
	public void childEntriesWillBecomeInvisible(Entry entry) {
	}
}

