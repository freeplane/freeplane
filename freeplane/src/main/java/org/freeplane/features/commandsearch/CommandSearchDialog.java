/*
 *  Freeplane - mind map editor
 *
 *  Copyright (C) 2020 Felix Natter, Dimitry Polivaev
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
package org.freeplane.features.commandsearch;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.ShowPreferencesAction;
import org.freeplane.core.ui.svgicons.FreeplaneIconFactory;
import org.freeplane.core.util.TextUtils;


public class CommandSearchDialog extends JDialog implements DocumentListener, ListCellRenderer<Object>, MouseListener, KeyListener {
    private JTextField input;
    private JList<Object> resultList;
    private ImageIcon prefsIcon;
    private ImageIcon menuIcon;

    private PreferencesIndexer preferencesIndexer;
    private MenuStructureIndexer menuStructureIndexer;

    CommandSearchDialog(Frame parent)
    {
        super(parent, TextUtils.getText("CommandSearchAction.text"),false);

        setLocationRelativeTo(parent);

        prefsIcon = FreeplaneIconFactory.toImageIcon(ResourceController.getResourceController().getIcon(ShowPreferencesAction.KEY + ".icon"));
        menuIcon = FreeplaneIconFactory.toImageIcon(ResourceController.getResourceController().getIcon("/images/menu_items.svg"));

        loadSources();

        input = new JTextField("");
        input.setColumns(30);
        input.addKeyListener(this);
        resultList = new JList<>();
        resultList.setCellRenderer(this);
        resultList.addMouseListener(this);
        resultList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.addKeyListener(this);
        Box box = Box.createVerticalBox();
        JScrollPane resultListScrollPane = new JScrollPane(resultList);
        getContentPane().add(box);
        box.add(input);
        box.add(resultListScrollPane);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        input.setSize(new Dimension(200, 20));
        resultList.setSize(new Dimension(200, 240));
        //updateMatches(input.getText());
        pack();

        input.getDocument().addDocumentListener(this);

        setVisible(true);
    }

    private void loadSources()
    {
        preferencesIndexer = new PreferencesIndexer();
        menuStructureIndexer = new MenuStructureIndexer();
    }

    public void changedUpdate(DocumentEvent e) {
        updateMatches(input.getText());
    }
    public void removeUpdate(DocumentEvent e) {
        updateMatches(input.getText());
    }
    public void insertUpdate(DocumentEvent e) {
        updateMatches(input.getText());
    }

    private void updateMatches(final String searchTerm)
    {
        resultList.setListData(new Object[0]);

        //PseudoDamerauLevenshtein pairwiseAlignment = new PseudoDamerauLevenshtein();
        java.util.List<Object> matches = new ArrayList<>();

        for (final MenuItem menuItem :menuStructureIndexer.getMenuItems())
        {
            if (menuItem.action == null || !menuItem.action.isEnabled())
            {
                continue;
            }
            if (menuItem.path.toLowerCase(Locale.ENGLISH).contains(searchTerm.toLowerCase(Locale.ENGLISH)))
            {
                matches.add(menuItem);
            }
        }
        for (final PreferencesItem prefsItem: preferencesIndexer.getPrefs())
        {
            if (prefsItem.key.contains(searchTerm.toLowerCase(Locale.ENGLISH)) ||
                prefsItem.path.toLowerCase(Locale.ENGLISH).contains(searchTerm.toLowerCase(Locale.ENGLISH)))
            {
                matches.add(prefsItem);
            }
        }
        Collections.sort(matches, (Object o1, Object o2) -> {
            final String s1 = (o1 instanceof MenuItem) ? ((MenuItem)o1).path : ((PreferencesItem)o1).text;
            final String s2 = (o2 instanceof MenuItem) ? ((MenuItem)o2).path : ((PreferencesItem)o2).text;
            return s1.compareToIgnoreCase(s2);
        });
        resultList.setListData(matches.toArray());
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> {
            JFrame parent = new JFrame("FP Search Dialog Test");
            parent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            parent.setPreferredSize(new Dimension(800, 600));
            parent.pack();

            CommandSearchDialog commandSearchDialog = new CommandSearchDialog(parent);
        });
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String text;
        Icon icon;
        String tooltip;
        if (value instanceof PreferencesItem)
        {
            PreferencesItem prefsItem = (PreferencesItem)value;
            text = prefsItem.path;
            icon = prefsIcon;
            tooltip = prefsItem.tooltip;
        }
        else
        {
            MenuItem menuItem = (MenuItem) value;
            text = menuItem.path;
            //icon = ResourceController.getResourceController().getIcon(menuItem.action.getIconKey());
            icon = menuIcon;
            //tooltip = TextUtils.getText(menuItem.action.getTooltipKey());
            tooltip = menuItem.accelerator;
        }
        JLabel label = (JLabel)(new DefaultListCellRenderer().getListCellRendererComponent(list, text, index, isSelected, cellHasFocus));
        if (icon != null)
        {
            label.setIcon(icon);
        }
        if (tooltip != null)
        {
            label.setToolTipText(tooltip);
        }
        return label;
    }

    private void executeItem(int index)
    {
        Object value = resultList.getModel().getElementAt(index);
        if (value instanceof PreferencesItem)
        {
            new ShowPreferenceItemAction((PreferencesItem)value).actionPerformed(null);
        }
        else
        {
            ((MenuItem)value).action.actionPerformed(null);
            // the list of enabled actions might have changed:
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try { Thread.sleep(200); }
                    catch(InterruptedException e) {
                        // nothing to do
                    }
                    updateMatches(input.getText());
                    resultList.revalidate();
                    resultList.repaint();
                    // restore selection if possible
                    if (index < resultList.getModel().getSize())
                    {
                        resultList.setSelectedIndex(index);
                        resultList.ensureIndexIsVisible(index);
                    }
                }
            }).start();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2)
        {
            int index = resultList.locationToIndex(e.getPoint());
            executeItem(index);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getSource() == resultList)
        {
            input.setText(input.getText() + e.getKeyChar());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

        final boolean wrapAround = false;

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
        {
            dispose();
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN && e.getSource() == input)
        {
            if (resultList.getModel().getSize() > 0) {
                int selectedIndex = resultList.getSelectedIndex();
                int newIndex = selectedIndex + 1;
                if (newIndex >= resultList.getModel().getSize())
                {
                    newIndex = wrapAround ? (0) : (resultList.getModel().getSize() - 1);
                }
                resultList.setSelectedIndex(newIndex);
                resultList.ensureIndexIsVisible(newIndex);
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_UP && e.getSource() == input)
        {
            if (resultList.getModel().getSize() > 0) {
                int selectedIndex = resultList.getSelectedIndex();
                if (selectedIndex == -1)
                {
                    resultList.setSelectedIndex(0);
                }
                else if (selectedIndex == 0 && wrapAround)
                {
                    resultList.setSelectedIndex(resultList.getModel().getSize() - 1);
                }
                else
                {
                    resultList.setSelectedIndex(selectedIndex - 1);
                }
                resultList.ensureIndexIsVisible(resultList.getSelectedIndex());
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_ENTER)
        {
            if (resultList.getSelectedIndex() >= 0)
            {
                executeItem(resultList.getSelectedIndex());
            }
        }
    }
}
