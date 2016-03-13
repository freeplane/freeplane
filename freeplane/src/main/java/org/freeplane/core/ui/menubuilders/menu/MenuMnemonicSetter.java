package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.freeplane.core.ui.ButtonNameMnemonicHolder;
import org.freeplane.core.ui.INameMnemonicHolder;
import org.freeplane.core.ui.MnemonicSetter;

public class MenuMnemonicSetter implements PopupMenuListener{
	
	final static public MenuMnemonicSetter INSTANCE = new MenuMnemonicSetter();
	

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		final JPopupMenu popupMenu = (JPopupMenu) e.getSource();
		final Component[] components = popupMenu.getComponents();
		final ArrayList<INameMnemonicHolder> mnemonicHolders = new ArrayList<INameMnemonicHolder>(components.length);
		for(Component component :components)
			if(component instanceof AbstractButton) {
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
