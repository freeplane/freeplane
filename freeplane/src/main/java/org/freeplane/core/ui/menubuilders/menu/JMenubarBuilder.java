package org.freeplane.core.ui.menubuilders.menu;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import org.dpolivaev.mnemonicsetter.MnemonicSetter;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.mode.Controller;

public class JMenubarBuilder implements EntryVisitor {
	private final IUserInputListenerFactory userInputListenerFactory;

	public JMenubarBuilder(IUserInputListenerFactory userInputListenerFactory) {
		super();
		this.userInputListenerFactory = userInputListenerFactory;
	}

	@Override
	public void visit(Entry target) {
		final FreeplaneMenuBar menuBar = userInputListenerFactory.getMenuBar();
		addMnemonicsBeforeShowing(menuBar);
		new EntryAccessor().setComponent(target, menuBar);
	}

	private void addMnemonicsBeforeShowing(final FreeplaneMenuBar menuBar) {
		menuBar.addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				menuBar.removeHierarchyListener(this);
				final FilterController filterController = Controller.getCurrentController().getExtension(FilterController.class);
				if(filterController != null)
					MnemonicSetter.INSTANCE.setComponentMnemonics(menuBar,
						filterController.getQuickEditor());
				else
					MnemonicSetter.INSTANCE.setComponentMnemonics(menuBar);
			}
		});
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		// TODO Auto-generated method stub
		return false;
	}
}
