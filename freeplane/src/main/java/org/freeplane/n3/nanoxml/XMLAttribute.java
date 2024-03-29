/*
 * XMLAttribute.java NanoXML/Java $Revision: 1.4 $ $Date: 2002/01/04 21:03:29 $
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

import java.util.Objects;

/**
 * An attribute in an XML element. This is an internal class.
 * 
 * @see org.freeplane.n3.nanoxml.XMLElement
 * @author Marc De Scheemaecker
 * @version $Name: RELEASE_2_2_1 $, $Revision: 1.4 $
 */
class XMLAttribute {
	/**
	 * The full name of the attribute.
	 */
	final private String fullName;
	/**
	 * The short name of the attribute.
	 */
	final private String name;
	/**
	 * The namespace URI of the attribute.
	 */
	final private String namespace;
	/**
	 * The type of the attribute.
	 */
	final private String type;
	/**
	 * The value of the attribute.
	 */
	private String value;

	/**
	 * Creates a new attribute.
	 * 
	 * @param fullName
	 *            the non-null full name
	 * @param name
	 *            the non-null short name
	 * @param namespace
	 *            the namespace URI, which may be null
	 * @param value
	 *            the value of the attribute
	 * @param type
	 *            the type of the attribute
	 */
	XMLAttribute(final String fullName, final String name, final String namespace, final String value, final String type) {
		this.fullName = fullName;
		this.name = name;
		this.namespace = namespace;
		this.value = value;
		this.type = type;
	}

	/**
	 * Returns the full name of the attribute.
	 */
	String getFullName() {
		return fullName;
	}

	/**
	 * Returns the short name of the attribute.
	 */
	String getName() {
		return name;
	}

	/**
	 * Returns the namespace of the attribute.
	 */
	String getNamespace() {
		return namespace;
	}

	/**
	 * Returns the type of the attribute.
	 * 
	 * @param type
	 *            the new type.
	 */
	String getType() {
		return type;
	}

	/**
	 * Returns the value of the attribute.
	 */
	String getValue() {
		return value;
	}

	/**
	 * Sets the value of the attribute.
	 * 
	 * @param value
	 *            the new value.
	 */
	void setValue(final String value) {
		this.value = value;
	}

    @Override
    public int hashCode() {
        return Objects.hash(fullName, name, namespace, type, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        XMLAttribute other = (XMLAttribute) obj;
        return Objects.equals(fullName, other.fullName) && Objects.equals(name, other.name)
                && Objects.equals(namespace, other.namespace) && Objects.equals(type, other.type)
                && Objects.equals(value, other.value);
    }
	
	
}
