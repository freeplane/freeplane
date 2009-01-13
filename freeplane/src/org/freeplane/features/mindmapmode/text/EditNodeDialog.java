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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Tools;

/**
 * @author foltin
 */
public class EditNodeDialog extends EditNodeBase {
	class LongNodeDialog extends EditDialog {
		final private JTextArea textArea;

		LongNodeDialog() {
			super(EditNodeDialog.this);
			final ViewController viewController = Controller.getController().getViewController();
			textArea = new JTextArea(getText());
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			final JScrollPane editorScrollPane = new JScrollPane(textArea);
			editorScrollPane
			    .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			int preferredHeight = viewController.getComponent(getNode()).getHeight();
			preferredHeight = Math.max(preferredHeight, Integer.parseInt(Controller
			    .getResourceController().getProperty("el__min_default_window_height")));
			preferredHeight = Math.min(preferredHeight, Integer.parseInt(Controller
			    .getResourceController().getProperty("el__max_default_window_height")));
			int preferredWidth = viewController.getComponent(getNode()).getWidth();
			preferredWidth = Math.max(preferredWidth, Integer.parseInt(Controller
			    .getResourceController().getProperty("el__min_default_window_width")));
			preferredWidth = Math.min(preferredWidth, Integer.parseInt(Controller
			    .getResourceController().getProperty("el__max_default_window_width")));
			editorScrollPane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
			final JPanel panel = new JPanel();
			final JButton okButton = new JButton();
			final JButton cancelButton = new JButton();
			final JButton splitButton = new JButton();
			final JCheckBox enterConfirms = new JCheckBox("",
			    binOptionIsTrue("el__enter_confirms_by_default"));
			MenuBuilder.setLabelAndMnemonic(okButton, getText("ok"));
			MenuBuilder.setLabelAndMnemonic(cancelButton, getText("cancel"));
			MenuBuilder.setLabelAndMnemonic(splitButton, getText("split"));
			MenuBuilder.setLabelAndMnemonic(enterConfirms, getText("enter_confirms"));
			if (EditNodeDialog.booleanHolderForConfirmState == null) {
				EditNodeDialog.booleanHolderForConfirmState = new Tools.BooleanHolder();
				EditNodeDialog.booleanHolderForConfirmState.setValue(enterConfirms.isSelected());
			}
			else {
				enterConfirms.setSelected(EditNodeDialog.booleanHolderForConfirmState.getValue());
			}
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					submit();
				}
			});
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					cancel();
				}
			});
			splitButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					split();
				}
			});
			enterConfirms.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					textArea.requestFocus();
					EditNodeDialog.booleanHolderForConfirmState
					    .setValue(enterConfirms.isSelected());
				}
			});
			textArea.addKeyListener(new KeyListener() {
				public void keyPressed(final KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						e.consume();
						confirmedCancel();
					}
					else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						if (enterConfirms.isSelected()
						        && (e.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
							e.consume();
							textArea.insert("\n", textArea.getCaretPosition());
						}
						else if (enterConfirms.isSelected()
						        || ((e.getModifiers() & InputEvent.ALT_MASK) != 0)) {
							e.consume();
							submit();
						}
						else {
							e.consume();
							textArea.insert("\n", textArea.getCaretPosition());
						}
					}
				}

				public void keyReleased(final KeyEvent e) {
				}

				public void keyTyped(final KeyEvent e) {
				}
			});
			textArea.addMouseListener(new MouseListener() {
				private void conditionallyShowPopup(final MouseEvent e) {
					if (e.isPopupTrigger()) {
						final JPopupMenu popupMenu = new EditPopupMenu(textArea);
						popupMenu.show(e.getComponent(), e.getX(), e.getY());
						e.consume();
					}
				}

				public void mouseClicked(final MouseEvent e) {
				}

				public void mouseEntered(final MouseEvent e) {
				}

				public void mouseExited(final MouseEvent e) {
				}

				public void mousePressed(final MouseEvent e) {
					conditionallyShowPopup(e);
				}

				public void mouseReleased(final MouseEvent e) {
					conditionallyShowPopup(e);
				}
			});
			final Font nodeFont = viewController.getFont(getNode());
			textArea.setFont(nodeFont);
			final Color nodeTextColor = viewController.getTextColor(getNode());
			textArea.setForeground(nodeTextColor);
			final Color nodeTextBackground = viewController.getBackgroundColor(getNode());
			textArea.setBackground(nodeTextBackground);
			textArea.setCaretColor(nodeTextColor);
			final JPanel buttonPane = new JPanel();
			buttonPane.add(enterConfirms);
			buttonPane.add(okButton);
			buttonPane.add(cancelButton);
			buttonPane.add(splitButton);
			buttonPane.setMaximumSize(new Dimension(1000, 20));
			if (Controller.getResourceController().getProperty("el__buttons_position").equals(
			    "above")) {
				panel.add(buttonPane);
				panel.add(editorScrollPane);
			}
			else {
				panel.add(editorScrollPane);
				panel.add(buttonPane);
			}
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			setContentPane(panel);
			if (firstEvent instanceof KeyEvent) {
				redispatchKeyEvents(textArea, firstEvent);
			}
			else {
				textArea.setCaretPosition(getText().length());
			}
		}

		/*
		 * (non-Javadoc)
		 * @see freeplane.view.mindmapview.EditNodeBase.Dialog#cancel()
		 */
		@Override
		protected void cancel() {
			getEditControl().cancel();
			super.cancel();
		}

		@Override
		public Component getMostRecentFocusOwner() {
			if (isFocused()) {
				return getFocusOwner();
			}
			else {
				return textArea;
			}
		}

		/*
		 * (non-Javadoc)
		 * @see freeplane.view.mindmapview.EditNodeBase.Dialog#isChanged()
		 */
		@Override
		protected boolean isChanged() {
			return !getText().equals(textArea.getText());
		}

		@Override
		public void show() {
			textArea.requestFocus();
			super.show();
		}

		/*
		 * (non-Javadoc)
		 * @see freeplane.view.mindmapview.EditNodeBase.Dialog#split()
		 */
		@Override
		protected void split() {
			getEditControl().split(textArea.getText(), textArea.getCaretPosition());
			super.split();
		}

		/*
		 * (non-Javadoc)
		 * @see freeplane.view.mindmapview.EditNodeBase.Dialog#submit()
		 */
		@Override
		protected void submit() {
			getEditControl().ok(textArea.getText());
			super.submit();
		}
	}

	/** Private variable to hold the last value of the "Enter confirms" state. */
	private static Tools.BooleanHolder booleanHolderForConfirmState;
	final private KeyEvent firstEvent;

	public EditNodeDialog(final NodeModel node, final String text, final KeyEvent firstEvent,
	                      final ModeController controller, final IEditControl editControl) {
		super(node, text, controller, editControl);
		this.firstEvent = firstEvent;
	}

	public void show() {
		final EditDialog dialog = new LongNodeDialog();
		dialog.pack();
		Controller.getController().getViewController().scrollNodeToVisible(node);
		UITools.setDialogLocationRelativeTo(dialog, getNode());
		dialog.show();
	}
}
