package org.freeplane.core.ui.menubuilders;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.awt.Container;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.JAutoCheckBoxMenuItem;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.Mockito;

public class JMenuItemBuilderTest {
	@Test
	public void createsMenuButtonWithAction() {
		Entry actionEntry = new Entry();
		final AFreeplaneAction action = Mockito.mock(AFreeplaneAction.class);
		actionEntry.setAction(action);

		Entry menuEntry = new Entry();
		final JMenu menu = new JMenu();
		menuEntry.setComponent(menu);
		menuEntry.addChild(actionEntry);
		
		final JMenuItemBuilder menuActionGroupBuilder = new JMenuItemBuilder();
		menuActionGroupBuilder.build(actionEntry);

		JMenuItem item = (JMenuItem)actionEntry.getComponent();

		assertThat(item.getAction(), CoreMatchers.<Action>equalTo(action));
		assertThat(item.getParent(), CoreMatchers.<Container>equalTo(menu.getPopupMenu()));
	}
	
	@Test
	public void createsMenuItemWithSelectableAction() {
		Entry actionEntry = new Entry();
		final AFreeplaneAction action = Mockito.mock(AFreeplaneAction.class);
		when(action.isSelectable()).thenReturn(true);
		actionEntry.setAction(action);

		Entry menuEntry = new Entry();
		final JMenu menu = new JMenu();
		menuEntry.setComponent(menu);
		menuEntry.addChild(actionEntry);
		
		final JMenuItemBuilder menuActionGroupBuilder = new JMenuItemBuilder();
		menuActionGroupBuilder.build(actionEntry);

		JAutoCheckBoxMenuItem item = (JAutoCheckBoxMenuItem)actionEntry.getComponent();

		assertThat(item.getAction(), CoreMatchers.<Action>equalTo(action));
		assertThat(item.getParent(), CoreMatchers.<Container>equalTo(menu.getPopupMenu()));
	}
	
	@Test
	public void createsMenuSeparator() {
		Entry separatorEntry = new Entry();
		separatorEntry.setBuilders(asList("separator"));

		Entry menuEntry = new Entry();
		final JMenu menu = new JMenu();
		menuEntry.setComponent(menu);
		menuEntry.addChild(separatorEntry);
		
		final JMenuItemBuilder menuActionGroupBuilder = new JMenuItemBuilder();
		menuActionGroupBuilder.build(separatorEntry);

		JPopupMenu.Separator separator = (JPopupMenu.Separator)separatorEntry.getComponent();

		assertThat(separator.getParent(), CoreMatchers.<Container>equalTo(menu.getPopupMenu()));
	}
}
