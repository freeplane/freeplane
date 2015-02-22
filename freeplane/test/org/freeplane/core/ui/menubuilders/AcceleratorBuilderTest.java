package org.freeplane.core.ui.menubuilders;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import java.awt.event.InputEvent;

import javax.swing.KeyStroke;

import org.freeplane.core.ui.AFreeplaneAction;
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
		acceleratorBuilder.visit(actionEntry);
		
		Mockito.verify(map).setDefaultAccelerator(actionKey, keyStroke);

	}
	
	@Test
	public void ignoresEntryWithoutAction() {
		Entry actionEntry = new Entry();
		String actionKey = "actionKey";
		actionEntry.setName(actionKey);
		String keyStroke = "CONTROL A";
		actionEntry.setAttribute("accelerator", keyStroke);

		IDefaultAcceleratorMap map = mock(IDefaultAcceleratorMap.class);
		final AcceleratorBuilder acceleratorBuilder = new AcceleratorBuilder(map);
		acceleratorBuilder.visit(actionEntry);
		
		Mockito.verify(map, never()).setDefaultAccelerator(actionKey, keyStroke);

	}
	
	@Test
	public void ignoresEntryWithoutAccelerator() {
		Entry actionEntry = new Entry();
		final AFreeplaneAction action = mock(AFreeplaneAction.class);
		actionEntry.setAction(action);

		IDefaultAcceleratorMap map = mock(IDefaultAcceleratorMap.class);
		final AcceleratorBuilder acceleratorBuilder = new AcceleratorBuilder(map);
		acceleratorBuilder.visit(actionEntry);
		
		Mockito.verify(map, never()).setDefaultAccelerator(anyString(), anyString());

	}


	@Test
	public void replacesControlByMetaForMacOS() {
		Entry actionEntry = new Entry();
		String actionKey = "actionKey";
		actionEntry.setName(actionKey);
		String keyStroke = "CONTROL A";
		actionEntry.setAttribute("accelerator", keyStroke);
		final AFreeplaneAction action = mock(AFreeplaneAction.class);
		actionEntry.setAction(action);

		IDefaultAcceleratorMap map = mock(IDefaultAcceleratorMap.class);
		final AcceleratorBuilder acceleratorBuilder = new AcceleratorBuilder(map){

			@Override
			protected boolean isMacOsX() {
				return true;
			}
			
		};
		acceleratorBuilder.visit(actionEntry);
		
		Mockito.verify(map).setDefaultAccelerator(actionKey, keyStroke.replaceAll("CONTROL", "META"));

	}
}
