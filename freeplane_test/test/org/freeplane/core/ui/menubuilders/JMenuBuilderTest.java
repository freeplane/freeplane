package org.freeplane.core.ui.menubuilders;

import static org.freeplane.core.ui.menubuilders.XmlEntryStructureBuilder.buildMenuStructure;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

	private void buildJMenu(Entry structure) {
			recursiveMenuStructureBuilder.build(structure);
	}


	@Before
	public void setup() {
		freeplaneActions = mock(FreeplaneActions.class);
		recursiveMenuStructureBuilder = new RecursiveMenuStructureBuilder();
		recursiveMenuStructureBuilder.setDefaultBuilder(Builder.EMTPY_BUILDER);
		recursiveMenuStructureBuilder.addBuilder("toolbar", new JToolbarBuilder());
		recursiveMenuStructureBuilder.addSubtreeDefaultBuilder("toolbar", "toolbar.actionGroup");
		recursiveMenuStructureBuilder.addBuilder("toolbar.actionGroup", new JToolbarActionGroupBuilder(freeplaneActions));
	}

	@Test
	public void createsEmptyToolbarComponent() {
		String xmlWithoutContent = "<freeplaneUIEntries>"
				+ "<entry name='home' builder='toolbar'/>"
				+ "</freeplaneUIEntries>";

		Entry builtMenuStructure = buildMenuStructure(xmlWithoutContent);
		
		buildJMenu(builtMenuStructure);
		
		assertThat(builtMenuStructure.getChild(0).getComponent().getClass(), CoreMatchers.<Object>is(FreeplaneToolBar.class));
	}


	@Test
	public void createsToolbarButtonWithAction() {
		String xmlWithoutContent = "<freeplaneUIEntries>"
				+ "<entry name='home' builder='toolbar'>"
				+ "<entry action='action'/>"
				+ "</entry>"
				+ "</freeplaneUIEntries>";

		Entry builtMenuStructure = buildMenuStructure(xmlWithoutContent);
		final AFreeplaneAction someAction = Mockito.mock(AFreeplaneAction.class);
		when(freeplaneActions.getAction("action")).thenReturn(someAction);
		
		
		buildJMenu(builtMenuStructure);
		
		assertThat(((JButton)builtMenuStructure.getChild(0).getChild(0).getComponent()).getAction(), CoreMatchers.equalTo(someAction));
	}

	@Test
	public void givengroupWithAction_addsActionButtonToToolbar() {
		String xmlWithoutContent = "<freeplaneUIEntries>"
				+ "<entry name='home' builder='toolbar'>"
				+ "<entry action='action'/>"
				+ "</entry>"
				+ "</freeplaneUIEntries>";

		Entry builtMenuStructure = buildMenuStructure(xmlWithoutContent);
		final AFreeplaneAction someAction = Mockito.mock(AFreeplaneAction.class);
		when(freeplaneActions.getAction("action")).thenReturn(someAction);
		
		
		buildJMenu(builtMenuStructure);
		
		final JToolBar toolbar = (JToolBar)builtMenuStructure.getChild(0).getComponent();
		final JButton button = (JButton)builtMenuStructure.getChild(0).getChild(0).getComponent();
		assertThat(button.getParent(), CoreMatchers.equalTo(toolbar));
	}
}
