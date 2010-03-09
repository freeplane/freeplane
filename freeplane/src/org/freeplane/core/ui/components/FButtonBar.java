/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.core.ui.components;

import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IAcceleratorChangeListener;

/**
 * @author Dimitry Polivaev
 * 03.07.2009
 */
public class FButtonBar extends JComponent implements IAcceleratorChangeListener, KeyEventDispatcher,
        WindowFocusListener {
	private static final int BUTTON_NUMBER = 12;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private Map<Integer, JButton[]> buttons;
	private int lastModifiers = -1;
	private int nextModifiers = 0;
	private Window ownWindowAncestor;
	final private Timer timer = new Timer(500, new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			onModifierChangeImpl();
		}
	});
	private final ModeController modeController;

	public FButtonBar(ModeController modeController) {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
		buttons = new HashMap<Integer, JButton[]>();
		onModifierChange();
		this.modeController = modeController;
	}

	public void acceleratorChanged(final JMenuItem action, final KeyStroke oldStroke, final KeyStroke newStroke) {
		final int oldButtonNumber = oldStroke != null ? oldStroke.getKeyCode() - KeyEvent.VK_F1 : -1;
		final int newButtonNumber = newStroke != null ? newStroke.getKeyCode() - KeyEvent.VK_F1 : -1;
		if (oldButtonNumber >= 0 && oldButtonNumber < BUTTON_NUMBER) {
			final int modifiers = oldStroke.getModifiers()
			        & (KeyEvent.CTRL_MASK | KeyEvent.META_MASK | KeyEvent.SHIFT_MASK | KeyEvent.ALT_MASK | KeyEvent.ALT_GRAPH_MASK);
			final JButton[] buttonRow = buttons.get(modifiers);
			final JButton button = buttonRow[oldButtonNumber];
			button.setAction(null);
			final String text = ResourceBundles.getText("f_button_unassigned");
			button.setText(text);
			button.setToolTipText(text);
		}
		if (newButtonNumber >= 0 && newButtonNumber < BUTTON_NUMBER) {
			final int modifiers = newStroke.getModifiers()
			        & (KeyEvent.CTRL_MASK | KeyEvent.META_MASK | KeyEvent.SHIFT_MASK | KeyEvent.ALT_MASK | KeyEvent.ALT_GRAPH_MASK);
			final JButton[] buttonRow = createButtons(modifiers);
			final JButton button = buttonRow[newButtonNumber];
			final String text = action.getActionCommand();
			button.setText(text);
			button.setToolTipText(text);
			button.setAction(action.getAction());
			button.setEnabled(action.isEnabled());
		}
	}

	private void cleanModifiers(final int modifiers) {
		if ((nextModifiers & modifiers) == 0) {
			return;
		}
		nextModifiers &= ~modifiers;
		onModifierChange();
	}

	private JButton[] createButtons() {
		final JButton[] buttons = new JButton[BUTTON_NUMBER];
		for (int i = 0; i < BUTTON_NUMBER; i++) {
			final String name = "/images/f" + (i + 1) + ".png";
			final JButton button = buttons[i] = new JButton(ResourceBundles.getText("f_button_unassigned"),
			    new ImageIcon(ResourceController.getResourceController().getResource(name))) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				protected void configurePropertiesFromAction(final Action a) {
				}
			};
			button.setFocusable(false);
			button.setEnabled(false);
			button.setBorder(BorderFactory.createEtchedBorder());
			if (System.getProperty("os.name").startsWith("Mac OS")) {
				button.setBorderPainted(false);
			}
			button.setContentAreaFilled(false);
		}
		return buttons;
	}

	private JButton[] createButtons(final int modifiers) {
		JButton[] buttonRow = buttons.get(modifiers);
		if (buttonRow == null) {
			buttonRow = createButtons();
			buttons.put(modifiers, buttonRow);
		}
		return buttonRow;
	}

	public boolean dispatchKeyEvent(final KeyEvent e) {
		if (ownWindowAncestor == null) {
			ownWindowAncestor = SwingUtilities.getWindowAncestor(this);
			if (ownWindowAncestor != null) {
				ownWindowAncestor.addWindowFocusListener(this);
			}
		}
		final Window windowAncestor = SwingUtilities.getWindowAncestor(e.getComponent());
		if (windowAncestor == ownWindowAncestor) {
			processDispatchedKeyEvent(e);
		}
		else{
			resetModifiers();
		}
		return false;
	}

	private void onModifierChange() {
		if (lastModifiers == nextModifiers) {
			return;
		}
		if (timer.isRunning()) {
			timer.stop();
		}
		if (nextModifiers == 0) {
			onModifierChangeImpl();
		}
		else {
			timer.start();
		}
	}

	private void onModifierChangeImpl() {
		if (lastModifiers == nextModifiers) {
			return;
		}
		lastModifiers = nextModifiers;
		removeAll();
		final JButton[] buttonRow = createButtons(nextModifiers);
		for (final JButton button : buttonRow) {
			add(button);
		}
		revalidate();
		repaint();
	}

	private void processDispatchedKeyEvent(final KeyEvent e) {
		final int keyCode = e.getKeyCode();
		switch (e.getID()) {
			case KeyEvent.KEY_PRESSED:
				switch (keyCode) {
					case KeyEvent.VK_CONTROL:
						setModifiers(KeyEvent.CTRL_MASK);
						break;
					case KeyEvent.VK_META:
						setModifiers(KeyEvent.META_MASK);
						break;
					case KeyEvent.VK_SHIFT:
						setModifiers(KeyEvent.SHIFT_MASK);
						break;
					case KeyEvent.VK_ALT:
						setModifiers(KeyEvent.ALT_MASK);
						break;
					case KeyEvent.VK_ALT_GRAPH:
						setModifiers(KeyEvent.ALT_GRAPH_MASK);
						break;
					default:
						if (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12 && timer.isRunning()) {
							timer.stop();
							onModifierChangeImpl();
						}
						return;
				}
			default:
				return;
			case KeyEvent.KEY_RELEASED:
				switch (keyCode) {
					case KeyEvent.VK_CONTROL:
						cleanModifiers(KeyEvent.CTRL_MASK);
						break;
					case KeyEvent.VK_META:
						cleanModifiers(KeyEvent.META_MASK);
						break;
					case KeyEvent.VK_SHIFT:
						cleanModifiers(KeyEvent.SHIFT_MASK);
						break;
					case KeyEvent.VK_ALT:
						cleanModifiers(KeyEvent.ALT_MASK);
					case KeyEvent.VK_ALT_GRAPH:
						cleanModifiers(KeyEvent.ALT_GRAPH_MASK);
						break;
					default:
						return;
				}
				break;
		}
	}

	private void resetModifiers() {
		if (nextModifiers == 0) {
			return;
		}
		nextModifiers = 0;
		onModifierChange();
	}

	private void setModifiers(final int modifiers) {
		if ((nextModifiers ^ modifiers) == 0) {
			return;
		}
		nextModifiers |= modifiers;
		onModifierChange();
	}

	public void windowGainedFocus(final WindowEvent e) {
	}

	public void windowLostFocus(final WindowEvent e) {
		resetModifiers();
	}

	@Override
    public void layout() {
		int w = getParent().getWidth();
		int border = 5;
		int h = getComponent(1).getPreferredSize().height;
		final int componentCount = getComponentCount();
		float availableWidth = w - 2 * border + 0f;
		final float dw = availableWidth/componentCount;
		int preferredWidth = 0;
		int narrowComponentPreferredWidth = 0;
		int narrowComponentCount = 0;
		for(int i = 0; i < componentCount; i++){
			final int cw = getComponent(i).getPreferredSize().width;
			preferredWidth += cw;
			if(cw <= dw){
				narrowComponentPreferredWidth += cw;
				narrowComponentCount++;
			}
		}
		final float k;
		if(availableWidth < preferredWidth){
			k = (availableWidth - narrowComponentPreferredWidth) / (preferredWidth - narrowComponentPreferredWidth);
		}
		else{
			k = availableWidth  / preferredWidth ;
		}
		
		float x = border;
		for(int i = 0; i < componentCount; i++){
			float cw = getComponent(i).getPreferredSize().width;
			if(k>1f || cw > dw){
				cw *= k;
			}
			getComponent(i).setBounds((int)x, 0, (int)cw, h);
			x+= cw;
		}
    }

	@Override
    public Dimension getPreferredSize() {
	    return new Dimension(getParent().getWidth(), getComponent(1).getPreferredSize().height);
    }
}
