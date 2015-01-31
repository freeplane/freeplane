package org.freeplane.core.ui.menubuilders;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.components.JAutoToggleButton;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.Mockito;

public class JToolbarActionBuilderTest {
	@Test
	public void createsToolbarButtonWithAction() {
		Entry actionEntry = new Entry();
		final AFreeplaneAction action = Mockito.mock(AFreeplaneAction.class);
		actionEntry.setAction(action);

		Entry toolbarEntry = new Entry();
		final FreeplaneToolBar toolbar = new FreeplaneToolBar("toolbar", SwingConstants.HORIZONTAL);
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
		final FreeplaneToolBar toolbar = new FreeplaneToolBar("toolbar", SwingConstants.HORIZONTAL);
		toolbarEntry.setComponent(toolbar);
		toolbarEntry.addChild(actionEntry);
		
		final JToolbarActionBuilder toolbarActionGroupBuilder = new JToolbarActionBuilder();
		toolbarActionGroupBuilder.build(actionEntry);

		JAutoToggleButton button = (JAutoToggleButton)actionEntry.getComponent();

		assertThat(button.getAction(), CoreMatchers.<Action>equalTo(action));
		assertThat(button.getParent(), CoreMatchers.equalTo((Container)toolbar));
	}
	
	@Test
	public void createsVerticalToolbarSeparator() {
		Entry separatorEntry = new Entry();
		separatorEntry.setBuilders(asList("separator"));

		Entry toolbarEntry = new Entry();
		final FreeplaneToolBar toolbar = new FreeplaneToolBar("toolbar", SwingConstants.HORIZONTAL);
		toolbarEntry.setComponent(toolbar);
		toolbarEntry.addChild(separatorEntry);
		
		final JToolbarActionBuilder toolbarActionGroupBuilder = new JToolbarActionBuilder();
		toolbarActionGroupBuilder.build(separatorEntry);

		JToolBar.Separator separator = (JToolBar.Separator)separatorEntry.getComponent();

		assertThat(separator.getParent(), CoreMatchers.equalTo((Container)toolbar));
		assertThat(separator.getOrientation(), CoreMatchers.equalTo(SwingConstants.VERTICAL));
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
