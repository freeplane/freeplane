/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.FocusManager;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;


/**
 * @author Dimitry Polivaev
 * Apr 23, 2011
 */
@SuppressWarnings("serial") 
public class ContainerMenuItem extends Box implements MenuElement{
	class MenuItemMouseListener extends MouseAdapter{
		public void mouseEntered(MouseEvent e) {
			ContainerMenuItem box = (ContainerMenuItem) SwingUtilities.getAncestorOfClass(ContainerMenuItem.class, e.getComponent());
			box.select();
        }
		
	}	public ContainerMenuItem(final String label, final JComponent component) {
        super(BoxLayout.X_AXIS);
		add(Box.createHorizontalStrut(10));
		final JLabel jlabel = new JLabel(label);
		add(jlabel);
		add(Box.createHorizontalStrut(10));
		final Component menuComponent;
		if(component instanceof JComboBox){
			final JComboBox box = (JComboBox) component;
			final JButton btn = new JButton(box.getSelectedItem().toString());
			btn.setMinimumSize(new Dimension(0,0));
			btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
			btn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JPopupMenu popup =  (JPopupMenu) MenuSelectionManager.defaultManager().getSelectedPath()[0];
					final Frame frame = UITools.getFrame();
					final JDialog d = new JDialog(frame, label);
					d.setModal(false);
					d.add(box);
					d.pack();
					final Point locationOnScreen = btn.getLocationOnScreen();
					d.setLocation(locationOnScreen);
					box.addKeyListener(new KeyListener() {
						public void keyTyped(KeyEvent e) {
						}
						
						public void keyReleased(KeyEvent e) {
						}
						
						public void keyPressed(KeyEvent e) {
							if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
								d.setVisible(false);
						}
					});
					box.addItemListener(new ItemListener() {
						public void itemStateChanged(ItemEvent e) {
							d.setVisible(false);
						}
					});
					popup.setVisible(false);
					d.setVisible(true);
					d.addWindowFocusListener(new WindowFocusListener() {
						public void windowLostFocus(WindowEvent e) {
						}
						
						public void windowGainedFocus(WindowEvent e) {
							frame.addWindowFocusListener(new WindowFocusListener() {
								public void windowLostFocus(WindowEvent e) {
								}
								
								public void windowGainedFocus(WindowEvent e) {
									d.setVisible(false);
									frame.removeWindowFocusListener(this);
								}
							});
							d.removeWindowFocusListener(this);
						}
					});
				}
			});
			menuComponent = btn;
		}
		else
			menuComponent = component;
		add(menuComponent);
		UITools.addMousesListenerToDescendants(menuComponent, new MenuItemMouseListener());
   }

	public void processMouseEvent(MouseEvent event, MenuElement[] path, MenuSelectionManager manager) {
    }

	public void processKeyEvent(KeyEvent event, MenuElement[] path, MenuSelectionManager manager) {
    }

	public void menuSelectionChanged(boolean isIncluded) {
		final Component focusOwner = FocusManager.getCurrentManager().getFocusOwner();
		if(! isIncluded && focusOwner != null && SwingUtilities.isDescendingFrom(focusOwner, this)){
			requestFocus();
		}
    }

	public MenuElement[] getSubElements() {
        return new MenuElement[]{};
    }

	public Component getComponent() {
        return this;
    }

	public void select() {
		MenuSelectionManager msm = MenuSelectionManager.defaultManager();
		if (msm.isComponentPartOfCurrentMenu(this)){
			MenuElement path[] = new MenuElement[2];
			path[0] = msm.getSelectedPath()[0];
			path[1] = this;
			msm.setSelectedPath(path);        
		 }
    }
}