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

package org.apache.padaf.xmpbox.parser;

import java.util.HashMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.PDFAExtensionSchema;
import org.apache.padaf.xmpbox.type.BadFieldValueException;

public class PDFAExtentionSchemaPreprocessor extends XMPDocumentBuilder implements XMPDocumentPreprocessor {

	public PDFAExtentionSchemaPreprocessor() throws XmpSchemaException {
		super();
	}

	public NSMapping process(byte[] xmp) throws XmpParsingException, XmpSchemaException, 
		XmpUnknownValueTypeException, XmpExpectedRdfAboutAttribute, XmpXpacketEndException, BadFieldValueException {
		parse(xmp);
		return this.nsMap;
	}

	protected void parseDescription(XMPMetadata metadata)
	throws XmpParsingException, XMLStreamException, XmpSchemaException,
	XmpUnknownValueTypeException, XmpExpectedRdfAboutAttribute,
	BadFieldValueException {

		nsMap.resetComplexBasicTypesDeclarationInSchemaLevel();
		int cptNS = reader.get().getNamespaceCount();
		HashMap<String, String> namespaces = new HashMap<String, String>();
		for (int i = 0; i < cptNS; i++) {
			namespaces.put(reader.get().getNamespacePrefix(i), reader.get().getNamespaceURI(i));
			if (nsMap.isComplexBasicTypes(reader.get().getNamespaceURI(i))) {
				nsMap.setComplexBasicTypesDeclarationForLevelSchema(reader
						.get().getNamespaceURI(i), reader.get()
						.getNamespacePrefix(i));
			}
		}

		// Different treatment for PDF/A Extension schema
		if (namespaces.containsKey(PDFAExtensionSchema.PDFAEXTENSION)) {

			if (namespaces.containsKey(PDFAExtensionSchema.PDFAPROPERTY)
					&& namespaces.containsKey(PDFAExtensionSchema.PDFASCHEMA)) {

				if (namespaces.containsValue(PDFAExtensionSchema.PDFAEXTENSIONURI)
						&& namespaces.containsValue(PDFAExtensionSchema.PDFAPROPERTYURI)
						&& namespaces.containsValue(PDFAExtensionSchema.PDFASCHEMAURI)) {
					PDFAExtensionSchema schema = metadata.createAndAddPDFAExtensionSchemaWithNS(namespaces);
					treatDescriptionAttributes(metadata, schema);
					parseExtensionSchema(schema, metadata);
				} else {
					throw new XmpUnexpectedNamespaceURIException("Unexpected namespaceURI in PDFA Extension Schema encountered");
				}

			} else {
				throw new XmpUnexpectedNamespacePrefixException("Unexpected namespace Prefix in PDFA Extension Schema");
			}

		} else {
			int openedTag = 0;
			while (reader.get().nextTag() == XMLStreamReader.START_ELEMENT) {
				openedTag=1;
				do {
					int tag = reader.get().next();
					if (tag == XMLStreamReader.START_ELEMENT) {
						openedTag++;
					} else if (tag == XMLStreamReader.END_ELEMENT) {
						openedTag--;
					} 
				} while (openedTag>0);
			}
		}
	}
}
