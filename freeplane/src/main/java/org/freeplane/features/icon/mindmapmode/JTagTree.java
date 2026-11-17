/*
 * Created on 14 Feb 2025
 *
 * author dimitry
 */
package org.freeplane.features.icon.mindmapmode;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.ToIntFunction;

import javax.swing.DropMode;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.freeplane.features.icon.Tag;
import org.freeplane.features.icon.TagCategories;

class JTagTree extends FilterableJTree {
	private static final long serialVersionUID = 1L;
	private final TagCategories tagCategories;
	private final Font tagFont;

	JTagTree(TagCategories tagCategories, Font tagFont) {
		super(tagCategories.getNodes());
		this.tagCategories = tagCategories;
		this.tagFont = tagFont;
        if(! GraphicsEnvironment.isHeadless()) {
            setEditable(true);
            getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
            setInvokesStopCellEditing(true);
            setDragEnabled(true);
            setDropMode(DropMode.ON_OR_INSERT);
            setCellRenderer(new TagCellRenderer(tagCategories));
            setToggleClickCount(0);
            setFont();
        }
	}

	/**
	 * Installs the lookup used by the tag renderer to append the current usage count.
	 */
	void setUsageProvider(ToIntFunction<DefaultMutableTreeNode> usageOf) {
		if (GraphicsEnvironment.isHeadless())
			return;
		// Keep counting outside the renderer: the tree only supplies each visible node.
		setCellRenderer(new TagCellRenderer(tagCategories, usageOf));
		// The new renderer changes cell contents, so refresh already-visible rows immediately.
		revalidate();
		repaint();
	}

	@Override
	public boolean isPathEditable(TreePath path) {
	    Object lastPathComponent = path.getLastPathComponent();
	    if (!(lastPathComponent instanceof DefaultMutableTreeNode))
	        return false;
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode) lastPathComponent;
	    return tagCategories.containsTag(node);
	}

	@Override
	public void setUI(TreeUI ui) {
		super.setUI(ui);
		if(tagFont != null) {
			setFont();
		}
	}

	private void setFont() {
		final Font font = tagFont.deriveFont(getFont().getSize2D());
		setFont(font);
		Rectangle2D rect = font.getStringBounds("*" , 0, 1,
				new FontRenderContext(new AffineTransform(), true, true));
		double textHeight = rect.getHeight();
		setRowHeight((int)  Math.ceil(textHeight * 1.4));
	}

	@Override
	public void cancelEditing() {
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode) getEditingPath().getLastPathComponent();
	    Tag tag = tagCategories.tagWithoutCategories(node);
	    super.cancelEditing();
	    if(tag.isEmpty() && node.isLeaf())
	        tagCategories.removeNodeFromParent(node);
	}


    TagCategorySelection createTransferable() {
        try {
            final TreePath[] selectionPaths = getSelectedNodePaths();
            if(selectionPaths == null)
                return null;
            String lastTransferableId = UUID.randomUUID().toString();
            StringWriter tagCategoryWriter = new StringWriter();
            StringWriter tagWriter = new StringWriter();
            for(TreePath treePath: selectionPaths) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
                tagCategories.writeTagCategories(node, "", tagCategoryWriter);
                tagCategories.writeCategorizedTag(node, tagWriter);
            }
            TagCategorySelection stringSelection = new TagCategorySelection(lastTransferableId, tagCategoryWriter.toString(), tagWriter.toString());
            return stringSelection;
        } catch (IOException e) {
            return null;
        }
    }

    TreePath[] getSelectedTagPaths() {
        return getSelectedPaths(false);
    }


    TreePath[] getSelectedNodePaths() {
        return getSelectedPaths(true);
    }

    private TreePath[] getSelectedPaths(boolean includeNonTags) {
        TreePath[] paths = getSelectionPaths();
        if (paths == null || paths.length == 0) {
            return null;
        }

        List<TreePath> filteredPaths = new ArrayList<>();

        for (TreePath path : paths) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if(includeNonTags || tagCategories.containsTag(node))
                filteredPaths.add(path);
            else
                return null;
        }

        removeDescendants(filteredPaths);

        if (filteredPaths.isEmpty()) {
            return null;
        }

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

	@Override
	public Dimension getMinimumSize() {
		 return super.getMinimumSize();

	}
	@Override
	public Dimension getMaximumSize() {
		 return super.getMaximumSize();

	}
	@Override
	public Dimension getPreferredSize() {
		 return super.getPreferredSize();
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		 super.setBounds(x, y, width, height);
	}



}
