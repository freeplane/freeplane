package org.freeplane.api;

import java.util.Date;

/** The currently selected node: <code>node</code> - read-write. */
public interface Node extends NodeRO {
	/** adds a new Connector to the given target node and returns the new
	 * connector for optional further editing (style); also enlists the
	 * Connector on the target Node object. */
	Connector addConnectorTo(Node target);

	/** as above, using String targetNodeId instead of Node object to establish the connector. */
	Connector addConnectorTo(String targetNodeId);

	/** inserts *new* node as child, takes care of all construction work and
	 * internal stuff inserts as last child. */
	Node createChild();

	/** like {@link #createChild()} but sets the node text to the given text.
	 * <pre>
	 * // instead of
	 * def child = node.createChild(); child.setObject(value);
	 * // use
	 * def child = node.createChild(value);
	 * </pre>
	 * @since 1.2 */
	Node createChild(Object value);

	/** inserts *new* node as child, takes care of all construction work and
	 * internal stuff */
	Node createChild(int position);

	/** inserts a copy of node as a new child.
	 * @since 1.2 */
	Node appendChild(NodeRO node);

	/** inserts a copy of the branch starting with node as a new child branch.
	 * @since 1.2 */
	Node appendBranch(NodeRO node);

	/** inserts the node as a clone of toBeCloned <em>including</em> its current and/or future
	 * subtree. That is all changes of descendent nodes of toBeCloned are reflected in the subtree
	 * of the new node <em>and vice versa</em>.
	 * <br><em>Note:</em> Cloning works symmetrically so we could better speak of two
	 * shared nodes instead of clone and cloned since none of both is privileged.
	 * @return the new child node
	 * @throws IllegalArgumentException if
	 *     a) this node (the to-be-parent) is contained in the subtree of toBeCloned,
	 *     b) toBeCloned is the root node,
	 *     c) toBeCloned comes from a different map.
	 * @since 1.5 */
	Node appendAsCloneWithSubtree(NodeRO toBeCloned);

	/** inserts the node as a clone of toBeCloned <em>without</em> its current and/or future
	 * subtree. That is toBeCloned and the new node have children of their own.
	 * <br><em>Note:</em> Cloning works symmetrically so we could better speak of two
	 * shared nodes instead of clone and cloned since none of both is privileged.
	 * @return the new child node
	 * @throws IllegalArgumentException if
	 *     a) this node (the to-be-parent) is contained in the subtree of toBeCloned,
	 *     b) toBeCloned is the root node,
	 *     c) toBeCloned comes from a different map.
	 * @since 1.5 */
	Node appendAsCloneWithoutSubtree(NodeRO toBeCloned);

	/** inserts the node(s) copied from clipboard as clone(s). Errors like
	 * if the clipboard doesn't contain proper content will only be reported to the log.
	 * You should prefer {@link #appendAsCloneWithSubtree(Proxy.NodeRO)} or {@link #appendAsCloneWithoutSubtree(Proxy.NodeRO)}
	 * instead if possible - they give you more control.
	 * @since 1.5 */
	void pasteAsClone();

	void delete();

	void moveTo(Node parentNode);

	void moveTo(Node parentNode, int position);

	/** removes the given connector on both sides. */
	void removeConnector(Connector connectorToBeRemoved);

	/**
	 * A node's text is String valued. This methods provides automatic conversion to String in the same way as
	 * for {@link #setText(Object)}, that is special conversion is provided for dates and calendars, other
	 * types are converted via value.toString().
	 *
	 * If the conversion result is not valid HTML it will be automatically converted to HTML.
	 *
	 * @param details An object for conversion to String. Use null to unset the details. Works well for all types
	 *        that {@link Convertible} handles, particularly {@link Convertible}s itself.
	 * @since 1.2
	 */
	void setDetails(Object details);

    /** Sets the raw (HTML) note text. */
    void setDetailsText(String html);

	/** use node.hideDetails = true/false to control visibility of details.
	 * @since 1.2 */
	void setHideDetails(boolean hide);

	void setFolded(boolean folded);

	/** set to true if this node should be freely positionable:
	 * <pre>
	 *   node.free = true
	 *   node.style.floating = true
	 * </pre>
     * @since 1.2 */
    void setFree(boolean free);

    void setMinimized(boolean shortened);

	/**
	 * Set the note text:
	 * <ul>
	 * <li>This methods provides automatic conversion to String in a way that node.getNote().getXyz()
	 *     methods will be able to convert the string properly to the wanted type.
	 * <li>Special conversion is provided for dates and calendars: They will be converted in a way that
	 *     node.note.date and node.note.calendar will work. All other types are converted via value.toString().
	 * <li>If the conversion result is not valid HTML it will be automatically converted to HTML.
	 * </ul>
	 * <p>
	 * <pre>{@code
	 *   // converts numbers and other stuff with toString()
	 *   node.note = 1.2
	 *   assert node.note.text == "<html><body><p>1.2"
	 *   assert node.note.plain == "1.2"
	 *   assert node.note.num == 1.2d
	 *   // == dates
	 *   // a date in some non-UTC time zone
	 *   def date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").
	 *       parse("1970-01-01 00:00:00.000-0200")
	 *   // converts to "1970-01-01T02:00:00.000+0000" (GMT)
	 *   // - note the shift due to the different time zone
	 *   // - the missing end tags don't matter for rendering
	 *   node.note = date
	 *   assert node.note == "<html><body><p>1970-01-01T02:00:00.000+0000"
	 *   assert node.note.plain == "1970-01-01T02:00:00.000+0000"
	 *   assert node.note.date == date
	 *   // == remove note
	 *   node.note = null
	 *   assert node.note.text == null
	 * }</pre>
	 * @param value An object for conversion to String. Works well for all types that {@link Convertible}
	 *        handles, particularly {@link Convertible}s itself.
	 * @since 1.2 (note that the old setNoteText() did not support non-String arguments.
	 */
	void setNote(Object value);

	/** Sets the raw (HTML) note text. */
	void setNoteText(String html);

	/** If <code>value</code> is a String the node object is set to it verbatim. For all other argument types it's
	 * an alias for {@link #setObject(Object)}.
	 * <pre>
	 * node.text = '006'
	 * assert node.object.class.simpleName == "String"
	 * node.object = '006'
	 * assert node.text == '6'
	 * assert node.object.class.simpleName == "Long"
	 * </pre>
	 * @see #setObject(Object)
	 * @since 1.2, semantics changed for Strings with 1.2.17 */
	void setText(Object value);

	/**
	 * A node's text object is normally String valued but it can be of any type since every Object can be converted
	 * to String for display. This methods provides automatic conversion to String in a way that node.to.getXyz()
	 * methods will be able to convert the string properly to the wanted type.
	 * <p>
	 * Special support is provided for numbers, dates and calendars that are stored unconverted. For display of
	 * them a standard formatter is used (use #setFormat() to change it). You may also pass
	 * {@link org.freeplane.features.format.IFormattedObject} instances ({@link org.freeplane.features.format.FormattedDate},
	 * {@link org.freeplane.features.format.FormattedNumber} or {@link org.freeplane.features.format.FormattedObject})
	 * directly to determine the format in one pass.
	 * <p>
	 * All other types are converted via value.toString().
	 * <p><b>Numbers</b>
	 * <pre>
	 * double number = 1.2222222d
	 * node.object = number
	 * // to enable math with node.object its type is not FormattedNumber
	 * assert node.object.class.simpleName == "Double"
	 * assert node.to.object.class.simpleName == "Double"
	 * // use globally bound TextUtils object
	 * def defaultNumberFormat = textUtils.defaultNumberFormat
	 * assert node.format != null
	 * // e.g. "1.22"
	 * assert node.text == defaultNumberFormat.format(number)
	 * assert node.to.num == number
	 * assert node.to.num + 1.0 == number + 1.0
	 * assert node.object + 1.0 == number + 1.0
	 * </pre>
	 * <p><b>Dates</b>
	 * <pre>
	 * def date = new Date(0) // when Unix time began
	 * node.object = date
	 * assert node.object.class.simpleName == "FormattedDate"
	 * assert node.to.object.class.simpleName == "FormattedDate"
	 * // use globally bound TextUtils object
	 * def defaultDateFormat = textUtils.defaultDateFormat
	 * assert node.object.toString() == defaultDateFormat.format(date)
	 * assert node.format == defaultDateFormat.pattern
	 * // e.g. "01/01/1970"
	 * assert node.text == defaultDateFormat.format(date)
	 * assert node.to.date == date
	 * </pre>
	 * <p><b>Date/Time</b>
	 * <pre>
	 * def date = new Date(0) // when Unix time began
	 * // the default format for dates does not contain a time component. Use node.dateTime to override it.
	 * node.dateTime = date
	 * assert node.object.class.simpleName == "FormattedDate"
	 * assert node.to.object.class.simpleName == "FormattedDate"
	 * // use globally bound TextUtils object
	 * def defaultDateFormat = textUtils.defaultDateTimeFormat
	 * assert node.object.toString() == defaultDateFormat.format(date)
	 * assert node.format == defaultDateFormat.pattern
	 * // e.g. "01/01/1970 01:00"
	 * assert node.text == defaultDateFormat.format(date)
	 * assert node.to.date == date
	 * </pre>
	 * @param value A not-null object.
	 * @since 1.2 */
	void setObject(Object value);

	/** sets the node text to a default formatted datetime object. (After setObject(Date) no time component is
	 * displayed so use this method if you want the time to be displayed.)
	 * @see #setObject(Object)
	 * @since 1.2 */
	void setDateTime(Date date);

	/** Converts data to a <a href="http://www.freesoft.org/CIE/RFC/1521/7.htm">BASE64</a> encoded string and
	 * sets it as this node's text. Long lines are folded to a length a bit less than 80.
	 * @since 1.2 */
	void setBinary(byte[] data);

	/** sets the format string of the formatter. It has to be appropriate for the data type of the contained object,
	 * otherwise the format is simply ignored. For instance use "dd.MM.yyyy" for dates but not for numbers:
	 * <pre>
	 * node.object = new Date()
	 * node.format = "dd.MMM.yyyy"  // ok: "13.07.2011"
	 * node.format = "#.00"  // still "13.07.2011". See log: "cannot format 13.07.2011 with #.00: multiple points"
	 * </pre>
	 * Numbers:
	 * <pre>
	 * node.object = 1.122
	 * node.format = "#.##"   // ok: "1.12" (US, GB, ...) or "1,12" (Germany, ...)
	 * node.format = "#.0000" // ok: "1.1220" (US, GB, ...) or "1,1220" (Germany, ...)
	 * </pre>
	 * @see #setObject(Object)
	 * @since 1.2 */
	void setFormat(String format);

	void setLastModifiedAt(Date date);

	void setCreatedAt(Date date);

	// Attributes
	/**
	 * Allows to set and to change attribute like array (or map) elements.
	 * See description of {@link Attributes} for details.
	 * @param value An object for conversion to String. Works well for all types that {@link Convertible}
	 *        handles, particularly {@link Convertible}s itself. Use null to unset an attribute.
	 * @return the new value
	 */
	Object putAt(String attributeName, Object value);

	/** allows to set all attributes at once:
	 * <pre>
	 *   node.attributes = [:] // clear the attributes
	 *   assert node.attributes.size() == 0
	 *   node.attributes = ["1st" : "a value", "2nd" : "another value"] // create 2 attributes
	 *   assert node.attributes.size() == 2
	 *   node.attributes = ["one attrib" : new Double(1.22)] // replace all attributes
	 *   assert node.attributes.size() == 1
	 *   assert node.attributes.getFirst("one attrib") == "1.22" // note the type conversion
	 *   assert node["one attrib"] == "1.22" // here we compare Convertible with String
	 * </pre>
	 */
	void setAttributes(java.util.Map<String, Object> attributes);

	void setLeft(boolean isLeft);

    /** Returns true if the node is password protected, no matter if currently accessible (password entered) or not.
     * @since 1.3.6 */
	boolean hasEncryption();

    /** decrypts a node and remove the password protection.
     * @since 1.3.6 */
    void removeEncryption(String password);

	/** Returns true if the node has password protection and is currently unaccessible (password has to be entered).
	 * @since 1.3.6 */
	boolean isEncrypted();

	/** encrypts a node. If the node has child nodes the branch is folded.
	 * @since 1.3.6 */
	void encrypt(String password);

	/** decrypts a node without removing the encryption.
     * @since 1.3.6 */
	void decrypt(String password);

    /**@since 1.3.7 */
	void setHorizontalShift(final int horizontalShift);

    /** use length units like "1 cm" or "6 pt"
     * @since 1.5.6 */
	void setHorizontalShift(String verticalShift);

	/**@since 1.3.7 */
	void setVerticalShift(final int verticalShift);

    /** use length units like "1 cm" or "6 pt"
     * @since 1.5.6 */
	void setVerticalShift(String verticalShift);

    /**@since 1.3.7 */
	void setMinimalDistanceBetweenChildren(final int minimalDistanceBetweenChildren);

    /** use length units like "1 cm" or "6 pt"
     * @since 1.5.6 */
	void setMinimalDistanceBetweenChildren(String verticalShift);

	/**
	 * A sort method that uses the result of the lambda ("block") for comparison. As this closure
	 * will be called with a node as an argument (to be referenced by <code>it</code>) the search can
	 * evaluate every node property, like attributes, icons, node text or notes.
	 * <p>
	 * Examples:
	 * <pre>
	 *    // sort by details text
	 *    node.sortChildrenBy{ it.details.to.plain }
	 *    // sort numerically
	 *    node.sortChildrenBy{ it.to.num0 }
	 * </pre>
	 * @param comparable a lambda that returns a Comparable value like a String. The closure will receive
	 *        a NodeModel as an argument.
	 * @since 1.4.1
	 */
	void sortChildrenBy(NodeToComparableMapper comparable);

	 /**
	  * Sets alias of the node
	  *
	  *
	  *  @since 1.7.1 */
	void setAlias(String alias);

	 /**
	  * Sets if the node can be accessed using global accessor, see {@link NodeRO#at(String)}
	  *
	  *  @since 1.7.1 */
	void setIsGlobal(boolean value);
}