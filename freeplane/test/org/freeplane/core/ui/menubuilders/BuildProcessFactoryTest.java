package org.freeplane.core.ui.menubuilders;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import javax.swing.JMenu;

import org.freeplane.features.mode.FreeplaneActions;
import org.junit.Test;


public class BuildProcessFactoryTest {
	@Test
	public void ifProcessOnPopupIsSet_delayesActionProcessing() throws Exception {
		final BuildProcessFactory buildProcessFactory = new BuildProcessFactory();
		final FreeplaneActions freeplaneActions = mock(FreeplaneActions.class);
		final PhaseProcessor phaseProcessor = buildProcessFactory.createBuildProcessor(freeplaneActions, new MenuEntryBuilder() {

			@Override
			public JMenu createMenuEntry(Entry entry) {
				return new JMenu(entry.getName());
			}
		});
		final Entry menuStructure = XmlEntryStructureBuilder.buildMenuStructure(
				"<Entry builder='main_menu'>"
						+ "<Entry name='submenu'>"
						+ "<Entry name='submenu' processOnPopup='true'>"
						+ "<Entry name='action'/>"
						+ "</Entry>"
						+ "</Entry>"
						+ "</Entry>");
		phaseProcessor.build(menuStructure);
		verify(freeplaneActions, never()).getAction("action");
	}

	@Test
	public void ifProcessOnPopupIsSet_buildsWhenItBecomesVisible() throws Exception {
		final BuildProcessFactory buildProcessFactory = new BuildProcessFactory();
		final FreeplaneActions freeplaneActions = mock(FreeplaneActions.class);
		final PhaseProcessor phaseProcessor = buildProcessFactory.createBuildProcessor(freeplaneActions, new MenuEntryBuilder() {

			@Override
			public JMenu createMenuEntry(Entry entry) {
				return new JMenu(entry.getName());
			}
		});
		final Entry menuStructure = XmlEntryStructureBuilder.buildMenuStructure(
				"<Entry builder='main_menu'>"
						+ "<Entry name='submenu'>"
						+ "<Entry name='submenu' processOnPopup='true'>"
						+ "<Entry name='action'/>"
						+ "</Entry>"
						+ "</Entry>"
						+ "</Entry>");
		phaseProcessor.build(menuStructure);
		final Entry openedEntry = menuStructure.getChild(0,0,0);
		JMenu menu = (JMenu) openedEntry.getComponent();
		menu.getPopupMenu().setVisible(true);
		verify(freeplaneActions).getAction("action");
	}
}
