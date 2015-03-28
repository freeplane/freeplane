package org.freeplane.core.ui.menubuilders.menu;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;
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
import org.freeplane.core.ui.menubuilders.ResourceAccessorStub;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;
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

	@Before
	public void setup() {
		actionEntry = new Entry();
		action = Mockito.mock(AFreeplaneAction.class);
		actionEntry.setAction(action);

		menuEntry = new Entry();
		menu = new JMenu();
		popupListener = mock(EntryPopupListener.class);
		menuActionGroupBuilder = new JMenuItemBuilder(popupListener, new ResourceAccessorStub());
	}
	
	@Test
	public void createsMenuButtonWithAction() {
		menuEntry.setComponent(menu);
		menuEntry.addChild(actionEntry);
		menuActionGroupBuilder.visit(actionEntry);

		JMenuItem item = (JMenuItem)actionEntry.getComponent();

		assertThat(item.getAction(), CoreMatchers.<Action>equalTo(action));
		assertThat(item.getParent(), CoreMatchers.<Container>equalTo(menu.getPopupMenu()));
	}

	@Test
	public void createsMenuItemWithSelectableAction() {
		menuEntry.addChild(actionEntry);
		when(action.isSelectable()).thenReturn(true);
		menuEntry.setComponent(menu);
		menuActionGroupBuilder.visit(actionEntry);

		JAutoCheckBoxMenuItem item = (JAutoCheckBoxMenuItem)actionEntry.getComponent();

		assertThat(item.getAction(), CoreMatchers.<Action>equalTo(action));
		assertThat(item.getParent(), CoreMatchers.<Container>equalTo(menu.getPopupMenu()));
	}
	
	@Test
	public void createsMenuSeparator() {
		menuEntry.setComponent(menu);
		Entry separatorEntry = new Entry();
		separatorEntry.setBuilders(asList("separator"));
		menuEntry.addChild(separatorEntry);
		
		menuActionGroupBuilder.visit(separatorEntry);

		JPopupMenu.Separator separator = (JPopupMenu.Separator)separatorEntry.getComponent();

		assertThat(separator.getParent(), CoreMatchers.<Container>equalTo(menu.getPopupMenu()));
	}

	@Test
	public void createsSubmenuWithoutAction() {
		Entry parentMenuEntry = new Entry();
		final JMenu parentMenu = new JMenu();
		parentMenuEntry.setComponent(parentMenu);
		parentMenuEntry.addChild(menuEntry);
		menuEntry.addChild(actionEntry);
		
		menuActionGroupBuilder.visit(menuEntry);

		JMenu item = (JMenu)menuEntry.getComponent();
		assertThat(item.getParent(), CoreMatchers.<Container>equalTo(parentMenu.getPopupMenu()));
	}

	@Test
	public void createsMainMenuWithoutAction() {
		Entry parentMenuEntry = new Entry();
		final FreeplaneMenuBar parentMenu = new FreeplaneMenuBar();
		parentMenuEntry.setComponent(parentMenu);
		parentMenuEntry.addChild(menuEntry);
		menuEntry.addChild(actionEntry);
		
		menuActionGroupBuilder.visit(menuEntry);

		JMenu item = (JMenu)menuEntry.getComponent();
		assertThat(item.getParent(), CoreMatchers.<Container>equalTo(parentMenu));
	}

	@Test
	public void createsSubmenuWithAction() {

		Entry parentMenuEntry = new Entry();
		final JMenu parentMenu = new JMenu();
		parentMenuEntry.setComponent(parentMenu);
		parentMenuEntry.addChild(menuEntry);
		menuEntry.addChild(actionEntry);
		menuEntry.setAction(action);
		
		menuActionGroupBuilder.visit(menuEntry);

		JMenu item = (JMenu)menuEntry.getComponent();

		final JMenuItem menuItem = (JMenuItem) item.getPopupMenu().getComponent(0);
		assertThat(menuItem.getAction(), CoreMatchers.<Action>equalTo(action));
	}
	
	@Test
	public void whenPopupMenuBecomesVisible_popupListenerIsCalled() {
		Entry parentMenuEntry = new Entry();
		final JMenu parentMenu = new JMenu();
		parentMenuEntry.setComponent(parentMenu);
		parentMenuEntry.addChild(menuEntry);
		menuEntry.addChild(actionEntry);
		
		menuActionGroupBuilder.visit(menuEntry);

		JMenu item = (JMenu)menuEntry.getComponent();
		item.getPopupMenu().setVisible(true);
		verify(popupListener).childEntriesWillBecomeVisible(menuEntry);
	}

	
	@Test
	public void whenPopupMenuBecomesInvisible_popupListenerIsCalled() {
		Entry parentMenuEntry = new Entry();
		final JMenu parentMenu = new JMenu();
		parentMenuEntry.setComponent(parentMenu);
		parentMenuEntry.addChild(menuEntry);
		menuEntry.addChild(actionEntry);
		
		menuActionGroupBuilder.visit(menuEntry);

		JMenu item = (JMenu)menuEntry.getComponent();
		item.getPopupMenu().setVisible(true);
		item.getPopupMenu().setVisible(false);
		verify(popupListener).childEntriesWillBecomeInvisible(menuEntry);
	}
}
