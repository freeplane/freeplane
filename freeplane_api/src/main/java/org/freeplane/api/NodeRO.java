package org.freeplane.api;

import java.util.Collection;
import java.util.Date;
import java.util.List;




/** The currently selected node: <code>node</code> - read-only. */
public interface NodeRO {
	/**
	 * Returns a single node located by path given as a string argument.
	 *
	 * <p>If no node or more than one node is available at the specified position, IllegalArgumentException is thrown.
	 *
	 * <p>The path is a concatenation of path elements described below.
	 *
	 *  <h2> Path examples:</h2>
	 *  <pre>
	 *  node.at(":'house'/'kitchen'/~chairs/~2")
	 *     - take global node with text 'house',
	 *     - in its subtree node find with text 'kitchen'
	 *     - find its child node with alias 'chairs'
	 *     - return the second child node of the chairs
	 *
	 *  node.at(".. 'house'/**&#47;~dog")
	 *      -- return node with alias 'dog' within subtree of ancestor node with text 'house'
	 *  </pre>
	 *
	 * Following path elements are defined:
	 *
	 * <h2>Child node:</h2>
	 * <pre>
	 * 'node text' or /"node text" : child node with text 'node text'
	 * 'node...' : child node containing text starting with 'node'
	 * * or '...' : any child node
	 * ~1 : child node at position 1 ( any number starting with 1 is allowed)
	 * ~nodeAlias : child node with alias 'nodeAlias', node alias can not be a number
	 * because numbers are reserved for the previous selector.
	 * </pre>
	 *
	 * <h2>Descendant node:</h2>
	 * <pre> /**&#47; </pre>
	 *
	 * <h2>Parent node:</h2>
	 * <pre> ..</pre>
	 *
	 * <h2>Ancestor node:</h2>
	 * <pre>
	 * ..'node text' or "node text" : the closest ancestor node with text 'node text'
	 * ..'node...' : the closest ancestor node containing text starting with 'node'
	 * ..~2  : second ancestor node also the parent node of the parent node (any positive number is allowed)
	 * ..~nodeAlias : the closest ancestor node with alias 'nodeAlias', node alias can not be a number
	 * </pre>
	 *
	 * <h2>Root node</h2>
	 * Prefix <b>{@code / }</b> as the first path character means the path starts from the root node.
	 *
	 * <pre>
	 * /'node text' or /"node text" : child node of the root with text 'node text'
	 * /'node...' : child node of the root containing text starting with 'node'
	 * /~nodeAlias : child node of the root with alias 'nodeAlias', node alias can not be a number
	 * </pre>
	 *
	 * <h2>Global node</h2>
	 * (allowed only as the first path element).
	 * Here global node is a node carrying global flag which can be set using menu or by script.
	 *
	 * <pre>
	 * :'node text' or :"node text" : global node with text 'node text'
	 * :'node...' : global node containing text starting with 'node'
	 * :~nodeAlias : global node with alias 'nodeAlias', node alias can not be a number
	 * </pre>

	 * @since 1.7.1 */
	Node at(String path);

	/**
	 * Returns a list of all nodes matching given path.
	 * It can contain arbitrary node number or be empty.
	 *
	 * Path syntax is described in the {@link #at(String) at} method.
	 *
	 * @since 1.7.1 */
	List<? extends Node> allAt(String path);

	Attributes getAttributes();

	/** allows to access attribute values like array elements. Note that the returned type is a
	 * {@link Convertible}, not a String. Nevertheless it behaves like a String in almost all respects,
	 * that is, in Groovy scripts it understands all String methods like lenght(), matches() etc.
	 * <pre>
	 *   // standard way
	 *   node.attributes.set("attribute name", "12")
	 *   // implicitely use getAt()
	 *   def val = node["attribute name"]
	 *   // use all conversions that Convertible provides (num, date, string, ...)
	 *   assert val.num == new Long(12)
	 *   // or use it just like a string
	 *   assert val.startsWith("1")
	 *   // check for availability of an attribute this way:
	 *   if (node["unknown attribute"])
	 *      // surprise: the node has an attribute with key "unknown attribute"
	 * </pre>
	 * @throws org.freeplane.plugin.script.ExecuteScriptException
	 * @since 1.2
	 */
	Convertible getAt(String attributeName);

	/** a reference to an accessor object for cloud properties of this node. This property is never null.
	 * @since 1.2
	 */
	Cloud getCloud();

    /** returns the index (0..) of this node in the (by Y coordinate sorted)
	 * list of this node's children. Returns -1 if childNode is not a child
	 * of this node. */
	int getChildPosition(Node childNode);

	/** returns the children of this node ordered by Y coordinate. */
	List<? extends Node> getChildren();

	Collection<? extends Connector> getConnectorsIn();

	Collection<? extends Connector> getConnectorsOut();

	/** returns the raw HTML text of the details if there is any or null otherwise.
	 * @since 1.2 */
	String getDetailsText();

	/** returns the text of the details as a Convertible like {@link #getNote()} for notes:
	 * <ul>
	 * <li>node.details.to.plain plain text of the node, possibly after formula evaluation
	 * <li>node.details.plain the same.
	 * <li>node.details.string the same.
	 * <li>node.details.html the raw html text. No formula evaluation is applied.
	 * <li>node.details.text the same.
	 * </ul>
	 * @since 1.2 */
	Convertible getDetails();

	/** returns true if node details are hidden.
	 * @since 1.2 */
	boolean getHideDetails();

	ExternalObject getExternalObject();

	/** a reference to an accessor object for icons of this node. This property is never null. */
	Icons getIcons();

	/** a reference to an accessor object for link properties of this node. This property is never null. */
	Link getLink();

	/** use it to create and inspect {@link Reminder}s. This property is never null. */
	Reminder getReminder();

	/** the map this node belongs to. */
	Map getMap();

	/** @deprecated since 1.2 - use Node.getId() instead. */
	@Deprecated
	String getNodeID();

	/** @since 1.2 */
	String getId();

	/** if countHidden is false then only nodes that are matched by the
	 * current filter are counted. */
	int getNodeLevel(boolean countHidden);

	/** returns the text of the details as a Convertible. Convertibles behave like Strings in most respects.
	 * Additionally String methods are overridden to handle Convertible arguments as if the argument were the
	 * result of Convertible.getText().
     * <ul>
     * <li>node.note.to.plain plain text of the node, possibly after formula evaluation
     * <li>node.note.plain the same.
     * <li>node.note.text the same.
     * <li>node.note.html the raw html text. No formula evaluation is applied.
     * <li>node.note.string the same.
     * </ul>
	 * @return Convertible getString(), getText() and toString() will return plain text instead of the HTML.
	 *         Use {@link #getNoteText()} to get the HTML text.
	 * @throws org.freeplane.plugin.script.ExecuteScriptException
	 * @since 1.2
	 */
	Convertible getNote();

	/** Returns the HTML text of the node. (Notes always contain HTML text.)
	 * @throws org.freeplane.plugin.script.ExecuteScriptException */
	String getNoteText();

	/** @since 1.2 */
	Node getParent();

	/** @deprecated since 1.2 - use {@link #getParent()} instead. */
	@Deprecated
	Node getParentNode();

	 /**
	  * Alias of the node
	  *
	  *  @since 1.7.1 */
	String getAlias();

	 /**
	  * True if the node can be accessed using global accessor, see {@link #at(String)}
	  *
	  *  @since 1.7.1 */
	boolean getIsGlobal();

    /** a list of all nodes starting from this node upto (and including) the root node.
     * <pre>
     *   def path = pathToRoot.collect{ it.plainText }.join('.')
     * </pre>
     * @since 1.3.3 */
    List<? extends Node> getPathToRoot();

    /** returns the next node with respect to this node in depth-first order.
     * Returns null if this node is the only one in the map. */
    Node getNext();

    /** returns the previous node with respect to this node in depth-first order.
     * Returns null if this node is the only one in the map. */
    Node getPrevious();

	/** The style attributes of a node can either be changed by assigning a named style like this:
	 * <pre>node.style.name = 'style.ok'</pre>
	 * or by changing attributes for this node individually like this:
	 * <pre>node.style.textColorCode = '#FF0000'</pre>
	 * Conditional styles of a node can only be investigated by {@link Node#hasStyle(String)}. Here a script that
	 * creates an index of all nodes having the style 'todo':
	 * <pre>
	 * def todos = node.map.root.createChild('To Do')
	 * c.find{ it.hasStyle('todo') }.each {
	 *     def child = todos.createChild(it.text)
	 *     child.link.node = it
	 * }
	 * </pre> */
	NodeStyle getStyle();

	/** returns true if the node has the style of this name - either manually set or as a conditional style or it is
	 * "default" which all nodes have. The following statement will always be true:
	 * @since 1.2 */
	boolean hasStyle(String styleName);

	/** Raw text of this node which might be plain or HTML text.
	 * Possible transformations (formula evaluation, formatting, ...) are not applied.
	 * <p>
	 * See
	 * <ul>
	 * <li> {@link #getPlainText()} for plain text or use {@link org.freeplane.core.util.HtmlUtils#htmlToPlain(String)}.
	 * <li> {@link #getHtmlText()} for HTML text or use {@link org.freeplane.core.util.HtmlUtils#plainToHTML(String)}.
	 * <li> {@link #getTransformedText()} or {@link #getValue()} for text after formula evaluation.
	 * <li> {@link #getObject()} for possible typed content.
	 * <li> {@link #getTo()} for text/object conversions.
	 * </ul>
	 * @since 1.2 */
	String getText();

	/** Plain text after removal of possible HTML markup.
	 * Possible transformations (formula evaluation, formatting, ...) are not applied.
	 * @since 1.2 */
	String getPlainText();

	/** Plain text after removal of possible HTML markup. Formulas are not evaluated.
	 * @deprecated since 1.2 - use getPlainText() or getTo().getPlain() instead. */
	@Deprecated
	String getPlainTextContent();

	/** The node text as HTML markup. Returns the same as {@link #getText()} if the node text
	 * already is HTML or converts the plain text to HTML otherwise.
	 * @since 1.2 */
	String getHtmlText();

	/** Plain or HTML text of this node after possible transformation (formula evaluation, formatting, ...).
	 * @since 1.2 */
	String getTransformedText();

	/** Plain or HTML text of this node after possible transformation (formula evaluation, formatting, ...)
	 * and after text shortening.
	 * See {@link #isMinimized()} for node shortening.
	 * @since 1.2 */
	String getDisplayedText();

	/** Plain text of this node after possible transformation and forced text shortening.
	 * @since 1.2 */
	String getShortText();

	/** The object that's displayed as the node text - normally the raw text of this node (then this method is
	 * equivalent to {@link #getText()}).
	 * But in case of typed content (for numbers, dates and calendars) {@link #getObject()} returns
	 * a proper {@link org.freeplane.features.format.IFormattedObject}. Use {@link #getPlainText()} to remove HTML.
	 * See {@link Node#setObject(Object)} for details.
	 * @since 1.2 */
	Object getObject();

	/** returns the format string of the formatter if available and null otherwise.
	 * @since 1.2 */
	String getFormat();

	/**
	 * returns an object that performs conversions (method name is choosen to give descriptive code):
	 * <dl>
	 * <dt>node.to.num <dd>Long or Double, see {@link Convertible#getDate()}.
	 * <dt>node.to.date <dd>Date, see {@link Convertible#getDate()}.
	 * <dt>node.to.string <dd>Text, see {@link Convertible#getString()}.
	 * <dt>node.to.text <dd>an alias for getString(), see {@link Convertible#getText()}.
	 * <dt>node.to.object <dd>returns what fits best, see {@link Convertible#getObject()}.
	 * </dl>
	 * @return ConvertibleObject
	 * @throws org.freeplane.plugin.script.ExecuteScriptException on formula evaluation errors
	 * @since 1.2
	 */
	Convertible getTo();

	/** an alias for {@link #getTo()}.
	 * @throws org.freeplane.plugin.script.ExecuteScriptException on formula evaluation errors
	 * @since 1.2 */
	Convertible getValue();

	/** Returns a <a href="http://www.freesoft.org/CIE/RFC/1521/7.htm">BASE64</a> encoded node text
	 * (see {@link Node#setBinary(byte[])}) as a binary object. Errors are signaled by a null return value.
	 * Whitespace characters are ignored.<br>
	 * Note that this method is not able to catch all encoding errors!
	 * @since 1.2 */
	byte[] getBinary();

	/** returns true if p is a parent, or grandparent, ... of this node, or if it <em>is equal</em>
	 * to this node; returns false otherwise. */
	boolean isDescendantOf(Node p);

	/** if this node is folded. Note that the folding state only concerns the visibility of the <em>child nodes</em>. */
	boolean isFolded();

	/** returns true if this node is freely positionable.
	 * @since 1.2 */
	public boolean isFree();

	boolean isLeaf();

	boolean isLeft();

	boolean isRoot();

	/** if this node is visible or not (due to filtering). Node folding is not considered.
	 * See {@link #isFolded()} for folding state. */
	boolean isVisible();

	/** if this node's text is shortened for display. */
	boolean isMinimized();

	/** The count of node sharing their content with this node. Use {@code if (node.countNodesSharingContent() > 0)}
	 * to check if a node has any clones.
	 * <br><em>Note:</em> {@link #getCountNodesSharingContent()} &ge; {@link #getCountNodesSharingContentAndSubtree()}.
	 * @return 0 if this node is standalone or the number of other nodes sharing content otherwise.
	 * @see #getNodesSharingContent()
	 * @see Node#appendAsCloneWithSubtree(Proxy.NodeRO)
	 * @see Node#appendAsCloneWithoutSubtree(Proxy.NodeRO)
	 * @since 1.5 */
	int getCountNodesSharingContent();

	/** The count of nodes sharing their content and subtree with this node.
	 * <br><em>Note:</em> {@link #getCountNodesSharingContent()} &ge; {@link #getCountNodesSharingContentAndSubtree()}.
	 * @return 0 if this node has no other nodes it is sharing its content and subtree with or its count otherwise.
	 * @see #getNodesSharingContentAndSubtree()
	 * @see Node#appendAsCloneWithSubtree(Proxy.NodeRO)
	 * @see Node#appendAsCloneWithoutSubtree(Proxy.NodeRO)
	 * @since 1.5 */
	int getCountNodesSharingContentAndSubtree();

	/** The count of nodes sharing their content with this node.
	 * <br><em>Note:</em> {@link #getCountNodesSharingContent()} &ge; {@link #getCountNodesSharingContentAndSubtree()}.
	 * @return 0 if this node is standalone or the number of other nodes sharing content otherwise.
	 * @see #getCountNodesSharingContent()
	 * @see Node#appendAsCloneWithSubtree(Proxy.NodeRO)
	 * @see Node#appendAsCloneWithoutSubtree(Proxy.NodeRO)
	 * @since 1.5 */
	List<? extends Node> getNodesSharingContent();

	/** The nodes sharing their content and subtree with this node.
	 * @return 0 if this node has no other nodes it is sharing its content and subtree with or its count otherwise.
	 * @see #getCountNodesSharingContentAndSubtree()
	 * @see Node#appendAsCloneWithSubtree(Proxy.NodeRO)
	 * @see Node#appendAsCloneWithoutSubtree(Proxy.NodeRO)
	 * @since 1.5 */
	List<? extends Node> getNodesSharingContentAndSubtree();

	/** Starting from this node, recursively searches for nodes for which <code>closure.call(node)</code>
	 * returns true. See {@link Controller#find(NodeCondition)} for details. */
	List<? extends Node> find(final NodeCondition condition);

	/** Returns all nodes of the branch that starts with this node in breadth-first order.
	 * See {@link Controller#findAll()} for map-global searches.
	 * @since 1.2 */
	List<? extends Node> findAll();

	/** Returns all nodes of the branch that starts with this node in depth-first order.
	 * See {@link Controller#findAllDepthFirst()} for map-global searches.
	 * @since 1.2 */
	List<? extends Node> findAllDepthFirst();

	Date getLastModifiedAt();

	Date getCreatedAt();

    /**@since 1.3.7 */
	int getHorizontalShift();

    /**@since 1.3.7 */
	int getVerticalShift();

    /**@since 1.3.7 */
	int getMinimalDistanceBetweenChildren();

	/**@since 1.7.2 */
	DependencyLookup getPrecedents();

	/**@since 1.7.2 */
	DependencyLookup getDependents();
}