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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JCheckBox;
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
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.WindowConfigurationStorage;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.util.TextUtils;


public class CommandSearchDialog extends JDialog 
    implements DocumentListener, ListCellRenderer<Object>, MouseListener, KeyListener {

    private static final String LIMIT_EXCEEDED_MESSAGE = TextUtils.getText("cmdsearch.limit_exceeded");
    private static final Icon WARNING_ICON = ResourceController.getResourceController().getIcon("/images/icons/messagebox_warning.svg");
    private static final int LIMIT_EXCEEDED_RANK = 100;
    private static final String WINDOW_CONFIG_PROPERTY = "cmdsearch_window_configuration";

    public enum Scope{
        MENUS, PREFERENCES, ICONS, ALL
    }

    private final JRadioButton searchMenus;
    private final JRadioButton searchPrefs;
    private final JRadioButton searchIcons;
    private final JRadioButton searchAll;
    private final JTextField input;
    private final JList<Object> resultList;
    private final JCheckBox closeAfterExecute;
    private final JCheckBox searchWholeWords;

    private final PreferencesIndexer preferencesIndexer;
    private final MenuStructureIndexer menuStructureIndexer;
    private final IconIndexer iconIndexer;

    CommandSearchDialog(Frame parent)
    {
        super(parent, TextUtils.getText("CommandSearchAction.text"),false);

        setLocationRelativeTo(parent);

        preferencesIndexer = new PreferencesIndexer();
        menuStructureIndexer = new MenuStructureIndexer();
        iconIndexer = new IconIndexer();

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
        searchMenus = createScopeButton(Scope.MENUS, "cmdsearch.menuitems_rb");
        searchPrefs = createScopeButton(Scope.PREFERENCES, "cmdsearch.preferences_rb");
        searchIcons = createScopeButton(Scope.ICONS, "cmdsearch.icons_rb");
        searchAll = createScopeButton(Scope.ALL, "cmdsearch.all_rb");
        scopeGroup.add(searchMenus);
        scopePanel.add(searchMenus);
        scopeGroup.add(searchIcons);
        scopePanel.add(searchIcons);
        scopeGroup.add(searchPrefs);
        scopePanel.add(searchPrefs);
        scopeGroup.add(searchAll);
        scopePanel.add(searchAll);
        searchWholeWords = new JCheckBox();
        LabelAndMnemonicSetter.setLabelAndMnemonic(searchWholeWords, TextUtils.getRawText("cmdsearch.searchWholeWords"));
        searchWholeWords.setSelected(ResourceController.getResourceController().getBooleanProperty("cmdsearch_whole_words"));
        searchWholeWords.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ResourceController.getResourceController().setProperty("cmdsearch_whole_words", searchWholeWords.isSelected());
                updateMatches(input.getText());
                input.requestFocusInWindow();
            }
        });
        
        Box whatbox = Box.createVerticalBox();
        whatbox.add(scopePanel);
        whatbox.add(searchWholeWords);
        whatbox.add(input);
        initScopeFromPrefs();

        panel.add(whatbox, BorderLayout.NORTH);
        panel.add(resultListScrollPane, BorderLayout.CENTER);

        Box optionsBox = Box.createVerticalBox();
        closeAfterExecute = new JCheckBox();
        LabelAndMnemonicSetter.setLabelAndMnemonic(closeAfterExecute, TextUtils.getRawText("cmdsearch.closeAfterExecute"));
        closeAfterExecute.setSelected(ResourceController.getResourceController().getBooleanProperty("cmdsearch_close_after_execute"));
        closeAfterExecute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ResourceController.getResourceController().setProperty("cmdsearch_close_after_execute", closeAfterExecute.isSelected());
                updateMatches(input.getText());
                input.requestFocusInWindow();
            }
        });
        optionsBox.add(closeAfterExecute);

        panel.add(optionsBox, BorderLayout.SOUTH);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        input.setColumns(40);
        resultList.setVisibleRowCount(20);

        final WindowConfigurationStorage windowConfigurationStorage = new WindowConfigurationStorage(WINDOW_CONFIG_PROPERTY);
        if (ResourceController.getResourceController().getProperty(WINDOW_CONFIG_PROPERTY) != null) {
            windowConfigurationStorage.restoreDialogPositions(this);
        } else
        {
            pack();
        }

        input.getDocument().addDocumentListener(this);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                windowConfigurationStorage.storeDialogPositions(CommandSearchDialog.this);
            }
        });
        
        this.addWindowFocusListener(new WindowFocusListener() {
			
			@Override
			public void windowLostFocus(WindowEvent e) {
			}
			
			@Override
			public void windowGainedFocus(WindowEvent e) {
				input.requestFocusInWindow();
				removeWindowFocusListener(this);
			}
		});

        setVisible(true);
    }

    private JRadioButton createScopeButton(Scope scope, String labelKey) {
        JRadioButton searchPrefs = new JRadioButton();
        LabelAndMnemonicSetter.setLabelAndMnemonic(searchPrefs, TextUtils.getRawText(labelKey));
        searchPrefs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ResourceController.getResourceController().setProperty("cmdsearch_scope", scope.name());
                updateMatches(input.getText());
                input.requestFocusInWindow();
            }
        });
        return searchPrefs;
    }


    private void initScopeFromPrefs()
    {
        final ResourceController resourceController = ResourceController.getResourceController();
         Scope scope = resourceController.getEnumProperty("cmdsearch_scope", Scope.ALL);
         switch (scope) {
         case MENUS:
             searchMenus.setSelected(true);
             break;

         case PREFERENCES:
             searchPrefs.setSelected(true);
             break;

         case ICONS:
             searchIcons.setSelected(true);
             break;

         case ALL:
             searchAll.setSelected(true);
             break;

        default:
            break;
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
        String trimmedInput = searchInput.trim();

        //PseudoDamerauLevenshtein pairwiseAlignment = new PseudoDamerauLevenshtein();
        List<SearchItem> matches = new ArrayList<>();
        boolean shouldSearchWholeWords =  ResourceController.getResourceController().getBooleanProperty("cmdsearch_whole_words");
        ItemChecker textChecker = new ItemChecker(shouldSearchWholeWords);

		if(trimmedInput.length() >= 1 
                && (searchInput.length() >= 3
                    || searchInput.endsWith(" ")
                    || shouldSearchWholeWords)
                ) {
            final String[] searchTerms = trimmedInput.split("\\s+");
            for (int i = 0; i <searchTerms.length; i++)
            {
                searchTerms[i] = searchTerms[i].toLowerCase(Locale.ENGLISH);
            }
            if (searchMenus.isSelected() || searchAll.isSelected())
            {
            	textChecker.findMatchingItems(menuStructureIndexer.getMenuItems(), searchTerms, matches::add);
            }
            if (searchPrefs.isSelected() || searchAll.isSelected())
            {
            	textChecker.findMatchingItems(preferencesIndexer.getPrefs(), searchTerms, matches::add);
            }
            if (searchIcons.isSelected() || searchAll.isSelected())
            {
            	textChecker.findMatchingItems(iconIndexer.getIconItems(), searchTerms, matches::add);
            }

            Collections.sort(matches);
        }
        int itemLimit = ResourceController.getResourceController().getIntProperty("cmdsearch_item_limit");
        if(matches.size() > itemLimit) {
            matches = matches.subList(0, itemLimit);
            matches.add(new InformationItem(LIMIT_EXCEEDED_MESSAGE, WARNING_ICON, LIMIT_EXCEEDED_RANK));
        }
        resultList.setListData(new Object[0]);
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

        SearchItem item = (SearchItem)value;

        String text = item.getDisplayedText();
        Icon icon = item.getTypeIcon();
        String tooltip = item.getTooltip();

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
        SearchItem item = (SearchItem)(resultList.getModel().getElementAt(index));

        boolean updateNecessary = item.execute();

        if (closeAfterExecute.isSelected())
        {
            dispose();
        }

        if (updateNecessary) {
            // the list of enabled actions might have changed:
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        // nothing to do
                    }
                    updateMatches(input.getText());
                    resultList.revalidate();
                    resultList.repaint();
                    // restore selection if possible
                    if (index < resultList.getModel().getSize()) {
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
