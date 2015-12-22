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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.JTextComponent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IAcceleratorChangeListener;
import org.freeplane.core.ui.KeyBindingProcessor;
import org.freeplane.core.ui.SetAcceleratorOnNextClickAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;

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
	private JFrame ownWindowAncestor;
	final private Timer timer = new Timer(500, new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			onModifierChangeImpl();
		}
	});
	private final KeyBindingProcessor keyProcessor;
	
	@SuppressWarnings("serial")
    private class ContentPane extends JPanel{
		@Override
        protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
			if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
				if(keyProcessor.processKeyBinding(ks, e, condition, pressed)) {
					return true;
				}
				return processFKey(e);
			}
			return false;
        }
	}

	public FButtonBar(JRootPane rootPane, KeyBindingProcessor proc) {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
		final Container oldContentPane = rootPane.getContentPane();
		final ContentPane newContentPane = new ContentPane();
		final LayoutManager layoutManager = oldContentPane.getLayout();
		oldContentPane.setLayout(null);
		newContentPane.setLayout(layoutManager);
		for (Component c : oldContentPane.getComponents()) {
			newContentPane.add(c);
		}
		rootPane.setContentPane(newContentPane);
		buttons = new HashMap<Integer, JButton[]>();
		onModifierChange();
		this.keyProcessor = proc;
	}

//	public void acceleratorChanged(final JMenuItem action, final KeyStroke oldStroke, final KeyStroke newStroke) {
//		final int oldButtonNumber = oldStroke != null ? oldStroke.getKeyCode() - KeyEvent.VK_F1 : -1;
//		final int newButtonNumber = newStroke != null ? newStroke.getKeyCode() - KeyEvent.VK_F1 : -1;
//		if (oldButtonNumber >= 0 && oldButtonNumber < BUTTON_NUMBER) {
//			final int modifiers = oldStroke.getModifiers()
//			        & (KeyEvent.CTRL_MASK | KeyEvent.META_MASK | KeyEvent.SHIFT_MASK | KeyEvent.ALT_MASK | KeyEvent.ALT_GRAPH_MASK);
//			final JButton[] buttonRow = buttons.get(modifiers);
//			final JButton button = buttonRow[oldButtonNumber];
//			setAcceleratorAction(button, oldStroke);
//		}
//		if (newButtonNumber >= 0 && newButtonNumber < BUTTON_NUMBER) {
//			final int modifiers = newStroke.getModifiers()
//			        & (KeyEvent.CTRL_MASK | KeyEvent.META_MASK | KeyEvent.SHIFT_MASK | KeyEvent.ALT_MASK | KeyEvent.ALT_GRAPH_MASK);
//			final JButton[] buttonRow = createButtons(modifiers);
//			final JButton button = buttonRow[newButtonNumber];
//			final String text = action.getActionCommand();
//			button.setText(text);
//			button.setToolTipText(text);
//			button.setAction(action.getAction());
//			button.setEnabled(action.isEnabled());
//		}
//	}
	 
	public void acceleratorChanged(final AFreeplaneAction action, final KeyStroke oldStroke, final KeyStroke newStroke) {
		final int oldButtonNumber = oldStroke != null ? oldStroke.getKeyCode() - KeyEvent.VK_F1 : -1;
		final int newButtonNumber = newStroke != null ? newStroke.getKeyCode() - KeyEvent.VK_F1 : -1;
		if (oldButtonNumber >= 0 && oldButtonNumber < BUTTON_NUMBER) {
			final int modifiers = oldStroke.getModifiers()
			        & (KeyEvent.CTRL_MASK | KeyEvent.META_MASK | KeyEvent.SHIFT_MASK | KeyEvent.ALT_MASK | KeyEvent.ALT_GRAPH_MASK);
			final JButton[] buttonRow = buttons.get(modifiers);
			final JButton button = buttonRow[oldButtonNumber];
			setAcceleratorAction(button, oldStroke);
		}
		if (newButtonNumber >= 0 && newButtonNumber < BUTTON_NUMBER) {
			final int modifiers = newStroke.getModifiers()
			        & (KeyEvent.CTRL_MASK | KeyEvent.META_MASK | KeyEvent.SHIFT_MASK | KeyEvent.ALT_MASK | KeyEvent.ALT_GRAPH_MASK);
			final JButton[] buttonRow = createButtons(modifiers);
			final JButton button = buttonRow[newButtonNumber];
			final String text = TextUtils.getText(action.getTextKey());
			button.setText(text);
			button.setToolTipText(text);
			button.setAction(action);
			button.setEnabled(action.isEnabled());
		}
	}


    private void setAcceleratorAction(final JButton button, final KeyStroke ks) {
        final SetAcceleratorOnNextClickAction setAcceleratorAction = new SetAcceleratorOnNextClickAction(ks);
        button.setAction(setAcceleratorAction);
        final String text = TextUtils.getText("f_button_unassigned");
        button.setText(text);
        button.setToolTipText(setAcceleratorAction.getValue(Action.NAME).toString());
    }

	private void cleanModifiers(final int modifiers) {
		if ((nextModifiers & modifiers) == 0) {
			return;
		}
		nextModifiers &= ~modifiers;
		onModifierChange();
	}

	private JButton[] createButtonRow(final int modifiers) {
		final JButton[] buttons = new JButton[BUTTON_NUMBER];
		for (int i = 0; i < BUTTON_NUMBER; i++) {
			final String name = "/images/f" + (i + 1) + ".png";
			final JButton button = buttons[i] = new JButton(new ImageIcon(
			    ResourceController.getResourceController().getResource(name))) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				protected void configurePropertiesFromAction(final Action a) {
				}
			};
			button.setFocusable(false);
			button.setBorder(BorderFactory.createEtchedBorder());
			if (System.getProperty("os.name").startsWith("Mac OS")) {
				button.setBorderPainted(false);
			}
            KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_F1 + i, modifiers);
            setAcceleratorAction(button, ks);
            
		}
		return buttons;
	}

	private JButton[] createButtons(final int modifiers) {
		JButton[] buttonRow = buttons.get(modifiers);
		if (buttonRow == null) {
			buttonRow = createButtonRow(modifiers);
			buttons.put(modifiers, buttonRow);
		}
		return buttonRow;
	}

	public boolean dispatchKeyEvent(final KeyEvent e) {
		if(! (Controller.getCurrentModeController() instanceof MModeController ))
			return false;
		if (ownWindowAncestor == null) {
			ownWindowAncestor = (JFrame) SwingUtilities.getWindowAncestor(this);
			if (ownWindowAncestor == null) {
				return false;
			}
			ownWindowAncestor.addWindowFocusListener(this);
		}
		final Window windowAncestor = SwingUtilities.getWindowAncestor(e.getComponent());
		
		if (windowAncestor == ownWindowAncestor && ownWindowAncestor.getJMenuBar() != null && ownWindowAncestor.getJMenuBar().isEnabled()) {
			processDispatchedKeyEvent(e);
		}
		else {
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
				}
				break;
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
					    break;
				}
				break;
            default:
                break;
		}
	}
	
	private boolean processFKey(final KeyEvent e){
		if(e.getID() != KeyEvent.KEY_PRESSED)
			return false;
		final Window windowAncestor = SwingUtilities.getWindowAncestor(e.getComponent());
		if (windowAncestor != ownWindowAncestor /*|| !ownWindowAncestor.getJMenuBar().isEnabled()*/) {
			resetModifiers();
			return false;
		}
		int keyCode = e.getKeyCode();
		if (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12 ) {
			final JButton btn = createButtons(nextModifiers)[keyCode - KeyEvent.VK_F1];
			if(btn.getAction() instanceof SetAcceleratorOnNextClickAction 
					&& e.getComponent() instanceof JTextComponent)
				return false;
			if(timer.isRunning()){
				timer.stop();
				onModifierChangeImpl();
			}
			btn.doClick();
			return true;
		}
		return false;
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
		final int w = getParent().getWidth();
		final int border = 5;
		final int h = getComponent(1).getPreferredSize().height;
		final int componentCount = getComponentCount();
		final float availableWidth = w - 2 * border + 0f;
		final float dw = availableWidth / componentCount;
		int preferredWidth = 0;
		int narrowComponentPreferredWidth = 0;
		for (int i = 0; i < componentCount; i++) {
			final int cw = getComponent(i).getPreferredSize().width;
			preferredWidth += cw;
			if (cw <= dw) {
				narrowComponentPreferredWidth += cw;
			}
		}
		final float k;
		if (availableWidth < preferredWidth) {
			k = (availableWidth - narrowComponentPreferredWidth) / (preferredWidth - narrowComponentPreferredWidth);
		}
		else {
			k = availableWidth / preferredWidth;
		}
		float x = border;
		for (int i = 0; i < componentCount; i++) {
			float cw = getComponent(i).getPreferredSize().width;
			if (k > 1f || cw > dw) {
				cw *= k;
			}
			getComponent(i).setBounds((int) x, 0, (int) cw, h);
			x += cw;
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(getParent().getWidth(), getComponent(1).getPreferredSize().height);
	}
}
