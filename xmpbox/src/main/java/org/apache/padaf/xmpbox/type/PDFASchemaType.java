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

public class PDFASchemaType extends AbstractStructuredType {

    public static final String ELEMENT_NS = "http://www.aiim.org/pdfa/ns/schema#";

    public static final String PREFERED_PREFIX = "pdfaSchema";

	@PropertyType(propertyType = "Text")
	public static final String SCHEMA = "schema";

	@PropertyType(propertyType = "URI")
	public static final String NAMESPACE_URI = "namespaceURI";

	@PropertyType(propertyType = "Text")
	public static final String PREFIX = "prefix";

	@PropertyType(propertyType = "seq PDFAProperty")
	public static final String PROPERTY = "property";

	@PropertyType(propertyType = "seq PDFAType")
	public static final String VALUE_TYPE = "valueType";

	public PDFASchemaType(XMPMetadata metadata) {
		super(metadata, ELEMENT_NS, PREFERED_PREFIX);
	}

	public String getNamespaceURI() {
		URIType tt = (URIType) getProperty(NAMESPACE_URI);
		return tt == null ? null : tt.getStringValue();
	}

	public String getPrefix() {
		TextType tt = (TextType) getProperty(PREFIX);
		return tt == null ? null : tt.getStringValue();
	}

	public ArrayProperty getProperty() {
		return (ArrayProperty) getArrayProperty(PROPERTY);
	}

	public ArrayProperty getValueType() {
		return (ArrayProperty) getArrayProperty(VALUE_TYPE);
	}

	
	
}
