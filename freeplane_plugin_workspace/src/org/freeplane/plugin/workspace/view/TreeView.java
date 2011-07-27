package org.freeplane.plugin.workspace.view;

import java.awt.BorderLayout;
import java.awt.event.ComponentListener;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.freeplane.plugin.workspace.controller.NodeExpansionListener;
import org.freeplane.plugin.workspace.controller.NodeSelectionListener;
import org.freeplane.plugin.workspace.model.WorkspaceTreeModel;

public class TreeView extends JPanel {

	private static final long serialVersionUID = 1L;
	
	protected JTree m_tree;
	protected DefaultTreeModel m_model;
	protected JTextField m_display;

	public TreeView() {
		this(new DefaultMutableTreeNode());
	}

	public TreeView(MutableTreeNode conigurationRoot) {
		this.setLayout(new BorderLayout());
		m_model = new WorkspaceTreeModel(conigurationRoot);

		m_tree = new JTree(m_model);
		m_tree.putClientProperty("JTree.lineStyle", "Angled");
		m_tree.setCellRenderer(new WorkspaceNodeRenderer());
		m_tree.setCellEditor(new WorkspaceCellEditor(m_tree, (DefaultTreeCellRenderer) m_tree.getCellRenderer()));
		m_tree.addTreeExpansionListener(new NodeExpansionListener());
		m_tree.addTreeSelectionListener(new NodeSelectionListener());
		m_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		m_tree.setShowsRootHandles(false);
		m_tree.setEditable(true);
		m_tree.setDragEnabled(true);
		
		this.add(new JScrollPane(m_tree), BorderLayout.CENTER);
	}
	
	public JTree getTree() {
		return m_tree;
	}

	public DefaultTreeModel getTreeModel() {
		return m_model;
	}

	public void addTreeMouseListener(MouseListener l) {
		this.m_tree.addMouseListener(l);
	}

	public void addTreeComponentListener(ComponentListener l) {
		this.m_tree.addComponentListener(l);
	}
}
