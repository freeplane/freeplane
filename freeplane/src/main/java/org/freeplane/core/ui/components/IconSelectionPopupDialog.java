/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  @author <a href="mailto:labe@users.sourceforge.net">Lars Berning</a>
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
package org.freeplane.core.ui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.IconDescription;
import org.freeplane.features.icon.factory.IconFactory;

public class IconSelectionPopupDialog extends JDialog implements KeyListener, MouseListener {
	static class Position {
		final private int x, y;

		public Position(final int x, final int y) {
			this.x = x;
			this.y = y;
		}

		/**
		 * @return Returns the x.
		 */
		public int getX() {
			return x;
		}

		/**
		 * @return Returns the y.
		 */
		public int getY() {
			return y;
		}

		@Override
		public String toString() {
			return ("(" + getX() + "," + getY() + ")");
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private JLabel descriptionLabel;
	final private JLabel[] iconLabels;
	final private JPanel iconPanel = new JPanel();
	final private List<? extends IconDescription> icons;
	private int mModifiers;
	final private int numOfIcons;
	private int result;
	private Position selected = new Position(0, 0);
	final private int xDimension;
	private int yDimension;

	public IconSelectionPopupDialog(final Frame frame, final List<? extends IconDescription> icons) {
		super(frame, TextUtils.getText("select_icon"));
		getContentPane().setLayout(new BorderLayout());
		this.icons = icons;
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent we) {
				close();
			}
		});
		numOfIcons = icons.size();
		xDimension = (int) Math.ceil(Math.sqrt(numOfIcons)) * 16 / 9;
		if (numOfIcons <= xDimension * (xDimension - 1)) {
			yDimension = xDimension - 1;
		}
		else {
			yDimension = xDimension;
		}
		final GridLayout gridlayout = new GridLayout(0, xDimension);
		gridlayout.setHgap(3);
		gridlayout.setVgap(3);
		iconPanel.setLayout(gridlayout);
		iconLabels = new JLabel[numOfIcons];
		for (int i = 0; i < numOfIcons; ++i) {
			final IconDescription icon = icons.get(i);
			iconPanel.add(iconLabels[i] = new JLabel(icon.getActionIcon()));
			iconLabels[i].setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			iconLabels[i].addMouseListener(this);
		}
		final int singleIconSize = (int) ((IconFactory.DEFAULT_UI_ICON_HEIGTH.toBaseUnits()+ 0.5) * 1.1);
		iconPanel.setPreferredSize(new Dimension(xDimension * singleIconSize, yDimension * singleIconSize));
		iconPanel.setMinimumSize(new Dimension(xDimension * singleIconSize, yDimension * singleIconSize));
		iconPanel.setMaximumSize(new Dimension(xDimension * singleIconSize, yDimension * singleIconSize));
		iconPanel.setSize(new Dimension(xDimension * singleIconSize, yDimension * singleIconSize));
		getContentPane().add(iconPanel, BorderLayout.CENTER);
		descriptionLabel = new JLabel(" ");
		getContentPane().add(descriptionLabel, BorderLayout.SOUTH);
		setSelectedPosition(new Position(0, 0));
		select(getSelectedPosition());
		addKeyListener(this);
		pack();
	}

	private void addIcon(final int pModifiers) {
		result = calculateIndex(getSelectedPosition());
		mModifiers = pModifiers;
		this.dispose();
	}

	private int calculateIndex(final Position position) {
		return position.getY() * xDimension + position.getX();
	}

	private boolean canSelect(final Position position) {
		return ((position.getX() >= 0) && (position.getX() < xDimension) && (position.getY() >= 0)
		        && (position.getY() < yDimension) && (calculateIndex(position) < numOfIcons));
	}

	private void close() {
		result = -1;
		mModifiers = 0;
		this.dispose();
	}

	private void cursorDown() {
		final Position newPosition = new Position(getSelectedPosition().getX(), getSelectedPosition().getY() + 1);
		if (canSelect(newPosition)) {
			select(newPosition);
		}
	}

	private void cursorLeft() {
		final Position newPosition = new Position(getSelectedPosition().getX() - 1, getSelectedPosition().getY());
		if (canSelect(newPosition)) {
			select(newPosition);
		}
	}

	private void cursorRight() {
		final Position newPosition = new Position(getSelectedPosition().getX() + 1, getSelectedPosition().getY());
		if (canSelect(newPosition)) {
			select(newPosition);
		}
	}

	private void cursorUp() {
		final Position newPosition = new Position(getSelectedPosition().getX(), getSelectedPosition().getY() - 1);
		if (canSelect(newPosition)) {
			select(newPosition);
		}
	}

	public KeyStroke getKeyStroke(String keystrokeResourceName) {
		final String keyStrokeDescription = ResourceController.getResourceController().getProperty(keystrokeResourceName);
		return UITools.getKeyStroke(keyStrokeDescription);
	}

	private int findIndexByKeyEvent(final KeyEvent keyEvent) {
		for (int i = 0; i < icons.size(); i++) {
			final IconDescription info = icons.get(i);
			final KeyStroke iconKeyStroke = getKeyStroke(info.getShortcutKey());
			if (iconKeyStroke != null
			        && (keyEvent.getKeyCode() == iconKeyStroke.getKeyCode()
			                && keyEvent.getKeyCode() != 0
			                && (iconKeyStroke.getModifiers() & InputEvent.SHIFT_MASK) == (keyEvent.getModifiers() & InputEvent.SHIFT_MASK) || keyEvent
			            .getKeyChar() == iconKeyStroke.getKeyChar()) && keyEvent.getKeyChar() != 0
			        && keyEvent.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Transfer shift masks from InputEvent to ActionEvent. But, why don't they
	 * use the same constants???? Java miracle.
	 */
	public int getModifiers() {
		int m = mModifiers;
		if ((mModifiers & (ActionEvent.SHIFT_MASK | InputEvent.SHIFT_DOWN_MASK)) != 0) {
			m |= ActionEvent.SHIFT_MASK;
		}
		if ((mModifiers & (ActionEvent.CTRL_MASK | InputEvent.CTRL_DOWN_MASK)) != 0) {
			m |= ActionEvent.CTRL_MASK;
		}
		if ((mModifiers & (ActionEvent.META_MASK | InputEvent.META_DOWN_MASK)) != 0) {
			m |= ActionEvent.META_MASK;
		}
		if ((mModifiers & (ActionEvent.ALT_MASK | InputEvent.ALT_DOWN_MASK)) != 0) {
			m |= ActionEvent.ALT_MASK;
		}
		return m;
	}

	private Position getPosition(final JLabel caller) {
		int index = 0;
		for (index = 0; index < iconLabels.length; index++) {
			if (caller == iconLabels[index]) {
				break;
			}
		}
		return new Position(index % xDimension, index / xDimension);
	}

	public int getResult() {
		return result;
	}

	private Position getSelectedPosition() {
		return selected;
	}

	private void highlight(final Position position) {
		iconLabels[calculateIndex(position)].setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(final KeyEvent keyEvent) {
		switch (keyEvent.getKeyCode()) {
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_KP_RIGHT:
				cursorRight();
				return;
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_KP_LEFT:
				cursorLeft();
				return;
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_KP_DOWN:
				cursorDown();
				return;
			case KeyEvent.VK_UP:
			case KeyEvent.VK_KP_UP:
				cursorUp();
				return;
			case KeyEvent.VK_ESCAPE:
				keyEvent.consume();
				close();
				return;
			case KeyEvent.VK_ENTER:
			case KeyEvent.VK_SPACE:
				keyEvent.consume();
				addIcon(keyEvent.getModifiers());
				return;
		}
		final int index = findIndexByKeyEvent(keyEvent);
		if (index != -1) {
			result = index;
			mModifiers = keyEvent.getModifiers();
			keyEvent.consume();
			this.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(final KeyEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(final KeyEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(final MouseEvent mouseEvent) {
		addIcon(mouseEvent.getModifiers());
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(final MouseEvent arg0) {
		select(getPosition((JLabel) arg0.getSource()));
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(final MouseEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(final MouseEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(final MouseEvent arg0) {
	}

	private void select(final Position position) {
		unhighlight(getSelectedPosition());
		setSelectedPosition(position);
		highlight(position);
		final int index = calculateIndex(position);
		final IconDescription iconInformation = icons.get(index);
		final String keyStroke = ResourceController.getResourceController().getProperty(iconInformation.getShortcutKey());
		if (keyStroke != null) {
			descriptionLabel.setText(iconInformation.getTranslatedDescription() + ", " + keyStroke);
		}
		else {
			descriptionLabel.setText(iconInformation.getTranslatedDescription());
		}
	}

	private void setSelectedPosition(final Position position) {
		selected = position;
	}

	private void unhighlight(final Position position) {
		iconLabels[calculateIndex(position)].setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
	}
}
