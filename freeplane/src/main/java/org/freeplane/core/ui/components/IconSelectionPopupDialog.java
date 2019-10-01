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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.IIconInformation;
import org.freeplane.features.icon.factory.IconFactory;

@SuppressWarnings("unchecked")
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
	private JLabel[] currentIconLabels;
	private JPanel iconPanel = new JPanel();
	private int mModifiers;
	private int currentIconCount;
	private int result;
	private Position selectedPosition = new Position(0, 0);
	private int xDimension;
	private int yDimension;

	// <SR>
	private List<? extends IIconInformation> allIcons;
	private List<? extends IIconInformation> currentIcons;

	private JPanel filterPanel;

	private static String lastSearchText = "";
	private static int GAP = 1;
	private int perIconSize = (int) (IconFactory.DEFAULT_UI_ICON_HEIGHT.in(LengthUnits.px).value * 1.15 + GAP * 2);
	private JTextField filterTextField;
	final private Color panelBackgroundColor = UIManager.getColor("Panel.background");
	// </SR>

	public IconSelectionPopupDialog(final Frame frame, final List<? extends IIconInformation> icons) {
		super(frame, TextUtils.getText("select_icon"));
		Container contentPane = getContentPane();

		currentIcons = allIcons = icons;

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowClosingListener();

		descriptionLabel = new JLabel(" ");
		filterPanel = setupFilterPanel_and_KeyListener();
		iconPanel = setupInitialIconPanel(icons);
		filterIcons();

		contentPane.setLayout(new BorderLayout());
		contentPane.add(filterPanel, BorderLayout.NORTH);
		contentPane.add(iconPanel, BorderLayout.CENTER);
		contentPane.add(descriptionLabel, BorderLayout.SOUTH);

		setSelectedPosition(new Position(0, 0));
		select(getSelectedPosition());
		addKeyListener(this);
		pack();

		forceRepaint();
	}

	private void addWindowClosingListener() {
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent we) {
				close();
			}
		});
	}

	private JPanel setupInitialIconPanel(final List<? extends IIconInformation> icons) {
		JPanel iconPanel = new JPanel();
		GridLayout iconPanelGridlayout = new GridLayout(0, 1);
		iconPanelGridlayout.setHgap(3); // SR
		iconPanelGridlayout.setVgap(3); // SR
		iconPanel.setLayout(iconPanelGridlayout);

		currentIconCount = allIcons.size();
		xDimension = (int) Math.ceil(Math.sqrt(currentIconCount));
		if (currentIconCount <= xDimension * (xDimension - 1)) {
			yDimension = xDimension - 1;
		} else {
			yDimension = xDimension;
		}

		currentIconLabels = new JLabel[currentIconCount];
		for (int i = 0; i < currentIconCount; ++i) {
			final IIconInformation icon = icons.get(i);
			iconPanel.add(currentIconLabels[i] = new JLabel(icon.getIcon()));
			currentIconLabels[i].setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			currentIconLabels[i].addMouseListener(this);
		}
		final int perIconSize = 27;
		iconPanel.setPreferredSize(new Dimension(xDimension * perIconSize, yDimension * perIconSize));
		iconPanel.setMinimumSize(new Dimension(xDimension * perIconSize, yDimension * perIconSize));
		iconPanel.setMaximumSize(new Dimension(xDimension * perIconSize, yDimension * perIconSize));
		iconPanel.setSize(new Dimension(xDimension * perIconSize, yDimension * perIconSize));
		return iconPanel;
	}

	private void forceRepaint() {
		// TRY TO FIX THE REPAINT ISSUE THAT IS SHOWING UP ON MACOS CATALINA
		iconPanel.repaint(50);
		repaint(50);
		iconPanel.repaint(50);
		repaint(50);
		iconPanel.repaint(50);
		repaint(50);
		iconPanel.repaint(50);
		repaint(50);
		iconPanel.repaint(50);
		repaint(50);
		iconPanel.repaint(50);
		repaint(50);
	}

	private JPanel setupFilterPanel_and_KeyListener() {
		JPanel filterPanel = new JPanel();
		GridLayout gridlayout = new GridLayout(0, 1);
		gridlayout.setHgap(3); // SR
		gridlayout.setVgap(3); // SR
		filterPanel.setLayout(gridlayout);

		filterTextField = setupFilterTextField_and_KeyListener();
		filterPanel.add(filterTextField);

		return filterPanel;
	}

	private JTextField setupFilterTextField_and_KeyListener() {
		JTextField filterTextField = new JTextField();
		filterTextField.setText(lastSearchText);
		filterTextField.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent keyEvent) {
				String filterText = filterTextField.getText();
				if (filterText.length() == 0) {
					if (keyEvent.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
						IconSelectionPopupDialog.this.keyPressed(keyEvent);
					}
				}
			}

			public void keyReleased(KeyEvent keyEvent) {
				boolean passCharOnToDialog = false;
				String filterText = filterTextField.getText();
				lastSearchText = filterText;
				if (!filterText.startsWith("/")) {
					filterTextField.setCaretPosition(filterText.length());
				}
				if (filterText.startsWith(".") && filterText.length() == 2) {
					filterTextField.setText("");
					passCharOnToDialog = true;
				} else {
					char keyChar = keyEvent.getKeyChar();
					switch (keyChar) {
					case KeyEvent.VK_ESCAPE:
						if (filterText.length() > 0) {
							// Consume the Esc here, clear the filterTextField.
							filterTextField.setText("");
							keyEvent.consume();
							break;
						}
						// else fall through to IconSelectionPopupDialog.this.keyPressed(keyEvent) below
					case KeyEvent.VK_LEFT:
					case KeyEvent.VK_RIGHT:
					case KeyEvent.VK_UP:
					case KeyEvent.VK_DOWN:
					case KeyEvent.VK_ENTER:
					case KeyEvent.CHAR_UNDEFINED:
						passCharOnToDialog = true;
						break;
					}
				}
				if (passCharOnToDialog) {
					// Pass on to this dialog's keyPressed(...) method.
					IconSelectionPopupDialog.this.keyPressed(keyEvent);
					return;
				} else {
					if (!filterText.startsWith(".")) {
						filterIcons();
					}
				}
			}

			public void keyTyped(KeyEvent keyEvent) {
			}
		});
		return filterTextField;
	}

	private void filterIcons() {
		String filterText = filterTextField.getText().toLowerCase();
		Pattern regex = null;

		if (filterText.trim().length() > 0) {
			if (filterText.startsWith("/") && filterText.trim().length() >= 2) {
				regex = Pattern.compile(filterText.substring(1).trim());
			}
			currentIcons = new Vector<IIconInformation>();

			for (IIconInformation icon : allIcons) {
				String iconLabel = icon.getTranslatedDescription();
				if (iconLabel.startsWith("icon_")) {
					iconLabel = iconLabel.substring(5);
				}
				boolean matches = false;
				if (filterText.startsWith("/")) {
					matches = regex.matcher(iconLabel).matches();
				} else {
					if (filterText.contains(" ")) {
						matches = true;
						StringTokenizer tokenizer = new StringTokenizer(filterText);
						while (tokenizer.hasMoreTokens()) {
							String token = tokenizer.nextToken();
							matches = matches && iconLabel.toLowerCase().contains(token);
							if (!matches) {
								break;
							}
						}
					} else {
						matches = iconLabel.toLowerCase().contains(filterText);
					}
				}
				if (matches) {
					((Vector<IIconInformation>) currentIcons).add(icon);
				}
			}
		} else {
			currentIcons = allIcons;
		}

		currentIconCount = currentIcons.size();

		refillIconPanel(currentIcons);

		selectedPosition = new Position(0, 0);
		select(selectedPosition);
		pack();

		// Trying to fix the Darcula repaint issue -- icon panel not painting properly.
		validate();
		repaint();
	}

	private void refillIconPanel(List<? extends IIconInformation> currentIcons) {
		removeAllIconsFromIconPanel();

		int currentIconCount = currentIcons.size();
		xDimension = new Double(Math.ceil(Math.sqrt(currentIconCount))).intValue();
		if (xDimension >= 6) {
			// Build a button-matrix which is closest to quadratic
			if (currentIconCount <= xDimension * (xDimension - 1)) {
				yDimension = xDimension - 1;
			} else {
				yDimension = xDimension;
			}
		} else {
			xDimension = 5; // Fix at minimum 5 wide
			yDimension = currentIconCount / xDimension + (currentIconCount % xDimension > 0 ? 1 : 0);
			if (yDimension == 0) {
				yDimension = 1; // Always have at least one row
			}
		}

		currentIconLabels = new JLabel[xDimension * yDimension];
		JPanel rowPanel = null;

		for (int i = 0; i < (xDimension * yDimension); ++i) {
			if (i % xDimension == 0) {
				rowPanel = createIconRowPanel();
				iconPanel.add(rowPanel);
			}
			if (i < currentIconCount) {
				final IIconInformation icon = currentIcons.get(i);
				rowPanel.add(currentIconLabels[i] = new JLabel(icon.getIcon()));
				currentIconLabels[i].setBackground(panelBackgroundColor);
				currentIconLabels[i]
						.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0),
								BorderFactory.createLineBorder(panelBackgroundColor, 1)));
				currentIconLabels[i].addMouseListener(this);
			} else {
				rowPanel.add(new JLabel(" "));
			}
		}

		int xDim = xDimension * perIconSize;
		int yDim = yDimension * perIconSize;
		Dimension size = new Dimension(xDim, yDim);
		iconPanel.setPreferredSize(size);
		iconPanel.setMinimumSize(size);
		iconPanel.setMaximumSize(size);
		iconPanel.setSize(size);
		iconPanel.repaint(100); // Try to fix icons not painting bug (shows up on macOS Catalina)
	}

	private void removeAllIconsFromIconPanel() {
		for (Component component : iconPanel.getComponents()) {
			component.removeMouseListener(this);
		}
		iconPanel.removeAll();
	}

	private JPanel createIconRowPanel() {
		JPanel rowPanel = new JPanel();
		Color panelBackgroundColor = UIManager.getColor("Panel.background");
		rowPanel.setBackground(panelBackgroundColor);
		rowPanel.setPreferredSize(new Dimension(xDimension * perIconSize, perIconSize));
		rowPanel.setMinimumSize(new Dimension(xDimension * perIconSize, perIconSize));
		rowPanel.setMaximumSize(new Dimension(xDimension * perIconSize, perIconSize));

		GridLayout rowPanelLayout = new GridLayout(1, xDimension);
		rowPanelLayout.setHgap(0);
		rowPanelLayout.setVgap(0);

		rowPanel.setLayout(rowPanelLayout);
		rowPanel.repaint();
		return rowPanel;
	}

	private void addIcon(final int pModifiers) {
		result = calculateIndexInAllIconsList();
		mModifiers = pModifiers;
		this.dispose();
	}

	private int calculateIndexInAllIconsList() {
		int indexInFilteredIconsList = calculateIndexInFilteredIconsList(getSelectedPosition());
		int indexInAllIconsList = -1;
		IIconInformation selectedIcon = currentIcons.get(indexInFilteredIconsList);
		for (int i = 0; i < allIcons.size(); i++) {
			if (allIcons.get(i) == selectedIcon) {
				indexInAllIconsList = i;
				break;
			}
		}
		return indexInAllIconsList;
	}

	private int calculateIndexInFilteredIconsList(final Position position) {
		return position.getY() * xDimension + position.getX();
	}

	private boolean canSelect(final Position position) {
		return ((position.getX() >= 0) && (position.getX() < xDimension) && (position.getY() >= 0)
				&& (position.getY() < yDimension) && (calculateIndexInFilteredIconsList(position) < currentIconCount));
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

	private int findIndexByKeyEvent(final KeyEvent keyEvent) {
		for (int i = 0; i < currentIcons.size(); i++) {
			final IIconInformation info = currentIcons.get(i);
			final KeyStroke iconKeyStroke = info.getKeyStroke();
			if (iconKeyStroke != null
					&& (keyEvent.getKeyCode() == iconKeyStroke.getKeyCode() && keyEvent.getKeyCode() != 0
							&& (iconKeyStroke.getModifiers() & InputEvent.SHIFT_MASK) == (keyEvent.getModifiers()
									& InputEvent.SHIFT_MASK)
							|| keyEvent.getKeyChar() == iconKeyStroke.getKeyChar())
					&& keyEvent.getKeyChar() != 0 && keyEvent.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Transfer shift masks from InputEvent to ActionEvent. But, why don't they use
	 * the same constants???? Java miracle.
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
		for (index = 0; index < currentIconLabels.length; index++) {
			if (caller == currentIconLabels[index]) {
				break;
			}
		}
		return new Position(index % xDimension, index / xDimension);
	}

	public int getResult() {
		return result;
	}

	private Position getSelectedPosition() {
		return selectedPosition;
	}

	private void highlight(final Position position) {
		currentIconLabels[calculateIndexInFilteredIconsList(position)].setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(0, 0, 0, 0), BorderFactory.createLineBorder(Color.RED, 2)));
	}

	/*
	 * (non-Javadoc)
	 * 
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
	 * 
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(final KeyEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(final KeyEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(final MouseEvent mouseEvent) {
		addIcon(mouseEvent.getModifiers());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(final MouseEvent arg0) {
		select(getPosition((JLabel) arg0.getSource()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(final MouseEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(final MouseEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(final MouseEvent arg0) {
	}

	private void select(final Position position) {
		unhighlight(getSelectedPosition());
		setSelectedPosition(position);
		highlight(position);
		final int index = calculateIndexInFilteredIconsList(position);
		final IIconInformation iconInformation = currentIcons.get(index);
		final String keyStroke = ResourceController.getResourceController()
				.getProperty(iconInformation.getShortcutKey());
		String positionLabel = "  Position (" + position.x + ", " + position.y + ")  Filtered IDX: "
				+ calculateIndexInFilteredIconsList(position) + "  AllItems IDX: " + calculateIndexInAllIconsList();
		if (keyStroke != null) {
			descriptionLabel.setText(iconInformation.getTranslatedDescription() + ", " + keyStroke + positionLabel);
		} else {
			descriptionLabel.setText(iconInformation.getTranslatedDescription() + positionLabel);
		}
	}

	private void setSelectedPosition(final Position position) {
		selectedPosition = position;
	}

	private void unhighlight(final Position position) {
		// iconLabels[calculateIndex(position)].setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		JLabel label = currentIconLabels[calculateIndexInFilteredIconsList(position)];
		if (label != null) {
			label.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
		}
	}
}
