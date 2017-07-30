# for single node: 
Submit and update CHILDREN, TEXT, NOTE, DETAILS, ATTRIBUTES, ICONS, OTHER_CONTENT

# for map and for inserted subtree:
Submit and update all nodes with all kinds of content recursively

# process events
* onNodeInserted
* onNodeDeleted
* onNodeMoved 
* nodeChanged

# for attached single clones and cloned subtrees
nodeID : attached nodeID, content: resulting list of clone node ids


#for detached single clones and cloned subtrees
nodeID : detached nodeID, content: resulting list of remaining clone node ids
