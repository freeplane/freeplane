package org.freeplane.core.ui.menubuilders.menu;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Container;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.JAutoCheckBoxMenuItem;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class JMenuItemBuilderTest {
	
	private Entry actionEntry;
	private AFreeplaneAction action;
	private Entry menuEntry;
	private JMenu menu;
	private JMenuItemBuilder menuActionGroupBuilder;
	private EntryPopupListener popupListener;
	private Entry groupEntry;
	private ResourceAccessor resourceAccessorMock;

	@Before
	public void setup() {
		actionEntry = new Entry();
		action = Mockito.mock(AFreeplaneAction.class);
		new EntryAccessor().setAction(actionEntry, action);

		menuEntry = new Entry();
		menuEntry.setName("menu");
		groupEntry = new Entry();
		menu = new JMenu();
		popupListener = mock(EntryPopupListener.class);
		resourceAccessorMock = mock(ResourceAccessor.class);
		when(resourceAccessorMock.getRawText(anyString())).thenReturn("");
		when(resourceAccessorMock.getRawText("menu")).thenReturn("menu");
		menuActionGroupBuilder = new JMenuItemBuilder(popupListener, resourceAccessorMock);
	}
	
	@Test
	public void createsMenuButtonWithAction() {
		new EntryAccessor().setComponent(menuEntry, menu);
		menuEntry.addChild(actionEntry);
		menuActionGroupBuilder.visit(actionEntry);

		JMenuItem item = (JMenuItem)new EntryAccessor().getComponent(actionEntry);

		assertThat(item.getAction(), CoreMatchers.<Action>equalTo(action));
		assertThat(item.getParent(), CoreMatchers.<Container>equalTo(menu.getPopupMenu()));
	}

	@Test
	public void createsMenuItemWithSelectableAction() {
		menuEntry.addChild(actionEntry);
		when(action.isSelectable()).thenReturn(true);
		new EntryAccessor().setComponent(menuEntry, menu);
		menuActionGroupBuilder.visit(actionEntry);

		JAutoCheckBoxMenuItem item = (JAutoCheckBoxMenuItem)new EntryAccessor().getComponent(actionEntry);

		assertThat(item.getAction(), CoreMatchers.<Action>equalTo(action));
		assertThat(item.getParent(), CoreMatchers.<Container>equalTo(menu.getPopupMenu()));
	}
	
	@Test
	public void createsMenuSeparator() {
		new EntryAccessor().setComponent(menuEntry, menu);
		Entry separatorEntry = new Entry();
		separatorEntry.setBuilders(asList("separator"));
		menuEntry.addChild(separatorEntry);
		
		menuActionGroupBuilder.visit(separatorEntry);

		JPopupMenu.Separator separator = (JPopupMenu.Separator)new EntryAccessor().getComponent(separatorEntry);

		assertThat(separator.getParent(), CoreMatchers.<Container>equalTo(menu.getPopupMenu()));
	}

	@Test
	public void createsSubmenuWithoutAction() {
		Entry parentMenuEntry = new Entry();
		final JMenu parentMenu = new JMenu();
		new EntryAccessor().setComponent(parentMenuEntry, parentMenu);
		parentMenuEntry.addChild(menuEntry);
		menuEntry.addChild(actionEntry);
		
		menuActionGroupBuilder.visit(menuEntry);

		JMenu item = (JMenu)new EntryAccessor().getComponent(menuEntry);
		assertThat(item.getParent(), CoreMatchers.<Container>equalTo(parentMenu.getPopupMenu()));
	}

	@Test
	public void createsMainMenuWithoutAction() {
		Entry parentMenuEntry = new Entry();
		final FreeplaneMenuBar parentMenu = new FreeplaneMenuBar();
		new EntryAccessor().setComponent(parentMenuEntry, parentMenu);
		parentMenuEntry.addChild(menuEntry);
		menuEntry.addChild(actionEntry);
		
		menuActionGroupBuilder.visit(menuEntry);

		JMenu item = (JMenu)new EntryAccessor().getComponent(menuEntry);
		assertThat(item.getParent(), CoreMatchers.<Container>equalTo(parentMenu));
	}

	@Test
	public void createsSubmenuWithAction() {

		Entry parentMenuEntry = new Entry();
		final JMenu parentMenu = new JMenu();
		new EntryAccessor().setComponent(parentMenuEntry, parentMenu);
		parentMenuEntry.addChild(menuEntry);
		menuEntry.addChild(actionEntry);
		new EntryAccessor().setAction(menuEntry, action);
		
		menuActionGroupBuilder.visit(menuEntry);

		JMenu item = (JMenu)new EntryAccessor().getComponent(menuEntry);

		final JMenuItem menuItem = (JMenuItem) item.getPopupMenu().getComponent(0);
		assertThat(menuItem.getAction(), CoreMatchers.<Action>equalTo(action));
	}

	@Test
	public void createsGroupWithAction() {
		Entry parentMenuEntry = new Entry();
		final JMenu parentMenu = new JMenu();
		new EntryAccessor().setComponent(parentMenuEntry, parentMenu);
		parentMenuEntry.addChild(groupEntry);
		groupEntry.addChild(actionEntry);
		new EntryAccessor().setAction(groupEntry, action);
		menuActionGroupBuilder.visit(groupEntry);
		final JMenuItem menuItem = (JMenuItem) parentMenu.getPopupMenu().getComponent(0);
		assertThat(menuItem.getAction(), CoreMatchers.<Action> equalTo(action));
	}
	
	@Test
	public void whenPopupMenuBecomesVisible_popupListenerIsCalled() {
		Entry parentMenuEntry = new Entry();
		final JMenu parentMenu = new JMenu();
		new EntryAccessor().setComponent(parentMenuEntry, parentMenu);
		parentMenuEntry.addChild(menuEntry);
		menuEntry.addChild(actionEntry);
		
		menuActionGroupBuilder.visit(menuEntry);

		JMenu item = (JMenu)new EntryAccessor().getComponent(menuEntry);
		item.getPopupMenu().setVisible(true);
		verify(popupListener).childEntriesWillBecomeVisible(menuEntry);
	}

	
	@Test
	public void whenPopupMenuBecomesInvisible_popupListenerIsCalled() {
		Entry parentMenuEntry = new Entry();
		final JMenu parentMenu = new JMenu();
		new EntryAccessor().setComponent(parentMenuEntry, parentMenu);
		parentMenuEntry.addChild(menuEntry);
		menuEntry.addChild(actionEntry);
		
		menuActionGroupBuilder.visit(menuEntry);

		JMenu item = (JMenu)new EntryAccessor().getComponent(menuEntry);
		item.getPopupMenu().setVisible(true);
		item.getPopupMenu().setVisible(false);
		verify(popupListener).childEntriesWillBecomeInvisible(menuEntry);
	}
}
