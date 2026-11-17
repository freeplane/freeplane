package org.freeplane.features.icon.mindmapmode;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.WeakHashMap;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.svgicons.FreeplaneIconFactory;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.Tag;
import org.freeplane.features.icon.TagCategories;
import org.freeplane.features.icon.TagUsage;
import org.freeplane.features.icon.Tags;
import org.freeplane.features.icon.factory.IconFactory;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class TagPanelManager {
    private static final int USAGE_REFRESH_DELAY_MS = 200;

    final private JPanel tagPanel;
    private JTagTree tagTree; // JTagTree extends our FilterableJTree
    private Font treeFont;
    private TagCategories treeCategories;
    private final MIconController iconController;
    private final JButton editCategoriesButton;
    // Coalesces rapid map edits into one recount rather than traversing the map per event.
    private final Timer usageRefreshTimer;
    private MapModel displayedMap;

    // The search field and its timer.
    private final JTextField filterField = new JTextField();
    private Timer filterTimer;

    // Add new cache-related fields
    private static class TreeCache {
        final JTagTree tree;
        final TagCategories categories;
        final Font font;
        // Null means a map edit has invalidated this cached snapshot.
        TagUsage usage;

        TreeCache(JTagTree tree, TagCategories categories, Font font) {
            this.tree = tree;
            this.categories = categories;
            this.font = font;
        }
    }

    private final WeakHashMap<MapModel, TreeCache> treeCache = new WeakHashMap<>();

    private class TableCreator implements INodeSelectionListener, IMapChangeListener, INodeChangeListener, HierarchyListener {
        @Override
        public void hierarchyChanged(HierarchyEvent e) {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (tagPanel.isShowing()) {
                    onChange(Controller.getCurrentController().getMap());
                }
            }
        }

        @Override
        public void onDeselect(NodeModel node) {
            TagPanelManager.this.updateTreeAndButton(null);
        }

        @Override
        public void onSelect(NodeModel node) {
            onChange(node.getMap());
        }

        private void onChange(MapModel map) {
            if (!tagPanel.isShowing()) {
                return;
            }
            if (map == null) {
                displayedMap = null;
                TagPanelManager.this.updateTreeAndButton(null);
                return;
            }

            TagCategories newCategories = map.getIconRegistry().getTagCategories();
            Font newFont = iconController.getTagFont(map.getRootNode());

            TreeCache cached = treeCache.get(map);
            if (cached != null && cached.categories == newCategories && cached.font.equals(newFont)) {
                tagTree = cached.tree;
                treeCategories = cached.categories;
                treeFont = cached.font;
            } else {
                treeCategories = newCategories;
                treeFont = newFont;
                tagTree = new TagTreeViewerFactory(newCategories, newFont).getTree();
                tagTree.setFont(newFont);
                applyFilter();
                tagTree.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if(e.getClickCount() != 2)
                            return;
                        insertTagIntoSelectedNodes(e);
                    }
                });
                setupFilterField();

                // Store in cache
                treeCache.put(map, new TreeCache(tagTree, treeCategories, treeFont));
            }

            displayedMap = map;
            TreeCache displayed = treeCache.get(map);
            if (displayed.usage == null) {
                refreshUsage(map, displayed);
            }
            TagPanelManager.this.updateTreeAndButton(tagTree);
        }

        @Override
        public void mapChanged(MapChangeEvent event) {
            onChange(event.getMap());
        }

        @Override
        public void nodeChanged(NodeChangeEvent event) {
            if (event.getProperty() == Tags.class) {
                // Only tag changes can alter a usage count; other node edits leave it valid.
                markUsageStale(event.getNode().getMap());
            }
        }

        @Override
        public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
            // A whole inserted branch may contain tags, so recount the displayed map.
            markUsageStale(parent.getMap());
        }

        @Override
        public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
            // Deleted nodes can remove one or more tag uses from the map.
            markUsageStale(nodeDeletionEvent.parent.getMap());
        }

        private void insertTagIntoSelectedNodes(MouseEvent e) {
            JTagTree tree = (JTagTree) e.getSource();
            int x = e.getX();
            int y = e.getY();
            TreePath path = tree.getPathForLocation(x, y);
            if (path == null) return; // Clicked outside any node
            Rectangle nodeBounds = tree.getUI().getPathBounds(tree, path);
            if (nodeBounds == null) return;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            Tag tag = treeCategories.categorizedTag(node);
            iconController.insertTagsIntoSelectedNodes(Collections.singletonList(tag));
        }
    }

    public TagPanelManager(ModeController modeController) {
        tagPanel = new JPanel(new GridBagLayout());
        tagPanel.setMinimumSize(new Dimension(0, 0));
        tagPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Create button with constant width
        editCategoriesButton = new JButton(modeController.getAction("ManageTagCategoriesAction"));

        final TableCreator tableCreator = new TableCreator();
        final MapController mapController = modeController.getMapController();
        mapController.addNodeSelectionListener(tableCreator);
        mapController.addMapChangeListener(tableCreator);
        mapController.addUINodeChangeListener(tableCreator);
        usageRefreshTimer = new Timer(USAGE_REFRESH_DELAY_MS, event -> refreshDisplayedUsage());
        usageRefreshTimer.setRepeats(false);
        tagPanel.addHierarchyListener(tableCreator);
        iconController = (MIconController) modeController.getExtension(IconController.class);

        updateTreeAndButton(null);
    }

    public JPanel getTagPanel() {
        return tagPanel;
    }

    private void markUsageStale(MapModel map) {
        TreeCache cache = treeCache.get(map);
        if (cache == null) {
            return;
        }
        cache.usage = null;
        if (map == displayedMap && tagPanel.isShowing()) {
            // Restarting implements debounce: wait until edits pause before recounting.
            usageRefreshTimer.restart();
        }
    }

    private void refreshDisplayedUsage() {
        if (displayedMap == null || !tagPanel.isShowing()) {
            return;
        }
        TreeCache cache = treeCache.get(displayedMap);
        if (cache != null && cache.usage == null) {
            refreshUsage(displayedMap, cache);
        }
    }

    private void refreshUsage(MapModel map, TreeCache cache) {
        // Compute one immutable snapshot and let the renderer look up every visible tag from it.
        TagUsage usage = TagUsage.countIn(map.getRootNode());
        TagCategories categories = cache.categories;
        cache.usage = usage;
        cache.tree.setUsageProvider(node -> categories.containsTag(node)
                ? usage.of(categories.categorizedContent(node))
                : TagCellRenderer.NO_USAGE_DISPLAYED);
    }

    // Updates the panel with the filter field at the top, then the tree (if available) and always the button underneath.
    private void updateTreeAndButton(JTree tree) {
        tagPanel.removeAll();
        int gridy = 0;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(5, 5, 0, 5);

        // Add the filter label and field at the top in a row
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setOpaque(false);

        // Add the label
        JLabel filterLabel = new JLabel();
        Icon filterIcon = ResourceController.getResourceController().getIcon("filterIcon");
        Icon scaledIcon = IconFactory.getInstance().getScaledIcon(filterIcon, filterLabel);
        filterLabel.setIcon(FreeplaneIconFactory.toImageIcon(scaledIcon));
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = 0;
        labelGbc.gridy = 0;
        labelGbc.insets = new Insets(0, 0, 0, 5); // 5-pixel gap between label and field
        filterPanel.add(filterLabel, labelGbc);

        // Add the filter field
        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 1;
        fieldGbc.gridy = 0;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.weightx = 1.0;
        filterPanel.add(filterField, fieldGbc);

        // Add the filter panel to the main panel
        gbc.gridy = gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weightx = 1.0;
        tagPanel.add(filterPanel, gbc);

        if (tree != null) {
            gbc.gridy = gridy++;
            gbc.anchor = GridBagConstraints.NORTH;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(0, 5, 5, 5);
            tagPanel.add(tree, gbc);
        }

        // Add the button with left alignment.
        GridBagConstraints gbcButton = new GridBagConstraints();
        gbcButton.gridx = 0;
        gbcButton.gridy = gridy++;
        gbcButton.insets = new Insets(5, 5, 5, 5);
        gbcButton.anchor = GridBagConstraints.WEST;
        gbcButton.fill = GridBagConstraints.NONE;
        tagPanel.add(editCategoriesButton, gbcButton);

        // Filler to push components to the top.
        GridBagConstraints gbcFiller = new GridBagConstraints();
        gbcFiller.gridx = 0;
        gbcFiller.gridy = gridy;
        gbcFiller.weighty = 1.0;
        gbcFiller.fill = GridBagConstraints.VERTICAL;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        tagPanel.add(filler, gbcFiller);

        tagPanel.revalidate();
        tagPanel.repaint();
    }

    /**
     * Setup the filter field to start/restart a timer on text changes.
     * When the timer fires, the content of the text field is used to build a predicate
     * which is then set on the JTagTree.
     */
    private void setupFilterField() {
        // Remove any existing listeners/timers first.
        if (filterTimer != null) {
            filterTimer.stop();
        }
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            private void restartTimer() {
                if (filterTimer != null) {
                    filterTimer.restart();
                }
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                restartTimer();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                restartTimer();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                restartTimer();
            }
        });
        // Delay of 300 ms (adjust as needed).
        filterTimer = new Timer(300, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterTimer.stop();
                // Build predicate: if empty, no filtering; else, filter nodes whose user object toString() contains the text.
                if (tagTree != null) {
                    applyFilter();
                }
            }
        });
        filterTimer.setRepeats(false);
    }

    private void applyFilter() {
        String text = filterField.getText().trim();
        tagTree.setFilter(text.isEmpty() ? null : node -> node.toString().toLowerCase().contains(text.toLowerCase()));
    }
}
