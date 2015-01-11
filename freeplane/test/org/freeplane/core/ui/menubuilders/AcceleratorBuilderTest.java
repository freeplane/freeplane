package org.freeplane.core.ui.menubuilders;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.awt.Container;
import java.awt.event.InputEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.KeyStroke;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.hamcrest.CoreMatchers;
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
		actionEntry.setAction(action);

		IDefaultAcceleratorMap map = mock(IDefaultAcceleratorMap.class);
		final AcceleratorBuilder acceleratorBuilder = new AcceleratorBuilder(map);
		acceleratorBuilder.build(actionEntry);
		
		Mockito.verify(map).setDefaultAccelerator(actionKey, keyStroke);

	}
	
	@Test
	public void ignoresEntryWithoutAction() {
		Entry actionEntry = new Entry();
		String actionKey = "actionKey";
		actionEntry.setName(actionKey);
		String keyStroke = KeyStroke.getKeyStroke('A', InputEvent.CTRL_MASK).toString();
		actionEntry.setAttribute("accelerator", keyStroke);

		IDefaultAcceleratorMap map = mock(IDefaultAcceleratorMap.class);
		final AcceleratorBuilder acceleratorBuilder = new AcceleratorBuilder(map);
		acceleratorBuilder.build(actionEntry);
		
		Mockito.verify(map, never()).setDefaultAccelerator(actionKey, keyStroke);

	}
	
	@Test
	public void ignoresEntryWithoutAccelerator() {
		Entry actionEntry = new Entry();
		final AFreeplaneAction action = mock(AFreeplaneAction.class);
		actionEntry.setAction(action);

		IDefaultAcceleratorMap map = mock(IDefaultAcceleratorMap.class);
		final AcceleratorBuilder acceleratorBuilder = new AcceleratorBuilder(map);
		acceleratorBuilder.build(actionEntry);
		
		Mockito.verify(map, never()).setDefaultAccelerator(anyString(), anyString());

	}
}
