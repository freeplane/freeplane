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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.IntStream;

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
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            Object oldValue = node.getUserObject();
            if(node.getParent() == uncategorizedTagsNode) {
                removeUncategorizedTagNode((Tag) oldValue);
                node.setUserObject(newValue);
                insertUncategorizedTagNodeSorted(node);
            }
            else {
                for (TreeModelListener listener: getTreeModelListeners()) {
                    if(listener instanceof TreeTagChangeListener)
                        ((TreeTagChangeListener<Tag>) listener).valueForPathChanged(path, (Tag)newValue);

                }
                super.valueForPathChanged(path, newValue);
            }
        }
    }

    private final DefaultTreeModel nodes;
    private TreeInverseMap<Tag> nodesByTags;
    final private SortedComboBoxModel<Tag> mapTags;
    final private TreeMap<String, List<TagReference>> tagReferences;
    private boolean categoriesChanged;

    private String categorySeparator;
    private final DefaultMutableTreeNode uncategorizedTagsNode;
    public static final String UNCATEGORIZED_NODE = " uncategorized node ";

    public TagCategories(){
        this(new DefaultMutableTreeNode(TextUtils.getRawText("tags")),
                new DefaultMutableTreeNode(TextUtils.getRawText("uncategorized_tags")),
                ResourceController.getResourceController().getProperty("category_separator"));
    }

    @SuppressWarnings("serial")
    public TagCategories(DefaultMutableTreeNode rootNode, DefaultMutableTreeNode uncategorizedTagsNode, String categorySeparator) {
        this.uncategorizedTagsNode = uncategorizedTagsNode;
        this.categorySeparator = categorySeparator;
        mapTags = new SortedComboBoxModel<>(Tag.class);
        rootNode.add(uncategorizedTagsNode);
        nodes = new TagCategoryTree(rootNode);
        tagReferences = new TreeMap<>();
        nodesByTags = null;
        categoriesChanged = false;
    }

    private TagCategories(TagCategories tagCategories) {
        this.mapTags = new SortedComboBoxModel<>(Tag.class);
        final DefaultMutableTreeNode rootNode = tagCategories.getRootNode();
        this.categorySeparator = tagCategories.categorySeparator;
        tagReferences = new TreeMap<>();
        tagCategories.tagReferences.entrySet().forEach(e -> tagReferences.put(e.getKey(), new ArrayList<>(e.getValue())));
        final DefaultMutableTreeNode rootCopy = copySubtree(rootNode);
        uncategorizedTagsNode = (DefaultMutableTreeNode) rootCopy.getLastChild();
        nodes = new TagCategoryTree(rootCopy);
        nodesByTags = null;
        tagCategories.mapTags.forEach(mapTags::addIfNotExists);
        categoriesChanged = false;
    }

    public String getTagCategorySeparator() {
        return categorySeparator;
    }

    public void updateTagCategorySeparator(String newCategorySeparator) {
        String initialCategorySeparator = this.categorySeparator;
        if(! initialCategorySeparator.equals(newCategorySeparator)) {
            setTagCategorySeparator(newCategorySeparator);
            final Tag[] initialTags = mapTags.stream()
                    .filter(tag -> tag.getContent().contains(initialCategorySeparator))
                    .toArray(Tag[]::new);
            for(Tag tag: initialTags) {
                final String updatedContent = tag.getContent().replace(initialCategorySeparator,  newCategorySeparator);
                final Tag updatedTag = new Tag(updatedContent, tag.getColor());
                mapTags.replace(tag, updatedTag);
                List<TagReference> replacedContentReferences = tagReferences.remove(tag.getContent());
                if(replacedContentReferences != null) {
                    tagReferences.computeIfAbsent(updatedContent, x -> new ArrayList<>()).addAll(replacedContentReferences);
                }
            }
            for(int i = uncategorizedTagsNode.getChildCount() - 1; i >= 0; i--) {
                DefaultMutableTreeNode uncategorizedTagNode = (DefaultMutableTreeNode) uncategorizedTagsNode.getChildAt(i);
                Tag tag = (Tag) uncategorizedTagNode.getUserObject();
                if(tag.getContent().contains(newCategorySeparator)) {
                    uncategorizedTagsNode.remove(i);
                    mapTags.remove(tag);
                    registerTagReference(tag, true);
                }


            }
            categoriesChanged = true;
        }
    }

    public void setTagCategorySeparator(String newCategorySeparator) {
        this.categorySeparator = newCategorySeparator;
    }

    public void writeCategorizedTag(DefaultMutableTreeNode node, StringWriter writer) {
        Object userObject = node.getUserObject();
        if (userObject instanceof Tag) {
            try {
            	Tag categorizedTag = new CategorizedTagForCategoryNode(node).categorizedTag(categorySeparator);
                writeTag(categorizedTag, writer);
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
        else if(node.getParent() != null)
            return;
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

    public void readTagCategories(DefaultMutableTreeNode target, final int firstIndex, Scanner scanner) {
        DefaultMutableTreeNode lastNode = target;
        int lastIndentation = -1;
        int index = firstIndex;
        LinkedList<String> categorizedContent = new LinkedList<>();
        String tagCategorySeparator = getTagCategorySeparator();
        if(! target.isRoot()) {
            String prefix = categorizedContent((DefaultMutableTreeNode)lastNode.getParent());
            if(! prefix.isEmpty())
            categorizedContent.add(prefix + tagCategorySeparator);
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            int indentation = getIndentationLevel(line);

            String lineTags = line.trim();
            for(int lineTagIndex = 0; lineTagIndex < lineTags.length();) {
                int lineTagEnd = lineTags.indexOf(tagCategorySeparator, lineTagIndex);
                String lineTag = lineTagEnd >= 0 ? lineTags.substring(lineTagIndex, lineTagEnd) : lineTags.substring(lineTagIndex);
                lineTagIndex =  lineTagEnd >= 0 ? lineTagEnd + tagCategorySeparator.length() : lineTags.length();
                Tag tag = readTag(lineTag);
                if(target == uncategorizedTagsNode) {
                    insertUncategorizedTagNodeSorted(tag);
                } else {
                    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(tag);
                    DefaultMutableTreeNode parent;
                    if (indentation == lastIndentation) {
                        parent = (DefaultMutableTreeNode) lastNode.getParent();
                    } else if (indentation > lastIndentation) {
                        parent = lastNode;
                        Object userObject = parent.getUserObject();
                        String categorizedParentContent =
                                (categorizedContent.isEmpty() ? "" :  categorizedContent.getLast() )
                                + ((userObject instanceof Tag)?(((Tag)userObject)).getContent() + tagCategorySeparator : "");
                        categorizedContent.add(categorizedParentContent);
                    } else {
                        parent = (DefaultMutableTreeNode) lastNode.getParent();
                        for (int i = 0; i < (lastIndentation - indentation); i++) {
                            parent = (DefaultMutableTreeNode) parent.getParent();
                            categorizedContent.removeLast();
                        }
                    }
                    parent.insert(newNode, target == parent ? index++ : parent.getChildCount());
                    String categorizedTagContent = categorizedContent.getLast()
                            + tag.getContent();
                    registerTagReference(new Tag(categorizedTagContent, tag.getColor()));
                    lastNode = newNode;
                    lastIndentation = indentation;
                    indentation++;
                }
            }
        }
        if(target != uncategorizedTagsNode)
            nodes.nodesWereInserted(target, IntStream.range(firstIndex, index).toArray());
    }

    private void insertNode(DefaultMutableTreeNode parent, int index, DefaultMutableTreeNode newChild) {
        parent.insert(newChild, index);
        nodes.nodesWereInserted(parent, new int[] {index});
    }

    private int findUncategorizedTagIndex(Tag tag) {
        int low = 0;
        int high = uncategorizedTagsNode.getChildCount() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            DefaultMutableTreeNode midNode = (DefaultMutableTreeNode) uncategorizedTagsNode.getChildAt(mid);
            Tag midUserObject = (Tag) midNode.getUserObject();

            if (tag.compareTo(midUserObject) == 0) {
                return mid;
            } else if (tag.compareTo(midUserObject) < 0) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        return -(low + 1);
    }

    private void insertUncategorizedTagNodeSorted(Tag tag) {
        int index = findUncategorizedTagIndex(tag);
        int insertionPoint = index >= 0 ? index : -index - 1;
        insertNode(uncategorizedTagsNode, insertionPoint, new DefaultMutableTreeNode(tag));
    }

    private void insertUncategorizedTagNodeSorted(DefaultMutableTreeNode node) {
        int index = findUncategorizedTagIndex((Tag) node.getUserObject());
        int insertionPoint = index >= 0 ? index : -index - 1;
        insertNode(uncategorizedTagsNode, insertionPoint, node);
    }

    private DefaultMutableTreeNode removeUncategorizedTagNode(Tag tag) {
        int index = findUncategorizedTagIndex(tag);

        if (index < 0)
            return null;
        DefaultMutableTreeNode removedNode = (DefaultMutableTreeNode) uncategorizedTagsNode.getChildAt(index);
        removeNodeFromParent(removedNode);
        return removedNode;
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
            readTagCategories(rootNode, rootNode.getChildCount() - 1, scanner);
        } catch (FileNotFoundException e1) {/**/}
    }

    public void load(String data) {
        try (Scanner scanner = new Scanner(data)){
            final DefaultMutableTreeNode rootNode = getRootNode();
            readTagCategories(rootNode, rootNode.getChildCount() - 1, scanner);
        }
    }
    public DefaultMutableTreeNode getRootNode() {
        return (DefaultMutableTreeNode) nodes.getRoot();
    }

    public DefaultMutableTreeNode getUncategorizedTagsNode() {
        return uncategorizedTagsNode;
    }

    public void addTreeModelListener(TreeModelListener treeModelListener) {
       nodes.addTreeModelListener(treeModelListener);
    }

    void removeTreeModelListener(TreeModelListener l) {
        nodes.removeTreeModelListener(l);
    }

    public TreeNode[] addChildNode(MutableTreeNode parent) {
    	DefaultMutableTreeNode rootNode = getRootNode();
        if(parent == null)
			parent = rootNode;
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(Tag.EMPTY_TAG);
        nodes.insertNodeInto(newNode, parent,  parent == rootNode ? parent.getChildCount() - 1 : parent.getChildCount());
        return nodes.getPathToRoot(newNode);
   }

    public TreeNode[] addSiblingNode(MutableTreeNode node) {
        TreeNode[] nothing = {};
        if(node == null)
            return nothing;
        MutableTreeNode parent = (MutableTreeNode) node.getParent();
        if(parent == null)
            return nothing;
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(Tag.EMPTY_TAG);
        nodes.insertNodeInto(newNode, parent, parent.getIndex(node) + 1);
        return nodes.getPathToRoot(newNode);
   }

    public void removeNodeFromParent(MutableTreeNode node) {
        if(node.getParent() != null)
            nodes.removeNodeFromParent(node);
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
        for(Tag qualifiedTag : tags) {
            if(qualifiedTag.isEmpty())
                categorizedTags.add(CategorizedTag.EMPTY_TAG);
            else {
                Tag tag = qualifiedTag.shortTag(getTagCategorySeparator());
                final Set<DefaultMutableTreeNode> tagCategoryNodes = getNodes(tag);
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

    private Set<DefaultMutableTreeNode> getNodes(Tag tag) {
        if(nodesByTags == null) {
            nodesByTags = new TreeInverseMap<Tag>(nodes);
            nodes.addTreeModelListener(nodesByTags);
        }
        return nodesByTags.getNodes(tag);
    }

    private void addAdhocTags(final LinkedList<CategorizedTag> categorizedTags, Set<Tag> addedAdhocTags, Tag tag) {
        if(addedAdhocTags.add(tag)) {
            final String tagContent = tag.getContent();
            final int separatorIndex = tagContent.lastIndexOf(categorySeparator);
            if(separatorIndex > 0)
                addAdhocTags(categorizedTags, addedAdhocTags, new Tag(tagContent.substring(0, separatorIndex)));
            categorizedTags.add(new UncategorizedTag(tag));
        }
    }

    public DefaultTreeModel getNodes() {
        return nodes;
    }


    public SortedComboBoxModel<Tag> getTagsAsListModel() {
        return mapTags;
    }

    public Tag createTag(String string) {
        return createTagReference(string).getTag();
    }

    public TagReference createTagReference(String string) {
        return registerTagReference(new Tag(string));
    }

    public TagReference registerTagReference(Tag tag) {
        return registerTagReference(tag, false);
    }

    private TagReference registerTagReference(Tag tag,  boolean setColor) {
        final int addedElementIndex = mapTags.addIfNotExists(tag);
        if(addedElementIndex >= 0) {
            final String fullContent = tag.getContent();
            DefaultMutableTreeNode rootNode = getRootNode();
            if (fullContent.contains(categorySeparator)) {
                DefaultMutableTreeNode currentNode = rootNode;
                for (int start = 0, end = fullContent.indexOf(categorySeparator);;
                        start = end + categorySeparator.length(),
                        end = fullContent.indexOf(categorySeparator, start)) {
                    boolean found = false;
                    String currentTag = end >= 0 ? fullContent.substring(start, end) : fullContent.substring(start);
                    for (@SuppressWarnings("unchecked")
                        Enumeration<?> children = currentNode.children();
                            children.hasMoreElements();) {
                        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) children.nextElement();
                        final Object userObject = childNode.getUserObject();
                        if(! (userObject instanceof Tag))
                            break;
                        Tag childTag = (Tag) userObject;

                        if (childTag.getContent().equals(currentTag)) {
                            currentNode = childNode;
                            found = true;
                            if(setColor)
                                childTag.setColor(tag.getColor());
                            break;
                        }
                    }

                    if (!found) {
                        String qualifiedContent = end >= 0 ? fullContent.substring(0, end) : fullContent;
                        Tag qualifiedTag = setColor && qualifiedContent == fullContent ? tag : new Tag(qualifiedContent);
                        if(currentNode.isRoot()) {
                            DefaultMutableTreeNode uncategorizedTagNode = removeUncategorizedTagNode(qualifiedTag);
                            if(uncategorizedTagNode != null) {
                                insertNode(currentNode, currentNode.getChildCount() - 1, uncategorizedTagNode);
                                currentNode = uncategorizedTagNode;
                                continue;
                            }
                        }
                        Color color = qualifiedTag.getColor();
                        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new Tag(currentTag, color));
                        insertNode(currentNode, currentNode.isRoot() ? currentNode.getChildCount() - 1 : currentNode.getChildCount(), newNode);
                        currentNode = newNode;
                        categoriesChanged = true;
                        TagReference tagReference = new TagReference(qualifiedTag);
                        ArrayList<TagReference> list = new ArrayList<>();
                        list.add(tagReference);
                        tagReferences.put(qualifiedContent, list);
                        if(qualifiedContent == fullContent )
                            return tagReference;
                    }
                    if(end < 0)
                        break;
                }

            } else {
                boolean tagFound = false;
                for(int i = 0; ! tagFound && i < rootNode.getChildCount(); i++) {
                    tagFound = ((DefaultMutableTreeNode)rootNode.getChildAt(i)).getUserObject().equals(tag);
                }

                if(! tagFound)
                    insertUncategorizedTagNodeSorted(tag);
            }
            TagReference tagReference = new TagReference(tag);
            ArrayList<TagReference> list = new ArrayList<>();
            list.add(tagReference);
            tagReferences.put(tag.getContent(), list);
            return tagReference;
        }
        else {
            Tag oldTag = mapTags.getElementAt( - addedElementIndex - 1);
            if(setColor)
                oldTag.setColor(tag.getColor());
            String content = oldTag.getContent();
            List<TagReference> references = tagReferences.get(content);
            return references.get(0);
        }
    }

    public Tag setTagColor(String tagContent, String tagColor) {
        return setTagColor(tagContent, tagColor(tagContent, tagColor));
    }

    public static Color tagColor(String tagContent, String tagColor) {
        return Optional.ofNullable(tagColor).map(ColorUtils::stringToColor)
                .orElseGet(() -> Tag.getDefaultColor(tagContent));
    }

    public Tag setTagColor(String tagContent, Color color) {
        Tag tag = registerTagReference(new Tag(tagContent, color), true).getTag();
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
        return createTagReference(spec).getTag();
    }

    public Tag registerTag(Tag tag) {
        return registerTagReference(tag).getTag();
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
            return tag.copy();
        }
        return userObject;
    }

    public boolean areCategoriesChanged() {
        return categoriesChanged;
    }

    public void fireNodeChanged(DefaultMutableTreeNode node) {
        nodes.nodeChanged(node);
    }

    public void replaceReferencedTags(List<String> replacements) {
        for(int i = 0; i < replacements.size(); i += 2) {
            String fromTag = replacements.get(i);
            if(fromTag.isEmpty())
                return;
            String toTag = replacements.get(i + 1);
            replaceReferencedTags(fromTag, toTag);
            String fromCategory = fromTag + categorySeparator;
            SortedMap<String, List<TagReference>> tailMap = tagReferences.tailMap(fromCategory);

            if(! tailMap.isEmpty()) {
                for(String from = tailMap.firstKey();
                        from.startsWith(fromCategory);
                        from = tailMap.firstKey()) {
                    String to = toTag.equals(UNCATEGORIZED_NODE) ? UNCATEGORIZED_NODE : toTag + from.substring(fromTag.length());
                    replaceReferencedTags(from, to);
                    if(tailMap.isEmpty())
                        break;
                }
            }
        }
    }

    private void replaceReferencedTags(String from, String to) {
        if(to.equals(UNCATEGORIZED_NODE)) {
            int lastSeparatorIndex = from.lastIndexOf(categorySeparator);
            if(lastSeparatorIndex >= 0) {
                to = from.substring(lastSeparatorIndex + categorySeparator.length());
            }
            else
                return;
        }
        List<TagReference> replacedTagReferences = tagReferences.remove(from);
        List<TagReference> list = tagReferences.computeIfAbsent(to, key -> new ArrayList<>());
        if(replacedTagReferences != null && ! from.isEmpty())
            list.addAll(replacedTagReferences);
    }

    public void updateTagReferences() {
        tagReferences.values().stream()
            .flatMap(List::stream)
            .map(TagReference::getTag)
            .filter(tag -> ! tagReferences.containsKey(tag.getContent()))
            .forEach(mapTags::remove);
        tagReferences.getOrDefault("", Collections.emptyList())
            .forEach(tagReference -> tagReference.setTag(Tag.REMOVED_TAG));
        updateTagReferences("", getRootNode());
    }

    private void updateTagReferences(String prefix, DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        String nextLevelPrefix;
        if(userObject instanceof Tag) {
            Tag tag = (Tag) userObject;
            String content = tag.getContent();
            String categorizedContent = prefix + content;
            Tag categorizedTag = new Tag(categorizedContent, tag.getColor());
            mapTags.addIfNotExists(categorizedTag);
            tagReferences.getOrDefault(categorizedContent, Collections.emptyList())
                .forEach(tagReference -> tagReference.setTag(categorizedTag));
            nextLevelPrefix = categorizedContent + getTagCategorySeparator();
        }
        else
            nextLevelPrefix = prefix;
        for(int i = 0; i < node.getChildCount(); i++)
            updateTagReferences(nextLevelPrefix, (DefaultMutableTreeNode) node.getChildAt(i));
    }

    public String categorizedContent(DefaultMutableTreeNode node) {
        final String tagCategorySeparator = getTagCategorySeparator();
        return new CategorizedTagForCategoryNode(node).getContent(tagCategorySeparator);
    }

    public List<Tag> getUncategorizedTags() {
        int tagCount = uncategorizedTagsNode.getChildCount();
        List<Tag> tags = new ArrayList<>(tagCount);
        for(int i = 0; i < tagCount; i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) uncategorizedTagsNode.getChildAt(i);
            tags.add((Tag) child.getUserObject());
        }
        return tags;
    }

    public void removeTag(Tag tag) {
        mapTags.remove(tag);
    }

    public void removeTagsAndCategories(String removed) {
        if(removed.isEmpty())
            return;
        int index = mapTags.getIndexOf(new Tag(removed, Color.BLACK));
        if(index >= 0) {
            String removedCategory = removed + categorySeparator;
            do {
                mapTags.remove(index);
            } while(mapTags.getSize() > index && mapTags.getElementAt(index).getContent().startsWith(removedCategory));
        }

    }

    public Tag createTag(DefaultMutableTreeNode currentNode, String text) {
        Tag tag = createTag(currentNode, text, null);
        if(tag.getColor() == null)
            tag.setColor(Tag.getDefaultColor(tag.getContent()));
        return tag;
    }

    public Tag createTag(DefaultMutableTreeNode currentNode, String text, Color color) {
        String categories = categorizedContent((DefaultMutableTreeNode) currentNode.getParent());
        String categorizedContent = categories.isEmpty() ? text : categories + categorySeparator + text;
        Tag tag = new Tag(categorizedContent, color);
        Optional<Tag> knownTag = mapTags.getElement(tag);
        if(knownTag.isPresent())
            return knownTag.get();
        mapTags.add(tag);
        TagReference tagReference = new TagReference(tag);
        ArrayList<TagReference> list = new ArrayList<>();
        list.add(tagReference);
        tagReferences.put(tag.getContent(), list);
        return tag;
    }

    public void registerTagReferenceIfUnknown(Tag tag) {
        if(! tagReferences.containsKey(tag.getContent()))
            registerTagReference(tag);
    }
}
