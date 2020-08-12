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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.ShowPreferencesAction;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.svgicons.FreeplaneIconFactory;
import org.freeplane.core.util.TextUtils;


public class CommandSearchDialog extends JDialog implements DocumentListener, ListCellRenderer<Object>, MouseListener, KeyListener {

    public enum Scope{
        MENUS, PREFERENCES, ALL
    };

    JRadioButton searchMenus;
    JRadioButton searchPrefs;
    JRadioButton searchBoth;
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
        input.setColumns(40);
        input.addKeyListener(this);
        resultList = new JList<>();
        resultList.setCellRenderer(this);
        resultList.addMouseListener(this);
        resultList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.addKeyListener(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JScrollPane resultListScrollPane = new JScrollPane(resultList);
        getContentPane().add(panel);

        ButtonGroup scopeGroup = new ButtonGroup();
        JPanel scopePanel = new JPanel();
        searchMenus = new JRadioButton("dummy");
        LabelAndMnemonicSetter.setLabelAndMnemonic(searchMenus, TextUtils.getRawText("cmdsearch.menuitems_rb"));
        searchMenus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ResourceController.getResourceController().setProperty("cmdsearch_scope", Scope.MENUS.name());
                updateMatches(input.getText());
            }
        });
        searchPrefs = new JRadioButton("dummy");
        LabelAndMnemonicSetter.setLabelAndMnemonic(searchPrefs, TextUtils.getRawText("cmdsearch.preferences_rb"));
        searchPrefs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ResourceController.getResourceController().setProperty("cmdsearch_scope", Scope.PREFERENCES.name());
                updateMatches(input.getText());
            }
        });
        searchBoth = new JRadioButton("dummy");
        LabelAndMnemonicSetter.setLabelAndMnemonic(searchBoth, TextUtils.getRawText("cmdsearch.both_rb"));
        searchBoth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ResourceController.getResourceController().setProperty("cmdsearch_scope", Scope.ALL.name());
                updateMatches(input.getText());
            }
        });
        scopeGroup.add(searchMenus);
        scopePanel.add(searchMenus);
        scopeGroup.add(searchPrefs);
        scopePanel.add(searchPrefs);
        scopeGroup.add(searchBoth);
        scopePanel.add(searchBoth);
        Box whatbox = Box.createVerticalBox();
        whatbox.add(scopePanel);
        whatbox.add(input);
        initScopeFromPrefs();

        panel.add(whatbox, BorderLayout.NORTH);
        panel.add(resultListScrollPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        input.setSize(new Dimension(300, 20));
        resultList.setSize(new Dimension(300, 240));
        //setDefaultText();
        //updateMatches(input.getText());
        pack();

        input.getDocument().addDocumentListener(this);
        input.requestFocus();

        setVisible(true);
    }

    private void loadSources()
    {
        preferencesIndexer = new PreferencesIndexer();
        menuStructureIndexer = new MenuStructureIndexer(false);
    }

    private void initScopeFromPrefs()
    {
        final ResourceController resourceController = ResourceController.getResourceController();
         Scope scope = resourceController.getEnumProperty("cmdsearch_scope", Scope.ALL);
         if (scope == Scope.MENUS)
         {
            searchMenus.setSelected(true);
         }
         else if (scope == Scope.PREFERENCES)
         {
             searchPrefs.setSelected(true);
         }
         else if (scope == Scope.ALL) {
             searchBoth.setSelected(true);
         }
    }

    private void setDefaultText()
    {
        input.setText("type to search for preferences/menu items");
        input.setSelectionStart(0);
        input.setSelectionEnd(input.getText().length());
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

    private void updateMatches(final String searchInput)
    {
        final String[] searchTerms = searchInput.trim().split("\\s+");
        for (int i = 0; i <searchTerms.length; i++)
        {
            searchTerms[i] = searchTerms[i].toLowerCase(Locale.ENGLISH);
        }

        //PseudoDamerauLevenshtein pairwiseAlignment = new PseudoDamerauLevenshtein();
        List<SearchItem> matches = new ArrayList<>();
        if(! searchTerms[0].isEmpty()) {
            if (searchMenus.isSelected() || searchBoth.isSelected())
            {
                gatherMenuItemMatches(searchTerms, matches);
            }
            if (searchPrefs.isSelected() || searchBoth.isSelected())
            {
                gatherPreferencesMatches(searchTerms, matches);
            }

            Collections.sort(matches);
        }
        resultList.setListData(new Object[0]);
        resultList.setListData(matches.toArray());
    }

    private boolean checkAndMatch(final String itemPath, final String[] searchTerms)
    {
        for (int i = 0; i < searchTerms.length; i++)
        {
            if (!itemPath.contains(searchTerms[i]))
            {
                return false;
            }
        }
        return true;
    }

    private void gatherMenuItemMatches(final String[] searchTerms, final java.util.List<SearchItem> matches)
    {
        for (final MenuItem menuItem :menuStructureIndexer.getMenuItems())
        {
            if (menuItem.action == null || !menuItem.action.isEnabled())
            {
                continue;
            }
            if (checkAndMatch(menuItem.path.toLowerCase(Locale.ENGLISH), searchTerms))
            {
                matches.add(menuItem);
            }
        }
    }

    private void gatherPreferencesMatches(final String[] searchTerms, final java.util.List<SearchItem> matches)
    {
        for (final PreferencesItem prefsItem: preferencesIndexer.getPrefs())
        {
            if (checkAndMatch(prefsItem.key.toLowerCase(Locale.ENGLISH), searchTerms) ||
                checkAndMatch(prefsItem.path.toLowerCase(Locale.ENGLISH), searchTerms))
            {
                matches.add(prefsItem);
            }
        }
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
