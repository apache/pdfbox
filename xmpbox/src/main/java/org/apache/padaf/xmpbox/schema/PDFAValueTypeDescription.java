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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.type.AbstractField;
import org.apache.padaf.xmpbox.type.Attribute;
import org.apache.padaf.xmpbox.type.ComplexPropertyContainer;
import org.apache.padaf.xmpbox.type.Elementable;
import org.apache.padaf.xmpbox.type.TextType;
import org.w3c.dom.Element;

/**
 * Representation of a PDF/A Value type schema
 * 
 * @author a183132
 * 
 */
public class PDFAValueTypeDescription implements Elementable {

	public static final String PDFATYPEPREFIX = "pdfaType";
	public static final String PDFATYPEPREFIXSEP = "pdfaType:";

	public static final String TYPE = "type";
	public static final String NS_URI = "namespaceURI";
	public static final String PREFIX = "prefix";
	public static final String DESCRIPTION = "description";
	public static final String FIELD = "field";

	protected FieldDescriptionContainer fields;
	protected XMPMetadata metadata;
	protected ComplexPropertyContainer content;

	/**
	 * Build a new valuetype description
	 * 
	 * @param metadata
	 *            The metadata to attach this schema
	 */
	public PDFAValueTypeDescription(XMPMetadata metadata) {
		this.metadata = metadata;
		content = new ComplexPropertyContainer(metadata, "rdf", "li");
		content
				.setAttribute(new Attribute(null, "rdf", "parseType",
						"Resource"));
		fields = new FieldDescriptionContainer();
		content.getElement().appendChild(fields.getElement());

	}

	/**
	 * set the name of this valuetype
	 * 
	 * @param name
	 *            The value to set
	 */
	public void setTypeNameValue(String name) {
		content.addProperty(new TextType(metadata, PDFATYPEPREFIX, TYPE, name));
	}

	/**
	 * set the namespaceURI of this valueType
	 * 
	 * @param nsURI
	 *            The value to set
	 */
	public void setNamespaceURIValue(String nsURI) {
		content.addProperty(new TextType(metadata, PDFATYPEPREFIX, NS_URI,
				nsURI));
	}

	/**
	 * set the prefix of this valuetype
	 * 
	 * @param prefix
	 *            The value to set
	 */
	public void setPrefixValue(String prefix) {
		content.addProperty(new TextType(metadata, PDFATYPEPREFIX, PREFIX,
				prefix));
	}

	/**
	 * set the description of this property
	 * 
	 * @param desc
	 *            The value to set
	 */
	public void setDescriptionValue(String desc) {
		content.addProperty(new TextType(metadata, PDFATYPEPREFIX, DESCRIPTION,
				desc));
	}

	/**
	 * Get value type property value as a string
	 * 
	 * @param qualifiedName
	 *            the Name of property wanted
	 * @return value of the property wanted which describe this property
	 */
	private String getValueTypeProperty(String qualifiedName) {
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
	 * Return the current defined type name (in a string)
	 * 
	 * @return the type value (so the name) given to this valuetype
	 */
	public String getTypeNameValue() {
		return getValueTypeProperty(PDFATYPEPREFIXSEP + TYPE);
	}

	/**
	 * Return the current nsURI (in a string)
	 * 
	 * @return the namespace URI value
	 */
	public String getNamespaceURIValue() {
		return getValueTypeProperty(PDFATYPEPREFIXSEP + NS_URI);
	}

	/**
	 * Return the current prefix (in a string)
	 * 
	 * @return The prefix value
	 */
	public String getPrefixValue() {
		return getValueTypeProperty(PDFATYPEPREFIXSEP + PREFIX);
	}

	/**
	 * Return the description of this valueType(in a string)
	 * 
	 * @return the description value
	 */
	public String getDescriptionValue() {
		return getValueTypeProperty(PDFATYPEPREFIXSEP + DESCRIPTION);
	}

	/**
	 * Get value type property value as a TextType
	 * 
	 * @param qualifiedName
	 *            the name of the property wanted
	 * @return the property wanted
	 */
	private TextType getTypeProperty(String qualifiedName) {
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
	 * Return the property corresponding to the type name definition
	 * 
	 * @return the type property (or the property that define the name of this
	 *         valuetype)
	 */
	public TextType getTypeName() {
		return getTypeProperty(PDFATYPEPREFIXSEP + TYPE);
	}

	/**
	 * Return the property corresponding to the Type namespaceURI definition
	 * 
	 * @return the namespace URI property
	 */
	public TextType getNamespaceURI() {
		return getTypeProperty(PDFATYPEPREFIXSEP + NS_URI);
	}

	/**
	 * Return the property corresponding to the type prefix definition
	 * 
	 * @return the prefix property
	 */
	public TextType getPrefix() {
		return getTypeProperty(PDFATYPEPREFIXSEP + PREFIX);
	}

	/**
	 * Return the property corresponding to the type description definition
	 * 
	 * @return the description property
	 */
	public TextType getDescription() {
		return getTypeProperty(PDFATYPEPREFIXSEP + DESCRIPTION);
	}

	/**
	 * Give all Fields description embedded in this valuetype
	 * 
	 * @return a list of Field defined for this valuetype
	 */
	public List<PDFAFieldDescription> getFields() {
		return Collections.unmodifiableList(fields.fields);
	}

	/**
	 * Add a field description to this valuetype
	 * 
	 * @param name
	 *            name of field
	 * @param valueType
	 *            valueType of field
	 * @param description
	 *            description of field
	 * @return the Field property created
	 */
	public PDFAFieldDescription addField(String name, String valueType,
			String description) {
		PDFAFieldDescription field = new PDFAFieldDescription(metadata);
		field.setNameValue(name);
		field.setValueTypeValue(valueType);
		field.setDescriptionValue(description);
		fields.addFieldDescription(field);
		return field;
	}

	/**
	 * Add a Structured Field to this valueType
	 * 
	 * @param field
	 *            the field to add to this valueType
	 */
	public void addField(PDFAFieldDescription field) {
		fields.addFieldDescription(field);
	}

	/**
	 * Get Dom Element for xml/rdf serialization
	 * 
	 * @return the DOM element
	 */
	public Element getElement() {
		return content.getElement();
	}

	/**
	 * Container of Field Description
	 * 
	 * @author a183132
	 * 
	 */
	public class FieldDescriptionContainer implements Elementable {

		protected Element element, content;
		protected List<PDFAFieldDescription> fields;

		/**
		 * 
		 * PDF/A Field Description Container constructor
		 */
		public FieldDescriptionContainer() {
			element = metadata.getFuturOwner().createElement(
					PDFAExtensionSchema.PDFATYPESEP + FIELD);
			content = metadata.getFuturOwner().createElement("rdf:Seq");
			element.appendChild(content);

			fields = new ArrayList<PDFAFieldDescription>();
		}

		/**
		 * Add a PDF/A Field Description to the current structure
		 * 
		 * @param obj
		 *            the field to add
		 */
		public void addFieldDescription(PDFAFieldDescription obj) {
			if (containsFieldDescription(obj)) {
				removeFieldDescription(obj);
			}
			fields.add(obj);
			content.appendChild(obj.content.getElement());
		}

		/**
		 * access to all PDF/A Field Descriptions
		 * 
		 * @return an Iterator on all PDF/A Field Descriptions declared
		 */
		public Iterator<PDFAFieldDescription> getAllFieldDescription() {
			return fields.iterator();
		}

		/**
		 * Check if two PDF/A Field Descriptions are similar
		 * 
		 * @param prop1
		 *            the first PDF/A Field Description
		 * @param prop2
		 *            the second PDF/A Field Description
		 * @return comparison result
		 */
		public boolean isSameFieldDescription(PDFAFieldDescription prop1,
				PDFAFieldDescription prop2) {
			if (prop1.getClass().equals(prop2.getClass())) {
				if (prop1.content.getElement().getTextContent().equals(
						prop2.content.getElement().getTextContent())) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Check if a specified PDF/A Field Description is embedded
		 * 
		 * @param field
		 *            PDF/A field Description
		 * @return result
		 */
		public boolean containsFieldDescription(PDFAFieldDescription field) {
			Iterator<PDFAFieldDescription> it = getAllFieldDescription();
			PDFAFieldDescription tmp;
			while (it.hasNext()) {
				tmp = it.next();
				if (isSameFieldDescription(tmp, field)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Remove a PDF/A Field description
		 * 
		 * @param field
		 *            the PDF/A Field Description
		 */
		public void removeFieldDescription(PDFAFieldDescription field) {
			if (containsFieldDescription(field)) {
				fields.remove(field);
				content.removeChild(field.content.getElement());
			}
		}

		/**
		 * Get Dom Element for xml/rdf serialization
		 * 
		 * @return the DOM element
		 */
		public Element getElement() {
			return element;
		}

	}

}
