package org.freeplane.core.ui.menubuilders.action;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import java.awt.event.InputEvent;

import javax.swing.KeyStroke;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.junit.Test;
import org.mockito.Mockito;

public class AcceleratorBuilderTest {

	@Test
	public void setsDefaultAcceleratorForAction() {
		Entry actionEntry = new Entry();
		String actionKey = "actionKey";
		actionEntry.setName(actionKey);
		String keyStroke = KeyStroke.getKeyStroke('A', InputEvent.CTRL_MASK).toString();
		actionEntry.setAttribute("accelerator", keyStroke);
		final AFreeplaneAction action = mock(AFreeplaneAction.class);
		new EntryAccessor().setAction(actionEntry, action);

		IAcceleratorMap map = mock(IAcceleratorMap.class);
		final AcceleratorBuilder acceleratorBuilder = new AcceleratorBuilder(map,  mock(EntriesForAction.class));
		acceleratorBuilder.visit(actionEntry);
		
		Mockito.verify(map).setDefaultAccelerator(action, keyStroke);

	}
	
	@Test
	public void ignoresEntryWithoutAction() {
		Entry actionEntry = new Entry();
		String actionKey = "actionKey";
		actionEntry.setName(actionKey);
		String keyStroke = "CONTROL A";
		actionEntry.setAttribute("accelerator", keyStroke);

		IAcceleratorMap map = mock(IAcceleratorMap.class);
		final AcceleratorBuilder acceleratorBuilder = new AcceleratorBuilder(map,  mock(EntriesForAction.class));
		acceleratorBuilder.visit(actionEntry);
		
		Mockito.verify(map, never()).setDefaultAccelerator(Mockito.<AFreeplaneAction> any(), Mockito.<String> any());

	}
	
	@Test
	public void givenEntryWithoutAccelerator_doesNotSetOwnDefaultAccelerator() {
		Entry actionEntry = new Entry();
		final AFreeplaneAction action = mock(AFreeplaneAction.class);
		new EntryAccessor().setAction(actionEntry, action);

		IAcceleratorMap map = mock(IAcceleratorMap.class);
		final AcceleratorBuilder acceleratorBuilder = new AcceleratorBuilder(map,  mock(EntriesForAction.class));
		acceleratorBuilder.visit(actionEntry);
		
		Mockito.verify(map, never()).setDefaultAccelerator(Mockito.<AFreeplaneAction> any(), anyString());

	}

	@Test
	public void givenEntryWithoutAccelerator_setsUserDefinedAccelerator() {
		Entry actionEntry = new Entry();
		final AFreeplaneAction action = mock(AFreeplaneAction.class);
		new EntryAccessor().setAction(actionEntry, action);

		IAcceleratorMap map = mock(IAcceleratorMap.class);
		final AcceleratorBuilder acceleratorBuilder = new AcceleratorBuilder(map, mock(EntriesForAction.class));
		acceleratorBuilder.visit(actionEntry);

		Mockito.verify(map).setUserDefinedAccelerator(action);

	}


	@Test
	public void registersEntryWithAction() {
		Entry actionEntry = new Entry();
		final AFreeplaneAction action = mock(AFreeplaneAction.class);
		new EntryAccessor().setAction(actionEntry, action);

		IAcceleratorMap map = mock(IAcceleratorMap.class);
		EntriesForAction entries = mock(EntriesForAction.class);
		final AcceleratorBuilder acceleratorBuilder = new AcceleratorBuilder(map, entries);
		acceleratorBuilder.visit(actionEntry);
		
		Mockito.verify(entries).registerEntry(action, actionEntry);

	}
}
