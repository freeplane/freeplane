package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;

import javax.swing.AbstractButton;

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
			String className = comp.getClass().getSimpleName();
			if ((className.equals("JButton") || className.equals("JToggleButton"))
					&& constraints.gridwidth == 1
					&& constraints.gridheight == 1) {
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
			if (c != unit && c.isVisible()) {
				if (c.getClass().equals(JUnitPanel.class))
					c.setPreferredSize(unitSize);
				else if (c.getClass().equals(JBigButton.class))
					c.setPreferredSize(new Dimension(unitSize.width * 7/4, unitSize.height));
			}
		}
	}

	@Override
	public void layoutContainer(Container parent) {
		setPreferredSizes(parent);
		super.layoutContainer(parent);
		changeSpecialButtonSizes(parent);
	}

	private void changeSpecialButtonSizes(Container parent) {
		for(int i = parent.getComponentCount() - 1; i> 0; i--) {
			Component component = parent.getComponent(i);
			if(! (component  instanceof JButtonWithDropdownMenu)) {
				continue;
			}
			int x = component.getX();
			int width = component.getWidth();
			for (Component previousComponent = parent.getComponent(i-1);
					previousComponent.getX() >= x && previousComponent.getX() + previousComponent.getWidth() <= x + width;
					previousComponent = parent.getComponent(i-1)) {
				previousComponent.setSize(previousComponent.getWidth(), component.getY() - previousComponent.getY());
				if(previousComponent instanceof AbstractButton) {
					if(! previousComponent.isPreferredSizeSet()) {
						previousComponent.setPreferredSize(previousComponent.getPreferredSize());
						IconReplacer.replaceByScaledImageIcon((AbstractButton) previousComponent);
					}
				}
				if(i > 1)
					i--;
				else
					break;
			}
		}
	}
}
