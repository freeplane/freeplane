package org.freeplane.core.ui.menubuilders.menu;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

public class JButtonWithDropdownMenu extends JButton {
	private static final long serialVersionUID = 1L;
	private JPopupMenu menu;

	public JButtonWithDropdownMenu(String text, Icon icon) {
		super(text, icon);
		menu = new JPopupMenu();
		setHorizontalAlignment(SwingConstants.CENTER);
		addActionListener(e -> menu.show(this, 0, this.getHeight()));
	}

	public JMenuItem addMenuAction(Action a) {
		return menu.add(a);
	}

}
