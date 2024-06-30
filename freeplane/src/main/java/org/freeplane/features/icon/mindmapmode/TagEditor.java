/*
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
package org.freeplane.features.icon.mindmapmode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.plaf.TableUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.JColorButton;
import org.freeplane.core.resources.components.ResponsiveFlowLayout;
import org.freeplane.core.ui.ActionAcceleratorManager;
import org.freeplane.core.ui.ColorTracker;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.AutoResizedTable;
import org.freeplane.core.ui.components.JFilterableComboBox;
import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.ui.components.TagIcon;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.CategorizedTag;
import org.freeplane.features.icon.Tag;
import org.freeplane.features.icon.TagCategories;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.EditorHolder;


class TagEditor {

    static class TagEditorHolder extends EditorHolder {

        public TagEditorHolder(NodeModel node, Window window) {
            super(node, window);
        }
    }

    private static class TagsWrapper extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private final ArrayList<Tag> tags;


        public TagsWrapper(ArrayList<Tag> tags) {
            super();
            this.tags = tags;
        }

        List<Tag> getCurrentTags(String initialSeparator, String newSeparator) {
            if(initialSeparator.equals(newSeparator))
                return tags;
            return tags.stream()
                    .map(tag -> tag.updateSeparator(initialSeparator, newSeparator))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        void insertEmptyTags(int first, int last) {
            for(int index = last; index >= first; index--)
                tags.add(first <= tags.size() ? first : tags.size(), Tag.EMPTY_TAG);
            fireTableRowsInserted(first, last);
         }

        void deleteTags(int first, int last) {
            int lastRemoved = last < tags.size() ? last : tags.size() - 1;
            int firstRemoved;
            if(last == tags.size() && first > 0 && getTag(first).isEmpty() && tags.get(first - 1).isEmpty())
                firstRemoved = first - 1;
            else
                firstRemoved = first;
            for(int index = lastRemoved; index >= firstRemoved; index--)
                tags.remove(index);
            fireTableRowsDeleted(firstRemoved, last);
         }

        @Override
        public int getRowCount() {
            return tags.size() + 1;
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return getTag(rowIndex);
        }

        Tag getTag(int index) {
            return index < tags.size() ? tags.get(index) : Tag.EMPTY_TAG;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if(columnIndex == 0) {
                Tag tag = (Tag)aValue;
                setTag(rowIndex, tag);
            }
        }

        void setTag(int index, Tag tag) {
            int tagCount = tags.size();
            if(index < tagCount) {
                tags.set(index, tag);
            }
            else {
                tags.add(tag);
                fireTableRowsInserted(tagCount + 1, tagCount + 1);
            }
            fireTableCellUpdated(index, 0);
        }

        @Override
        public String getColumnName(int column) {
            return "";
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public void sortTags(int first, int last) {
            if(first == 0 && last + 1 == tags.size())
                Collections.sort(tags);
            else
                Collections.sort(tags.subList(first, last + 1));
            fireTableRowsUpdated(first, last);
        }

        public void insertTag(int index, Tag tag) {
            if(index < 0)
                index = tags.size();
            tags.add(index, tag);
            fireTableRowsInserted(index, index);
        }

        public void moveTag(int oldIndex, int newIndex) {
            if(oldIndex != newIndex) {
                if(newIndex >= tags.size())
                    insertTag(tags.size(), Tag.EMPTY_TAG);
                Tag tag;
                if(oldIndex < tags.size())
                    tag = removeTag(oldIndex);
                else {
                    tag = Tag.EMPTY_TAG;
                }
                if(newIndex <= tags.size())
                    insertTag(newIndex, tag);
            }
        }

        public Tag removeTag(int index) {
            Tag tag = tags.remove(index);
            fireTableRowsDeleted(index, index);
            return tag;
        }
    }

    @SuppressWarnings("serial")
    class TableCellTransferHandler extends TransferHandler {
        @Override
        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            JTable table = (JTable) c;
            int[] rows = table.getSelectedRows();
            int col = table.getSelectedColumn();

            StringWriter writer = new StringWriter();

            for (int row : rows) {
                Object cellValue = table.getValueAt(row, col);
                if (cellValue instanceof Tag) {
                    final Tag tag = ((Tag) cellValue);
                    try {
                        TagCategories.writeTag(tag, writer);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    if (cellValue != null) {
                        writer.append(cellValue.toString());
                    }
                    writer.append(System.lineSeparator());
                }
            }
            return new TagSelection(writer.toString());
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport info) {
            return info.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport info) {
            if (!canImport(info)) {
                return false;
            }

            String data;
            final Transferable transferable = info.getTransferable();
            try {
                data = (String) transferable.getTransferData(
                        transferable.isDataFlavorSupported(TagSelection.tagFlavor)
                        ? TagSelection.tagFlavor
                        : DataFlavor.stringFlavor);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            JTable target = (JTable) info.getComponent();
            if (info.isDrop()) {
                JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
                int index = dl.getRow();
                insertData(target, data, index);
            } else {
                int index = target.getSelectedRow();
                if (index >= 0) {
                    insertData(target, data, index);
                } else {
                    return false;
                }
            }
            return true;
        }

        private void insertData(JTable target, String data, int index) {
            TableModel model = target.getModel();
            String[] rows = data.split(System.lineSeparator());
            for (String row : rows)
                ((TagsWrapper) model).insertTag(index++, createTagIfAbsent(row, true));
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            super.exportDone(source, data, action);
            if (action == MOVE) {
                deleteTags();
            }
        }
    }

    private static Map<String, CategorizedTag> getCategorizedTagsByContent(TagCategories source) {
        TreeMap<String, CategorizedTag> categorizedTagsByContent = new TreeMap<>();
        final String tagCategorySeparator = source.getTagCategorySeparator();
        source.categorizedTags()
            .forEach(tag -> categorizedTagsByContent.computeIfAbsent(tag.getContent(tagCategorySeparator), x -> tag));
        return categorizedTagsByContent;
    }



    private static final String WIDTH_PROPERTY = "tagDialog.width";
    private static final String HEIGHT_PROPERTY = "tagDialog.height";

    private final NodeModel node;
    private MIconController iconController;
    private JTable tagTable;
    private JDialog dialog;
    private final Map<String, CategorizedTag> qualifiedCategorizedTags;
    private final JColorButton colorButton;
    private final Action modifyColorAction;
    private final JTextField tagCategorySeparatorField;
    private TagCategories tagCategories;

	TagEditor(MIconController iconController, RootPaneContainer frame, NodeModel node){
        this.iconController = iconController;
        this.node = node;
        String title = TextUtils.getText("edit_tags") + " (" + TextController.getController().getShortPlainText(node) + ")";

        this.dialog = frame instanceof Frame ? new JDialog((Frame)frame, title, /*modal=*/false) : new JDialog((JDialog)frame, title, /*modal=*/false);
        final JButton okButton = new JButton();
        final JButton cancelButton = new JButton();
        final JButton sortButton = new JButton();
        modifyColorAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                modifyTagColor();
            }
        };
        colorButton = new JColorButton(modifyColorAction);
        colorButton.setColor(Tag.EMPTY_TAG.getColor());
        final JCheckBox enterConfirms = new JCheckBox("", ResourceController.getResourceController()
            .getBooleanProperty("el__enter_confirms_by_default"));
        LabelAndMnemonicSetter.setLabelAndMnemonic(okButton, TextUtils.getRawText("ok"));
        LabelAndMnemonicSetter.setLabelAndMnemonic(cancelButton, TextUtils.getRawText("cancel"));
        LabelAndMnemonicSetter.setLabelAndMnemonic(sortButton, TextUtils.getRawText("sort"));
        LabelAndMnemonicSetter.setLabelAndMnemonic(enterConfirms, TextUtils.getRawText("enter_confirms"));
        modifyColorAction.setEnabled(false);
        okButton.addActionListener(e -> {
            closeDialog();
            submit();
        });
        cancelButton.addActionListener(e -> closeDialog());
        sortButton.addActionListener(e -> sortSelectedTags());
        final TagCategories sourceCategories = getTagCategories();
        tagCategories = sourceCategories.copy();

        final JPanel buttonPane = new JPanel(new ResponsiveFlowLayout());
        tagCategorySeparatorField = new JTextField(10);
        tagCategorySeparatorField.setText(tagCategories.getTagCategorySeparator());
        buttonPane.add(TranslatedElementFactory.createLabel("OptionPanel.category_separator"));
        buttonPane.add(tagCategorySeparatorField);

        buttonPane.add(enterConfirms);
        buttonPane.add(okButton);
        buttonPane.add(cancelButton);
        buttonPane.add(colorButton);
        buttonPane.add(sortButton);
        buttonPane.setMaximumSize(new Dimension(1000, 20));
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        final Container contentPane = dialog.getContentPane();
        JRestrictedSizeScrollPane editorScrollPane = createScrollPane();

        qualifiedCategorizedTags = getCategorizedTagsByContent(tagCategories);

        List<Tag> originalNodeTags = iconController.getTags(node);

        qualifiedCategorizedTags.put("", CategorizedTag.EMPTY_TAG);

        tagTable = createTagTable(originalNodeTags);
        tagCategorySeparatorField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                updateTagCategorySeparator();
            }

        });
        ActionMap am = tagTable.getActionMap();
        JMenuBar menubar = new JMenuBar();
        JMenu editMenu = TranslatedElementFactory.createMenu("edit");
        JMenuItem addTagMenuItem = TranslatedElementFactory.createMenuItem("menu_addTag");
        addTagMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Event.SHIFT_MASK));
        addTagMenuItem.addActionListener(ev -> insertTags());
        editMenu.add(addTagMenuItem);

        JMenuItem removeMenuItem = TranslatedElementFactory.createMenuItem("menu_remove");
        removeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
        removeMenuItem.addActionListener(ev -> deleteTags());
        editMenu.add(removeMenuItem);

        JMenuItem copyMenuItem = TranslatedElementFactory.createMenuItem("menu_copy");
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        copyMenuItem.addActionListener(ev -> TransferHandler.getCopyAction().actionPerformed(toTableEvent(ev)));
        editMenu.add(copyMenuItem);

        JMenuItem cutMenuItem = TranslatedElementFactory.createMenuItem("CutAction.text");
        cutMenuItem.addActionListener(ev -> TransferHandler.getCutAction().actionPerformed(toTableEvent(ev)));
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        editMenu.add(cutMenuItem);

        JMenuItem pasteMenuItem = TranslatedElementFactory.createMenuItem("PasteAction.text");
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        pasteMenuItem.addActionListener(ev -> TransferHandler.getPasteAction().actionPerformed(toTableEvent(ev)));
        editMenu.add(pasteMenuItem);

        JMenuItem colorMenuItem = TranslatedElementFactory.createMenuItem("choose_tag_color");
        colorMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        colorMenuItem.addActionListener(modifyColorAction);
        editMenu.add(colorMenuItem);

        menubar.add(editMenu);

        JMenu insertMenu = TranslatedElementFactory.createMenu("insert");
        JMenuItem insertMenuItem = TranslatedElementFactory.createMenuItem("choose_tag_insert");
        insertMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        insertMenuItem.addActionListener(e -> insertSelectedTagsIntoSelectedNodes());
        editMenu.add(insertMenuItem);

        if(! tagCategories.isEmpty()) {
            insertMenu.addSeparator();
            insertMenu.add(iconController.createTagSubmenu("menu_tag",
                    sourceCategories,
                    tag -> getTableModel().insertTag(tagTable.getSelectedRow(),
                            tag.categorizedTag(getTagCategorySeparator()))));
        }
        menubar.add(insertMenu);
        dialog.setJMenuBar(menubar);

        editorScrollPane.setViewportView(tagTable);
        editorScrollPane.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                tagTable.revalidate();
                tagTable.repaint();
            }

        });
        enterConfirms.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                tagTable.requestFocus();
                ResourceController.getResourceController().setProperty("el__enter_confirms_by_default",
                    Boolean.toString(enterConfirms.isSelected()));
            }
        });
        tagTable.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(final KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                        e.consume();
                        closeDialog();
                        break;
                    case KeyEvent.VK_ENTER:
                        if((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 0) {

                            e.consume();
                            if (enterConfirms.isSelected() == ((e.getModifiers() & InputEvent.ALT_MASK) != 0)) {
                                insertTags();
                                break;
                            }
                            closeDialog();
                            submit();
                        }
                        break;
                    case KeyEvent.VK_DELETE:
                    case KeyEvent.VK_BACK_SPACE:
                        e.consume();
                        deleteTags();
                        break;
                }
            }
            @Override
            public void keyReleased(final KeyEvent e) {
            }

            @Override
            public void keyTyped(final KeyEvent e) {
            }
        });

        tagTable.getSelectionModel().addListSelectionListener(this::updateColorButton);
        tagTable.getModel().addTableModelListener(this::selectRowsAfterUpdate);
        tagTable.changeSelection(tagTable.getRowCount() - 1, 0, true, false);

        contentPane.add(editorScrollPane, BorderLayout.CENTER);
        final boolean areButtonsAtTheTop = ResourceController.getResourceController().getBooleanProperty("el__buttons_above");
        contentPane.add(buttonPane, areButtonsAtTheTop ? BorderLayout.NORTH : BorderLayout.SOUTH);
        node.addExtension(new TagEditorHolder(node, dialog));
        dialog.pack();
        restoreDialogSize(dialog);
        dialog.addComponentListener(new ComponentListener() {
            @Override
            public void componentShown(final ComponentEvent e) {
            }

            @Override
            public void componentResized(final ComponentEvent e) {
                saveDialogSize(dialog);
            }

            @Override
            public void componentMoved(final ComponentEvent e) {
            }

            @Override
            public void componentHidden(final ComponentEvent e) {
                node.removeExtension(TagEditorHolder.class);
                dialog.dispose();
            }
        });

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                if (dialog.isVisible()) {
                    confirmedSubmit();
                }
            }
        });
    }
    private ActionEvent toTableEvent(ActionEvent ev) {
        return new ActionEvent(tagTable, ev.getID(), ev.getActionCommand(), ev.getWhen(), ev.getModifiers());
    }
    private void restoreDialogSize(final JDialog dialog) {
        ResourceController resourceController = ResourceController.getResourceController();
        int width = resourceController.getIntProperty(WIDTH_PROPERTY, 0);
        int height = resourceController.getIntProperty(HEIGHT_PROPERTY, 0);
        if(width > 0 && height > 0)
            dialog.setSize(width, height);
    }
    private void closeDialog() {
        dialog.setVisible(false);
    }

    private void insertSelectedTagsIntoSelectedNodes() {
        final List<Tag> selectedTags = IntStream.of(tagTable.getSelectedRows())
        .mapToObj(row -> (Tag)tagTable.getValueAt(row, 0))
        .collect(Collectors.toList());
        iconController.insertTagsIntoSelectedNodes(selectedTags);
    }

    private void insertTags() {
        ListSelectionModel selectionModel = tagTable.getSelectionModel();
        int minSelectedRow = selectionModel.getMinSelectionIndex();
        if(minSelectedRow >= 0) {
            final int maxSelectionRow = selectionModel.getMaxSelectionIndex();
            final int count = maxSelectionRow - minSelectedRow + 1;
            getTableModel().insertEmptyTags(minSelectedRow + count, maxSelectionRow + count);
        }
    }
    private void modifyTagColor() {
        int selectedRow = tagTable.getSelectedRow();
        if(selectedRow < 0)
            return;
        final Tag modifiedCategorizedTag = (Tag) tagTable.getValueAt(selectedRow, 0);
        Tag modifiedTag = modifiedCategorizedTag;
        if(modifiedTag.isEmpty())
            return;

        Color defaultColor = new Color(modifiedTag.getDefaultColor().getRGB(), true);
        Color initialColor = modifiedTag.getColor();
        final Color result = ColorTracker.showCommonJColorChooserDialog(tagTable, modifiedTag.getContent(),
                initialColor, defaultColor);
        if(result != null && ! initialColor.equals(result)){
            modifiedTag.setColor(result);
            TagsWrapper tableModel = getTableModel();
            IntStream.range(0, tagTable.getRowCount() - 1)
            .filter(i -> tagTable.getValueAt(i, 0) == modifiedCategorizedTag)
            .forEach(i -> tableModel.fireTableCellUpdated(i, 0));
            updateColorButton();
        }

    }

    protected void sortSelectedTags() {
        ListSelectionModel selectionModel = tagTable.getSelectionModel();
        if(selectionModel.getMinSelectionIndex() < selectionModel.getMaxSelectionIndex())
            getTableModel().sortTags(selectionModel.getMinSelectionIndex(), selectionModel.getMaxSelectionIndex());
        else
            getTableModel().sortTags(0, tagTable.getRowCount() - 2);

    }
    void show() {
        Controller.getCurrentModeController().getController().getMapViewManager().scrollNodeToVisible(node);
        if (ResourceController.getResourceController().getBooleanProperty("el__position_window_below_node")) {
            UITools.setDialogLocationUnder(dialog, node);
        }
        else {
            UITools.setDialogLocationRelativeTo(dialog, node);
        }
        dialog.setVisible(true);
    }

    private boolean wasAnyValueModified() {
        final TagCategories tagCategories = getTagCategories();
        if(! tagCategorySeparatorField.getText().equals(tagCategories.getTagCategorySeparator())) {
            return true;
        }

        List<Tag> tags = getCurrentTags();

        List<Tag> originalNodeTags = iconController.getTags(node);
        Set<Tag> filteringSet = new HashSet<>();
        final List<Tag> newNodeTags = tags.stream()
                .filter(tag -> tag.isEmpty() || filteringSet.add(tag))
                .collect(Collectors.toList());
        if(originalNodeTags.size() != newNodeTags.size())
            return true;
        for(int i = 0; i < newNodeTags.size(); i++) {
            final Tag originalTag = originalNodeTags.get(i);
            final Tag newTag = newNodeTags.get(i);
            if(! originalTag.getContent().equals(newTag.getContent()))
                return true;
            if(! originalTag.getColor().equals(newTag.getColor()))
                return true;
        }
        return false;

    }

    protected void submit() {
        final MapModel map = node.getMap();
        final TagCategories tagCategories = getTagCategories().copy();
        final boolean isSeparatorUpdated = ! tagCategorySeparatorField.getText().equals(tagCategories.getTagCategorySeparator());
        if(isSeparatorUpdated) {
            tagCategories.updateTagCategorySeparator(tagCategorySeparatorField.getText());
            iconController.setTagCategories(map, tagCategories);
        }
        else if(tagCategories.areCategoriesChanged())
            iconController.setTagCategories(map, tagCategories);

        List<Tag> tags = getCurrentTags();
        iconController.setTags(node, tags.stream().collect(Collectors.toList()), true);
    }
    private TagCategories getTagCategories() {
        return node.getMap().getIconRegistry().getTagCategories();
    }


    private List<Tag> getCurrentTags() {
        return getTableModel().getCurrentTags(tagCategories.getTagCategorySeparator(), getTagCategorySeparator());
    }

    private JRestrictedSizeScrollPane createScrollPane() {
        final JRestrictedSizeScrollPane scrollPane = new JRestrictedSizeScrollPane();
        UITools.setScrollbarIncrement(scrollPane);
        scrollPane.setMinimumSize(new Dimension(0, 60));
        return scrollPane;
    }
    private Tag createTagIfAbsent(String spec, boolean specContainsColor) {
        return specContainsColor ? tagCategories.registerTag(spec) : tagCategories.registerTag(new Tag(spec));
    }

    private JTable createTagTable(List<Tag> tags) {
        JTable table = new AutoResizedTable(new TagsWrapper(new ArrayList<>(tags))) {



            @Override
			public void setUI(TableUI ui) {
				super.setUI(ui);
				Font tagFont = iconController.getTagFont(node);
				final Font font = tagFont.deriveFont(getFont().getSize2D());
                setFont(font);
				Rectangle2D rect = font.getStringBounds("*" , 0, 1,
		        		new FontRenderContext(new AffineTransform(), true, true));
		        double textHeight = rect.getHeight();
				setRowHeight((int)  Math.ceil(textHeight * 1.4));
			}

			@Override
            public boolean editCellAt(int row, int column, EventObject e) {
                if(super.editCellAt(row, column, e)){
                    final Component editorComponent = getEditorComponent();
                    if (editorComponent instanceof JComboBox) {
                        final ComboBoxEditor editor = ((JComboBox<?>)editorComponent).getEditor();
                        Component textFieldComponent = editor.getEditorComponent();
                        if(textFieldComponent instanceof JTextField) {
							JTextField textField = (JTextField)textFieldComponent;
							if(! textField.getText().isEmpty())
									textField.selectAll();
							SwingUtilities.invokeLater(textFieldComponent::requestFocusInWindow);
                        }
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void editingStopped(ChangeEvent e) {
                 super.editingStopped(e);

            }

            @Override
            public void editingCanceled(ChangeEvent e) {
                 super.editingCanceled(e);

            }
        };
        table.setTransferHandler(new TableCellTransferHandler());
        table.setDragEnabled(true);
        table.setDropMode(DropMode.ON);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object value) {
                if(value == null)
                    setIcon(null);
                else {
                    Tag tag =  (Tag)value;
                    final String initialSeparator = tagCategories.getTagCategorySeparator();
                    final String currentSeparator = getTagCategorySeparator();
                    setIcon(new TagIcon(tag.updateSeparator(initialSeparator, currentSeparator), table.getFont()));
                }
            }

        });

        @SuppressWarnings("serial")
        JFilterableComboBox<CategorizedTag> comboBox = new JFilterableComboBox<>(() -> qualifiedCategorizedTags.values(),
                (items, text) -> text.isEmpty() || qualifiedCategorizedTags.keySet().stream().anyMatch(item -> item.equals(text)),
                (item, text) -> item.getContent(getTagCategorySeparator()).toLowerCase().contains(text.toLowerCase()));

        @SuppressWarnings("serial")
        DefaultListCellRenderer cellRenderer = new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Icon icon;
                if(value == null)
                    icon = null;
                else {
                    CategorizedTag tag = (CategorizedTag)value;
                    icon = new TagIcon(tag.categorizedTag(getTagCategorySeparator()), table.getFont());
                }
                return super.getListCellRendererComponent(list, icon, index, isSelected, cellHasFocus);
            }};
        comboBox.setRenderer(cellRenderer);
        comboBox.setEditable(true);
        DefaultCellEditor cellEditor = new DefaultCellEditor(comboBox) {
            private static final long serialVersionUID = 1L;

            @Override
            public Object getCellEditorValue() {
                Object value = super.getCellEditorValue();
                if(value instanceof CategorizedTag)
                    return ((CategorizedTag)value).categorizedTag(getTagCategorySeparator());
                else
                    return createTagIfAbsent(value.toString(), false);
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                    boolean isSelected, int row, int column) {
                final String text = (value instanceof Tag) ? ((Tag)value).getContent() : value.toString();
                return super.getTableCellEditorComponent(table, text, isSelected, row, column);
            }

            @Override
            public boolean isCellEditable(EventObject anEvent) {
                if(anEvent instanceof MouseEvent)
                   return super.isCellEditable(anEvent);
               else if(anEvent instanceof KeyEvent) {
                KeyEvent keyEvent = (KeyEvent)anEvent;
                return ! keyEvent.isControlDown() && ! keyEvent.isMetaDown()
                        && (keyEvent.getKeyChar() != KeyEvent.CHAR_UNDEFINED || keyEvent.getKeyCode() == KeyEvent.VK_F2);
            } else
                   return false;
            }

            @Override
            public boolean stopCellEditing() {
                 return ! comboBox.isFilterRunning() && super.stopCellEditing();
            }


        };
        cellEditor.setClickCountToStart(2);
        table.getColumnModel().getColumn(0).setCellEditor(cellEditor);

        @SuppressWarnings("serial")
        Action moveSelectedLocationsUpAction = new AbstractAction("moveSelectedLocationsUp") {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveSelectedLocationsUp();
            }
        };

        @SuppressWarnings("serial")
        Action moveSelectedLocationsDownAction = new AbstractAction("moveSelectedLocationsDown") {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveSelectedLocationsDown();
            }
        };

        ActionMap actionMap = table.getActionMap();
        actionMap.put("moveSelectedLocationsUp", moveSelectedLocationsUpAction);
        actionMap.put("moveSelectedLocationsDown", moveSelectedLocationsDownAction);

        // Register keyboard shortcuts
        InputMap inputMap = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionAcceleratorManager acceleratorManager = ResourceController.getResourceController().getAcceleratorManager();
        inputMap.put(acceleratorManager.getAccelerator("NodeUpAction"), "moveSelectedLocationsUp");
        inputMap.put(acceleratorManager.getAccelerator("NodeDownAction"), "moveSelectedLocationsDown");
        return table;
    }

	private void saveDialogSize(final JDialog dialog) {
        ResourceController resourceController = ResourceController.getResourceController();
        resourceController.setProperty(WIDTH_PROPERTY, dialog.getWidth());
        resourceController.setProperty(HEIGHT_PROPERTY, dialog.getHeight());
    }


    private void confirmedSubmit() {
        if (dialog.isVisible()) {
            if (wasAnyValueModified()) {
                final int action = JOptionPane.showConfirmDialog(dialog, TextUtils.getText("long_node_changed_submit"), "",
                    JOptionPane.YES_NO_CANCEL_OPTION);

                if (action == JOptionPane.YES_OPTION) {
                    submit();
                }
                else if (action == JOptionPane.CANCEL_OPTION || action == JOptionPane.CLOSED_OPTION) {
                    return;
                }
            }
            closeDialog();
        }
    }

    private TagsWrapper getTableModel() {
        return (TagsWrapper)tagTable.getModel();
    }

    private void updateColorButton(ListSelectionEvent e) {
        if(!e.getValueIsAdjusting())
            updateColorButton();
    }

    private void selectRowsAfterUpdate(TableModelEvent e) {
        if(e.getType() == TableModelEvent.INSERT)
            EventQueue.invokeLater(() ->
                tagTable.getSelectionModel().setSelectionInterval(e.getFirstRow(), e.getLastRow()));
        else if(e.getType() == TableModelEvent.DELETE)
            EventQueue.invokeLater(() ->
            tagTable.getSelectionModel().setSelectionInterval(e.getFirstRow(), e.getFirstRow()));
        else if(e.getType() == TableModelEvent.UPDATE && e.getFirstRow() == tagTable.getSelectedRow()) {
            if(tagTable.getSelectedRow() <= tagTable.getRowCount() - 2) {
                tagTable.changeSelection(tagTable.getSelectedRow() + 1 , 0, false, false);
            }
            else
                EventQueue.invokeLater(this::updateColorButton);
        }
    }

    private void deleteTags() {
        ListSelectionModel selectionModel = tagTable.getSelectionModel();
        int minSelectedRow = selectionModel.getMinSelectionIndex();
        if(minSelectedRow >= 0) {
            getTableModel().deleteTags(minSelectedRow, selectionModel.getMaxSelectionIndex());
        }
    }

    private void updateColorButton() {
        int firstIndex = tagTable.getSelectedRow();
        if(firstIndex == -1) {
            return;
        }
        Tag tag = (Tag) tagTable.getValueAt(firstIndex, 0);
        if(tag.isEmpty()) {
            modifyColorAction.setEnabled(false);
            colorButton.setColor(Tag.EMPTY_TAG.getColor());
            return;
        }
        else {
            modifyColorAction.setEnabled(true);
            colorButton.setColor(tag.getColor());
        }
    }

    private void moveSelectedLocationsUp() {
        int[] selectedRows = tagTable.getSelectedRows();
        TagsWrapper tagTableModel = (TagsWrapper) tagTable.getModel();
        if (selectedRows.length > 0 && selectedRows[0] > 0) {
            for (int i = 0; i < selectedRows.length; i++) {
                int selectedIndex = selectedRows[i];
                tagTableModel.moveTag(selectedIndex, selectedIndex - 1);
            }
            SwingUtilities.invokeLater(() -> {
                tagTable.getSelectionModel().setValueIsAdjusting(true);
                tagTable.clearSelection();
                for (int selectedIndex : selectedRows) {
                    tagTable.addRowSelectionInterval(selectedIndex - 1, selectedIndex - 1);
                }
                tagTable.getSelectionModel().setValueIsAdjusting(false);
            });
        }
    }

    private void moveSelectedLocationsDown() {
        int[] selectedRows = tagTable.getSelectedRows();
        TagsWrapper tagTableModel = (TagsWrapper) tagTable.getModel();
        if (selectedRows.length > 0) {
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                int selectedIndex = selectedRows[i];
                tagTableModel.moveTag(selectedIndex, selectedIndex + 1);
            }
            SwingUtilities.invokeLater(() -> {
                tagTable.getSelectionModel().setValueIsAdjusting(true);
                tagTable.clearSelection();
                for (int selectedIndex : selectedRows) {
                    tagTable.addRowSelectionInterval(selectedIndex + 1, selectedIndex + 1);
                }
                tagTable.getSelectionModel().setValueIsAdjusting(false);
            });
        }
    }

    private String getTagCategorySeparator() {
        return tagCategorySeparatorField.getText();
    }
    private void updateTagCategorySeparator() {
        final String newTagCategorySeparator = getTagCategorySeparator();
        final String currentTagCategorySeparator = tagCategories.getTagCategorySeparator();
        if(! currentTagCategorySeparator.equals(newTagCategorySeparator)) {
            if (! newTagCategorySeparator.isEmpty()) {
                tagTable.tableChanged(new TableModelEvent(getTableModel()));
            }
            else
                tagCategorySeparatorField.setText(currentTagCategorySeparator);
        }
    }
}