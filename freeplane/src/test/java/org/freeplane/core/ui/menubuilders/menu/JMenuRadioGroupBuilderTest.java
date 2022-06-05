package org.freeplane.core.ui.menubuilders.menu;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.util.Compat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class JMenuRadioGroupBuilderTest {
	private Entry actionEntry;
	private AFreeplaneAction action;
	private Entry menuEntry;
	private ResourceAccessor resourceAccessorMock;
	private IAcceleratorMap accelerators;
	private AcceleratebleActionProvider acceleratebleActionProvider;
	private JMenuRadioGroupBuilder radioGroupBuilder;

	@Before
	public void setup() {
		actionEntry = new Entry();
		action = Mockito.mock(AFreeplaneAction.class);
		actionEntry.setName("action");
		new EntryAccessor().setAction(actionEntry, action);
		menuEntry = new Entry();
		menuEntry.setName("menu");
		EntryPopupListener popupListener = mock(EntryPopupListener.class);
		resourceAccessorMock = mock(ResourceAccessor.class);
		when(resourceAccessorMock.getRawText(anyString())).thenReturn("");
		when(resourceAccessorMock.getRawText("menu")).thenReturn("menu");
		accelerators = mock(IAcceleratorMap.class);
		Compat.setIsApplet(false);
		acceleratebleActionProvider = new AcceleratebleActionProvider();
		radioGroupBuilder = new JMenuRadioGroupBuilder(popupListener, accelerators, acceleratebleActionProvider,
		    resourceAccessorMock);
	}
	@Test
	public void buildsWholeSubtree() throws Exception {
		assertThat(radioGroupBuilder.shouldSkipChildren(null), equalTo(true));
	}
}
