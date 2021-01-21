package org.freeplane.core.ui.menubuilders.menu;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.awt.Container;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.components.JAutoToggleButton;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.Mockito;

public class JToolbarComponentBuilderTest {
	@Test
	public void createsToolbarButtonWithAction() {
		Entry actionEntry = new Entry();
		final AFreeplaneAction action = Mockito.mock(AFreeplaneAction.class);
		new EntryAccessor().setAction(actionEntry, action);

		Entry toolbarEntry = new Entry();
		final FreeplaneToolBar toolbar = new FreeplaneToolBar("toolbar", SwingConstants.HORIZONTAL);
		new EntryAccessor().setComponent(toolbarEntry, toolbar);
		toolbarEntry.addChild(actionEntry);
		
		final JToolbarComponentBuilder toolbarActionGroupBuilder = new JToolbarComponentBuilder();
		toolbarActionGroupBuilder.visit(actionEntry);

		JButton button = (JButton)new EntryAccessor().getComponent(actionEntry);

		assertThat(button.getAction(), CoreMatchers.<Action>equalTo(action));
		assertThat(button.getParent(), CoreMatchers.equalTo((Container)toolbar));
	}
	
	@Test
	public void createsToolbarButtonWithSelectableAction() {
		Entry actionEntry = new Entry();
		final AFreeplaneAction action = Mockito.mock(AFreeplaneAction.class);
		when(action.isSelectable()).thenReturn(true);
		new EntryAccessor().setAction(actionEntry, action);

		Entry toolbarEntry = new Entry();
		final FreeplaneToolBar toolbar = new FreeplaneToolBar("toolbar", SwingConstants.HORIZONTAL);
		new EntryAccessor().setComponent(toolbarEntry, toolbar);
		toolbarEntry.addChild(actionEntry);
		
		final JToolbarComponentBuilder toolbarActionGroupBuilder = new JToolbarComponentBuilder();
		toolbarActionGroupBuilder.visit(actionEntry);

		JAutoToggleButton button = (JAutoToggleButton)new EntryAccessor().getComponent(actionEntry);

		assertThat(button.getAction(), CoreMatchers.<Action>equalTo(action));
		assertThat(button.getParent(), CoreMatchers.equalTo((Container)toolbar));
	}
	
	@Test
	public void createsVerticalToolbarSeparator() {
		Entry separatorEntry = new Entry();
		separatorEntry.setBuilders(asList("separator"));

		Entry toolbarEntry = new Entry();
		final FreeplaneToolBar toolbar = new FreeplaneToolBar("toolbar", SwingConstants.HORIZONTAL);
		new EntryAccessor().setComponent(toolbarEntry, toolbar);
		toolbarEntry.addChild(separatorEntry);
		
		final JToolbarComponentBuilder toolbarActionGroupBuilder = new JToolbarComponentBuilder();
		toolbarActionGroupBuilder.visit(separatorEntry);

		JToolBar.Separator separator = (JToolBar.Separator)new EntryAccessor().getComponent(separatorEntry);

		assertThat(separator.getParent(), CoreMatchers.equalTo((Container)toolbar));
		assertThat(separator.getOrientation(), CoreMatchers.equalTo(SwingConstants.VERTICAL));
	}
}
