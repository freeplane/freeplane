package org.freeplane.features.treetable.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

@EnabledAction(checkOnNodeChange = true)
class TreeTableColumnAction extends AFreeplaneAction {

	private static final long serialVersionUID = 1L;

	private final Predicate<NodeModel> isApplicable;
	private final Consumer<NodeModel> operation;

	TreeTableColumnAction(final String key, final Predicate<NodeModel> isApplicable,
			final Consumer<NodeModel> operation) {
		super(key);
		this.isApplicable = isApplicable;
		this.operation = operation;
	}

	@Override
	protected void setEnabled() {
		final NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		super.setEnabled(selected != null && isApplicable.test(selected));
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		if (selected != null && isApplicable.test(selected))
			operation.accept(selected);
	}
}
