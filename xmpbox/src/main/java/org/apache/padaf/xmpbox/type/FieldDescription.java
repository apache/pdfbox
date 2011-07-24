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

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.PDFAFieldDescription;

/**
 * Represents one Property Description described in xml file in order to be use
 * in automatic SchemaDescriptionBulding
 * 
 * @author a183132
 * 
 */
public class FieldDescription {
	protected String name;
	protected String valueType;
	protected String description;

	/**
	 * Constructor of a FieldDescription in order to be use in automatic
	 * SchemaDescriptionBulding
	 * 
	 * @param name
	 *            Name of the field described
	 * @param valueType
	 *            The type of the field described
	 * @param description
	 *            The description of the field described
	 */
	public FieldDescription(String name, String valueType, String description) {
		this.name = name;
		this.valueType = valueType;
		this.description = description;
	}

	/**
	 * get value Type declared
	 * 
	 * @return ValueType declared
	 */
	public String getValueType() {
		return valueType;
	}

	/**
	 * Get Description declared
	 * 
	 * @return Description declared
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Get Name declared
	 * 
	 * @return Name declared
	 */
	public String getName() {
		return name;
	}

	/**
	 * Create a PDFAFieldDescription which can be included in
	 * PDFAPropertyDescription (for Schema Descritpions in PDF/A Extension
	 * Schema)
	 * 
	 * @param metadata
	 *            The metadata to attach the future property
	 * @return The PDF/A Field Description generated
	 */
	public PDFAFieldDescription createPDFAFieldDescription(XMPMetadata metadata) {
		PDFAFieldDescription fieldDesc = new PDFAFieldDescription(metadata);
		fieldDesc.setNameValue(name);
		fieldDesc.setValueTypeValue(valueType);
		fieldDesc.setDescriptionValue(description);
		return fieldDesc;
	}
}
