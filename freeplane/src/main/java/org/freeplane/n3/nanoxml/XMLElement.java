/*
 * XMLElement.java NanoXML/Java $Revision: 1.5 $ $Date: 2002/02/06 18:50:12 $
 * $Name: RELEASE_2_2_1 $ This file is part of NanoXML 2 for Java. Copyright (C)
 * 2000-2002 Marc De Scheemaecker, All Rights Reserved. This software is
 * provided 'as-is', without any express or implied warranty. In no event will
 * the authors be held liable for any damages arising from the use of this
 * software. Permission is granted to anyone to use this software for any
 * purpose, including commercial applications, and to alter it and redistribute
 * it freely, subject to the following restrictions: 1. The origin of this
 * software must not be misrepresented; you must not claim that you wrote the
 * original software. If you use this software in a product, an acknowledgment
 * in the product documentation would be appreciated but is not required. 2.
 * Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software. 3. This notice may not be
 * removed or altered from any source distribution.
 */
package org.freeplane.n3.nanoxml;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 * XMLElement is an XML element. The standard NanoXML builder generates a tree
 * of such elements.
 * 
 * @see org.freeplane.n3.nanoxml.StdXMLBuilder
 * @author Marc De Scheemaecker
 * @version $Name: RELEASE_2_2_1 $, $Revision: 1.5 $
 */
public class XMLElement implements Serializable {
	/**
	 * No line number defined.
	 */
	public static final int NO_LINE = -1;
	/**
	 * Necessary for serialization.
	 */
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates an empty element.
	 * 
	 * @param fullName
	 *            the full name of the element
	 * @param namespace
	 *            the namespace URI.
	 * @param systemID
	 *            the system ID of the XML data where the element starts.
	 * @param lineNr
	 *            the line in the XML data where the element starts.
	 */
	public static XMLElement createElement(final String fullName, final String namespace, final String systemID,
	                                       final int lineNr) {
		return new XMLElement(fullName, namespace, systemID, lineNr);
	}

	/**
	 * The attributes of the element.
	 */
	private Vector<XMLAttribute> attributes;
	/**
	 * The child elements.
	 */
	private Vector<XMLElement> children;
	/**
	 * The content of the element.
	 */
	private String content;
	/**
	 * The full name of the element.
	 */
	private String fullName;
	/**
	 * The line in the source data where this element starts.
	 */
	final private int lineNr;
	/**
	 * The name of the element.
	 */
	private String name;
	/**
	 * The namespace URI.
	 */
	private String namespace;
	/**
	 * The parent element.
	 */
	private XMLElement parent;
	/**
	 * The system ID of the source data where this element is located.
	 */
	private String systemID;

	/**
	 * Creates an empty element to be used for #PCDATA content.
	 */
	public XMLElement() {
		this(null, null, null, XMLElement.NO_LINE);
	}

	/**
	 * Creates an empty element.
	 * 
	 * @param fullName
	 *            the name of the element.
	 */
	public XMLElement(final String fullName) {
		this(fullName, null, null, XMLElement.NO_LINE);
	}

	/**
	 * Creates an empty element.
	 * 
	 * @param fullName
	 *            the full name of the element
	 * @param namespace
	 *            the namespace URI.
	 */
	public XMLElement(final String fullName, final String namespace) {
		this(fullName, namespace, null, XMLElement.NO_LINE);
	}

	/**
	 * Creates an empty element.
	 * 
	 * @param fullName
	 *            the name of the element.
	 * @param systemID
	 *            the system ID of the XML data where the element starts.
	 * @param lineNr
	 *            the line in the XML data where the element starts.
	 */
	public XMLElement(final String fullName, final String systemID, final int lineNr) {
		this(fullName, null, systemID, lineNr);
	}

	/**
	 * Creates an empty element.
	 * 
	 * @param fullName
	 *            the full name of the element
	 * @param namespace
	 *            the namespace URI.
	 * @param systemID
	 *            the system ID of the XML data where the element starts.
	 * @param lineNr
	 *            the line in the XML data where the element starts.
	 */
	public XMLElement(final String fullName, final String namespace, final String systemID, final int lineNr) {
		attributes = new Vector<XMLAttribute>();
		children = new Vector<XMLElement>(8);
		this.fullName = fullName;
		if (namespace == null) {
			name = fullName;
		}
		else {
			final int index = fullName.indexOf(':');
			if (index >= 0) {
				name = fullName.substring(index + 1);
			}
			else {
				name = fullName;
			}
		}
		this.namespace = namespace;
		content = null;
		this.lineNr = lineNr;
		this.systemID = systemID;
		parent = null;
	}

	/**
	 * Adds a child element.
	 * 
	 * @param child
	 *            the non-null child to add.
	 */
	public void addChild(final XMLElement child) {
		if (child == null) {
			throw new IllegalArgumentException("child must not be null");
		}
		if ((child.getName() == null) && (!children.isEmpty())) {
			final XMLElement lastChild = children.lastElement();
			if (lastChild.getName() == null) {
				lastChild.setContent(lastChild.getContent() + child.getContent());
				return;
			}
		}
		(child).parent = this;
		children.addElement(child);
	}

	/**
	 * Creates an empty element.
	 * 
	 * @param fullName
	 *            the name of the element.
	 */
	public XMLElement createElement(final String fullName) {
		return new XMLElement(fullName);
	}

	/**
	 * Creates an empty element.
	 * 
	 * @param fullName
	 *            the full name of the element
	 * @param namespace
	 *            the namespace URI.
	 */
	public XMLElement createElement(final String fullName, final String namespace) {
		return new XMLElement(fullName, namespace);
	}

	/**
	 * Creates an empty element.
	 * 
	 * @param fullName
	 *            the name of the element.
	 * @param systemID
	 *            the system ID of the XML data where the element starts.
	 * @param lineNr
	 *            the line in the XML data where the element starts.
	 */
	public XMLElement createElement(final String fullName, final String systemID, final int lineNr) {
		return new XMLElement(fullName, systemID, lineNr);
	}

	/**
	 * Creates an element to be used for #PCDATA content.
	 */
	public XMLElement createPCDataElement() {
		return new XMLElement();
	}

	/**
	 * Returns an enumeration of all attribute names.
	 * 
	 * @return the non-null enumeration.
	 */
	public Enumeration<String> enumerateAttributeNames() {
		final Vector<String> result = new Vector<String>();
		final Enumeration<XMLAttribute> enumeration = attributes.elements();
		while (enumeration.hasMoreElements()) {
			result.addElement(enumeration.nextElement().getFullName());
		}
		return result.elements();
	}

	/**
	 * Returns an enumeration of all child elements.
	 * 
	 * @return the non-null enumeration
	 */
	public Enumeration<XMLElement> enumerateChildren() {
		return children.elements();
	}

	/**
	 * Returns true if the element equals another element.
	 * 
	 * @param rawElement
	 *            the element to compare to
	 */
	@Override
	public boolean equals(final Object rawElement) {
		try {
			return this.equalsXMLElement((XMLElement) rawElement);
		}
		catch (final ClassCastException e) {
			return false;
		}
	}

	/**
	 * Returns true if the element equals another element.
	 * 
	 * @param rawElement
	 *            the element to compare to
	 */
	public boolean equalsXMLElement(final XMLElement elt) {
		if (!name.equals(elt.getName())) {
			return false;
		}
		if (attributes.size() != elt.getAttributeCount()) {
			return false;
		}
		final Enumeration<XMLAttribute> enumeration = attributes.elements();
		while (enumeration.hasMoreElements()) {
			final XMLAttribute attr = enumeration.nextElement();
			if (!elt.hasAttribute(attr.getName(), attr.getNamespace())) {
				return false;
			}
			final String value = elt.getAttribute(attr.getName(), attr.getNamespace(), null);
			if (!attr.getValue().equals(value)) {
				return false;
			}
			final String type = elt.getAttributeType(attr.getName(), attr.getNamespace());
			if (!attr.getType().equals(type)) {
				return false;
			}
		}
		if (children.size() != elt.getChildrenCount()) {
			return false;
		}
		for (int i = 0; i < children.size(); i++) {
			final XMLElement child1 = this.getChildAtIndex(i);
			final XMLElement child2 = elt.getChildAtIndex(i);
			if (!child1.equalsXMLElement(child2)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Cleans up the object when it's destroyed.
	 */
	@Override
	protected void finalize() throws Throwable {
		attributes.clear();
		attributes = null;
		children = null;
		fullName = null;
		name = null;
		namespace = null;
		content = null;
		systemID = null;
		parent = null;
		super.finalize();
	}

	/**
	 * Searches an attribute.
	 * 
	 * @param fullName
	 *            the non-null full name of the attribute.
	 * @return the attribute, or null if the attribute does not exist.
	 */
	private XMLAttribute findAttribute(final String fullName) {
		if (fullName == null) {
			throw new IllegalArgumentException("fullName must not be null");
		}
		final Enumeration<XMLAttribute> enumeration = attributes.elements();
		while (enumeration.hasMoreElements()) {
			final XMLAttribute attr = enumeration.nextElement();
			if (attr.getFullName().equals(fullName)) {
				return attr;
			}
		}
		return null;
	}

	/**
	 * Searches an attribute.
	 * 
	 * @param name
	 *            the non-null short name of the attribute.
	 * @param namespace
	 *            the name space, which may be null.
	 * @return the attribute, or null if the attribute does not exist.
	 */
	private XMLAttribute findAttribute(final String name, final String namespace) {
		if (name == null) {
			throw new IllegalArgumentException("name must not be null");
		}
		final Enumeration<XMLAttribute> enumeration = attributes.elements();
		while (enumeration.hasMoreElements()) {
			final XMLAttribute attr = enumeration.nextElement();
			boolean found = attr.getName().equals(name);
			if (namespace == null) {
				found &= (attr.getNamespace() == null);
			}
			else {
				found &= namespace.equals(attr.getNamespace());
			}
			if (found) {
				return attr;
			}
		}
		return null;
	}

	/**
	 * @deprecated As of NanoXML/Java 2.1, replaced by
	 *             {@link #getAttribute(java.lang.String,java.lang.String)}
	 *             Returns the value of an attribute.
	 * @param name
	 *            the non-null name of the attribute.
	 * @return the value, or null if the attribute does not exist.
	 */
	@Deprecated
	public String getAttribute(final String name) {
		return this.getAttribute(name, null);
	}

	/**
	 * Returns the value of an attribute.
	 * 
	 * @param name
	 *            the non-null full name of the attribute.
	 * @param defaultValue
	 *            the default value of the attribute.
	 * @return the value, or defaultValue if the attribute does not exist.
	 */
	public int getAttribute(final String name, final int defaultValue) {
		final String value = this.getAttribute(name, Integer.toString(defaultValue));
		return Integer.parseInt(value);
	}

	/**
	 * Returns the value of an attribute.
	 * 
	 * @param name
	 *            the non-null full name of the attribute.
	 * @param defaultValue
	 *            the default value of the attribute.
	 * @return the value, or defaultValue if the attribute does not exist.
	 */
	public String getAttribute(final String name, final String defaultValue) {
		final XMLAttribute attr = this.findAttribute(name);
		if (attr == null) {
			return defaultValue;
		}
		else {
			return attr.getValue();
		}
	}

	/**
	 * Returns the value of an attribute.
	 * 
	 * @param name
	 *            the non-null name of the attribute.
	 * @param namespace
	 *            the namespace URI, which may be null.
	 * @param defaultValue
	 *            the default value of the attribute.
	 * @return the value, or defaultValue if the attribute does not exist.
	 */
	public int getAttribute(final String name, final String namespace, final int defaultValue) {
		final String value = this.getAttribute(name, namespace, Integer.toString(defaultValue));
		return Integer.parseInt(value);
	}

	/**
	 * Returns the value of an attribute.
	 * 
	 * @param name
	 *            the non-null name of the attribute.
	 * @param namespace
	 *            the namespace URI, which may be null.
	 * @param defaultValue
	 *            the default value of the attribute.
	 * @return the value, or defaultValue if the attribute does not exist.
	 */
	public String getAttribute(final String name, final String namespace, final String defaultValue) {
		final XMLAttribute attr = this.findAttribute(name, namespace);
		if (attr == null) {
			return defaultValue;
		}
		else {
			return attr.getValue();
		}
	}

	/**
	 * Returns the number of attributes.
	 */
	public int getAttributeCount() {
		return attributes.size();
	}

	/**
	 * Returns the namespace of an attribute.
	 * 
	 * @param name
	 *            the non-null full name of the attribute.
	 * @return the namespace, or null if there is none associated.
	 */
	public String getAttributeNamespace(final String name) {
		final XMLAttribute attr = this.findAttribute(name);
		if (attr == null) {
			return null;
		}
		else {
			return attr.getNamespace();
		}
	}

	/**
	 * Returns all attributes as a Properties object.
	 * 
	 * @return the non-null set.
	 */
	public Properties getAttributes() {
		final Properties result = new Properties();
		final Enumeration<XMLAttribute> enumeration = attributes.elements();
		while (enumeration.hasMoreElements()) {
			final XMLAttribute attr = enumeration.nextElement();
			result.put(attr.getFullName(), attr.getValue());
		}
		return result;
	}

	/**
	 * Returns all attributes in a specific namespace as a Properties object.
	 * 
	 * @param namespace
	 *            the namespace URI of the attributes, which may be null.
	 * @return the non-null set.
	 */
	public Properties getAttributesInNamespace(final String namespace) {
		final Properties result = new Properties();
		final Enumeration<XMLAttribute> enumeration = attributes.elements();
		while (enumeration.hasMoreElements()) {
			final XMLAttribute attr = enumeration.nextElement();
			if (namespace == null) {
				if (attr.getNamespace() == null) {
					result.put(attr.getName(), attr.getValue());
				}
			}
			else {
				if (namespace.equals(attr.getNamespace())) {
					result.put(attr.getName(), attr.getValue());
				}
			}
		}
		return result;
	}

	/**
	 * Returns the type of an attribute.
	 * 
	 * @param name
	 *            the non-null full name of the attribute.
	 * @return the type, or null if the attribute does not exist.
	 */
	public String getAttributeType(final String name) {
		final XMLAttribute attr = this.findAttribute(name);
		if (attr == null) {
			return null;
		}
		else {
			return attr.getType();
		}
	}

	/**
	 * Returns the type of an attribute.
	 * 
	 * @param name
	 *            the non-null name of the attribute.
	 * @param namespace
	 *            the namespace URI, which may be null.
	 * @return the type, or null if the attribute does not exist.
	 */
	public String getAttributeType(final String name, final String namespace) {
		final XMLAttribute attr = this.findAttribute(name, namespace);
		if (attr == null) {
			return null;
		}
		else {
			return attr.getType();
		}
	}

	/**
	 * Returns the child at a specific index.
	 * 
	 * @param index
	 *            the index of the child
	 * @return the non-null child
	 * @throws java.lang.ArrayIndexOutOfBoundsException
	 *             if the index is out of bounds.
	 */
	public XMLElement getChildAtIndex(final int index) throws ArrayIndexOutOfBoundsException {
		return (XMLElement) children.elementAt(index);
	}

	/**
	 * Returns a vector containing all the child elements.
	 * 
	 * @return the vector.
	 */
	public Vector<XMLElement> getChildren() {
		return children;
	}

	/**
	 * Returns the number of children.
	 * 
	 * @return the count.
	 */
	public int getChildrenCount() {
		return children.size();
	}

	/**
	 * Returns a vector of all child elements named <I>name</I>.
	 * 
	 * @param name
	 *            the full name of the children to search for.
	 * @return the non-null vector of child elements.
	 */
	public Vector<XMLElement> getChildrenNamed(final String name) {
		final Vector<XMLElement> result = new Vector<XMLElement>(children.size());
		final Enumeration<XMLElement> enumeration = children.elements();
		while (enumeration.hasMoreElements()) {
			final XMLElement child = enumeration.nextElement();
			final String childName = child.getFullName();
			if ((childName != null) && childName.equals(name)) {
				result.addElement(child);
			}
		}
		return result;
	}

	/**
	 * Returns a vector of all child elements named <I>name</I>.
	 * 
	 * @param name
	 *            the name of the children to search for.
	 * @param namespace
	 *            the namespace, which may be null.
	 * @return the non-null vector of child elements.
	 */
	public Vector<XMLElement> getChildrenNamed(final String name, final String namespace) {
		final Vector<XMLElement> result = new Vector<XMLElement>(children.size());
		final Enumeration<XMLElement> enumeration = children.elements();
		while (enumeration.hasMoreElements()) {
			final XMLElement child = (XMLElement) enumeration.nextElement();
			String str = child.getName();
			boolean found = (str != null) && (str.equals(name));
			str = child.getNamespace();
			if (str == null) {
				found &= (name == null);
			}
			else {
				found &= str.equals(namespace);
			}
			if (found) {
				result.addElement(child);
			}
		}
		return result;
	}

	/**
	 * Return the #PCDATA content of the element. If the element has a
	 * combination of #PCDATA content and child elements, the #PCDATA sections
	 * can be retrieved as unnamed child objects. In this case, this method
	 * returns null.
	 * 
	 * @return the content.
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Searches a child element.
	 * 
	 * @param name
	 *            the full name of the child to search for.
	 * @return the child element, or null if no such child was found.
	 */
	public XMLElement getFirstChildNamed(final String name) {
		final Enumeration<XMLElement> enumeration = children.elements();
		while (enumeration.hasMoreElements()) {
			final XMLElement child = enumeration.nextElement();
			final String childName = child.getFullName();
			if ((childName != null) && childName.equals(name)) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Searches a child element.
	 * 
	 * @param name
	 *            the name of the child to search for.
	 * @param namespace
	 *            the namespace, which may be null.
	 * @return the child element, or null if no such child was found.
	 */
	public XMLElement getFirstChildNamed(final String name, final String namespace) {
		final Enumeration<XMLElement> enumeration = children.elements();
		while (enumeration.hasMoreElements()) {
			final XMLElement child = enumeration.nextElement();
			String str = child.getName();
			boolean found = (str != null) && (str.equals(name));
			str = child.getNamespace();
			if (str == null) {
				found &= (name == null);
			}
			else {
				found &= str.equals(namespace);
			}
			if (found) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Returns the full name (i.e. the name including an eventual namespace
	 * prefix) of the element.
	 * 
	 * @return the name, or null if the element only contains #PCDATA.
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * Returns the line number in the data where the element started.
	 * 
	 * @return the line number, or NO_LINE if unknown.
	 * @see #NO_LINE
	 * @see #getSystemID
	 */
	public int getLineNr() {
		return lineNr;
	}

	/**
	 * Returns the name of the element.
	 * 
	 * @return the name, or null if the element only contains #PCDATA.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the namespace of the element.
	 * 
	 * @return the namespace, or null if no namespace is associated with the
	 *         element.
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Returns the parent element. This method returns null for the root
	 * element.
	 */
	public XMLElement getParent() {
		return parent;
	}

	/**
	 * Returns the system ID of the data where the element started.
	 * 
	 * @return the system ID, or null if unknown.
	 * @see #getLineNr
	 */
	public String getSystemID() {
		return systemID;
	}

	/**
	 * Returns whether an attribute exists.
	 * 
	 * @return true if the attribute exists.
	 */
	public boolean hasAttribute(final String name) {
		return this.findAttribute(name) != null;
	}

	/**
	 * Returns whether an attribute exists.
	 * 
	 * @return true if the attribute exists.
	 */
	public boolean hasAttribute(final String name, final String namespace) {
		return this.findAttribute(name, namespace) != null;
	}

	/**
	 * Returns whether the element has children.
	 * 
	 * @return true if the element has children.
	 */
	public boolean hasChildren() {
		return (!children.isEmpty());
	}

	/**
	 * Inserts a child element.
	 * 
	 * @param child
	 *            the non-null child to add.
	 * @param index
	 *            where to put the child.
	 */
	public void insertChild(final XMLElement child, final int index) {
		if (child == null) {
			throw new IllegalArgumentException("child must not be null");
		}
		if ((child.getName() == null) && (!children.isEmpty())) {
			final XMLElement lastChild = (XMLElement) children.lastElement();
			if (lastChild.getName() == null) {
				lastChild.setContent(lastChild.getContent() + child.getContent());
				return;
			}
		}
		(child).parent = this;
		children.insertElementAt(child, index);
	}

	/**
	 * Returns whether the element is a leaf element.
	 * 
	 * @return true if the element has no children.
	 */
	public boolean isLeaf() {
		return children.isEmpty();
	}

	/**
	 * Removes an attribute.
	 * 
	 * @param name
	 *            the non-null name of the attribute.
	 */
	public void removeAttribute(final String name) {
		if (name == null) {
			throw new IllegalArgumentException("name must not be null");
		}
		for (int i = 0; i < attributes.size(); i++) {
			final XMLAttribute attr = attributes.elementAt(i);
			if (attr.getFullName().equals(name)) {
				attributes.removeElementAt(i);
				return;
			}
		}
	}

	/**
	 * Removes an attribute.
	 * 
	 * @param name
	 *            the non-null name of the attribute.
	 * @param namespace
	 *            the namespace URI of the attribute, which may be null.
	 */
	public void removeAttribute(final String name, final String namespace) {
		if (name == null) {
			throw new IllegalArgumentException("name must not be null");
		}
		for (int i = 0; i < attributes.size(); i++) {
			final XMLAttribute attr = attributes.elementAt(i);
			boolean found = attr.getName().equals(name);
			if (namespace == null) {
				found &= (attr.getNamespace() == null);
			}
			else {
				found &= attr.getNamespace().equals(namespace);
			}
			if (found) {
				attributes.removeElementAt(i);
				return;
			}
		}
	}

	/**
	 * Removes a child element.
	 * 
	 * @param child
	 *            the non-null child to remove.
	 */
	public void removeChild(final XMLElement child) {
		if (child == null) {
			throw new IllegalArgumentException("child must not be null");
		}
		children.removeElement(child);
	}

	/**
	 * Removes the child located at a certain index.
	 * 
	 * @param index
	 *            the index of the child, where the first child has index 0.
	 */
	public void removeChildAtIndex(final int index) {
		children.removeElementAt(index);
	}

	/**
	 * Sets an attribute.
	 * 
	 * @param name
	 *            the non-null full name of the attribute.
	 * @param value
	 *            the non-null value of the attribute.
	 */
	public void setAttribute(final String name, final String value) {
		if (value == null) {
			throw new IllegalArgumentException("value must not be null");
		}
		XMLAttribute attr = this.findAttribute(name);
		if (attr == null) {
			attr = new XMLAttribute(name, name, null, value, "CDATA");
			attributes.addElement(attr);
		}
		else {
			attr.setValue(value);
		}
	}

	/**
	 * Sets an attribute.
	 * 
	 * @param fullName
	 *            the non-null full name of the attribute.
	 * @param namespace
	 *            the namespace URI of the attribute, which may be null.
	 * @param value
	 *            the non-null value of the attribute.
	 */
	public void setAttribute(final String fullName, final String namespace, final String value) {
		if (fullName == null) {
			throw new IllegalArgumentException("fullName must not be null");
		}
		if (value == null) {
			throw new IllegalArgumentException("value must not be null");
		}
		final int index = fullName.indexOf(':');
		final String name = fullName.substring(index + 1);
		XMLAttribute attr = this.findAttribute(name, namespace);
		if (attr == null) {
			attr = new XMLAttribute(fullName, name, namespace, value, "CDATA");
			attributes.addElement(attr);
		}
		else {
			attr.setValue(value);
		}
	}

	/**
	 * Sets the #PCDATA content. It is an error to call this method with a
	 * non-null value if there are child objects.
	 * 
	 * @param content
	 *            the (possibly null) content.
	 */
	public void setContent(final String content) {
		this.content = content;
	}

	/**
	 * Sets the full name. This method also sets the short name and clears the
	 * namespace URI.
	 * 
	 * @param name
	 *            the non-null name.
	 */
	public void setName(final String name) {
		if (name == null) {
			throw new IllegalArgumentException("name must not be null");
		}
		this.name = name;
		fullName = name;
		namespace = null;
	}

	/**
	 * Sets the name.
	 * 
	 * @param fullName
	 *            the non-null full name.
	 * @param namespace
	 *            the namespace URI, which may be null.
	 */
	public void setName(final String fullName, final String namespace) {
		if (fullName == null) {
			throw new IllegalArgumentException("fullName must not be null");
		}
		final int index = fullName.indexOf(':');
		if ((namespace == null) || (index < 0)) {
			name = fullName;
		}
		else {
			name = fullName.substring(index + 1);
		}
		this.fullName = fullName;
		this.namespace = namespace;
	}
}
