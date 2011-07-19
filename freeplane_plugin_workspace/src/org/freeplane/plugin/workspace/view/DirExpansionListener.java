package org.freeplane.plugin.workspace.view;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;


//Make sure expansion is threaded and updating the tree model
//only occurs within the event dispatching thread.
public class DirExpansionListener implements TreeExpansionListener {
	
	private DefaultTreeModel m_model;

	public DirExpansionListener (DefaultTreeModel model) {
		this.m_model = model;
	}
	
    public void treeExpanded(TreeExpansionEvent event) {
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode)event.getPath().getLastPathComponent();
        final FileNode fnode = extractFileNode(node);
        Thread runner = new Thread() {
            public void run() {
                if (fnode != null && fnode.expand(node)) {
                    Runnable runnable = new Runnable() {
                        public void run() {
                            m_model.reload(node);
                        }
                    };
                    SwingUtilities.invokeLater(runnable);
                }
            }
        };
        runner.start();
    }
    
    public void treeCollapsed(TreeExpansionEvent event) {
    	
    }
    
    private FileNode extractFileNode(DefaultMutableTreeNode node) {
        if (node == null)
            return null;
        Object obj = node.getUserObject();
        if (obj instanceof IconData)
            obj = ((IconData)obj).getObject();
        if (obj instanceof FileNode)
            return (FileNode)obj;
        else
            return null;
    }
}