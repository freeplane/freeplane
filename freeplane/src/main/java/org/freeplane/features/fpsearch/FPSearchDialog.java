package org.freeplane.features.fpsearch;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class FPSearchDialog extends JDialog implements DocumentListener, ListCellRenderer<Object>, MouseListener {
    private JTextField input;
    private JList<Object> resultList;

    private PreferencesIndexer preferencesIndexer;
    private MenuStructureIndexer menuStructureIndexer;

    FPSearchDialog(Frame parent)
    {
        super(parent,"Action search",false);

        setLocationRelativeTo(parent);

        loadSources();

        input = new JTextField("");
        resultList = new JList<>();
        resultList.setCellRenderer(this);
        resultList.addMouseListener(this);
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
        JLabel label;
        if (value instanceof PreferencesItem)
        {
            label = new JLabel(((PreferencesItem)value).text);
            label.setForeground(Color.GRAY);
        }
        else
        {
            label = new JLabel(((MenuItem)value).path);
            label.setForeground(Color.BLACK);
        }
        if (isSelected)
        {
            label.setOpaque(true);
            label.setBackground(Color.BLUE);
        }
        return label;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2)
        {
            int index = resultList.locationToIndex(e.getPoint());
            Object value = resultList.getModel().getElementAt(index);
            if (value instanceof PreferencesItem)
            {
                new ShowPreferenceItemAction((PreferencesItem)value).actionPerformed(null);
            }
            else
            {
                ((MenuItem)value).action.actionPerformed(null);
                // the list of enabled action might have changed:
                updateMatches(input.getText());
                resultList.revalidate();
            }
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
}
