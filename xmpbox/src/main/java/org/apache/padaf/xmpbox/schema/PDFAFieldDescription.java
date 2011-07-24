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
import org.apache.padaf.xmpbox.type.ComplexPropertyContainer;
import org.apache.padaf.xmpbox.type.TextType;


/**
 * Representation of a PDF/A Field schema (used in PDFAValueTypeDescription)
 * 
 * @author a183132
 * 
 */
public class PDFAFieldDescription {

	public static final String PDFAFIELDPREFIX = "pdfaField";
	public static final String PDFAFIELDPREFIXSEP = "pdfaField:";

	@PropertyType(propertyType = "Text")
	public static final String NAME = "name";

	@PropertyType(propertyType = "Text")
	public static final String VALUETYPE = "valueType";

	@PropertyType(propertyType = "Text")
	public static final String DESCRIPTION = "description";

	protected XMPMetadata metadata;
	protected ComplexPropertyContainer content;

	/**
	 * Build a new PDF/A field description
	 * 
	 * @param metadata
	 *            The metadata to attach this description
	 */
	public PDFAFieldDescription(XMPMetadata metadata) {
		this.metadata = metadata;
		content = new ComplexPropertyContainer(metadata, "rdf", "li");
		content
				.setAttribute(new Attribute(null, "rdf", "parseType",
						"Resource"));
	}

	/**
	 * set the name of this field
	 * 
	 * @param name
	 *            The value to set
	 */
	public void setNameValue(String name) {
		content
				.addProperty(new TextType(metadata, PDFAFIELDPREFIX, NAME, name));
	}

	/**
	 * set the valueType of this field
	 * 
	 * @param valueType
	 *            The value to set
	 */
	public void setValueTypeValue(String valueType) {
		content.addProperty(new TextType(metadata, PDFAFIELDPREFIX, VALUETYPE,
				valueType));
	}

	/**
	 * set the description of this field
	 * 
	 * @param description
	 *            The value to set
	 */
	public void setDescriptionValue(String description) {
		content.addProperty(new TextType(metadata, PDFAFIELDPREFIX,
				DESCRIPTION, description));
	}

	/**
	 * Get value of a specified property field
	 * 
	 * @param qualifiedName
	 *            The value to get
	 * @return the Value Type of specified field
	 */
	private String getFieldPropertyValue(String qualifiedName) {
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
	 * Return the current defined field name (in a string)
	 * 
	 * @return The declared name of this field
	 */
	public String getNameValue() {
		return getFieldPropertyValue(PDFAFIELDPREFIXSEP + NAME);
	}

	/**
	 * Return the current defined field valueType (in a string)
	 * 
	 * @return the value Type of this field
	 */
	public String getValueTypeValue() {
		return getFieldPropertyValue(PDFAFIELDPREFIXSEP + VALUETYPE);
	}

	/**
	 * Return the current field description (in a string)
	 * 
	 * @return the description of this field
	 */
	public String getDescriptionValue() {
		return getFieldPropertyValue(PDFAFIELDPREFIXSEP + DESCRIPTION);
	}

	/**
	 * Get one Property which describes the field
	 * 
	 * @param qualifiedName
	 *            the nameproperty
	 * @return the property wanted
	 */
	private TextType getFieldProperty(String qualifiedName) {
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
	 * Return the property corresponding to the field name definition
	 * 
	 * @return the name property of this field
	 */
	public TextType getName() {
		return getFieldProperty(PDFAFIELDPREFIXSEP + NAME);
	}

	/**
	 * Return the property corresponding to the field namespaceURI definition
	 * 
	 * @return the valuetype property of this field
	 */
	public TextType getValueType() {
		return getFieldProperty(PDFAFIELDPREFIXSEP + VALUETYPE);
	}

	/**
	 * Return the property corresponding to the field description definition
	 * 
	 * @return the description property of this field
	 */
	public TextType getDescription() {
		return getFieldProperty(PDFAFIELDPREFIXSEP + DESCRIPTION);
	}

}
