package org.freeplane.plugin.workspace.view;

import java.awt.BorderLayout;
import java.awt.event.ComponentListener;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

import org.freeplane.plugin.workspace.controller.NodeExpansionListener;
import org.freeplane.plugin.workspace.controller.NodeSelectionListener;

public class TreeView extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final int view_margin = 3;
	
	protected JTree m_tree;
	protected JTextField m_display;

	public TreeView() {
		this.setLayout(new BorderLayout());

		m_tree = new JTree();
		m_tree.setBorder(BorderFactory.createEmptyBorder(2, view_margin, view_margin, view_margin));
		m_tree.putClientProperty("JTree.lineStyle", "Angled");
		m_tree.setCellRenderer(new WorkspaceNodeRenderer());
		m_tree.setCellEditor(new WorkspaceCellEditor(m_tree, (DefaultTreeCellRenderer) m_tree.getCellRenderer()));
		m_tree.addTreeExpansionListener(new NodeExpansionListener());
		m_tree.addTreeSelectionListener(new NodeSelectionListener());
		m_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		m_tree.setShowsRootHandles(false);
		m_tree.setEditable(true);		
		
		this.add(new JScrollPane(m_tree), BorderLayout.CENTER);
		
		// TODO: DOCEAR - choose actions to use in Toolbar
//		WorkspaceToolBar workspaceToolBar = new WorkspaceToolBar();
//		add(workspaceToolBar, BorderLayout.NORTH);
	}
	
	public JTree getTreeView() {
		return m_tree;
	}
	
	public void addTreeMouseListener(MouseListener l) {
		this.m_tree.addMouseListener(l);
	}

	public void addTreeComponentListener(ComponentListener l) {
		this.m_tree.addComponentListener(l);
	}
	
}
