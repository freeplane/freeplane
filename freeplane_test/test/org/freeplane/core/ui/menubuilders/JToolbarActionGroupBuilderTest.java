package org.freeplane.core.ui.menubuilders;

import static org.junit.Assert.assertThat;

import java.awt.Container;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.SwingConstants;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.Mockito;

public class JToolbarActionGroupBuilderTest {
	private FreeplaneToolBar freeplaneToolbar() {
		return new FreeplaneToolBar("toolbar", SwingConstants.HORIZONTAL);
	}
	

	@Test
	public void createsToolbarButtonWithAction() {
		Entry actionEntry = new Entry();
		final AFreeplaneAction action = Mockito.mock(AFreeplaneAction.class);
		actionEntry.setAction(action);

		Entry toolbarEntry = new Entry();
		toolbarEntry.setComponent(freeplaneToolbar());
		toolbarEntry.addChild(actionEntry);
		
		final JToolbarActionGroupBuilder toolbarActionGroupBuilder = new JToolbarActionGroupBuilder();
		toolbarActionGroupBuilder.build(actionEntry);

		assertThat(((JButton)actionEntry.getComponent()).getAction(), CoreMatchers.<Action>equalTo(action));
	}

	@Test
	public void givengroupWithAction_addsActionButtonToToolbar() {
		Entry actionEntry = new Entry();
		final AFreeplaneAction action = Mockito.mock(AFreeplaneAction.class);
		actionEntry.setAction(action);

		Entry toolbarEntry = new Entry();
		final FreeplaneToolBar toolbar = freeplaneToolbar();
		toolbarEntry.setComponent(toolbar);
		toolbarEntry.addChild(actionEntry);
		
		final JToolbarActionGroupBuilder toolbarActionGroupBuilder = new JToolbarActionGroupBuilder();
		toolbarActionGroupBuilder.build(actionEntry);

		assertThat(((JButton)actionEntry.getComponent()).getParent(), CoreMatchers.equalTo((Container)toolbar));
	}


}
