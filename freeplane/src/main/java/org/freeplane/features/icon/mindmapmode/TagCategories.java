/*
 * Created on 27 Apr 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon.mindmapmode;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.Optional;
import java.util.Scanner;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.icon.IconRegistry;
import org.freeplane.features.icon.Tag;

class TagCategories {
    private final IconRegistry registry = new IconRegistry();
    final DefaultTreeModel nodes;
    private File tagCategoryFile;

    TagCategories(final File tagCategoryFile){
        this.tagCategoryFile = tagCategoryFile;
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Tags");
        nodes = new DefaultTreeModel(rootNode);
        load();
    }

    static void writeTagCategories(DefaultMutableTreeNode node, String indent,
            boolean withColor, Writer writer) throws IOException {
        int childCount = node.getChildCount();
        Object userObject = node.getUserObject();
        if (userObject instanceof Tag) {
            Tag tag = (Tag) userObject;
            writer.append(indent + tag.getContent());
            if (withColor && childCount == 0)
                writer.append(ColorUtils.colorToRGBAString(tag.getIconColor()));
            writer.append("\n");
            indent = indent + " ";
        }
        for (int i = 0; i < childCount; i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            writeTagCategories(childNode, indent, withColor, writer);
        }
    }

    boolean isEmpty() {
        return getRootNode().isLeaf();
    }

    void readTagCategories(DefaultMutableTreeNode parentNode, boolean withColor, Scanner scanner) {
        scanner.useDelimiter(System.lineSeparator());
        DefaultMutableTreeNode lastNode = parentNode;
        int lastLevel = -1;
        int currentLevel = 0;
        while (scanner.hasNext()) {
            String line = scanner.next();
            currentLevel = 0;
            while (currentLevel < line.length() && line.charAt(currentLevel) == ' ') {
                currentLevel++;
            }
            String nodeName = line.trim();
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(nodeName);
            if (currentLevel > lastLevel) {
                lastNode.add(newNode);
            } else {
                Object userObject = lastNode.getUserObject();
                lastNode.setUserObject(readTag((String) userObject, withColor));
                for (int i = currentLevel; i <= lastLevel; i++) {
                    lastNode = (DefaultMutableTreeNode) lastNode.getParent();
                    Object parentObject = lastNode.getUserObject();
                    if (parentObject instanceof String)
                        lastNode.setUserObject(readTag((String) parentObject, false));
                }
                lastNode.add(newNode);
            }
            lastNode = newNode;
            lastLevel = currentLevel;
        }
        Object userObject = lastNode.getUserObject();
        lastNode.setUserObject(readTag((String) userObject, withColor));
        for (int i = currentLevel; i <= lastLevel; i++) {
            lastNode = (DefaultMutableTreeNode) lastNode.getParent();
            Object parentObject = lastNode.getUserObject();
            if (parentObject instanceof String)
                lastNode.setUserObject(readTag((String) parentObject, false));
        }
    }

    private Tag readTag(String spec, boolean withColor) {
        if (withColor) {
            int colorIndex = spec.lastIndexOf("#");
            String content = spec.substring(0, colorIndex);
            String colorSpec = spec.substring(colorIndex);
            return registry.setTagColor(content, colorSpec);
        } else
            return registry.createTag(spec);
    }

    private void load() {
        try (Scanner scanner = new Scanner(tagCategoryFile)){
            readTagCategories(getRootNode(), true, scanner);
        } catch (FileNotFoundException e1) {/**/}
    }

    DefaultMutableTreeNode getRootNode() {
        return (DefaultMutableTreeNode) nodes.getRoot();
    }

    IconRegistry getRegistry() {
        return registry;
    }

    void addTreeModelListener(TreeModelListener treeModelListener) {
       nodes.addTreeModelListener(treeModelListener);
    }

    void removeTreeModelListener(TreeModelListener l) {
        nodes.removeTreeModelListener(l);
    }

    TreeNode[] addNode(MutableTreeNode parent) {
        if(parent == null)
            parent = getRootNode();
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(Tag.EMPTY_TAG);
        nodes.insertNodeInto(newNode, parent, parent.getChildCount());
        return nodes.getPathToRoot(newNode);
   }

    void removeNodeFromParent(MutableTreeNode node) {
        nodes.removeNodeFromParent(node);
    }

    void save() {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tagCategoryFile))){
            writeTagCategories(getRootNode(), "", true, writer);
        } catch (IOException e) {
            LogUtils.severe(e);
        }
    }

    void tagChanged(Tag tag) {
        tagChanged(getRootNode(), tag);
    }

    private void tagChanged(DefaultMutableTreeNode node, Tag updatedTag) {
        int childCount = node.getChildCount();
        Object userObject = node.getUserObject();
        if (userObject == updatedTag) {
            nodes.nodeChanged(node);
        }
        for (int i = 0; i < childCount; i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            tagChanged(childNode, updatedTag);
        }
    }

    void add(DefaultMutableTreeNode parent, String data) {
        if (parent == null) {
            parent = getRootNode();
        }
        try(Scanner st = new Scanner(new StringReader(data))){
            readTagCategories(parent, true, st);
        }
        nodes.nodeStructureChanged(parent);
    }

    Optional<Color> getColor(Tag tag) {
        return registry.getTagColor(tag);
    }
}