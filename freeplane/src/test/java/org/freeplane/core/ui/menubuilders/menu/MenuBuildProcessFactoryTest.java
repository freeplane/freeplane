package org.freeplane.core.ui.menubuilders.menu;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.swing.JMenu;

import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.menubuilders.XmlEntryStructureBuilder;
import org.freeplane.core.ui.menubuilders.action.EntriesForAction;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.BuildPhaseListener;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.features.mode.FreeplaneActions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;


public class MenuBuildProcessFactoryTest {
	private PhaseProcessor phaseProcessor;
	private FreeplaneActions freeplaneActions;

	@Before
	public void setup() {
		freeplaneActions = mock(FreeplaneActions.class);
		final ResourceAccessor resourceAccessorMock = mock(ResourceAccessor.class);
		when(resourceAccessorMock.getRawText(ArgumentMatchers.anyString())).thenReturn("text");
		final IUserInputListenerFactory userInputListenerFactory = mock(IUserInputListenerFactory.class);
		final FreeplaneMenuBar menubar = TestMenuBarFactory.createFreeplaneMenuBar();
		when(userInputListenerFactory.getMenuBar()).thenReturn(menubar);
		phaseProcessor = new MenuBuildProcessFactory(userInputListenerFactory, freeplaneActions,
			resourceAccessorMock, mock(IAcceleratorMap.class), new EntriesForAction(), Collections.<BuildPhaseListener>emptyList()).getBuildProcessor();
	}

	@Test
	public void ifProcessOnPopupIsSet_delayesActionProcessing() throws Exception {
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
		final Entry menuStructure = XmlEntryStructureBuilder.buildMenuStructure(
				"<Entry builder='main_menu'>"
						+ "<Entry name='submenu'>"
						+ "<Entry name='submenu' processOnPopup='true'>"
						+ "<Entry name='action'/>"
						+ "</Entry>"
						+ "</Entry>"
						+ "</Entry>");
		phaseProcessor.build(menuStructure);
        setVisible(menuStructure, 0, 0);
        setVisible(menuStructure, 0, 0, 0);
		verify(freeplaneActions).getAction("action");
	}

    private void setVisible(final Entry menuStructure, int... indices) {
        final Entry openedEntry = menuStructure.getChild(indices);
		JMenu menu = (JMenu) new EntryAccessor().getComponent(openedEntry);
		menu.getPopupMenu().setVisible(true);
    }

}
