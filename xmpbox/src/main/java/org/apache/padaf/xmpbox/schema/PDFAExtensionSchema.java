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
import java.util.Map;
import java.util.Map.Entry;


import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.parser.XmpSchemaException;
import org.apache.padaf.xmpbox.type.Attribute;
import org.apache.padaf.xmpbox.type.Elementable;
import org.w3c.dom.Element;

/**
 * Representation of a PDF/A Extension schema description schema
 * 
 * @author a183132
 * 
 */
public class PDFAExtensionSchema extends XMPSchema {

	public static final String PDFAEXTENSION = "pdfaExtension";
	public static final String PDFAEXTENSIONURI = "http://www.aiim.org/pdfa/ns/extension/";

	public static final String PDFASCHEMA = "pdfaSchema";
	public static final String PDFASCHEMASEP = "pdfaSchema:";
	public static final String PDFASCHEMAURI = "http://www.aiim.org/pdfa/ns/schema#";

	public static final String PDFAPROPERTY = "pdfaProperty";
	public static final String PDFAPROPERTYSEP = "pdfaProperty:";	
	public static final String PDFAPROPERTYURI = "http://www.aiim.org/pdfa/ns/property#";

	public static final String PDFATYPE = "pdfaType";
	public static final String PDFATYPESEP = "pdfaType:";
	public static final String PDFATYPEURI = "http://www.aiim.org/pdfa/ns/type#";

	public static final String PDFAFIELD = "pdfaField";
	public static final String PDFAFIELDSEP = "pdfaField:";
	public static final String PDFAFIELDURI = "http://www.aiim.org/pdfa/ns/field#";

	@PropertyType(propertyType = "Text")
	public static final String SCHEMA = "schema";

	@PropertyType(propertyType = "URI")
	public static final String NS_URI = "namespaceURI";

	@PropertyType(propertyType = "Text")
	public static final String PREFIX = "prefix";

	@PropertyType(propertyType = "Seq Property")
	public static final String PROPERTY = "property";

	@PropertyType(propertyType = "Seq ValueType")
	public static final String VALUETYPE = "valueType";

	private SchemaDescriptionContainer descriptions;

	/**
	 * Build a new PDFExtension schema
	 * 
	 * @param metadata
	 *            The metadata to attach this schema XMPMetadata
	 */
	public PDFAExtensionSchema(XMPMetadata metadata) {
		this(metadata,PDFAEXTENSION);
	}

	public PDFAExtensionSchema(XMPMetadata metadata, String prefix) {
		super(metadata, prefix, PDFAEXTENSIONURI);
		setAttribute(new Attribute(NS_NAMESPACE, "xmlns", PDFASCHEMA,
				PDFASCHEMAURI));
		setAttribute(new Attribute(NS_NAMESPACE, "xmlns", PDFAPROPERTY,
				PDFAPROPERTYURI));
		setAttribute(new Attribute(NS_NAMESPACE, "xmlns", PDFATYPE, PDFATYPEURI));
		setAttribute(new Attribute(NS_NAMESPACE, "xmlns", PDFAFIELD,
				PDFAFIELDURI));

		descriptions = new SchemaDescriptionContainer();
		getElement().appendChild(descriptions.getElement());

	}
	/**
	 * Build a new PDFExtension schema with specified namespaces declaration
	 * 
	 * @param metadata
	 *            The metadata to attach this schema
	 * @param namespaces
	 *            List of namespaces to define
	 * @throws XmpSchemaException
	 *             The namespace URI of PDF/A Extension schema missing
	 */
	public PDFAExtensionSchema(XMPMetadata metadata,
			Map<String, String> namespaces) throws XmpSchemaException {
		super(metadata, PDFAEXTENSION, PDFAEXTENSIONURI);
		if (!namespaces.containsKey(PDFAEXTENSION)) {
			throw new XmpSchemaException(
					"Extension schema is declared without the pdfaSchema namespace specification");
		}
		namespaces.remove(PDFAEXTENSION);

		for (Entry<String, String> entry : namespaces.entrySet()) {
			setAttribute(new Attribute(NS_NAMESPACE, "xmlns", entry.getKey(),
					entry.getValue()));
		}
		descriptions = new SchemaDescriptionContainer();
		getElement().appendChild(descriptions.getElement());

	}

	/**
	 * Give the prefix of the PDF/A Extension schema
	 * 
	 * @return prefix value
	 */
	public String getPrefixValue() {
		return PDFAEXTENSION;

	}

	/**
	 * Give the namespace URI of the PDF/A Extension schema
	 * 
	 * @return namespace URI
	 */
	public String getNamespaceValue() {
		return PDFAEXTENSIONURI;
	}

	/**
	 * Add to the Extension Schema a new description schema param desc the
	 * schema description
	 * 
	 * @param desc
	 *            the new schema description
	 * @return the previous schema with same prefix, null otherwise
	 */
	public SchemaDescription addSchemaDescription(SchemaDescription desc) {
		return descriptions.addSchemaDescription(desc);
	}

	/**
	 * create Extension Schema a new description schema
	 * 
	 * @return a new empty description schema
	 */
	public SchemaDescription createSchemaDescription() {
		SchemaDescription desc = new SchemaDescription(metadata);
		return desc;
	}

	/**
	 * Give a list of all description declared
	 * 
	 * @return List of all schemaDescriptions declared
	 */
	public List<SchemaDescription> getDescriptionSchema() {
		return Collections.unmodifiableList(descriptions.schemaDescriptions);
	}

	/**
	 * Give an iterator of all description declared
	 * 
	 * @return a SchemaDescription Iterator
	 */
	public Iterator<SchemaDescription> getIteratorOfDescriptions() {
		return descriptions.getAllSchemasDescription();
	}

	/**
	 * Container of Description Schema embedded in PDF/A Extension Schema
	 * 
	 * @author a183132
	 * 
	 */
	public class SchemaDescriptionContainer implements Elementable {

		protected Element element, content;
		protected List<SchemaDescription> schemaDescriptions;

		/**
		 * 
		 * SchemasDescription Container constructor
		 */
		public SchemaDescriptionContainer() {
			element = metadata.getFuturOwner().createElement(
					PDFAEXTENSION + ":schemas");
			content = metadata.getFuturOwner().createElement("rdf:Bag");
			element.appendChild(content);

			schemaDescriptions = new ArrayList<SchemaDescription>();
		}

		/**
		 * Add a SchemaDescription to the current structure
		 * 
		 * @param obj
		 *            the property to add
		 * @return the old SchemaDescription corresponding to the same namespace
		 *         prefix if exist, else null
		 */
		public SchemaDescription addSchemaDescription(SchemaDescription obj) {
			SchemaDescription sd = getSameSchemaDescription(obj);
			if (sd != null) {
				schemaDescriptions.remove(sd);
				content.removeChild(sd.content.getElement());
			}
			// if(containsSchemaDescription(obj)){
			// removeSchemaDescription(obj);
			// }
			schemaDescriptions.add(obj);
			content.appendChild(obj.content.getElement());
			return sd;
		}

		/**
		 * Get Schema Description embedded with the same prefix as that given in
		 * parameters
		 * 
		 * @param obj
		 *            Schema Description with same prefix
		 * @return The schema Description contained
		 */
		protected SchemaDescription getSameSchemaDescription(
				SchemaDescription obj) {
			String oPrefix = obj.getPrefix();
			for (SchemaDescription existing : schemaDescriptions) {
				if (oPrefix.equals(existing.getPrefix())) {
					return existing;
				}
			}
			// else not found
			return null;
		}

		/**
		 * Return all SchemaDescription
		 * 
		 * @return SchemaDescriptions Iterator in order to be use in PDF/A
		 *         Extension Schema class
		 */
		public Iterator<SchemaDescription> getAllSchemasDescription() {
			return schemaDescriptions.iterator();
		}

		// /**
		// * Check if two SchemaDescription are similar
		// * @param prop1
		// * @param prop2
		// * @return
		// */
		// public boolean isSameSchemaDescription(SchemaDescription prop1,
		// SchemaDescription prop2){
		// if(prop1.getClass().equals(prop2.getClass()) ){
		// if(prop1.content.getElement().getTextContent().equals(prop2.content.getElement().getTextContent())){
		// return true;
		// }
		// }
		// return false;
		// }

		// /**
		// * Check if a specified SchemaDescription is embedded
		// * @param schema
		// * @return
		// */
		// public boolean containsSchemaDescription(SchemaDescription schema){
		// Iterator<SchemaDescription> it=getAllSchemasDescription();
		// SchemaDescription tmp;
		// while(it.hasNext()){
		// tmp=it.next();
		// if(isSameSchemaDescription(tmp, schema) ){
		// return true;
		// }
		// }
		// return false;
		// }

		// /**
		// * Remove a schema
		// * @param schema
		// */
		// public void removeSchemaDescription(SchemaDescription schema){
		// if(containsSchemaDescription(schema)){
		// schemaDescriptions.remove(schema);
		// content.removeChild(schema.content.getElement());
		// }
		// }

		/**
		 * Get Dom Element for xml/rdf serialization
		 * 
		 * @return the DOM Element
		 */
		public Element getElement() {
			return element;
		}

	}

}
