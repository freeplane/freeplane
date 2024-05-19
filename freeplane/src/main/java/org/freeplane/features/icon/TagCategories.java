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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
import org.freeplane.core.util.collection.SortedComboBoxModel;
import org.freeplane.features.icon.mindmapmode.UncategorizedTag;

public class TagCategories {
    public static Tag readTag(String spec) {
        int colorIndex = spec.length() - 9;
        if(colorIndex > 0 && spec.charAt(colorIndex) == '#') {
            String content = spec.substring(0, colorIndex);
            String colorSpec = spec.substring(colorIndex);
            try {
                return new Tag(content, tagColor(content, colorSpec));
            } catch (NumberFormatException e) {
                LogUtils.severe(e);
            }
        }
        return new Tag(spec);
    }

    @SuppressWarnings("serial")
    private class TagCategoryTree extends DefaultTreeModel {
        private TagCategoryTree(TreeNode root) {
            super(root);
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
            nodesByTags.valueForPathChanged(path, (Tag)newValue);
            super.valueForPathChanged(path, newValue);
        }
    }

    private final DefaultTreeModel nodes;
    private final TreeInverseMap<Tag> nodesByTags;
    final private SortedComboBoxModel<Tag> mapTags;

    private String managedCategorySeparator;
    private String adhocCategorySeparator;

    public TagCategories(){
        this(new DefaultMutableTreeNode(TextUtils.getRawText("tags")),
                ResourceController.getResourceController().getProperty("managed_category_separator"),
                ResourceController.getResourceController().getProperty("adhoc_category_separator"));
    }

    @SuppressWarnings("serial")
    public TagCategories(DefaultMutableTreeNode rootNode, String managedCategorySeparator, String adhocCategorySeparator) {
        this.managedCategorySeparator = managedCategorySeparator;
        this.adhocCategorySeparator = adhocCategorySeparator;
        mapTags = new SortedComboBoxModel<>(Tag.class);
        nodes = new TagCategoryTree(rootNode);
        nodesByTags = new TreeInverseMap<Tag>(nodes);
    }

    private TagCategories(TagCategories tagCategories) {
        this.mapTags = new SortedComboBoxModel<>(Tag.class);
        final DefaultMutableTreeNode rootNode = tagCategories.getRootNode();
        final DefaultMutableTreeNode rootCopy = copySubtree(rootNode);
        this.managedCategorySeparator = tagCategories.managedCategorySeparator;
        this.adhocCategorySeparator = tagCategories.adhocCategorySeparator;
        nodes = new TagCategoryTree(rootCopy);
        nodesByTags = new TreeInverseMap<Tag>(nodes);
        tagCategories.mapTags.forEach(this::registerTag);
    }

    public String getTagCategorySeparatorForMap() {
        return managedCategorySeparator;
    }

    public void setTagCategorySeparatorForMap(String managedCategorySeparator) {
        this.managedCategorySeparator = managedCategorySeparator;
    }

    public String getTagCategorySeparatorForNode() {
        return adhocCategorySeparator;
    }

    public void setTagCategorySeparatorForNode(String adhocCategorySeparator) {
        this.adhocCategorySeparator = adhocCategorySeparator;
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

            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(registerTag(line.trim()));
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

    public List<CategorizedTag> categorizedTags(){
        return categorizedTags(mapTags);
    }

    public List<CategorizedTag> categorizedTags(Iterable<Tag> tags){
        final LinkedList<CategorizedTag> categorizedTags = new LinkedList<>();
        Set<Tag> addedAdhocTags = new HashSet<Tag>();
        for(Tag tag : tags) {
            if(tag.isEmpty())
                categorizedTags.add(CategorizedTag.EMPTY_TAG);
            else {
                final Set<DefaultMutableTreeNode> tagCategoryNodes = nodesByTags.getNodes(tag);
                if(tagCategoryNodes.isEmpty())
                    addAdhocTags(categorizedTags, addedAdhocTags, tag);
                else {
                    for(DefaultMutableTreeNode node : tagCategoryNodes)
                        categorizedTags.add(new CategorizedTagForCategoryNode(node, getTag(tag)));
                }
            }
        }
        return categorizedTags;
    }

    private void addAdhocTags(final LinkedList<CategorizedTag> categorizedTags, Set<Tag> addedAdhocTags, Tag tag) {
        if(addedAdhocTags.add(tag)) {
            final String tagContent = tag.getContent();
            final int separatorIndex = tagContent.lastIndexOf(adhocCategorySeparator);
            if(separatorIndex > 0)
                addAdhocTags(categorizedTags, addedAdhocTags, new Tag(tagContent.substring(0, separatorIndex)));
            categorizedTags.add(new UncategorizedTag(tag));
        }
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
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(setTagColor(currentTag));
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


    public SortedComboBoxModel<Tag> getTagsAsListModel() {
        return mapTags;
    }

    public Tag createTag(String string) {
        Tag tag = new Tag(string);
        return registerTag(tag);
    }

    public Tag registerTag(Tag tag) {
        Tag registeredTag = mapTags.addIfNotExists(tag);
        return registeredTag;
    }

    public Tag setTagColor(String tagContent, String tagColor) {
        return setTagColor(tagContent, tagColor(tagContent, tagColor));
    }

    public static Color tagColor(String tagContent, String tagColor) {
        return Optional.ofNullable(tagColor).map(ColorUtils::stringToColor)
                .orElseGet(() -> Tag.getDefaultColor(tagContent));
    }

    public Tag setTagColor(String tagContent, Color value) {
        Tag tag = createTag(tagContent);
        tag.setColor(value);
        return tag;
    }


    public Optional<Tag>getTag(Tag required) {
        return mapTags.getElement(required);
    }

    public Color getTagColor(Tag required) {
        return getTag(required).get().getColor();
    }

    public Tag registerTag(String spec) {
        int colorIndex = spec.length() - 9;
        if(colorIndex > 0 && spec.charAt(colorIndex) == '#') {
            String content = spec.substring(0, colorIndex);
            String colorSpec = spec.substring(colorIndex);
            try {
                return setTagColor(content, colorSpec);
            } catch (NumberFormatException e) {
                LogUtils.severe(e);
            }
        }
        return createTag(spec);
    }

    public Tag setTagColor(Tag prototype) {
        return setTagColor(prototype.getContent(), prototype.getColor());
    }


    public TagCategories copy() {
        final TagCategories tagCategories = new TagCategories(this);
        return tagCategories;
    }

    private DefaultMutableTreeNode copySubtree( DefaultMutableTreeNode node) {
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(copyTag(node));
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            newNode.add(copySubtree(child));
        }
        return newNode;
    }

    private Object copyTag(DefaultMutableTreeNode node) {
        final Object userObject = node.getUserObject();
        if(userObject instanceof Tag) {
            final Tag tag = ((Tag)userObject);
            return setTagColor(tag);
        }
        return userObject;
    }
}
