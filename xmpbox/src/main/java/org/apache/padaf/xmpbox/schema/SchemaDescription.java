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
import org.apache.padaf.xmpbox.type.BadFieldValueException;
import org.apache.padaf.xmpbox.type.ComplexPropertyContainer;
import org.apache.padaf.xmpbox.type.Elementable;
import org.apache.padaf.xmpbox.type.TextType;
import org.w3c.dom.Element;

/**
 * Representation of one schema description (used in PDFAExtension Schema)
 * 
 * @author a183132
 * 
 */
public class SchemaDescription implements Elementable {

	protected XMPMetadata metadata;
	protected ValueTypesDescriptionContainer valueTypes;
	protected PropertyDescriptionContainer properties;
	protected ComplexPropertyContainer content;

	/**
	 * Create a new Schema Description
	 * 
	 * @param metadata
	 *            Metadata where this SchemaDescription will be included
	 */
	public SchemaDescription(XMPMetadata metadata) {
		this.metadata = metadata;
		content = new ComplexPropertyContainer(metadata, "rdf", "li");
		content
				.setAttribute(new Attribute(null, "rdf", "parseType",
						"Resource"));

		// <pdfaSchema:property><seq>
		properties = new PropertyDescriptionContainer();
		content.getElement().appendChild(properties.getElement());
		// <pdfaSchema:valueType><seq>
		valueTypes = new ValueTypesDescriptionContainer();
		content.getElement().appendChild(valueTypes.getElement());

	}

	/**
	 * Add a property to the current structure
	 * 
	 * @param obj
	 *            the property to add
	 */
	public void addProperty(AbstractField obj) {
		content.addProperty(obj);
	}

	/**
	 * Get schema Description property value as a string
	 * 
	 * @param qualifiedName
	 *            the name of one of properties that constitute a schema
	 *            description
	 * @return the value of property specified
	 */
	private String getPdfaTextValue(String qualifiedName) {
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
	 * Set Description of this schema
	 * 
	 * @param description
	 *            The value to set
	 */
	public void setSchemaValue(String description) {
		// <pdfaSchema:schema>
		content.addProperty(new TextType(metadata,
				PDFAExtensionSchema.PDFASCHEMA, PDFAExtensionSchema.SCHEMA,
				description));
	}

	/**
	 * Return the schema description value
	 * 
	 * @return The value to set
	 */
	public String getSchema() {
		return getPdfaTextValue(PDFAExtensionSchema.PDFASCHEMASEP
				+ PDFAExtensionSchema.SCHEMA);
	}

	/**
	 * Set the Schema Namespace URI
	 * 
	 * @param uri
	 *            the namespace URI to set for this Schema Description
	 */
	public void setNameSpaceURIValue(String uri) {
		content
				.addProperty(new TextType(metadata,
						PDFAExtensionSchema.PDFASCHEMA,
						PDFAExtensionSchema.NS_URI, uri));
	}

	/**
	 * Return the Schema nameSpaceURI value
	 * 
	 * @return the namespace URI defined for this Schema Description
	 */
	public String getNameSpaceURI() {
		return getPdfaTextValue(PDFAExtensionSchema.PDFASCHEMASEP
				+ PDFAExtensionSchema.NS_URI);
	}

	/**
	 * Set the preferred schema namespace prefix
	 * 
	 * @param prefix
	 *            the prefix to set for this Schema Description
	 */
	public void setPrefixValue(String prefix) {
		content.addProperty(new TextType(metadata,
				PDFAExtensionSchema.PDFASCHEMA, PDFAExtensionSchema.PREFIX,
				prefix));
	}

	/**
	 * Return the preferred schema namespace prefix value
	 * 
	 * @return the namespace URI defined for this Schema Description
	 */
	public String getPrefix() {
		return getPdfaTextValue(PDFAExtensionSchema.PDFASCHEMASEP
				+ PDFAExtensionSchema.PREFIX);
	}

	/**
	 * Give all PDFAProperties description embedded in this schema
	 * 
	 * @return List of all PDF/A Property Descriptions that specified properties
	 *         used for the schema associed to this schema description
	 */
	public List<PDFAPropertyDescription> getProperties() {
		return Collections.unmodifiableList(properties.properties);
	}

	/**
	 * Add a property description to this schema
	 * 
	 * @param name
	 *            property name
	 * @param type
	 *            property value type
	 * @param category
	 *            property category
	 * @param desc
	 *            property description
	 * @return the created PDFAPropertyDescription
	 * @throws BadFieldValueException
	 *             When Category field not contain 'internal' or 'external'
	 */
	public PDFAPropertyDescription addProperty(String name, String type,
			String category, String desc) throws BadFieldValueException {

		PDFAPropertyDescription prop = new PDFAPropertyDescription(metadata);
		prop.setNameValue(name);
		prop.setValueTypeValue(type);
		prop.setCategoryValue(category);
		prop.setDescriptionValue(desc);
		properties.addPropertyDescription(prop);

		return prop;
	}

	/**
	 * Give all ValueTypes description embedded in this schema
	 * 
	 * @return list of all valuetypes defined in this schema description
	 */
	public List<PDFAValueTypeDescription> getValueTypes() {
		return Collections.unmodifiableList(valueTypes.valueTypes);
	}

	/**
	 * Add a valueType description to this schema
	 * 
	 * @param type
	 *            valuetype type (its name)
	 * @param namespaceURI
	 *            valuetype namespace URI
	 * @param prefix
	 *            valuetype prefix
	 * @param description
	 *            valuetype description
	 * @param fields
	 *            list of PDF/A Field Descriptions associated
	 * @return the created PDFAPropertyDescription
	 */
	public PDFAValueTypeDescription addValueType(String type,
			String namespaceURI, String prefix, String description,
			List<PDFAFieldDescription> fields) {
		PDFAValueTypeDescription valueType = new PDFAValueTypeDescription(
				metadata);
		valueType.setTypeNameValue(type);
		valueType.setNamespaceURIValue(namespaceURI);
		valueType.setPrefixValue(prefix);
		valueType.setDescriptionValue(description);
		// Field is optional
		if (fields != null) {
			int size = fields.size();
			for (int i = 0; i < size; i++) {
				valueType.addField(fields.get(i));
			}

		}
		valueTypes.addValueTypeDescription(valueType);

		return valueType;
	}

	/**
	 * Container for PDF/A Value Type Descriptions associated to a Schema
	 * Description
	 * 
	 * @author a183132
	 * 
	 */
	public class ValueTypesDescriptionContainer implements Elementable {

		protected Element element, content;
		protected List<PDFAValueTypeDescription> valueTypes;

		/**
		 * 
		 * PDF/A Value Type Descriptions Container constructor
		 */
		public ValueTypesDescriptionContainer() {
			element = metadata.getFuturOwner().createElement(
					PDFAExtensionSchema.PDFASCHEMASEP
							+ PDFAExtensionSchema.VALUETYPE);
			content = metadata.getFuturOwner().createElement("rdf:Seq");
			element.appendChild(content);

			valueTypes = new ArrayList<PDFAValueTypeDescription>();
		}

		/**
		 * Add a PDF/A Value Type Description to the current structure
		 * 
		 * @param obj
		 *            the PDF/A Value Type Description to add
		 */
		public void addValueTypeDescription(PDFAValueTypeDescription obj) {
			if (containsValueTypeDescription(obj)) {
				removeValueTypeDescription(getValueTypeDescription(obj
						.getTypeNameValue()));
			}
			valueTypes.add(obj);
			content.appendChild(obj.content.getElement());
		}

		/**
		 * access all PDF/A Value Type Descriptions
		 * 
		 * @return Iterator on all PDF/A Value Type Descriptions defined in
		 *         order to be used in SchemaDescription class
		 */
		public Iterator<PDFAValueTypeDescription> getAllValueTypeDescription() {
			return valueTypes.iterator();
		}

		/**
		 * Get the PDF/A Value Type description from its type name
		 * 
		 * @param type
		 *            the name defined for the value type wanted
		 * @return The wanted PDF/A Value Type Description
		 */
		public PDFAValueTypeDescription getValueTypeDescription(String type) {
			Iterator<PDFAValueTypeDescription> it = getAllValueTypeDescription();
			PDFAValueTypeDescription tmp;
			while (it.hasNext()) {
				tmp = it.next();
				if (tmp.getTypeNameValue().equals(type)) {
					return tmp;
				}
			}
			return null;
		}

		/**
		 * Check if two PDFAValueTypeDescription are similar
		 * 
		 * @param prop1
		 *            first PDF/A Value Type Description
		 * @param prop2
		 *            second PDF/A Value Type Description
		 * @return comparison result
		 */
		public boolean isSameValueTypeDescription(
				PDFAValueTypeDescription prop1, PDFAValueTypeDescription prop2) {
			if (prop1.getClass().equals(prop2.getClass())) {
				if (prop1.getTypeNameValue().equals(prop2.getTypeNameValue())) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Check if a specified PDFAValueTypeDescription is embedded
		 * 
		 * @param vtype
		 *            PDF/A Value Type Descriptions
		 * @return result
		 */
		public boolean containsValueTypeDescription(
				PDFAValueTypeDescription vtype) {
			Iterator<PDFAValueTypeDescription> it = getAllValueTypeDescription();
			PDFAValueTypeDescription tmp;
			while (it.hasNext()) {
				tmp = it.next();
				if (isSameValueTypeDescription(tmp, vtype)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Remove a PDF/A Value Type Description
		 * 
		 * @param vtype
		 *            PDF/A Value Type Description to remove
		 */
		public void removeValueTypeDescription(PDFAValueTypeDescription vtype) {
			if (containsValueTypeDescription(vtype)) {
				valueTypes.remove(vtype);
				content.removeChild(vtype.content.getElement());
			}
		}

		/**
		 * get the DOM element for xml/rdf serialization
		 * 
		 * @return the DOM Element
		 */
		public Element getElement() {
			return element;
		}

	}

	/**
	 * Container for PDF/A Property Descriptions associated to a Schema
	 * Description
	 * 
	 * @author a183132
	 * 
	 */
	public class PropertyDescriptionContainer implements Elementable {

		protected Element element, content;
		protected List<PDFAPropertyDescription> properties;

		/**
		 * 
		 * PDF/A Property Descriptions Container constructor
		 */
		public PropertyDescriptionContainer() {
			element = metadata.getFuturOwner().createElement(
					PDFAExtensionSchema.PDFASCHEMASEP
							+ PDFAExtensionSchema.PROPERTY);
			content = metadata.getFuturOwner().createElement("rdf:Seq");
			element.appendChild(content);

			properties = new ArrayList<PDFAPropertyDescription>();
		}

		/**
		 * Add a PropertyDescription to the current structure
		 * 
		 * @param obj
		 *            the property to add
		 */
		public void addPropertyDescription(PDFAPropertyDescription obj) {
			if (containsPropertyDescription(obj)) {
				removePropertyDescription(getPropertyDescription(obj
						.getNameValue()));
			}
			properties.add(obj);
			content.appendChild(obj.content.getElement());
		}

		/**
		 * Return all PropertyDescription
		 * 
		 * @return Iterator on all PDF/A Property Descriptions in order to be
		 *         used in SchemaDescription class
		 */
		public Iterator<PDFAPropertyDescription> getAllPropertyDescription() {
			return properties.iterator();
		}

		/**
		 * Check if two PDF/A Property Description are similar
		 * 
		 * @param prop1
		 *            first PDF/A Property Description
		 * @param prop2
		 *            second PDF/A Property Description
		 * @return comparison result
		 */
		public boolean isSamePropertyDescription(PDFAPropertyDescription prop1,
				PDFAPropertyDescription prop2) {
			if (prop1.getClass().equals(prop2.getClass())) {
				// Assuming that 2 properties can't have the same name
				if (prop1.getNameValue().equals(prop2.getNameValue())) {
					return true;
				}
				// if(prop1.content.getElement().getTextContent().equals(prop2.content.getElement().getTextContent())){
				// return true;
				// }
			}
			return false;
		}

		/**
		 * Check if a specified PDF/A Property Description is embedded
		 * 
		 * @param prop
		 *            PDF/A Property Description
		 * @return result
		 */
		public boolean containsPropertyDescription(PDFAPropertyDescription prop) {
			Iterator<PDFAPropertyDescription> it = getAllPropertyDescription();
			PDFAPropertyDescription tmp;
			while (it.hasNext()) {
				tmp = it.next();
				if (isSamePropertyDescription(tmp, prop)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Get a PDFAPropertyDescription from its name
		 * 
		 * @param name
		 *            name defined for PDF/A Property Description
		 * @return the PDF/A Property Description
		 */
		public PDFAPropertyDescription getPropertyDescription(String name) {
			Iterator<PDFAPropertyDescription> it = getAllPropertyDescription();
			PDFAPropertyDescription tmp;
			while (it.hasNext()) {
				tmp = it.next();
				if (tmp.getNameValue().equals(name)) {
					return tmp;
				}
			}
			return null;
		}

		/**
		 * Remove a PDF/A Property Description
		 * 
		 * @param prop
		 *            PDF/A Property Description
		 */
		public void removePropertyDescription(PDFAPropertyDescription prop) {
			if (containsPropertyDescription(prop)) {
				properties.remove(prop);
				content.removeChild(prop.content.getElement());
			}
		}

		/**
		 * get the DOM element for xml/rdf serialization
		 * 
		 * @return the DOM Element
		 */
		public Element getElement() {
			return element;
		}

	}

	/**
	 * get the DOM element for xml/rdf serialization
	 * 
	 * @return the DOM Element
	 */
	public Element getElement() {
		return content.getElement();
	}

}
