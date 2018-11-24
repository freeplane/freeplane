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
import java.awt.Container;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.RootPaneContainer;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.spellchecker.mindmapmode.SpellCheckerController;

/**
 * @author foltin
 */
abstract public class EditNodeBase {
	public static enum EditedComponent{TEXT, DETAIL, NOTE}
	abstract static class EditDialog{
		 private final JDialog dialog;
		protected JDialog getDialog() {
        	return dialog;
        }

		class CancelAction extends AbstractAction {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
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
				if (dialog.isVisible()) {
					confirmedSubmit();
				}
			}
		}

		class SplitAction extends AbstractAction {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				split();
			}
		}

		class SubmitAction extends AbstractAction {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				submit();
			}
		}

		private EditNodeBase base;

		protected EditDialog(final EditNodeBase base, final String title, final RootPaneContainer frame) {
			dialog = frame instanceof Frame ? new JDialog((Frame)frame, title, /*modal=*/true) : new JDialog((JDialog)frame, title, /*modal=*/true);
			dialog.getContentPane().setLayout(new BorderLayout());
			dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			final DialogWindowListener dfl = new DialogWindowListener();
			dialog.addWindowListener(dfl);
			this.base = base;
		}

		protected void cancel() {
			dialog.setVisible(false);
		}

		protected void confirmedCancel() {
			if (isChanged()) {
				final int action = JOptionPane.showConfirmDialog(dialog, TextUtils.getText("long_node_changed_cancel"), "",
				    JOptionPane.OK_CANCEL_OPTION);
				if (action == JOptionPane.CANCEL_OPTION) {
					return;
				}
			}
			cancel();
		}

		protected void confirmedSubmit() {
			if (isChanged()) {
				final int action = JOptionPane.showConfirmDialog(dialog, TextUtils.getText("long_node_changed_submit"), "",
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
			dialog.setVisible(false);
		}

		protected void submit() {
			dialog.setVisible(false);
		}

		public void show() {
	        dialog.show();
        }

		public void dispose() {
	        dialog.dispose();
        }

		public Container getContentPane() {
	        return dialog.getContentPane();
        }

		public Component getFocusOwner() {
	        return dialog.getFocusOwner();
        }

		public Component getMostRecentFocusOwner() {
	        return dialog.getMostRecentFocusOwner();
        }


	}

	protected JPopupMenu createPopupMenu(Component component){
		JPopupMenu menu = new JPopupMenu();
		if(! (component instanceof JTextComponent)){
			return menu;
		}
		final ActionMap actionMap = ((JTextComponent)component).getActionMap();
		final Action copyAction = actionMap.get(DefaultEditorKit.copyAction);
		addAction(menu, copyAction, "CopyAction.text");
		final Action cutAction = actionMap.get(DefaultEditorKit.cutAction);
		addAction(menu, cutAction, "CutAction.text");
		final Action pasteAction = actionMap.get(DefaultEditorKit.pasteAction);
		addAction(menu, pasteAction, "PasteAction.text");
		SpellCheckerController.getController().addSpellCheckerMenu(menu);
		return menu;
	}

	protected void addAction(JPopupMenu menu, final Action action, final String label) {
		if(action == null)
			return;
	    final String text = TextUtils.getRawText(label);
	    final JMenuItem item = menu.add(new JMenuItem());
	    LabelAndMnemonicSetter.setLabelAndMnemonic(item, text);
	    item.addActionListener(action);
    }

	public interface IEditControl {
		void cancel();

		void ok(String newText);

		void split(String newText, int position);

		boolean canSplit();

		EditedComponent getEditType();
	}

	protected static final int BUTTON_CANCEL = 1;
	protected static final int BUTTON_OK = 0;
	protected static final int BUTTON_SPLIT = 2;
	final private IEditControl editControl;
// 	final private ModeController modeController;
	protected NodeModel node;
	private String text;
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
		final EventBuffer keyEventDispatcher = MTextController.getController().getEventQueue();
		if (textComponent.hasFocus()) {
			keyEventDispatcher.deactivate();
			return;
		}
		keyEventDispatcher.activate();
		keyEventDispatcher.setTextComponent(textComponent);
		if (firstKeyEvent == null) {
			return;
		}
		if (firstKeyEvent.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
			switch (firstKeyEvent.getKeyCode()) {
				case KeyEvent.VK_HOME:
					final int modelIdx = textComponent.viewToModel(new Point(0, 0));
					if (modelIdx >= 0) // modelIdx is -1 for LaTeX formulas!
						textComponent.setCaretPosition(modelIdx);
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
		}
	}

	/**
	 */
	public void setTextFieldListener(final FocusListener listener) {
		textFieldListener = listener;
	}

	abstract public void show(RootPaneContainer frame);
	public void setBackground(Color background) {
	    this.background = background;

    }
}
