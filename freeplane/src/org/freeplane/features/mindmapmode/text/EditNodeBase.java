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
package org.freeplane.features.mindmapmode.text;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.features.mindmapmode.ortho.SpellCheckerController;

/**
 * @author foltin
 */
public class EditNodeBase {
	protected class EditCopyAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		final private JTextComponent textComponent;

		public EditCopyAction(final JTextComponent textComponent) {
			super(ResourceBundles.getText("CopyAction.text"));
			this.textComponent = textComponent;
		}

		public void actionPerformed(final ActionEvent e) {
			final String selection = textComponent.getSelectedText();
			if (selection != null) {
				getClipboard().setContents(new StringSelection(selection), null);
			}
		}
	}

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

		EditDialog(final EditNodeBase base, final Frame frame) {
			super(frame, base.getText("edit_long_node"), /*modal=*/true);
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
				final int action = JOptionPane.showConfirmDialog(this, base.getText("long_node_changed_cancel"), "",
				    JOptionPane.OK_CANCEL_OPTION);
				if (action == JOptionPane.CANCEL_OPTION) {
					return;
				}
			}
			cancel();
		}

		protected void confirmedSubmit() {
			if (isChanged()) {
				final int action = JOptionPane.showConfirmDialog(this, base.getText("long_node_changed_submit"), "",
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

	public class EditPopupMenu extends JPopupMenu {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public EditPopupMenu(final JTextComponent textComponent) {
			this.add(new EditCopyAction(textComponent));
			SpellCheckerController.getController(modeController).addSpellCheckerMenu(this);
		}
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
	final private ModeController modeController;
	protected NodeModel node;
	protected String text;
	protected FocusListener textFieldListener = null;

	protected EditNodeBase(final NodeModel node, final String text, final ModeController modeController,
	                       final IEditControl editControl) {
		this.modeController = modeController;
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
	public Clipboard getClipboard() {
		return Toolkit.getDefaultToolkit().getSystemClipboard();
	}

	/**
		 *
		 */
	protected Controller getController() {
		return modeController.getController();
	}

	/**
	 */
	public IEditControl getEditControl() {
		return editControl;
	}

	protected ModeController getModeController() {
		return modeController;
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
	protected String getText(final String string) {
		return ResourceBundles.getText(string);
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
			LinkedList events = new LinkedList();

			public boolean dispatchKeyEvent(final KeyEvent e) {
				events.add(e);
				return true;
			}

			public void focusGained(final FocusEvent e) {
				e.getComponent().removeFocusListener(this);
				currentKeyboardFocusManager.removeKeyEventDispatcher(this);
				final Iterator iterator = events.iterator();
				while (iterator.hasNext()) {
					final KeyEvent ke = (KeyEvent) iterator.next();
					ke.setSource(textComponent);
					textComponent.dispatchEvent(ke);
				}
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
					textComponent.setCaretPosition(0);
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
			textComponent.dispatchEvent(firstKeyEvent);
		}
	}

	/**
	 */
	public void setText(final String string) {
		text = string;
	}

	/**
	 */
	public void setTextFieldListener(final FocusListener listener) {
		textFieldListener = listener;
	}
}
