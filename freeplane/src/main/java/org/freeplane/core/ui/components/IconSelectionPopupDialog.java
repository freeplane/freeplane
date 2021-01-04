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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.IconDescription;
import org.freeplane.features.icon.factory.IconFactory;

public class IconSelectionPopupDialog extends JDialog implements KeyListener, MouseListener {

	private static final BevelBorder USUAL = new BevelBorder(BevelBorder.RAISED);
    private static final BevelBorder HIGHLIGHTED =  new BevelBorder(BevelBorder.LOWERED, Color.CYAN,
            Color.CYAN);
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static String lastSearchText = "";
	final private JLabel descriptionLabel;
	final private List<JLabel> iconLabels;
	final private JPanel iconPanel = new JPanel();
	final private List<? extends IconDescription> icons;
	private final JTextField filterTextField;
	private int mModifiers;
	final private int numOfIcons;
	private int result;
	private JLabel selected;

	public IconSelectionPopupDialog(final Frame frame, final List<? extends IconDescription> icons) {
		super(frame, TextUtils.getText("select_icon"));
		Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
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
			label.putClientProperty(IconDescription.class, icon);
			iconLabels.add(label);
            iconPanel.add(label);
            label.setBorder(USUAL);
            label.addMouseListener(this);
		}
		Dimension preferredSize = iconPanel.getPreferredSize();
		JScrollPane scrollPane = new JScrollPane(iconPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
		        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(preferredSize.width, preferredSize.width / 2));
		filterTextField = setupFilterTextField_and_KeyListener();
		addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowLostFocus(WindowEvent e) {
            }
            
            @Override
            public void windowGainedFocus(WindowEvent e) {
                filterTextField.requestFocusInWindow();
                removeWindowFocusListener(this);
            }
        });
        contentPane.add(filterTextField, BorderLayout.NORTH);
        contentPane.add(scrollPane, BorderLayout.CENTER);
		descriptionLabel = new JLabel(" ");
		contentPane.add(descriptionLabel, BorderLayout.SOUTH);
		JLabel firstIcon = iconLabels.get(0);
        final JLabel label = firstIcon;
        selected = label;
		highlight(firstIcon);
		addKeyListener(this);
		pack();
	}
	
	   private JTextField setupFilterTextField_and_KeyListener() {
	        JTextField filterTextField = new JTextField();
	        filterTextField.setText(lastSearchText);
	        filterTextField.getDocument().addDocumentListener(new DocumentListener() {
                
                @Override
                public void removeUpdate(DocumentEvent e) {
                    filterIcons();
                }
                
                @Override
                public void insertUpdate(DocumentEvent e) {
                    filterIcons();
                }
                
                @Override
                public void changedUpdate(DocumentEvent e) {
                    filterIcons();
                }
            });
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
	                        iconPanel.revalidate();
	                        iconPanel.repaint();
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

	            for (JLabel label : iconLabels) {
	                boolean matches = false;
                    for (String tag : getTags(label)) {
                        if (filterText.startsWith("/")) {
                            matches = regex.matcher(tag).matches();
                        } else {
                            if (filterText.contains(" ")) {
                                matches = true;
                                StringTokenizer tokenizer = new StringTokenizer(filterText);
                                while (tokenizer.hasMoreTokens()) {
                                    String token = tokenizer.nextToken();
                                    matches = matches && tag.contains(token);
                                    if (!matches) {
                                        break;
                                    }
                                }
                            } else {
                                matches = tag.contains(filterText);
                            }
                            if(matches)
                                break;
                        }
                    }
                    label.setVisible(matches);
	            }
	        }
	        else {
	            for (Component component : iconPanel.getComponents()) {
	                component.setVisible(true);
	            }
	        }
	        if(selected == null && ! selected.isVisible()) {
	            select(new Point(0, 0));
	            if(selected != null)
	                selected.scrollRectToVisible(new Rectangle());
	        }
	    }

        private String[] getTags(JLabel label) {
            IconDescription iconDescription = (IconDescription) label.getClientProperty(IconDescription.class);
            String iconLabel = iconDescription.getTranslatedDescription();
            if (iconLabel.startsWith("icon_")) {
                iconLabel = iconLabel.substring(5);
            }
            return new String[] {iconLabel.toLowerCase(), iconDescription.getFile().toLowerCase()};
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
	    if(selected == null)
	        return;
		final Point newPosition = new Point(selected.getX(), selected.getY() + selected.getWidth() + 1);
		int newIndex = findIndex(newPosition);
		if (newIndex >= 0) {
			select(newIndex);
		}
	}

	private void cursorLeft() {
        if(selected == null)
            return;
        final Point newPosition = new Point(selected.getX() - 1, selected.getY());
        int newIndex = findIndex(newPosition);
        if (newIndex >= 0) {
            select(newIndex);
        }
	}

	private void cursorRight() {
        if(selected == null)
            return;
        final Point newPosition = new Point(selected.getX() + selected.getHeight() + 1, selected.getY());
        int newIndex = findIndex(newPosition);
        if (newIndex >= 0) {
            select(newIndex);
        }
	}

	private void cursorUp() {
        if(selected == null)
            return;
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
        label.setBorder(HIGHLIGHTED);
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
	    final String message;
	    if(index >= 0) {
	        JLabel newSelected = iconLabels.get(index);
	        this.selected = newSelected;
	        highlight(newSelected);
	        final IconDescription iconInformation = icons.get(index);
	        final String keyStroke = ResourceController.getResourceController().getProperty(iconInformation.getShortcutKey());
	        if (keyStroke != null) {
	            message = iconInformation.getTranslatedDescription() + ", " + keyStroke;
	        }
	        else {
	            message = iconInformation.getTranslatedDescription();
	        }
	    }
	    else {
	        this.selected = null;
	        message = "";
	    }
	    descriptionLabel.setText(message);
	}
	private void unhighlight(final JLabel label) {
	    label.setBorder(USUAL);
	}
}
