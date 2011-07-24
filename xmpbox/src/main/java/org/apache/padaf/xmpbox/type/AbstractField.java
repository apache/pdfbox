/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.padaf.xmpbox.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.padaf.xmpbox.XMPMetadata;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Astract Object representation of a XMP 'field' (-> Properties and specific
 * Schemas)
 * 
 * @author a183132
 * 
 */
public abstract class AbstractField implements Elementable {

	/**
	 * ALL PROPERTIES MUST NOT BE USED MORE THAN ONE TIME BECAUSE THE SAME
	 * ELEMENT CANNOT BE MORE THAN ONE TIME IN THE SAME DOM DOCUMENT (if you
	 * choose to use the same property in different places in the same document,
	 * the element associated will not appear)
	 */

	protected Element element;
	protected Document parent;

	private String namespaceURI, prefix, propertyName;
	private Map<String, Attribute> attributes;

	/**
	 * Constructor of a XMP field without namespaceURI
	 * 
	 * @param metadata
	 *            The metadata to attach to this field
	 * @param prefix
	 *            the prefix to set for this field
	 * @param propertyName
	 *            the local name to set for this field
	 */
	public AbstractField(XMPMetadata metadata, String prefix,
			String propertyName) {
		String qualifiedName;
		this.prefix = prefix;
		qualifiedName = prefix + ":" + propertyName;
		this.parent = metadata.getFuturOwner();
		this.propertyName = propertyName;
		element = parent.createElement(qualifiedName);
		attributes = new HashMap<String, Attribute>();
	}

	/**
	 * Constructor of a XMP Field
	 * 
	 * @param metadata
	 *            The metadata to attach to this field
	 * @param namespaceURI
	 *            the namespace URI
	 * @param prefix
	 *            the prefix to set for this field
	 * @param propertyName
	 *            the local name to set for this field
	 */
	public AbstractField(XMPMetadata metadata, String namespaceURI,
			String prefix, String propertyName) {
		String qualifiedName;
		this.prefix = prefix;
		qualifiedName = prefix + ":" + propertyName;
		this.parent = metadata.getFuturOwner();
		this.namespaceURI = namespaceURI;
		this.propertyName = propertyName;
		element = parent.createElementNS(namespaceURI, qualifiedName);
		attributes = new HashMap<String, Attribute>();
	}

	/**
	 * Get the DOM element for rdf/xml serialization
	 * 
	 * @return The DOM Element
	 */
	public Element getElement() {
		return element;
	}

	/**
	 * Get the namespace URI of this entity
	 * 
	 * @return the namespace URI
	 */
	public String getNamespace() {
		return namespaceURI;
	}

	/**
	 * Get the prefix of this entity
	 * 
	 * @return the prefix specified
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Get the qualified Name of this entity (prefix+localName)
	 * 
	 * @return the full qualified name
	 */
	public String getQualifiedName() {
		return prefix + ":" + propertyName;

	}

	/**
	 * Get the propertyName (or localName)
	 * 
	 * @return the local Name
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * Set a new attribute for this entity
	 * 
	 * @param value
	 *            The Attribute property to add
	 */
	public void setAttribute(Attribute value) {
		if (attributes.containsKey(value.getQualifiedName())) {
			// if same name in element, attribute will be replaced
			attributes.remove(value.getQualifiedName());
		}
		if (value.getNamespace() == null) {
			attributes.put(value.getQualifiedName(), value);
			element.setAttribute(value.getQualifiedName(), value.getValue());
		} else {
			attributes.put(value.getQualifiedName(), value);
			element.setAttributeNS(value.getNamespace(), value
					.getQualifiedName(), value.getValue());
		}
	}

	/**
	 * Check if an attribute is declared for this entity
	 * 
	 * @param qualifiedName
	 *            the full qualified name of the attribute concerned
	 * @return true if attribute is present
	 */
	public boolean containsAttribute(String qualifiedName) {
		return attributes.containsKey(qualifiedName);
	}

	/**
	 * Get an attribute with its name in this entity
	 * 
	 * @param qualifiedName
	 *            the full qualified name of the attribute wanted
	 * @return The attribute property
	 */
	public Attribute getAttribute(String qualifiedName) {
		return attributes.get(qualifiedName);
	}

	/**
	 * Get attributes list defined for this entity
	 * 
	 * @return Attributes list
	 */
	public List<Attribute> getAllAttributes() {
		return new ArrayList<Attribute>(attributes.values());
	}

	/**
	 * Remove an attribute of this entity
	 * 
	 * @param qualifiedName
	 *            the full qualified name of the attribute wanted
	 */
	public void removeAttribute(String qualifiedName) {
		if (containsAttribute(qualifiedName)) {
			element.removeAttribute(qualifiedName);
			attributes.remove(qualifiedName);
		}

	}

}
