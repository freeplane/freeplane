package org.freeplane.core.ui.menubuilders;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Container;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.menubuilders.action.ActionFinder;
import org.freeplane.core.ui.menubuilders.generic.BuilderDestroyerPair;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor;
import org.freeplane.core.ui.menubuilders.generic.RecursiveMenuStructureProcessor;
import org.freeplane.core.ui.menubuilders.menu.JToolbarActionBuilder;
import org.freeplane.core.ui.menubuilders.menu.JToolbarBuilder;
import org.freeplane.features.mode.FreeplaneActions;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class MenuBuilderIntegrationTest {
	RecursiveMenuStructureProcessor recursiveMenuStructureBuilder;
	FreeplaneActions freeplaneActions;

	private Entry buildJMenu(String content) {
		final Entry buildMenuStructure = XmlEntryStructureBuilder.buildMenuStructure(content);
		final RecursiveMenuStructureProcessor actionBuilder = new RecursiveMenuStructureProcessor();
		actionBuilder.setDefaultBuilder(new ActionFinder(freeplaneActions));
		new PhaseProcessor().withPhase("action", actionBuilder).withPhase("menuUI", recursiveMenuStructureBuilder)
		    .build(buildMenuStructure);
		return buildMenuStructure;
	}

	@Before
	public void setup() {
		freeplaneActions = mock(FreeplaneActions.class);
		recursiveMenuStructureBuilder = new RecursiveMenuStructureProcessor();
		recursiveMenuStructureBuilder.setDefaultBuilderPair(new BuilderDestroyerPair(EntryVisitor.EMTPY, EntryVisitor.EMTPY));
		recursiveMenuStructureBuilder.addBuilderPair("toolbar", new BuilderDestroyerPair(new JToolbarBuilder(), null));
		recursiveMenuStructureBuilder.setSubtreeDefaultBuilderPair("toolbar", "toolbar.action");
		recursiveMenuStructureBuilder.addBuilderPair("toolbar.action", new BuilderDestroyerPair(new JToolbarActionBuilder(), null));
	}

	@Test
	public void createsEmptyToolbarComponent() {
		String content = "<FreeplaneUIEntries>"
				+ "<Entry name='home' builder='toolbar'/>"
				+ "</FreeplaneUIEntries>";

		Entry builtMenuStructure = buildJMenu(content);
		assertThat(builtMenuStructure.getChild(0).getComponent().getClass(), CoreMatchers.<Object>is(FreeplaneToolBar.class));
	}

	@Test
	public void createsToolbarButtonWithAction() {
		String content = "<FreeplaneUIEntries>"
				+ "<Entry name='home' builder='toolbar'>"
				+ "<Entry name='action'/>"
				+ "</Entry>"
				+ "</FreeplaneUIEntries>";

		final AFreeplaneAction someAction = Mockito.mock(AFreeplaneAction.class);
		when(freeplaneActions.getAction("action")).thenReturn(someAction);

		Entry builtMenuStructure = buildJMenu(content);
		
		assertThat(((JButton)builtMenuStructure.getChild(0).getChild(0).getComponent()).getAction(), CoreMatchers.<Action>equalTo(someAction));
	}

	@Test
	public void givengroupWithAction_addsActionButtonToToolbar() {
		String content = "<FreeplaneUIEntries>"
				+ "<Entry name='home' builder='toolbar'>"
				+ "<Entry name='action'/>"
				+ "</Entry>"
				+ "</FreeplaneUIEntries>";

		final AFreeplaneAction someAction = Mockito.mock(AFreeplaneAction.class);
		when(freeplaneActions.getAction("action")).thenReturn(someAction);
		
		Entry builtMenuStructure = buildJMenu(content);
		
		final JToolBar toolbar = (JToolBar)builtMenuStructure.getChild(0).getComponent();
		final JButton button = (JButton)builtMenuStructure.getChild(0).getChild(0).getComponent();
		assertThat(button.getParent(), CoreMatchers.equalTo((Container)toolbar));
	}
}
