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

package org.apache.padaf.xmpbox.schema;

import java.util.Iterator;


import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.type.AbstractField;
import org.apache.padaf.xmpbox.type.Attribute;
import org.apache.padaf.xmpbox.type.BadFieldValueException;
import org.apache.padaf.xmpbox.type.ComplexPropertyContainer;
import org.apache.padaf.xmpbox.type.Elementable;
import org.apache.padaf.xmpbox.type.TextType;
import org.w3c.dom.Element;

/**
 * Representation of a PDF/A property type schema
 * 
 * @author a183132
 * 
 */
public class PDFAPropertyDescription implements Elementable {

	public static final String PDFAPROPPREFIX = "pdfaProperty";
	public static final String PDFAPROPPREFIXSEP = "pdfaProperty:";

	public static final String NAME = "name";
	public static final String VALUETYPE = "valueType";
	public static final String CATEGORY = "category";
	public static final String DESCRIPTION = "description";

	protected XMPMetadata metadata;
	protected ComplexPropertyContainer content;

	/**
	 * Build a new property description
	 * 
	 * @param metadata
	 *            The metadata to attach this schema
	 */
	public PDFAPropertyDescription(XMPMetadata metadata) {
		this.metadata = metadata;
		content = new ComplexPropertyContainer(metadata, "rdf", "li");
		content
				.setAttribute(new Attribute(null, "rdf", "parseType",
						"Resource"));
	}

	/**
	 * set the name of this property
	 * 
	 * @param name
	 *            The value to set
	 */
	public void setNameValue(String name) {
		content.addProperty(new TextType(metadata, PDFAPROPPREFIX, NAME, name));
	}

	/**
	 * set the value type of this property
	 * 
	 * @param type
	 *            The value to set
	 */
	public void setValueTypeValue(String type) {
		content.addProperty(new TextType(metadata, PDFAPROPPREFIX, VALUETYPE,
				type));
	}

	/**
	 * set the category of this property
	 * 
	 * @param category
	 *            The value to set
	 * @throws BadFieldValueException
	 *             if category value not 'internal' or 'external'
	 */
	public void setCategoryValue(String category) throws BadFieldValueException {
		if (category.equals("external") || category.equals("internal")) {
			content.addProperty(new TextType(metadata, PDFAPROPPREFIX,
					CATEGORY, category));
		} else {
			throw new BadFieldValueException(
					"Unexpected value '"
							+ category
							+ "' for property category (only values 'internal' or 'external' are allowed)");
		}
	}

	/**
	 * set the description of this property
	 * 
	 * @param desc
	 *            The value to set
	 */
	public void setDescriptionValue(String desc) {
		content.addProperty(new TextType(metadata, PDFAPROPPREFIX, DESCRIPTION,
				desc));
	}

	/**
	 * Get the property value
	 * 
	 * @param qualifiedName
	 *            the name of property wanted
	 * @return property value
	 */
	private String getPropertyValue(String qualifiedName) {
		Iterator<AbstractField> it = content.getAllProperties().iterator();
		AbstractField tmp;
		while (it.hasNext()) {
			tmp = it.next();
			if (tmp.getQualifiedName().equals(qualifiedName)) {
				return ((TextType) tmp).getStringValue();
			}
		}
		return null;
	}

	/**
	 * Return the current defined name (in a string)
	 * 
	 * @return the name value defined for this property
	 */
	public String getNameValue() {
		return getPropertyValue(PDFAPROPPREFIXSEP + NAME);
	}

	/**
	 * Return the current ValueType (in a string)
	 * 
	 * @return the valueType value defined for this property
	 */
	public String getValueTypeValue() {
		return getPropertyValue(PDFAPROPPREFIXSEP + VALUETYPE);
	}

	/**
	 * Return the current category (in a string)
	 * 
	 * @return the category value defined for this property
	 */
	public String getCategoryValue() {
		return getPropertyValue(PDFAPROPPREFIXSEP + CATEGORY);
	}

	/**
	 * Return the current description (in a string)
	 * 
	 * @return the description value defined for this property
	 */
	public String getDescriptionValue() {
		return getPropertyValue(PDFAPROPPREFIXSEP + DESCRIPTION);
	}

	/**
	 * Get a property from its qualified name as a TextType object
	 * 
	 * @param qualifiedName
	 *            the Name of property wanted
	 * @return the property wanted
	 */
	private TextType getProperty(String qualifiedName) {
		Iterator<AbstractField> it = content.getAllProperties().iterator();
		AbstractField tmp;
		while (it.hasNext()) {
			tmp = it.next();
			if (tmp.getQualifiedName().equals(qualifiedName)) {
				return (TextType) tmp;
			}
		}
		return null;
	}

	/**
	 * Return the property corresponding to the property name definition
	 * 
	 * @return the Name property defined for this PropertyDescription
	 */
	public TextType getName() {
		return getProperty(PDFAPROPPREFIXSEP + NAME);
	}

	/**
	 * Return the property corresponding to the property valueType definition
	 * 
	 * @return the ValueType property defined for this PropertyDescription
	 */
	public TextType getValueType() {
		return getProperty(PDFAPROPPREFIXSEP + VALUETYPE);
	}

	/**
	 * Return the property corresponding to the property category definition
	 * 
	 * @return the Category property defined for this PropertyDescription
	 */
	public TextType getCategory() {
		return getProperty(PDFAPROPPREFIXSEP + CATEGORY);
	}

	/**
	 * Return the property corresponding to the property description definition
	 * 
	 * @return the Description property defined for this PropertyDescription
	 */
	public TextType getDescription() {
		return getProperty(PDFAPROPPREFIXSEP + DESCRIPTION);
	}

	/**
	 * Get Dom Element for xml/rdf serialization
	 * 
	 * @return the DOM Element
	 */
	public Element getElement() {
		return content.getElement();
	}
}
