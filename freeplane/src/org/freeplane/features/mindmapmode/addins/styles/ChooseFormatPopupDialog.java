/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package org.freeplane.features.mindmapmode.addins.styles;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.WindowConfigurationStorage;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.mindmapnode.pattern.Pattern;
import org.freeplane.features.mindmapnode.pattern.StylePatternPanel;
import org.freeplane.features.mindmapnode.pattern.StylePatternPanel.StylePatternPanelType;

/** */
class ChooseFormatPopupDialog extends JDialog implements KeyListener {
	public static final int CANCEL = -1;
	public static final int OK = 1;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String WINDOW_PREFERENCE_STORAGE_PROPERTY = "accessories.plugins.dialogs.ChooseFormatPopupDialog.window_storage";
	private JButton jCancelButton;
	private JPanel jContentPane = null;
	private JButton jOKButton;
	final private ModeController mController;
	private StylePatternPanel mStylePatternFrame;
	private int result = ChooseFormatPopupDialog.CANCEL;

	/**
	 * This constructor is used, if you need the user to enter a pattern
	 * generally.
	 */
	public ChooseFormatPopupDialog(final Frame frame, final ModeController controller, final String dialogTitle,
	                               final Pattern pattern) {
		super(frame);
		mController = controller;
		initialize(dialogTitle);
		mStylePatternFrame.setPattern(pattern);
		mStylePatternFrame.addListeners();
	}

	private void cancelPressed() {
		result = ChooseFormatPopupDialog.CANCEL;
		close();
	}

	private void close() {
		final WindowConfigurationStorage storage = new NormalWindowCfgStorage();
		storage.storeDialogPositions(this, ChooseFormatPopupDialog.WINDOW_PREFERENCE_STORAGE_PROPERTY);
		setVisible(false);
		this.dispose();
	}

	/**
	 * This method initializes jButton1
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJCancelButton() {
		if (jCancelButton == null) {
			jCancelButton = new JButton();
			jCancelButton.setAction(new AbstractAction() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(final ActionEvent e) {
					cancelPressed();
				}
			});
			MenuBuilder.setLabelAndMnemonic(jCancelButton, ResourceBundles.getText(("cancel")));
		}
		return jCancelButton;
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new GridBagLayout());
			/*
			 * public GridBagConstraints(int gridx, int gridy, int gridwidth,
			 * int gridheight, double weightx, double weighty, int anchor, int
			 * fill, Insets insets, int ipadx, int ipady)
			 */
			JScrollPane scrollPane = new JScrollPane(getStylePatternFrame());
			UITools.setScrollbarIncrement(scrollPane);
			jContentPane.add(scrollPane, new GridBagConstraints(0, 0, 2, 1, 2.0, 8.0,
			    GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jContentPane.add(getJOKButton(), new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.EAST,
			    GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			jContentPane.add(getJCancelButton(), new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.EAST,
			    GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			getRootPane().setDefaultButton(getJOKButton());
		}
		return jContentPane;
	}

	/**
	 * This method initializes jButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJOKButton() {
		if (jOKButton == null) {
			jOKButton = new JButton();
			jOKButton.setAction(new AbstractAction() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(final ActionEvent e) {
					okPressed();
				}
			});
			MenuBuilder.setLabelAndMnemonic(jOKButton, ResourceBundles.getText("ok"));
		}
		return jOKButton;
	}

	public Pattern getPattern() {
		return mStylePatternFrame.getResultPattern();
	}

	public Pattern getPattern(final Pattern copyIntoPattern) {
		return mStylePatternFrame.getResultPattern(copyIntoPattern);
	}

	/**
	 * @return Returns the result.
	 */
	public int getResult() {
		return result;
	}

	private Component getStylePatternFrame() {
		if (mStylePatternFrame == null) {
			mStylePatternFrame = new StylePatternPanel(mController, StylePatternPanelType.WITHOUT_NAME_AND_CHILDS);
			mStylePatternFrame.init();
		}
		return mStylePatternFrame;
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize(final String dialogTitle) {
		this.setTitle(ResourceBundles.getText(dialogTitle));
		final JPanel contentPane = getJContentPane();
		this.setContentPane(contentPane);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent we) {
				cancelPressed();
			}
		});
		addKeyListener(this);
		final Action action = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent arg0) {
				cancelPressed();
			}
		};
		UITools.addEscapeActionToDialog(this, action);
		pack();
		final String marshalled = ResourceController.getResourceController().getProperty(
		    ChooseFormatPopupDialog.WINDOW_PREFERENCE_STORAGE_PROPERTY);
		NormalWindowCfgStorage.decorateDialog(marshalled, this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(final KeyEvent keyEvent) {
		System.out.println("key pressed: " + keyEvent);
		switch (keyEvent.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				cancelPressed();
				keyEvent.consume();
				break;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(final KeyEvent keyEvent) {
		System.out.println("keyReleased: " + keyEvent);
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(final KeyEvent keyEvent) {
		System.out.println("keyTyped: " + keyEvent);
	}

	private void okPressed() {
		result = ChooseFormatPopupDialog.OK;
		close();
	}
}
