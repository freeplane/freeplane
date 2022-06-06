package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;

public class UnitGridBagLayout extends GridBagLayout {
	private static final long serialVersionUID = 1L;
	
	private static Component findUnitComponent(Container parent) {
		LayoutManager layout = parent.getLayout();
		if(! (layout instanceof UnitGridBagLayout))
			return null;
		UnitGridBagLayout unitLayout = (UnitGridBagLayout) layout;
		for (int i = 0; unitLayout.unit == null 
				&& i < parent.getComponentCount(); i++) {
			Component comp = parent.getComponent(i);
			GridBagConstraints constraints = unitLayout.getConstraints(comp);
			if (comp.getClass().getSimpleName().endsWith("Button")
					&& ((GridBagConstraints)constraints).gridwidth == 1
					&& ((GridBagConstraints)constraints).gridheight == 1) {
				unitLayout.unit = comp;
			}
			else if(comp instanceof Container) {
				unitLayout.unit = findUnitComponent((Container) comp);
			}
		}
		return unitLayout.unit;
	}
	
	private Component unit;
	
	@Override
	public Dimension preferredLayoutSize(Container parent) {
		setPreferredSizes(parent);
		return super.preferredLayoutSize(parent);
	}
	private void setPreferredSizes(Container parent) {
		if(unit == null) {
			unit = findUnitComponent(parent);
		}
		if(unit == null)
			return;
		Dimension unitSize = unit.getPreferredSize();
		for (int i = 0; i < parent.getComponentCount(); i++) {
			Component c = parent.getComponent(i);
			if (c != unit && c.isVisible() && c instanceof JUnitPanel)
				c.setPreferredSize(unitSize);
		}
	}
	@Override
	public void layoutContainer(Container parent) {
		setPreferredSizes(parent);
		super.layoutContainer(parent);
	}
	
}