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
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DropMode;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.JColorButton;
import org.freeplane.core.ui.ColorTracker;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.ui.components.TagIcon;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.CategorizedTagForCategoryNode;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.IconRegistry;
import org.freeplane.features.icon.Tag;
import org.freeplane.features.icon.TagCategories;
import org.freeplane.features.icon.TreeTagChangeListener;
import org.freeplane.features.map.MapModel;

class TagCategoryEditor implements IExtension {
    private static final int UUID_LENGTH = 36;

    private static final int TRANSFERABLE_ID_LENGTH = UUID_LENGTH + System.lineSeparator().length();

    @SuppressWarnings("serial")
    static class TagCellRenderer extends DefaultTreeCellRenderer {
        private Object rootNode; // Reference to the root node object

        public TagCellRenderer(DefaultMutableTreeNode rootNode) {
            this.rootNode = rootNode;
            setHorizontalAlignment(CENTER);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                boolean expanded, boolean leaf, int row, boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, null, sel, expanded, leaf, row, hasFocus);
            if (value instanceof DefaultMutableTreeNode) {
                Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
                if (userObject instanceof Tag) {
                    Tag tag = (Tag) userObject;

                    if (value != rootNode) {
                        setText(null);
                        setIcon(new TagIcon(tag, getFont())); // Example of
                                                              // setting
                                                              // a custom icon
                    } else {
                        setText(tag.getContent());
                    }
                } else if (userObject != null) {
                    setText(userObject.toString());
                }
            }

            return this;
        }
    }

    static class TagCellEditor extends AbstractCellEditor implements TreeCellEditor {

        private static final long serialVersionUID = 1L;

        private JTextField textField;

        private DefaultMutableTreeNode currentNode;

        private TagCategories registry;

        public TagCellEditor(TagCategories registry) {
            this.registry = registry;
            textField = new JTextField();
            textField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    stopCellEditing();
                }
            });

            textField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    stopCellEditing();
                }
            });
        }

        @Override
        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected,
                boolean expanded, boolean leaf, int row) {
            currentNode = (DefaultMutableTreeNode) value;
            Tag tag = (Tag) currentNode.getUserObject();
            String content = tag.getContent();
			textField.setText(content);
            textField.setColumns(Math.max(30, content.length()));
            return textField;
        }

        @Override
        public boolean isCellEditable(EventObject event) {
            if (event instanceof MouseEvent) {
                return ((MouseEvent) event).getClickCount() >= 2;
            }
            return true;
        }

        @Override
        public Object getCellEditorValue() {
            return registry.createTag(textField.getText());
        }

        @Override
        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }

        @Override
        public void cancelCellEditing() {
            fireEditingCanceled();
        }
    }

    @SuppressWarnings("serial")
    class TreeTransferHandler extends TransferHandler {
        private final TagCategories tagCategories;

        public TreeTransferHandler(TagCategories tagCategories) {
            this.tagCategories = tagCategories;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            if(c != tree)
                throw new IllegalArgumentException("Unexpected argument " + c);
            saveLastSelectionParentsNodes();
            JTree tree = (JTree) c;
            return createTransferable();
        }

        TagCategorySelection createTransferable() {
            try {
                final TreePath[] selectionPaths = getSelectionPaths();
                if(selectionPaths == null)
                    return null;
                lastTransferableId = UUID.randomUUID().toString();
                StringWriter tagCategoryWriter = new StringWriter();
                tagCategoryWriter.append(lastTransferableId).append(System.lineSeparator());
                StringWriter tagWriter = new StringWriter();
                for(TreePath path: selectionPaths) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    TagCategories.writeTagCategories(node, "", tagCategoryWriter);
                    TagCategories.writeTag(node, tagWriter);
                }
                TagCategorySelection stringSelection = new TagCategorySelection(tagCategoryWriter.toString(), tagWriter.toString());
                return stringSelection;
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            if (!support.isDrop()) {
                return false;
            }
            if (! support.isDataFlavorSupported(DataFlavor.stringFlavor))
                return false;

            // Get the drop location and component.
            JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
            JTree tree = (JTree) support.getComponent();
            TreePath target = dropLocation.getPath();

            // Get the node at the drop location.
            if (target != null) {
                DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) target.getLastPathComponent();
                DefaultMutableTreeNode sourceNode = (DefaultMutableTreeNode) tree
                        .getLastSelectedPathComponent();
                if (sourceNode ==targetNode || targetNode.isNodeAncestor(sourceNode)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            support.setShowDropLocation(true);
            JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
            int childIndex = dl.getChildIndex();
            TreePath dest = dl.getPath();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dest.getLastPathComponent();
            try {
                if (childIndex == -1) {
                    childIndex = parent.getChildCount();
                }
                insertTransferable(parent, childIndex, support.getTransferable());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            super.exportDone(source, data, action);
            if (action == MOVE) {
                removeNodes();
            }
        }

     }

    class TagRenamer implements TreeModelListener, TreeTagChangeListener<Tag>{
        private final List<String> oldTags = new ArrayList<>();
        private final List<String> newTags = new ArrayList<>();

        @Override
        public void treeNodesChanged(TreeModelEvent e) {/**/}

        @Override
        public void valueForPathChanged(TreePath path, Tag newTag) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            String commonPrefix = categorizedContent((DefaultMutableTreeNode) node.getParent());
            final Tag oldTag = (Tag) node.getUserObject();
            if(commonPrefix.isEmpty()) {
                oldTags.add(oldTag.getContent());
                newTags.add(newTag.getContent());
            }
            else {
                oldTags.add(commonPrefix + getTagCategorySeparator() +  oldTag.getContent());
                newTags.add(commonPrefix + getTagCategorySeparator() +  newTag.getContent());
            }
        }

        @Override
        public void treeNodesInserted(TreeModelEvent e) {
            Object[] insertedNodes = e.getChildren();
            if(lastSelectionParentsNodes.size() == insertedNodes.length) {
                for(int i = 0; i < insertedNodes.length; i++) {
                    final String oldParent = lastSelectionParentsNodes.get(i);
                    final DefaultMutableTreeNode insertedNode = (DefaultMutableTreeNode) insertedNodes[i];
                    final Tag newTag = (Tag) insertedNode.getUserObject();
                    if(oldParent.isEmpty())
                        oldTags.add(newTag.getContent());
                    else
                        oldTags.add(oldParent + getTagCategorySeparator() + newTag.getContent());
                    newTags.add(categorizedContent(insertedNode));
                }
            }


        }

        @Override
        public void treeNodesRemoved(TreeModelEvent e) {
            for(Object node:e.getChildren()) {
                String removedQuallifiedTag = categorizedContent((DefaultMutableTreeNode) node);
                oldTags.add(removedQuallifiedTag);
                newTags.add("");
            }
        }

        @Override
        public void treeStructureChanged(TreeModelEvent e) {
            oldTags.clear();
            newTags.clear();
        }

        public void apply() {
            System.out.println(oldTags);
            System.out.println(newTags);
        }

    }


    private static final String WIDTH_PROPERTY = "tagDialog.width";

    private static final String HEIGHT_PROPERTY = "tagDialog.height";

    private final String title;

    private final JDialog dialog;

    private final JColorButton colorButton;
    private final Action modifyColorAction;

    private final JTree tree;

    private final TagCategories tagCategories;

    private boolean contentWasModified;

    private final MIconController iconController;

    private final MapModel map;

    private String lastTransferableId;

    private List<String> lastSelectionParentsNodes;

    private final TagRenamer tagRenamer;


    TagCategoryEditor(RootPaneContainer frame, MIconController iconController, MapModel map) {
        this.iconController = iconController;
        this.map = map;
        title = TextUtils.getText("tag_category_manager");
        contentWasModified = false;
        final boolean modal = true;
        this.dialog = frame instanceof Frame ? new JDialog((Frame) frame, title, modal)
                : new JDialog((JDialog) frame, title, modal);
        final JButton okButton = new JButton();
        final JButton cancelButton = new JButton();
        modifyColorAction = new AbstractAction() {
            private static final long serialVersionUID = 1L;

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
        LabelAndMnemonicSetter.setLabelAndMnemonic(enterConfirms, TextUtils.getRawText(
                "enter_confirms"));
        modifyColorAction.setEnabled(false);
        okButton.addActionListener(e -> {
            close();
            submit();
        });
        cancelButton.addActionListener(e -> close());

        final JPanel buttonPane = new JPanel();
        buttonPane.add(enterConfirms);
        buttonPane.add(okButton);
        buttonPane.add(cancelButton);
        buttonPane.add(colorButton);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        final Container contentPane = dialog.getContentPane();

        final IconRegistry iconRegistry = map.getIconRegistry();
        this.tagCategories = iconRegistry.getTagCategories().copy();
        tree = new JTree(tagCategories.getNodes()) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isPathEditable(TreePath path) {
                Object lastPathComponent = path.getLastPathComponent();
                if (!(lastPathComponent instanceof DefaultMutableTreeNode))
                    return false;
                Object userObject = ((DefaultMutableTreeNode) lastPathComponent).getUserObject();
                return userObject instanceof Tag;
            }

			@Override
			public void setUI(TreeUI ui) {
				super.setUI(ui);
				Font tagFont = iconController.getTagFont(map.getRootNode());
				final Font font = tagFont.deriveFont(getFont().getSize2D());
                setFont(font);
				Rectangle2D rect = font.getStringBounds("*" , 0, 1,
		        		new FontRenderContext(new AffineTransform(), true, true));
		        double textHeight = rect.getHeight();
				setRowHeight((int)  Math.ceil(textHeight * 1.4));
			}


        };
        tree.setEditable(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.setInvokesStopCellEditing(true);
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        tree.setTransferHandler(new TreeTransferHandler(tagCategories));
        tree.setCellRenderer(new TagCellRenderer(tagCategories.getRootNode()));
        tree.setCellEditor(new TagCellEditor(tagCategories));

        configureKeyBindings();

        JRestrictedSizeScrollPane editorScrollPane = createScrollPane();
        editorScrollPane.setViewportView(tree);
        editorScrollPane.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                tree.revalidate();
                tree.repaint();
            }

        });
        enterConfirms.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                tree.requestFocus();
                ResourceController.getResourceController().setProperty(
                        "el__enter_confirms_by_default", Boolean.toString(enterConfirms
                                .isSelected()));
            }
        });
        tree.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(final KeyEvent e) {
                switch (e.getKeyCode()) {
                case KeyEvent.VK_ESCAPE:
                    e.consume();
                    close();
                    break;
                case KeyEvent.VK_ENTER:
                    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 0) {
                        e.consume();
                        if (enterConfirms.isSelected() == ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0)) {
                            addNode((e.getModifiersEx() & (InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK)) != 0);
                            break;
                        }
                        close();
                        submit();
                    }
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

        tree.getSelectionModel().addTreeSelectionListener(this::updateColorButton);
        tagCategories.addTreeModelListener(new TreeModelListener() {

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                contentWasModified = true;
                updateColorButton();
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                contentWasModified = true;
                updateColorButton();
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                contentWasModified = true;
                SwingUtilities.invokeLater(() ->
                    Stream.of(e.getChildren())
                    .map(e.getTreePath()::pathByAddingChild)
                    .forEach(tree::addSelectionPath));
            }

            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                contentWasModified = true;
                updateColorButton();
            }
        });

        tagRenamer = new TagRenamer();
        tagCategories.addTreeModelListener(tagRenamer);

        contentPane.add(editorScrollPane, BorderLayout.CENTER);
        final boolean areButtonsAtTheTop = ResourceController.getResourceController()
                .getBooleanProperty("el__buttons_above");
        contentPane.add(buttonPane, areButtonsAtTheTop ? BorderLayout.NORTH : BorderLayout.SOUTH);
        configureDialog(dialog);
        restoreDialogSize(dialog);
        dialog.pack();
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

    private void close() {
        dialog.setVisible(false);
        map.removeExtension(this);
    }

    private void configureKeyBindings() {
        InputMap im = tree.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = tree.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "startEditing");
        Action editNodeAction = am.get("startEditing");

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "removeNode");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "removeNode");

        @SuppressWarnings("serial")
        AbstractAction addChildNodeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNode(true);
            }
        };

        @SuppressWarnings("serial")
        AbstractAction addSiblingNodeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNode(false);
            }
        };

        @SuppressWarnings("serial")
        AbstractAction removeNodeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeNodes();
            }
        };
        am.put("removeNode", removeNodeAction);

        @SuppressWarnings("serial")
        AbstractAction copyNodeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyNodes();
            }
        };
        am.put(TransferHandler.getCopyAction().getValue(Action.NAME), copyNodeAction);

        @SuppressWarnings("serial")
        AbstractAction cutNodeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveLastSelectionParentsNodes();
                copyNodes();
                removeNodes();
            }
        };
        am.put(TransferHandler.getCutAction().getValue(Action.NAME), cutNodeAction);

        @SuppressWarnings("serial")
        AbstractAction pasteNodeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pasteNode();
            }
        };
        am.put(TransferHandler.getPasteAction().getValue(Action.NAME), pasteNodeAction);
        JMenuBar menubar = new JMenuBar();
        JMenu editMenu = TranslatedElementFactory.createMenu("edit");

        JMenuItem addChildMenuItem = TranslatedElementFactory.createMenuItem("menu_addChild");
        addChildMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Event.SHIFT_MASK | Event.ALT_MASK));
        addChildMenuItem.addActionListener(addChildNodeAction);
        editMenu.add(addChildMenuItem);

        JMenuItem addSiblingMenuItem = TranslatedElementFactory.createMenuItem("menu_addSibling");
        addSiblingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Event.SHIFT_MASK));
        addSiblingMenuItem.addActionListener(addSiblingNodeAction);
        editMenu.add(addSiblingMenuItem);

        JMenuItem removeMenuItem = TranslatedElementFactory.createMenuItem("menu_remove");
        removeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
        removeMenuItem.addActionListener(removeNodeAction);
        editMenu.add(removeMenuItem);

        JMenuItem editMenuItem = TranslatedElementFactory.createMenuItem("edit");
        editMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        editMenuItem.addActionListener(e -> editNodeAction.actionPerformed(
                new ActionEvent(tree, e.getID(), e.getActionCommand())));
        editMenu.add(editMenuItem);

        JMenuItem copyMenuItem = TranslatedElementFactory.createMenuItem("menu_copy");
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        copyMenuItem.addActionListener(copyNodeAction);
        editMenu.add(copyMenuItem);

        JMenuItem cutMenuItem = TranslatedElementFactory.createMenuItem("CutAction.text");
        cutMenuItem.addActionListener(cutNodeAction);
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        editMenu.add(cutMenuItem);

        JMenuItem pasteMenuItem = TranslatedElementFactory.createMenuItem("PasteAction.text");
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        pasteMenuItem.addActionListener(pasteNodeAction);
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
        insertMenu.add(insertMenuItem);

        menubar.add(insertMenu);
        dialog.setJMenuBar(menubar);
    }

    private void insertSelectedTagsIntoSelectedNodes() {
        final TreePath[] selectionPaths = getSelectionPaths();
        if(selectionPaths == null)
            return;
        final List<Tag> selectedTags = Stream.of(selectionPaths)
                .map(TreePath::getLastPathComponent)
                .map(DefaultMutableTreeNode.class::cast)
                .map(DefaultMutableTreeNode::getUserObject)
                .filter(Tag.class::isInstance)
                .map(Tag.class::cast)
                .collect(Collectors.toList());
        ((MIconController)IconController.getController()).insertTagsIntoSelectedNodes(selectedTags);
    }

    private void addNode(boolean asChild) {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree
                .getLastSelectedPathComponent();
        TreeNode[] nodes = (asChild || selectedNode == null || selectedNode.isRoot()) ? tagCategories.addChildNode(selectedNode) : tagCategories.addSiblingNode(selectedNode);
        if(nodes.length == 0)
            return;
        TreePath path = new TreePath(nodes);
        tree.scrollPathToVisible(path);
        tree.setSelectionPath(path);
        tree.startEditingAtPath(path);
    }

    private void saveLastSelectionParentsNodes() {
        final TreePath[] selectionPaths = removeDescendantPaths(tree.getSelectionPaths());
        if(selectionPaths == null)
            lastSelectionParentsNodes = Collections.emptyList();
        lastSelectionParentsNodes = Stream.of(selectionPaths)
                .map(TreePath::getLastPathComponent)
                .map(DefaultMutableTreeNode.class::cast)
                .map(DefaultMutableTreeNode::getParent)
                .map(DefaultMutableTreeNode.class::cast)
                .map(this::categorizedContent)
                .collect(Collectors.toList());
    }
    private TreePath[] removeDescendantPaths(TreePath[] paths) {
        if (paths == null || paths.length == 0) {
            return null;
        }

        List<TreePath> filteredPaths = new ArrayList<>();

        // Add all paths initially
        for (TreePath path : paths) {
            filteredPaths.add(path);
        }

        // Remove descendants
        for (int i = 0; i < filteredPaths.size(); i++) {
            TreePath path = filteredPaths.get(i);
            for (int j = 0; j < filteredPaths.size(); j++) {
                if (i != j) {
                    TreePath otherPath = filteredPaths.get(j);
                    if (otherPath.isDescendant(path)) { // Check if path is a descendant of otherPath
                        filteredPaths.remove(i);
                        i--; // Adjust the index after removal
                        break; // Break as no need to compare with other paths once removed
                    }
                }
            }
        }

        return filteredPaths.toArray(new TreePath[0]);
    }
    private void removeNodes() {
        final TreePath[] selectionPaths = getSelectionPaths();
        if(selectionPaths == null)
            return;
        Stream.of(selectionPaths)
                .map(TreePath::getLastPathComponent)
                .map(DefaultMutableTreeNode.class::cast)
                .forEach(tagCategories::removeNodeFromParent);
    }

    private void copyNodes() {
        TagCategorySelection stringSelection = ((TreeTransferHandler)tree.getTransferHandler()).createTransferable();
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }


    private void pasteNode() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            Transferable t = clipboard.getContents(null);
            if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree
                        .getLastSelectedPathComponent();
                insertTransferable(selectedNode, selectedNode.getChildCount(), t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DataFlavor flavor(Transferable t) {
            if(t.isDataFlavorSupported(TagCategorySelection.tagCategoryFlavor))
                return TagCategorySelection.tagCategoryFlavor;
            if(t.isDataFlavorSupported(TagCategorySelection.tagFlavor))
                return TagCategorySelection.tagFlavor;
            if(t.isDataFlavorSupported(TagCategorySelection.stringFlavor))
                return TagCategorySelection.stringFlavor;
            throw new IllegalArgumentException("No supported flavor found");

    }

    private void modifyTagColor() {
        Tag tag = getSelectedTag();
        if (tag != null && !tag.isEmpty()) {
            Color defaultColor = new Color(tag.getDefaultColor().getRGB(), true);
            Color initialColor = tag.getColor();
            final Color result = ColorTracker.showCommonJColorChooserDialog(tree, tag.getContent(),
                    initialColor, defaultColor);
            if (result != null && !initialColor.equals(result) || result == defaultColor) {
                tag.setColor(result);
                tagCategories.tagChanged(tag);
                updateColorButton();
            }
        }
    }

    private Tag getSelectedTag() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree
                .getLastSelectedPathComponent();
        if (selectedNode == null)
            return null;

        Object userObject = selectedNode.getUserObject();
        if (!(userObject instanceof Tag))
            return null;

        return (Tag) userObject;
    }

    void show() {
        if(! dialog.isVisible()) {
            UITools.setDialogLocationRelativeTo(dialog, dialog.getParent());
            dialog.setVisible(true);
        }
        else
            dialog.toFront();
    }

    protected void submit() {
        iconController.setTagCategories(map, tagCategories);
        tagRenamer.apply();
    }

    private JRestrictedSizeScrollPane createScrollPane() {
        final JRestrictedSizeScrollPane scrollPane = new JRestrictedSizeScrollPane();
        UITools.setScrollbarIncrement(scrollPane);
        scrollPane.setMinimumSize(new Dimension(0, 60));
        return scrollPane;
    }

    private void configureDialog(JDialog dialog) {
        dialog.setModal(false);
    }

    private void saveDialogSize(final JDialog dialog) {
        ResourceController resourceController = ResourceController.getResourceController();
        resourceController.setProperty(WIDTH_PROPERTY, dialog.getWidth());
        resourceController.setProperty(HEIGHT_PROPERTY, dialog.getHeight());
    }

    private void restoreDialogSize(final JDialog dialog) {
        Dimension preferredSize = dialog.getPreferredSize();
        ResourceController resourceController = ResourceController.getResourceController();
        preferredSize.width = Math.max(preferredSize.width, resourceController.getIntProperty(
                WIDTH_PROPERTY, 0));
        preferredSize.height = Math.max(preferredSize.height, resourceController.getIntProperty(
                HEIGHT_PROPERTY, 0));
        dialog.setPreferredSize(preferredSize);
    }

    private void confirmedSubmit() {
        if (dialog.isVisible()) {
            if (contentWasModified) {
                final int action = JOptionPane.showConfirmDialog(dialog, TextUtils.getText(
                        "long_node_changed_submit"), "", JOptionPane.YES_NO_CANCEL_OPTION);

                if (action == JOptionPane.YES_OPTION) {
                    submit();
                } else if (action == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }
            close();
        }
    }

    private void updateColorButton(@SuppressWarnings("unused") TreeSelectionEvent e) {
        updateColorButton();
    }

    private void updateColorButton() {
        Tag tag = getSelectedTag();
        if (tag == null || tag.isEmpty()) {
            modifyColorAction.setEnabled(false);
            colorButton.setColor(Tag.EMPTY_TAG.getColor());
            return;
        }
        else {
            modifyColorAction.setEnabled(true);
            colorButton.setColor(tag.getColor());
        }
    }

    private TreePath[] getSelectionPaths() {
        TreePath[] paths = tree.getSelectionPaths();
        if (paths == null || paths.length == 0) {
            return null;
        }

        List<TreePath> filteredPaths = new ArrayList<>();

        for (TreePath path : paths) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if(node.getUserObject() instanceof Tag)
                filteredPaths.add(path);
        }

        if (paths.length == 0) {
            return null;
        }

        removeDescendants(filteredPaths);

        return filteredPaths.toArray(new TreePath[0]);
    }

    private void removeDescendants(List<TreePath> filteredPaths) {
        for (int i = 0; i < filteredPaths.size(); i++) {
            TreePath path = filteredPaths.get(i);
            for (int j = 0; j < filteredPaths.size(); j++) {
                if (i != j) {
                    TreePath otherPath = filteredPaths.get(j);
                    if (otherPath.isDescendant(path)) {
                        filteredPaths.remove(i);
                        i--;
                        break;
                    }
                }
            }
        }
    }

    private void insertTransferable(DefaultMutableTreeNode parent, int childIndex, Transferable t)
            throws UnsupportedFlavorException, IOException {
        final DataFlavor flavor = flavor(t);
        String data = (String) t.getTransferData(flavor);
        if(flavor.equals(TagCategorySelection.tagCategoryFlavor)) {
            if(data.startsWith(lastTransferableId)) {

            }
            tagCategories.insert(parent, childIndex, data.substring(TRANSFERABLE_ID_LENGTH));
        }
        else
            tagCategories.insert(parent, childIndex, data);
    }

    private String categorizedContent(DefaultMutableTreeNode node) {
        final String tagCategorySeparator = getTagCategorySeparator();
        return new CategorizedTagForCategoryNode(node).getContent(tagCategorySeparator);
    }

    private String getTagCategorySeparator() {
        return tagCategories.getTagCategorySeparator();
    }
}
