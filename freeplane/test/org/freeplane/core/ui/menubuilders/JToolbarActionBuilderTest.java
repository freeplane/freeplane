package org.freeplane.core.ui.menubuilders;

import static org.junit.Assert.assertThat;

import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.SwingConstants;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.components.JAutoToggleButton;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.Mockito;

public class JToolbarActionBuilderTest {
	private FreeplaneToolBar freeplaneToolbar() {
		return new FreeplaneToolBar("toolbar", SwingConstants.HORIZONTAL);
	}
	

	@Test
	public void createsToolbarButtonWithAction() {
		Entry actionEntry = new Entry();
		final AFreeplaneAction action = Mockito.mock(AFreeplaneAction.class);
		actionEntry.setAction(action);

		Entry toolbarEntry = new Entry();
		final FreeplaneToolBar toolbar = freeplaneToolbar();
		toolbarEntry.setComponent(toolbar);
		toolbarEntry.addChild(actionEntry);
		
		final JToolbarActionBuilder toolbarActionGroupBuilder = new JToolbarActionBuilder();
		toolbarActionGroupBuilder.build(actionEntry);

		JButton button = (JButton)actionEntry.getComponent();

		assertThat(button.getAction(), CoreMatchers.<Action>equalTo(action));
		assertThat(button.getParent(), CoreMatchers.equalTo((Container)toolbar));
	}
	
	@Test
	public void createsToolbarButtonWithSelectableAction() {
		Entry actionEntry = new Entry();
		final AFreeplaneAction action = new SelectableFreeplaneActionStub();
		actionEntry.setAction(action);

		Entry toolbarEntry = new Entry();
		final FreeplaneToolBar toolbar = freeplaneToolbar();
		toolbarEntry.setComponent(toolbar);
		toolbarEntry.addChild(actionEntry);
		
		final JToolbarActionBuilder toolbarActionGroupBuilder = new JToolbarActionBuilder();
		toolbarActionGroupBuilder.build(actionEntry);

		JAutoToggleButton button = (JAutoToggleButton)actionEntry.getComponent();

		assertThat(button.getAction(), CoreMatchers.<Action>equalTo(action));
		assertThat(button.getParent(), CoreMatchers.equalTo((Container)toolbar));
	}
	
}

@SuppressWarnings("serial")
@SelectableAction
class SelectableFreeplaneActionStub extends AFreeplaneAction {
	SelectableFreeplaneActionStub() {
		super(null, null, null);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	}
}
