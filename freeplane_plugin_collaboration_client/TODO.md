# Start without clones

## for single node: 
Submit and update CHILDREN, TEXT, NOTE, DETAILS, ATTRIBUTES, ICONS, OTHER_CONTENT

## for whole map and for inserted subtree:
Submit and update all nodes with all kinds of content recursively

## process events to create Update objects
* onNodeInserted
* onNodeDeleted
* onNodeMoved 
* nodeChanged

## collect Update objects in UpdateBatch

## integrate websockets
for the beginning everything goes through undo buffer.
In the next step not adding some updates to undo buffer can be considered.

# support clones

## for attached single clones and cloned subtrees
nodeID : attached nodeID, content: resulting list of clone node ids


## for detached single clones and cloned subtrees
nodeID : detached nodeID, content: resulting list of remaining clone node ids
