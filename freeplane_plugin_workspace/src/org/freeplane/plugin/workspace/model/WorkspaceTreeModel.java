package org.freeplane.plugin.workspace.model;


public interface WorkspaceTreeModel {
	/**
	 * @param node
	 * @param targetNode
	 * @return
	 */
	public boolean addNodeTo(AWorkspaceTreeNode node, AWorkspaceTreeNode targetNode);
	
	/**
	 * @param node
	 * @param targetNode
	 * @param allowRenaming
	 * @return
	 */
	public boolean addNodeTo(AWorkspaceTreeNode node, AWorkspaceTreeNode targetNode, boolean allowRenaming);

	/**
	 * @param node
	 * @param targetNode
	 * @param allowRenaming
	 * @return
	 */
	public boolean insertNodeTo(AWorkspaceTreeNode node, AWorkspaceTreeNode targetNode, int atPos, boolean allowRenaming);
	
	/**
	 * @param node
	 */
	public void removeAllElements(AWorkspaceTreeNode node);
	
	/**
	 * @param node
	 */
	public void removeNodeFromParent(AWorkspaceTreeNode node);
	
	/**
	 * @param node
	 */
	public void cutNodeFromParent(AWorkspaceTreeNode node);
	
	public void nodeMoved(AWorkspaceTreeNode node, Object from, Object to);
	
	public void nodeChanged(AWorkspaceTreeNode node, Object oldValue, Object newValue);
		
	public void changeNodeName(AWorkspaceTreeNode node, String newName) throws WorkspaceModelException;
	
	/**
	 * @param key
	 * @return
	 */
	public boolean containsNode(String key);
	
	/**
	 * @param key
	 * @return
	 */
	public AWorkspaceTreeNode getNode(String key);
	
	public void reload(AWorkspaceTreeNode node);
	
	public AWorkspaceTreeNode getRoot();

	public void requestSave();

}
