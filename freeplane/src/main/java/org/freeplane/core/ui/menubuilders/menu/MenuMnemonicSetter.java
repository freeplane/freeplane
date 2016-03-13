package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.JMenuItem;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.freeplane.core.ui.ButtonNameMnemonicHolder;
import org.freeplane.core.ui.INameMnemonicHolder;
import org.freeplane.core.ui.MenuItemMnemonicHolder;
import org.freeplane.core.ui.MnemonicSetter;
import org.freeplane.core.util.Compat;

public class MenuMnemonicSetter implements PopupMenuListener{
	
	final static public MenuMnemonicSetter INSTANCE = new MenuMnemonicSetter();
	

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		final Container popupMenu = (Container) e.getSource();
		setComponentMnemonics(popupMenu);
	}

	public void setComponentMnemonics(final Container popupMenu) {
		if(Compat.isMacOsX())
			return; // Mac OS generally does not support mnemonics
		final Component[] components = popupMenu.getComponents();
		final ArrayList<INameMnemonicHolder> mnemonicHolders = new ArrayList<INameMnemonicHolder>(components.length);
		for(Component component :components)
			if(component instanceof JMenuItem) {
				final JMenuItem item = (JMenuItem) component;
				mnemonicHolders.add(new MenuItemMnemonicHolder(item));
			}
			else if(component instanceof AbstractButton) {
				final AbstractButton button = (AbstractButton) component;
				mnemonicHolders.add(new ButtonNameMnemonicHolder(button));
			}
		final MnemonicSetter mnemonicSetter = MnemonicSetter.of(mnemonicHolders);
		mnemonicSetter.setMnemonics();
	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
	}

}
