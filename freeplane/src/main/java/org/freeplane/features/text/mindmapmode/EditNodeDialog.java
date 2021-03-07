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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * @author foltin
 */
public class EditNodeDialog extends EditNodeBase {
    private final JTextComponent textComponent;
	private final boolean enableSplit;

	private class LongNodeDialog extends EditDialog {

		public LongNodeDialog(final RootPaneContainer frame, final String title, final Color background) {
			super(EditNodeDialog.this, title, frame);
			getDialog().setModal(ResourceController.getResourceController().getBooleanProperty("enforceModalEditorDialogs"));
			final JScrollPane editorScrollPane;
			textComponent.setText(getText());
			final JScrollPane ancestorScrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, textComponent);
			if (ancestorScrollPane != null) {
			    editorScrollPane = ancestorScrollPane;
			}
			else {
			    editorScrollPane = new JScrollPane(textComponent);
			}
			final JButton okButton = new JButton();
			final JButton cancelButton = new JButton();
			final JButton splitButton = new JButton();
			final JCheckBox enterConfirms = new JCheckBox("", ResourceController.getResourceController()
			    .getBooleanProperty("el__enter_confirms_by_default"));
			LabelAndMnemonicSetter.setLabelAndMnemonic(okButton, TextUtils.getRawText("ok"));
			LabelAndMnemonicSetter.setLabelAndMnemonic(cancelButton, TextUtils.getRawText("cancel"));
			LabelAndMnemonicSetter.setLabelAndMnemonic(splitButton, TextUtils.getRawText("split"));
			LabelAndMnemonicSetter.setLabelAndMnemonic(enterConfirms, TextUtils.getRawText("enter_confirms"));
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					submit();
				}
			});
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					cancel();
				}
			});
			splitButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					split();
				}
			});
			enterConfirms.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					textComponent.requestFocus();
					ResourceController.getResourceController().setProperty("el__enter_confirms_by_default",
					    Boolean.toString(enterConfirms.isSelected()));
				}
			});
			textComponent.addKeyListener(new KeyListener() {
				@Override
				public void keyPressed(final KeyEvent e) {
					switch (e.getKeyCode()) {
						case KeyEvent.VK_ESCAPE:
							e.consume();
							confirmedCancel();
							break;
						case KeyEvent.VK_ENTER:
							e.consume();
							if ((e.getModifiers() & InputEvent.SHIFT_MASK) != 0
							        || enterConfirms.isSelected() == ((e.getModifiers() & InputEvent.ALT_MASK) != 0)) {
								insertString("\n");
								break;
							}
							submit();
							break;
						case KeyEvent.VK_TAB:
							e.consume();
							insertString("    ");
							break;
					}
				}

				public void insertString(final String text) {
					try {
						textComponent.getDocument().insertString(textComponent.getCaretPosition(), text, null);
					}
					catch (BadLocationException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void keyReleased(final KeyEvent e) {
				}

				@Override
				public void keyTyped(final KeyEvent e) {
				}
			});
			textComponent.addMouseListener(new MouseListener() {
				private void conditionallyShowPopup(final MouseEvent e) {
					if (e.isPopupTrigger()) {
						final Component component = e.getComponent();
						final JPopupMenu popupMenu = createPopupMenu(component);
						popupMenu.show(component, e.getX(), e.getY());
						e.consume();
					}
				}

				@Override
				public void mouseClicked(final MouseEvent e) {
				}

				@Override
				public void mouseEntered(final MouseEvent e) {
				}

				@Override
				public void mouseExited(final MouseEvent e) {
				}

				@Override
				public void mousePressed(final MouseEvent e) {
					conditionallyShowPopup(e);
				}

				@Override
				public void mouseReleased(final MouseEvent e) {
					conditionallyShowPopup(e);
				}
			});
			final JPanel buttonPane = new JPanel();
			buttonPane.add(enterConfirms);
			buttonPane.add(okButton);
			buttonPane.add(cancelButton);
			if (enableSplit)
				buttonPane.add(splitButton);
			buttonPane.setMaximumSize(new Dimension(1000, 20));
			final Container contentPane = getDialog().getContentPane();
			contentPane.add(editorScrollPane, BorderLayout.CENTER);
			final boolean areButtonsAtTheTop = ResourceController.getResourceController().getBooleanProperty("el__buttons_above");
			contentPane.add(buttonPane, areButtonsAtTheTop ? BorderLayout.NORTH : BorderLayout.SOUTH);
            textComponent.requestFocus();
		}

		/*
		 * (non-Javadoc)
		 * @see freeplane.view.mindmapview.EditNodeBase.Dialog#cancel()
		 */
		@Override
		protected void cancel() {
			super.cancel();
			getEditControl().cancel();
		}

		@Override
		public Component getMostRecentFocusOwner() {
			if (getDialog().isFocused()) {
				return getFocusOwner();
			}
			else {
				return textComponent;
			}
		}

		/*
		 * (non-Javadoc)
		 * @see freeplane.view.mindmapview.EditNodeBase.Dialog#isChanged()
		 */
		@Override
		protected boolean isChanged() {
			return !getText().equals(textComponent.getText());
		}

		/*
		 * (non-Javadoc)
		 * @see freeplane.view.mindmapview.EditNodeBase.Dialog#split()
		 */
		@Override
		protected void split() {
			super.split();
			getEditControl().split(textComponent.getText(), textComponent.getCaretPosition());
		}

		/*
		 * (non-Javadoc)
		 * @see freeplane.view.mindmapview.EditNodeBase.Dialog#submit()
		 */
		@Override
		protected void submit() {
			super.submit();
			getEditControl().ok(textComponent.getText());
		}
	}

	/** Private variable to hold the last value of the "Enter confirms" state. */
	final private KeyEvent firstEvent;
	private String title;

	public EditNodeDialog(NodeModel node, KeyEvent firstEvent, boolean editorBlocks, 
	        IEditControl editControl,
	                      boolean enableSplit, JEditorPane textEditor) {
        super(node, textEditor.getText(), editorBlocks, editControl);
        this.firstEvent = firstEvent;
        this.enableSplit = enableSplit;
		textComponent = textEditor;
	}

	@Override
	public void show(final RootPaneContainer frame) {
		if (title == null) {
			title = TextUtils.getText("edit_long_node");
		}
		final EditDialog editor = new LongNodeDialog(frame, title, getBackground());
		redispatchKeyEvents(textComponent, firstEvent);
        if (firstEvent == null) {
            textComponent.setCaretPosition(textComponent.getDocument().getLength());
        }
		final JDialog dialog = editor.getDialog();
		configureDialog(dialog);
        restoreDialogSize(dialog);
		dialog.pack();
		Controller.getCurrentModeController().getController().getMapViewManager().scrollNodeToVisible(node);
		if (ResourceController.getResourceController().getBooleanProperty("el__position_window_below_node")) {
			UITools.setDialogLocationUnder(dialog, getNode());
		}
		else {
			UITools.setDialogLocationRelativeTo(dialog, getNode());
		}
		editor.show();
		dialog.addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(final ComponentEvent e) {
			}

			@Override
			public void componentResized(final ComponentEvent e) {
			    saveDialogSize(dialog);
			}

			@Override
			public void componentMoved(final ComponentEvent e) {
			}

			@Override
			public void componentHidden(final ComponentEvent e) {
				editor.dispose();
			}
		});
	}

	protected void configureDialog(JDialog dialog) {
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
