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
import java.util.List;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.PDFAFieldDescription;


/**
 * Represents one ValueType Description described in xml file in order to be use
 * in automatic SchemaDescriptionBulding
 * 
 * @author a183132
 * 
 */
public class ValueTypeDescription {
	protected String type;
	protected String namespaceURI;
	protected String prefix;
	protected String description;
	protected List<FieldDescription> fields;

	/**
	 * Constructor of ValueType Description with fields associated in order to
	 * be use in automatic SchemaDescriptionBulding
	 * 
	 * @param type
	 *            type (or name) of valuetype to describe
	 * @param namespaceURI
	 *            the namespace URI of valuetype to describe
	 * @param prefix
	 *            the prefix of valuetype to describe
	 * @param description
	 *            the description of valuetype to describe
	 * @param fields
	 *            Fields descriptions list associated to the valuetype to
	 *            describe
	 */
	public ValueTypeDescription(String type, String namespaceURI,
			String prefix, String description, List<FieldDescription> fields) {
		this.type = type;
		this.namespaceURI = namespaceURI;
		this.prefix = prefix;
		this.description = description;
		this.fields = fields;

	}

	/**
	 * /** Constructor of ValueType Description without fields associated in
	 * order to be use in automatic SchemaDescriptionBulding
	 * 
	 * @param type
	 *            type (or name) of valuetype to describe
	 * @param namespaceURI
	 *            the namespace URI of valuetype to describe
	 * @param prefix
	 *            the prefix of valuetype to describe
	 * @param description
	 *            the description of valuetype to describe
	 */
	public ValueTypeDescription(String type, String namespaceURI,
			String prefix, String description) {
		this.type = type;
		this.namespaceURI = namespaceURI;
		this.prefix = prefix;
		this.description = description;
		this.fields = null;

	}

	/**
	 * Get Type (or name) of this value type
	 * 
	 * @return Type defined
	 */
	public String getType() {
		return type;
	}

	/**
	 * Get the namespace URI
	 * 
	 * @return the namespace URI
	 */
	public String getNamespaceURI() {
		return namespaceURI;
	}

	/**
	 * Get the Prefix
	 * 
	 * @return the prefix defined
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Get the Description
	 * 
	 * @return the description defined
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Get the list of field description associated to this value type
	 * 
	 * @return List of fields declared for this valueType
	 */
	public List<FieldDescription> getFields() {
		return fields;
	}

	/**
	 * Get PDF/A Field Descriptions in order to be used in PDF/A Extension
	 * schema
	 * 
	 * @param metadata
	 *            The metadata to attach the future property
	 * @return The list of PDF/A Field Descriptions
	 */
	public List<PDFAFieldDescription> getPDFAFieldsAssocied(XMPMetadata metadata) {
		if (fields != null) {
			List<PDFAFieldDescription> pdfaFields = new ArrayList<PDFAFieldDescription>();
			for (FieldDescription field : fields) {
				pdfaFields.add(field.createPDFAFieldDescription(metadata));
			}
			return pdfaFields;
		}

		return null;
	}

}
