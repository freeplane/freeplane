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
import java.awt.Point;
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

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.IconDescription;
import org.freeplane.features.icon.factory.IconFactory;

public class IconSelectionPopupDialog extends JDialog implements KeyListener, MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private JLabel descriptionLabel;
	final private List<JLabel> iconLabels;
	final private JPanel iconPanel = new JPanel();
	final private List<? extends IconDescription> icons;
	private int mModifiers;
	final private int numOfIcons;
	private int result;
	private JLabel selected;

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
        final int singleIconSize = (int) ((IconFactory.DEFAULT_UI_ICON_HEIGTH.toBaseUnits()+ 0.5) * 1.1);
        int xDimension = Math.min(20, (int) Math.ceil(Math.sqrt(numOfIcons)) * 16 / 9);
        final ToolbarLayout layout = ToolbarLayout.vertical();
        layout.setMaximumWidth(Math.min(singleIconSize * xDimension, UITools.getScreenBounds(frame.getGraphicsConfiguration()).width * 4 / 5));
        
		iconPanel.setLayout(layout);
		iconLabels = new ArrayList<>(numOfIcons);
		for (int i = 0; i < numOfIcons; ++i) {
			final IconDescription icon = icons.get(i);
			JLabel label = new JLabel(icon.getActionIcon());
			iconLabels.add(label);
            iconPanel.add(label);
            label.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            label.addMouseListener(this);
		}
		Dimension preferredSize = iconPanel.getPreferredSize();
		JScrollPane scrollPane = new JScrollPane(iconPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
		        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(preferredSize.width, preferredSize.width / 2));
        getContentPane().add(scrollPane, BorderLayout.CENTER);
		descriptionLabel = new JLabel(" ");
		getContentPane().add(descriptionLabel, BorderLayout.SOUTH);
		JLabel firstIcon = iconLabels.get(0);
        final JLabel label = firstIcon;
        selected = label;
		highlight(firstIcon);
		addKeyListener(this);
		pack();
	}

	private void addIcon(final int pModifiers) {
		result =  iconLabels.indexOf(selected);
		mModifiers = pModifiers;
		this.dispose();
	}

	private int findIndex(final Point location) {
		for(int i = 0; i < iconLabels.size(); i++) {
		    JLabel label = iconLabels.get(i);
		    if(label.getBounds().contains(location))
		        return i;
		}
		return -1;
	}

	private void close() {
		result = -1;
		mModifiers = 0;
		this.dispose();
	}

	private void cursorDown() {
		final Point newPosition = new Point(selected.getX(), selected.getY() + selected.getWidth() + 1);
		int newIndex = findIndex(newPosition);
		if (newIndex >= 0) {
			select(newIndex);
		}
	}

	private void cursorLeft() {
	    int selectedIndex = iconLabels.indexOf(selected) - 1;
	    if(selectedIndex >= 0)
	        select(selectedIndex);
	}

	private void cursorRight() {
        int selectedIndex = iconLabels.indexOf(selected) + 1;
        if(selectedIndex < iconLabels.size())
            select(selectedIndex);
	}

	private void cursorUp() {
        final Point newPosition = new Point(selected.getX(), selected.getY() - 1);
        int newIndex = findIndex(newPosition);
        if (newIndex >= 0) {
            select(newIndex);
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

	public int getResult() {
		return result;
	}

    private JLabel findLabel(final Point location) {
        return iconLabels.get(findIndex(location));
    }

    private void highlight(JLabel label) {
        label.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    }

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	@Override
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
	@Override
    public void keyReleased(final KeyEvent arg0) {/**/}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	@Override
    public void keyTyped(final KeyEvent arg0) {/**/}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
    public void mouseClicked(final MouseEvent mouseEvent) {
		addIcon(mouseEvent.getModifiers());
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
    public void mouseEntered(final MouseEvent arg0) {
		select(((JLabel) arg0.getSource()).getLocation());
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
    public void mouseExited(final MouseEvent arg0) {/**/}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
    public void mousePressed(final MouseEvent arg0) {/**/}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
    public void mouseReleased(final MouseEvent arg0) {/**/}

	private void select(final Point location) {
	    int index = findIndex(location);
		select(index);
	}

    private void select(final int index) {
        unhighlight(this.selected);
        JLabel newSelected = iconLabels.get(index);
        this.selected = newSelected;
		highlight(newSelected);
		final IconDescription iconInformation = icons.get(index);
		final String keyStroke = ResourceController.getResourceController().getProperty(iconInformation.getShortcutKey());
		if (keyStroke != null) {
			descriptionLabel.setText(iconInformation.getTranslatedDescription() + ", " + keyStroke);
		}
		else {
			descriptionLabel.setText(iconInformation.getTranslatedDescription());
		}
    }

	private void unhighlight(final JLabel label) {
	    label.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
	}
}
