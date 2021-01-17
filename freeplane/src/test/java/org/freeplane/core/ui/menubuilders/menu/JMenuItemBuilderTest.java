package org.freeplane.core.ui.menubuilders.menu;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Container;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.AccelerateableAction;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.JAutoCheckBoxMenuItem;
import org.freeplane.core.ui.menubuilders.HeadlessFreeplaneRunner;
import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.util.Compat;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class JMenuItemBuilderTest {
	static{
		new HeadlessFreeplaneRunner();
	}
	
	private Entry actionEntry;
	private AFreeplaneAction action;
	private Entry menuEntry;
	private JMenu menu;
	private JMenuItemBuilder menuActionGroupBuilder;
	private EntryPopupListener popupListener;
	private Entry groupEntry;
	private ResourceAccessor resourceAccessorMock;
	private IAcceleratorMap accelerators;
	private AcceleratebleActionProvider acceleratebleActionProvider;

	private JMenuItem getFirstSubMenuItem(Entry entry) {
		JMenu menu = (JMenu) new EntryAccessor().getComponent(entry);
		final JMenuItem menuItem = (JMenuItem) menu.getPopupMenu().getComponent(0);
		return menuItem;
	}

	@Before
	public void setup() {
		actionEntry = new Entry();
		action = Mockito.mock(AFreeplaneAction.class);
		actionEntry.setName("action");
		when(action.getKey()).thenReturn("action");
		when(action.getRawText()).thenReturn("action");
		new EntryAccessor().setAction(actionEntry, action);

		menuEntry = new Entry();
		menuEntry.setName("menu");
		groupEntry = new Entry();
		menu = new JMenu();
		popupListener = mock(EntryPopupListener.class);
		resourceAccessorMock = mock(ResourceAccessor.class);
		when(resourceAccessorMock.getRawText(anyString())).thenReturn("");
		when(resourceAccessorMock.getRawText("menu")).thenReturn("menu");
		accelerators = mock(IAcceleratorMap.class);
		acceleratebleActionProvider = new AcceleratebleActionProvider() {
			@Override
			protected boolean isApplet() {
				return false;
			}
		};
		menuActionGroupBuilder = new JMenuItemBuilder(popupListener, accelerators, acceleratebleActionProvider,
		    resourceAccessorMock);
	}
	
	@Test
	public void createsMenuButtonWithAction() {
		new EntryAccessor().setComponent(menuEntry, menu);
		menuEntry.addChild(actionEntry);
		menuActionGroupBuilder.visit(actionEntry);

		JMenuItem item = (JMenuItem)new EntryAccessor().getComponent(actionEntry);

		assertThatMenuItemHasCorrectAction(item);
		assertThat(item.getParent(), CoreMatchers.<Container>equalTo(menu.getPopupMenu()));
	}

	@Test
	public void createsMenuButtonWithAcceleratedAction() {
		final EntryAccessor entryAccessor = new EntryAccessor();
		entryAccessor.setComponent(menuEntry, menu);
		menuEntry.addChild(actionEntry);
		final KeyStroke keyStroke = KeyStroke.getKeyStroke('A');
		when(accelerators.getAccelerator(action)).thenReturn(keyStroke);
		menuActionGroupBuilder.visit(actionEntry);
		JMenuItem item = (JMenuItem) new EntryAccessor().getComponent(actionEntry);
		Assert.assertThat(item.getAccelerator(), equalTo(keyStroke));
	}

	@Test
	public void createsMenuItemWithSelectableAction() {
		menuEntry.addChild(actionEntry);
		when(action.isSelectable()).thenReturn(true);
		new EntryAccessor().setComponent(menuEntry, menu);
		menuActionGroupBuilder.visit(actionEntry);

		JAutoCheckBoxMenuItem item = (JAutoCheckBoxMenuItem)new EntryAccessor().getComponent(actionEntry);

		final AccelerateableAction itemAction = (AccelerateableAction) item.getAction();
		assertThat(itemAction.getOriginalAction(), CoreMatchers.<Action> equalTo(action));
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
		final FreeplaneMenuBar parentMenu = TestMenuBarFactory.createFreeplaneMenuBar();
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

		final JMenuItem menuItem = getFirstSubMenuItem(menuEntry);
		assertThatMenuItemHasCorrectAction(menuItem);
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
		final JMenuItem menuItem = getFirstSubMenuItem(groupEntry);
		assertThatMenuItemHasCorrectAction(menuItem);
	}

	private void assertThatMenuItemHasCorrectAction(final JMenuItem menuItem) {
	    final AccelerateableAction itemAction = (AccelerateableAction) menuItem.getAction();
		assertThat(itemAction.getOriginalAction(), CoreMatchers.<Action> equalTo(action));
    }
	
	@Test
	public void whenPopupMenuBecomesVisible_itsOwnPopupListenerIsCalled() {
		if(Compat.isMacOsX())
			return;
		menuEntry.addChild(actionEntry);
		
		menuActionGroupBuilder.visit(menuEntry);

		JMenu item = (JMenu)new EntryAccessor().getComponent(menuEntry);
		item.getPopupMenu().setVisible(true);
		verify(popupListener).childEntriesWillBecomeVisible(menuEntry);
	}

	
	@Test
	public void whenPopupMenuBecomesVisible_itsChildActionPopupListenerIsCalled() {
		if(Compat.isMacOsX())
			return;
		menuEntry.addChild(actionEntry);
		menuActionGroupBuilder.visit(menuEntry);
		JMenu item = (JMenu) new EntryAccessor().getComponent(menuEntry);
		item.getPopupMenu().setVisible(true);
		verify(popupListener).childEntriesWillBecomeVisible(actionEntry);
	}

	@Test
	public void whenNoPopupMenuBecomesVisible_PopupListenerIsNotCalled() {
		menuActionGroupBuilder.visit(menuEntry);
		verify(popupListener, never()).childEntriesWillBecomeVisible(Mockito.<Entry> anyObject());
	}

	@Test
	public void whenPopupMenuBecomesVisible_itsChildGroupPopupListenerIsCalled() {
		if(Compat.isMacOsX())
			return;
		menuEntry.addChild(groupEntry);
		menuActionGroupBuilder.visit(menuEntry);
		JMenu menu = (JMenu) new EntryAccessor().getComponent(menuEntry);
		menu.getPopupMenu().setVisible(true);
		verify(popupListener).childEntriesWillBecomeVisible(groupEntry);
	}

	@Test
	public void whenPopupMenuBecomesInvisible_popupListenerIsCalled() throws Exception {
		if(Compat.isMacOsX())
			return;
		Entry parentMenuEntry = new Entry();
		final JMenu parentMenu = new JMenu();
		new EntryAccessor().setComponent(parentMenuEntry, parentMenu);
		parentMenuEntry.addChild(menuEntry);
		menuEntry.addChild(actionEntry);
		
		menuActionGroupBuilder.visit(menuEntry);

		JMenu item = (JMenu)new EntryAccessor().getComponent(menuEntry);
		item.getPopupMenu().setVisible(true);
		item.getPopupMenu().setVisible(false);
		Thread.sleep(100);
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
			}
		});
		
		verify(popupListener).childEntriesHidden(menuEntry);
	}
}
