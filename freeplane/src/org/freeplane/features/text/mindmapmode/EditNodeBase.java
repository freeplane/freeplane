/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.features.text.mindmapmode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.mindmapmode.ortho.SpellCheckerController;

/**
 * @author foltin
 */
abstract public class EditNodeBase {
	abstract static class EditDialog extends JDialog {
		class CancelAction extends AbstractAction {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent e) {
				confirmedCancel();
			}
		}

		class DialogWindowListener extends WindowAdapter {
			/*
			 * (non-Javadoc)
			 * @seejava.awt.event.WindowAdapter#windowLostFocus(java.awt.event.
			 * WindowEvent)
			 */
			/*
			 * (non-Javadoc)
			 * @see
			 * java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent
			 * )
			 */
			@Override
			public void windowClosing(final WindowEvent e) {
				if (isVisible()) {
					confirmedSubmit();
				}
			}
		}

		class SplitAction extends AbstractAction {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent e) {
				split();
			}
		}

		class SubmitAction extends AbstractAction {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent e) {
				submit();
			}
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private EditNodeBase base;

		EditDialog(final EditNodeBase base, final String title, final Frame frame) {
			super(frame, title, /*modal=*/true);
			getContentPane().setLayout(new BorderLayout());
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			final DialogWindowListener dfl = new DialogWindowListener();
			addWindowListener(dfl);
			this.base = base;
		}

		protected void cancel() {
			setVisible(false);
		}

		protected void confirmedCancel() {
			if (isChanged()) {
				final int action = JOptionPane.showConfirmDialog(this, TextUtils.getText("long_node_changed_cancel"), "",
				    JOptionPane.OK_CANCEL_OPTION);
				if (action == JOptionPane.CANCEL_OPTION) {
					return;
				}
			}
			cancel();
		}

		protected void confirmedSubmit() {
			if (isChanged()) {
				final int action = JOptionPane.showConfirmDialog(this, TextUtils.getText("long_node_changed_submit"), "",
				    JOptionPane.YES_NO_CANCEL_OPTION);
				if (action == JOptionPane.CANCEL_OPTION) {
					return;
				}
				if (action == JOptionPane.YES_OPTION) {
					submit();
					return;
				}
			}
			cancel();
		}

		/**
		 * @return Returns the base.
		 */
		EditNodeBase getBase() {
			return base;
		}

		abstract protected boolean isChanged();

		/**
		 * @param base
		 *            The base to set.
		 */
		void setBase(final EditNodeBase base) {
			this.base = base;
		}

		protected void split() {
			setVisible(false);
		}

		protected void submit() {
			setVisible(false);
		}
	}

	protected JPopupMenu createPopupMenu(Component component){
		JPopupMenu menu = new JPopupMenu();
		if(! (component instanceof JTextComponent)){
			return menu;
		}
		final ActionMap actionMap = ((JTextComponent)component).getActionMap();
		final Action copyAction = actionMap.get(DefaultEditorKit.copyAction);
		if(copyAction != null)
			menu.add(TextUtils.getText("CopyAction.text")).addActionListener(copyAction);
		final Action cutAction = actionMap.get(DefaultEditorKit.cutAction);
		if(cutAction != null)
			menu.add(TextUtils.getText("CutAction.text")).addActionListener(cutAction);
		final Action pasteAction = actionMap.get(DefaultEditorKit.pasteAction);
		if(pasteAction != null)
			menu.add(TextUtils.getText("PasteAction.text")).addActionListener(pasteAction);
		SpellCheckerController.getController().addSpellCheckerMenu(menu);
		return menu;
	}

	public interface IEditControl {
		void cancel();

		void ok(String newText);

		void split(String newText, int position);
	}

	protected static final int BUTTON_CANCEL = 1;
	protected static final int BUTTON_OK = 0;
	protected static final int BUTTON_SPLIT = 2;
	final private IEditControl editControl;
// 	final private ModeController modeController;
	protected NodeModel node;
	protected String text;
	private Color background;
	protected Color getBackground() {
    	return background;
    }
	protected FocusListener textFieldListener = null;
	protected EditNodeBase(final NodeModel node, final String text,
	                       final IEditControl editControl) {
//		this.modeController = modeController;
		this.editControl = editControl;
		this.node = node;
		this.text = text;
	}

	public void closeEdit() {
		if (textFieldListener != null) {
			textFieldListener.focusLost(null);
		}
	}

	/**
	 */
	public IEditControl getEditControl() {
		return editControl;
	}

	/**
	 */
	public NodeModel getNode() {
		return node;
	}

	/**
	 */
	protected String getText() {
		return text;
	}

	/**
	 */
	public FocusListener getTextFieldListener() {
		return textFieldListener;
	}

	protected void redispatchKeyEvents(final JTextComponent textComponent, final KeyEvent firstKeyEvent) {
		if (textComponent.hasFocus()) {
			return;
		}
		final KeyboardFocusManager currentKeyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		class KeyEventQueue implements KeyEventDispatcher, FocusListener {
			ArrayList<KeyEvent> events = new ArrayList<KeyEvent>(100);

			public boolean dispatchKeyEvent(final KeyEvent ke) {
			    if(events.contains(ke)){
			        return false;
			    }
			    KeyEvent newEvent = new KeyEvent(textComponent, ke.getID(), ke.getWhen(), ke.getModifiers(), ke.getKeyCode(), ke.getKeyChar(), ke.getKeyLocation());
			    events.add(newEvent);
			    ke.consume();
				return true;
			}

			public void focusGained(final FocusEvent e) {
				e.getComponent().removeFocusListener(this);
				for (int i = 0; i < events.size(); i++) {
	                e.getComponent().dispatchEvent(events.get(i));
				}
				currentKeyboardFocusManager.removeKeyEventDispatcher(this);
			}

			public void focusLost(final FocusEvent e) {
			}
		};
		final KeyEventQueue keyEventDispatcher = new KeyEventQueue();
		currentKeyboardFocusManager.addKeyEventDispatcher(keyEventDispatcher);
		textComponent.addFocusListener(keyEventDispatcher);
		if (firstKeyEvent == null) {
			return;
		}
		if (firstKeyEvent.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
			switch (firstKeyEvent.getKeyCode()) {
				case KeyEvent.VK_HOME:
					textComponent.setCaretPosition(textComponent.viewToModel(new Point(0, 0)));
					//					firstKeyEvent.consume();
					break;
				case KeyEvent.VK_END:
					textComponent.setCaretPosition(textComponent.getDocument().getLength());
					//					firstKeyEvent.consume();
					break;
			}
		}
		else {
			textComponent.selectAll();
			keyEventDispatcher.dispatchKeyEvent(firstKeyEvent);
		}
	}

	/**
	 */
	public void setTextFieldListener(final FocusListener listener) {
		textFieldListener = listener;
	}

	abstract public void show(JFrame frame);
	public void setBackground(Color background) {
	    this.background = background;
	    
    }
}
