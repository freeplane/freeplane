/*
 * Created on 27 Apr 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.mindmapmode.UncategorizedTag;

public class TagCategories {
    private final IconRegistry registry = new IconRegistry(null);
    private final DefaultTreeModel nodes;
    private final TreeInverseMap<Tag> nodesByTags;
    private String tagCategorySeparatorForMap;
    private String tagCategorySeparatorForNode;

    public TagCategories(){
        this(new DefaultMutableTreeNode(TextUtils.getRawText("tags")),
                ResourceController.getResourceController().getProperty("map_tag_category_separator"),
                ResourceController.getResourceController().getProperty("node_tag_category_separator"));
    }

    @SuppressWarnings("serial")
    public TagCategories(DefaultMutableTreeNode rootNode, String tagCategorySeparatorForMap, String tagCategorySeparatorForNode) {
        super();
        this.tagCategorySeparatorForMap = tagCategorySeparatorForMap;
        this.tagCategorySeparatorForNode = tagCategorySeparatorForNode;
        nodes = new DefaultTreeModel(rootNode) {

            @Override
            public void valueForPathChanged(TreePath path, Object newValue) {
                nodesByTags.valueForPathChanged(path, (Tag)newValue);
                super.valueForPathChanged(path, newValue);
            }

        };
        nodesByTags = new TreeInverseMap<Tag>(nodes);
    }

    public String getTagCategorySeparatorForMap() {
        return tagCategorySeparatorForMap;
    }

    public void setTagCategorySeparatorForMap(String tagCategorySeparatorForMap) {
        this.tagCategorySeparatorForMap = tagCategorySeparatorForMap;
    }

    public String getTagCategorySeparatorForNode() {
        return tagCategorySeparatorForNode;
    }

    public void setTagCategorySeparatorForNode(String tagCategorySeparatorForNode) {
        this.tagCategorySeparatorForNode = tagCategorySeparatorForNode;
    }

    public static void writeTag(DefaultMutableTreeNode node, StringWriter writer) {
        Object userObject = node.getUserObject();
        if (userObject instanceof Tag) {
            Tag tag = (Tag) userObject;
            try {
                writeTag(tag, writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void writeTagCategories(DefaultMutableTreeNode node, String indent,
            Writer writer) throws IOException {
        Object userObject = node.getUserObject();
        if (userObject instanceof Tag) {
            Tag tag = (Tag) userObject;
            writer.append(indent);
            writeTag(tag, writer);
            indent = indent + " ";
        }
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            writeTagCategories(childNode, indent, writer);
        }
    }

    public static void writeTag(Tag tag, Writer writer) throws IOException {
        if(! tag.isEmpty()) {
            writer.append(tag.getContent());
            writer.append(ColorUtils.colorToRGBAString(tag.getColor()));
        }
        writer.append(System.lineSeparator());
    }

    public boolean isEmpty() {
        return getRootNode().isLeaf();
    }

    public void readTagCategories(DefaultMutableTreeNode target, int index, Scanner scanner) {
        DefaultMutableTreeNode lastNode = target;
        int lastIndentation = -1;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            int indentation = getIndentationLevel(line);

            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(registry.readTag(line.trim()));
            DefaultMutableTreeNode parent;
            if (indentation == lastIndentation) {
                parent = (DefaultMutableTreeNode) lastNode.getParent();
            } else if (indentation > lastIndentation) {
                parent = lastNode;
            } else {
                parent = (DefaultMutableTreeNode) lastNode.getParent();
                for (int i = 0; i < (lastIndentation - indentation); i++) {
                    parent = (DefaultMutableTreeNode) parent.getParent();
                }
            }
            insertNode(parent, target == parent ? index++ : parent.getChildCount(), newNode);

            lastNode = newNode;
            lastIndentation = indentation;
        }
    }

    private void insertNode(DefaultMutableTreeNode parent, int index, DefaultMutableTreeNode newChild) {
        parent.insert(newChild, index);
        getNodes().nodesWereInserted(parent, new int[] {index});
    }

    private int getIndentationLevel(String line) {
        int indentation = 0;
        while (line.charAt(indentation) == ' ') {
            indentation++;
        }
        return indentation;
    }

    public void load(File tagCategoryFile) {
        try (Scanner scanner = new Scanner(tagCategoryFile)){
            final DefaultMutableTreeNode rootNode = getRootNode();
            readTagCategories(rootNode, rootNode.getChildCount(), scanner);
        } catch (FileNotFoundException e1) {/**/}
    }

    public void load(String data) {
        try (Scanner scanner = new Scanner(data)){
            final DefaultMutableTreeNode rootNode = getRootNode();
            readTagCategories(rootNode, rootNode.getChildCount(), scanner);
        }
    }
    public DefaultMutableTreeNode getRootNode() {
        return (DefaultMutableTreeNode) getNodes().getRoot();
    }

    public IconRegistry getRegistry() {
        return registry;
    }

    public void addTreeModelListener(TreeModelListener treeModelListener) {
       getNodes().addTreeModelListener(treeModelListener);
    }

    void removeTreeModelListener(TreeModelListener l) {
        getNodes().removeTreeModelListener(l);
    }

    public TreeNode[] addChildNode(MutableTreeNode parent) {
        if(parent == null)
            parent = getRootNode();
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(Tag.EMPTY_TAG);
        getNodes().insertNodeInto(newNode, parent, parent.getChildCount());
        return getNodes().getPathToRoot(newNode);
   }

    public TreeNode[] addSiblingNode(MutableTreeNode node) {
        TreeNode[] nothing = {};
        if(node == null)
            return nothing;
        MutableTreeNode parent = (MutableTreeNode) node.getParent();
        if(parent == null)
            return nothing;
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(Tag.EMPTY_TAG);
        getNodes().insertNodeInto(newNode, parent, parent.getIndex(node) + 1);
        return getNodes().getPathToRoot(newNode);
   }

    public void removeNodeFromParent(MutableTreeNode node) {
        if(node.getParent() != null)
            getNodes().removeNodeFromParent(node);
    }

    void save(File tagCategoryFile) {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tagCategoryFile))){
            writeTagCategories(getRootNode(), "", writer);
        } catch (IOException e) {
            LogUtils.severe(e);
        }
    }

    public String serialize() {
        try {
            StringWriter writer = new StringWriter();
            TagCategories.writeTagCategories(getRootNode(), "", writer);
            String serializedData = writer.toString();
            return serializedData;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void tagChanged(Tag tag) {
        tagChanged(getRootNode(), tag);
    }

    private void tagChanged(DefaultMutableTreeNode node, Tag updatedTag) {
        int childCount = node.getChildCount();
        Object userObject = node.getUserObject();
        if (userObject == updatedTag) {
            getNodes().nodeChanged(node);
        }
        for (int i = 0; i < childCount; i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            tagChanged(childNode, updatedTag);
        }
    }

    public void insert(DefaultMutableTreeNode parent, int index, String data) {
        if (parent == null) {
            parent = getRootNode();
        }
        try(Scanner st = new Scanner(new StringReader(data))){
            readTagCategories(parent, index, st);
        }
    }

    Color getColor(Tag tag) {
        return registry.getTagColor(tag);
    }


    public List<CategorizedTag> categorizedTags() {
        return categorizedTags(registry);
     }

    public List<CategorizedTag> categorizedTags(IconRegistry registry){
        return categorizedTags(registry.getTagsAsListModel(), registry);
    }

    public List<CategorizedTag> categorizedTags(Iterable<Tag> tags, IconRegistry iconRegistry){
        final LinkedList<CategorizedTag> categorizedTags = new LinkedList<>();
        for(Tag tag : tags) {
            if(tag.isEmpty())
                categorizedTags.add(CategorizedTag.EMPTY_TAG);
            else {
                final Set<DefaultMutableTreeNode> tagCategoryNodes = nodesByTags.getNodes(tag);
                if(tagCategoryNodes.isEmpty())
                    categorizedTags.add(new UncategorizedTag(iconRegistry.registryTag(tag)));
                else {
                    for(DefaultMutableTreeNode node : tagCategoryNodes)
                        categorizedTags.add(new CategorizedTagForCategoryNode(node, iconRegistry.getTag(tag)));
                }
            }
        }
        return categorizedTags;
    }

    public boolean register(CategorizedTag tag) {
        List<Tag> categoryTags = tag.categoryTags();
        DefaultMutableTreeNode rootNode = getRootNode();

        DefaultMutableTreeNode currentNode = rootNode;
        boolean registered = false;

        for (Tag currentTag : categoryTags) {
            boolean found = false;

            for (@SuppressWarnings("unchecked")
                Enumeration<?> children = currentNode.children();
                    children.hasMoreElements();) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) children.nextElement();
                Tag childTag = (Tag) childNode.getUserObject();

                if (childTag.equals(currentTag)) {
                    currentNode = childNode;
                    found = true;
                    break;
                }
            }

            if (!found) {
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(currentTag.copy());
                insertNode(currentNode, currentNode.getChildCount(), newNode);
                currentNode = newNode;
                registered = true;
            }
        }
        return registered;
    }

    public DefaultTreeModel getNodes() {
        return nodes;
    }

    public TagCategories copy(IconRegistry iconRegistry) {
        final DefaultMutableTreeNode rootNode = getRootNode();
        final DefaultMutableTreeNode rootCopy = copySubtree(iconRegistry, rootNode);
        final TagCategories tagCategories = new TagCategories(rootCopy, tagCategorySeparatorForMap, tagCategorySeparatorForNode);
        return tagCategories;
    }

    private DefaultMutableTreeNode copySubtree(IconRegistry iconRegistry, DefaultMutableTreeNode node) {
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(copyTag(iconRegistry, node));
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            newNode.add(copySubtree(iconRegistry, child));
        }
        return newNode;
    }

    private Object copyTag(IconRegistry iconRegistry, DefaultMutableTreeNode node) {
        final Object userObject = node.getUserObject();
        if(userObject instanceof Tag) {
            final Tag tag = ((Tag)userObject).copy();
            iconRegistry.getTag(tag).ifPresent(registeredTag -> tag.setColor(registeredTag.getColor()));
            return tag;
        }
        return userObject;
    }
}
