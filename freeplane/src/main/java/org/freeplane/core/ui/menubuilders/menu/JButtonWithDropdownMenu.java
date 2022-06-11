package org.freeplane.core.ui.menubuilders.menu;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

class JButtonWithDropdownMenu extends JButton {
	private static final long serialVersionUID = 1L;
	private JPopupMenu menu;

	public JButtonWithDropdownMenu(String text) {
		super(text);
		menu = new JPopupMenu();
		setHorizontalAlignment(SwingConstants.CENTER);
		addActionListener(e -> menu.show(this, 0, this.getHeight()));
	}

	public JMenuItem addMenuAction(Action a) {
		return menu.add(a);
	}

}
