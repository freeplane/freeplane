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
import org.freeplane.features.mode.FreeplaneActions;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class JMenuBuilderTest {
	RecursiveMenuStructureBuilder recursiveMenuStructureBuilder;
	FreeplaneActions freeplaneActions;

	private Entry buildJMenu(String xmlWithoutContent) {
		final Entry buildMenuStructure = XmlEntryStructureBuilder.buildMenuStructure(xmlWithoutContent);
		final RecursiveMenuStructureBuilder actionBuilder = new RecursiveMenuStructureBuilder();
		actionBuilder.setDefaultBuilder(new ActionFinder(freeplaneActions));
		actionBuilder.build(buildMenuStructure);
		recursiveMenuStructureBuilder.build(buildMenuStructure);
		return buildMenuStructure;
	}

	@Before
	public void setup() {
		freeplaneActions = mock(FreeplaneActions.class);
		recursiveMenuStructureBuilder = new RecursiveMenuStructureBuilder();
		recursiveMenuStructureBuilder.setDefaultBuilder(Builder.EMTPY_BUILDER);
		recursiveMenuStructureBuilder.addBuilder("toolbar", new JToolbarBuilder());
		recursiveMenuStructureBuilder.addSubtreeDefaultBuilder("toolbar", "toolbar.actionGroup");
		recursiveMenuStructureBuilder.addBuilder("toolbar.actionGroup", new JToolbarActionGroupBuilder());
	}

	@Test
	public void createsEmptyToolbarComponent() {
		String xmlWithoutContent = "<freeplaneUIEntries>"
				+ "<entry name='home' builder='toolbar'/>"
				+ "</freeplaneUIEntries>";

		Entry builtMenuStructure = buildJMenu(xmlWithoutContent);
		assertThat(builtMenuStructure.getChild(0).getComponent().getClass(), CoreMatchers.<Object>is(FreeplaneToolBar.class));
	}

	@Test
	public void createsToolbarButtonWithAction() {
		String xmlWithoutContent = "<freeplaneUIEntries>"
				+ "<entry name='home' builder='toolbar'>"
				+ "<entry name='action'/>"
				+ "</entry>"
				+ "</freeplaneUIEntries>";

		final AFreeplaneAction someAction = Mockito.mock(AFreeplaneAction.class);
		when(freeplaneActions.getAction("action")).thenReturn(someAction);

		Entry builtMenuStructure = buildJMenu(xmlWithoutContent);
		
		assertThat(((JButton)builtMenuStructure.getChild(0).getChild(0).getComponent()).getAction(), CoreMatchers.<Action>equalTo(someAction));
	}

	@Test
	public void givengroupWithAction_addsActionButtonToToolbar() {
		String xmlWithoutContent = "<freeplaneUIEntries>"
				+ "<entry name='home' builder='toolbar'>"
				+ "<entry name='action'/>"
				+ "</entry>"
				+ "</freeplaneUIEntries>";

		final AFreeplaneAction someAction = Mockito.mock(AFreeplaneAction.class);
		when(freeplaneActions.getAction("action")).thenReturn(someAction);
		
		Entry builtMenuStructure = buildJMenu(xmlWithoutContent);
		
		final JToolBar toolbar = (JToolBar)builtMenuStructure.getChild(0).getComponent();
		final JButton button = (JButton)builtMenuStructure.getChild(0).getChild(0).getComponent();
		assertThat(button.getParent(), CoreMatchers.equalTo((Container)toolbar));
	}
}
