package org.freeplane.features.fpsearch;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class FPSearchDialog extends JDialog implements DocumentListener, ListCellRenderer<Object>, MouseListener, KeyListener {
    private JTextField input;
    private JList<Object> resultList;

    private PreferencesIndexer preferencesIndexer;
    private MenuStructureIndexer menuStructureIndexer;

    FPSearchDialog(Frame parent)
    {
        super(parent,"Command search",false);

        setLocationRelativeTo(parent);

        loadSources();

        input = new JTextField("");
        input.addKeyListener(this);
        resultList = new JList<>();
        resultList.setCellRenderer(this);
        resultList.addMouseListener(this);
        resultList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.addKeyListener(this);
        JScrollPane resultListScrollPane = new JScrollPane(resultList);
        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(input, c);
        c.gridy = 1;
        getContentPane().add(resultListScrollPane, c);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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
        java.util.List<Object> matches = new ArrayList<Object>();

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
                prefsItem.text.toLowerCase(Locale.ENGLISH).contains(searchTerm.toLowerCase(Locale.ENGLISH)))
            {
                matches.add(prefsItem);
            }
        }
        Collections.sort(matches, (Object o1, Object o2) -> {
            final String s1 = (o1 instanceof MenuItem) ? ((MenuItem)o1).path : ((PreferencesItem)o1).text;
            final String s2 = (o2 instanceof MenuItem) ? ((MenuItem)o2).path : ((PreferencesItem)o2).text;
            return s1.compareTo(s2);
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

            FPSearchDialog fpSearchDialog = new FPSearchDialog(parent);
        });
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String text;
        if (value instanceof PreferencesItem)
        {
            text = "Pref: " + ((PreferencesItem)value).text;
        }
        else
        {
            MenuItem menuItem = (MenuItem) value;
            text = menuItem.path;
        }
        return new DefaultListCellRenderer().getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
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
                    }
                    updateMatches(input.getText());
                    resultList.revalidate();
                    resultList.repaint();
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
    }

    @Override
    public void keyPressed(KeyEvent e) {

        final boolean wrapAround = false;

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
        {
            dispose();
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN)
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
        else if (e.getKeyCode() == KeyEvent.VK_UP)
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

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
