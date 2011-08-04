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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.padaf.xmpbox.CreateXMPMetadataException;
import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.PDFAExtensionSchema;
import org.apache.padaf.xmpbox.schema.PDFAFieldDescription;
import org.apache.padaf.xmpbox.schema.SchemaDescription;
import org.apache.padaf.xmpbox.schema.XMPSchema;
import org.apache.padaf.xmpbox.type.AbstractSimpleProperty;
import org.apache.padaf.xmpbox.type.Attribute;
import org.apache.padaf.xmpbox.type.BadFieldValueException;
import org.apache.padaf.xmpbox.type.BooleanType;
import org.apache.padaf.xmpbox.type.ComplexProperty;
import org.apache.padaf.xmpbox.type.ComplexPropertyContainer;
import org.apache.padaf.xmpbox.type.DateType;
import org.apache.padaf.xmpbox.type.IntegerType;
import org.apache.padaf.xmpbox.type.RealType;
import org.apache.padaf.xmpbox.type.TextType;
import org.apache.padaf.xmpbox.type.ThumbnailType;


/**
 * Parse serialized XMP (in XML/RDF Format) to the XmpBox representation.
 * 
 * @author a183132
 * 
 */
public class XMPDocumentBuilder {
	protected NSMapping nsMap;

	protected ThreadLocal<XMLStreamReader> reader = new ThreadLocal<XMLStreamReader>();
	
	protected List<XMPDocumentPreprocessor> preprocessors = new ArrayList<XMPDocumentPreprocessor>();

	/**
	 * Constructor of a XMPDocumentBuilder
	 * 
	 * @throws XmpSchemaException
	 *             When instancing schema object failed or in PDF/A Extension
	 *             case, if its namespace miss
	 */
	public XMPDocumentBuilder() throws XmpSchemaException {
		nsMap = new NSMapping();
	}

	/**
	 * Parsing method. Return a XMPMetadata object with all elements read
	 * 
	 * @param xmp
	 *            serialized XMP
	 * @return Metadata with all information read
	 * @throws XmpParsingException
	 *             When element expected not found
	 * @throws XmpSchemaException
	 *             When instancing schema object failed or in PDF/A Extension
	 *             case, if its namespace miss
	 * @throws XmpUnknownValueTypeException
	 *             When ValueType found not correspond to basic type and not has
	 *             been declared in current schema
	 * @throws XmpExpectedRdfAboutAttribute
	 *             When rdf:Description not contains rdf:about attribute
	 * @throws XmpXpacketEndException
	 *             When xpacket end Processing Instruction is missing or is
	 *             incorrect
	 * @throws BadFieldValueException
	 *             When treat a Schema associed to a schema Description in PDF/A
	 *             Extension schema
	 */

	public XMPMetadata parse(byte[] xmp) throws XmpParsingException,
			XmpSchemaException, XmpUnknownValueTypeException,
			XmpExpectedRdfAboutAttribute, XmpXpacketEndException,
			BadFieldValueException {

		if (!(this instanceof XMPDocumentPreprocessor)) {
			for (XMPDocumentPreprocessor processor : preprocessors) {
				NSMapping additionalNSMapping = processor.process(xmp);
				this.nsMap.importNSMapping(additionalNSMapping);				
			}
		}

		ByteArrayInputStream is = new ByteArrayInputStream(xmp);
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			reader.set(factory.createXMLStreamReader(is));

			// expect xpacket processing instruction
			expectNext(XMLStreamReader.PROCESSING_INSTRUCTION,
			"Did not find initial xpacket processing instruction");
			XMPMetadata metadata = parseInitialXpacket(reader.get().getPIData());

			// expect x:xmpmeta
			expectNextTag(XMLStreamReader.START_ELEMENT,
			"Did not find initial x:xmpmeta");
			expectName("adobe:ns:meta/", "xmpmeta");

			// expect rdf:RDF
			expectNextTag(XMLStreamReader.START_ELEMENT,
			"Did not find initial rdf:RDF");
			expectName("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "RDF");

			nsMap.resetComplexBasicTypesDeclarationInEntireXMPLevel();
			// add all namespaces which could declare nsURI of a basicValueType
			// all others declarations are ignored
			int nsCount = reader.get().getNamespaceCount();
			for (int i = 0; i < nsCount; i++) {
				if (nsMap.isComplexBasicTypes(reader.get().getNamespaceURI(i))) {
					nsMap.setComplexBasicTypesDeclarationForLevelXMP(
							reader.get().getNamespaceURI(i), 
							reader.get().getNamespacePrefix(i));
				}
			}

			// now work on each rdf:Description
			int type = reader.get().nextTag();
			while (type == XMLStreamReader.START_ELEMENT) {
				parseDescription(metadata);
				type = reader.get().nextTag();
			}

			// all description are finished
			// expect end of rdf:RDF
			expectType(XMLStreamReader.END_ELEMENT,
			"Expected end of descriptions");
			expectName("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "RDF");

			// expect ending xmpmeta
			expectNextTag(XMLStreamReader.END_ELEMENT,
			"Did not find initial x:xmpmeta");
			expectName("adobe:ns:meta/", "xmpmeta");

			// expect final processing instruction
			expectNext(XMLStreamReader.PROCESSING_INSTRUCTION,
			"Did not find final xpacket processing instruction");
			// treats xpacket end
			if (!reader.get().getPITarget().equals("xpacket")) {
				throw new XmpXpacketEndException("Excepted PI xpacket");
			}
			String xpackData = reader.get().getPIData();
			// end attribute must be present and placed in first
			// xmp spec says Other unrecognized attributes can follow, but
			// should be ignored
			if (xpackData.startsWith("end=")) {
				// check value (5 for end='X')
				if (xpackData.charAt(5)!='r' && xpackData.charAt(5)!='w') {
					throw new XmpXpacketEndException(
					"Excepted xpacket 'end' attribute with value 'r' or 'w' ");
				}
			} else {
				// should find end='r/w'
				throw new XmpXpacketEndException(
				"Excepted xpacket 'end' attribute (must be present and placed in first)");
			}

			metadata.setEndXPacket(xpackData);
			// return constructed object
			return metadata;
		} catch (XMLStreamException e) {
			throw new XmpParsingException("An error has occured when processing the underlying XMP source", e);
		} finally {
			reader.remove();
			IOUtils.closeQuietly(is);
		}
	}

	/**
	 * Parsing method using serialized xmp read from a stream
	 * 
	 * @param is
	 *            The stream to read
	 * @return Metadata with all information read
	 * @throws XmpParsingException
	 *             When element expected not found When element expected not
	 *             found
	 * @throws XmpSchemaException
	 *             When instancing schema object failed or in PDF/A Extension
	 *             case, if its namespace miss
	 * @throws XmpUnknownValueTypeException
	 *             When ValueType found not correspond to basic type and not has
	 *             been declared in current schema
	 * @throws XmpExpectedRdfAboutAttribute
	 *             When rdf:Description not contains rdf:about attribute
	 * @throws XmpXpacketEndException
	 *             When xpacket end Processing Instruction is missing or is
	 *             incorrect
	 * @throws BadFieldValueException
	 *             When treat a Schema associed to a schema Description in PDF/A
	 *             Extension schema
	 */
	public XMPMetadata parse(InputStream input) throws XmpParsingException,
			XmpSchemaException, XmpUnknownValueTypeException,
			XmpExpectedRdfAboutAttribute, XmpXpacketEndException,
			BadFieldValueException {
		
		byte[] bos = getStreamAsByteArray(input);
		return parse(bos);
	}

	private byte[] getStreamAsByteArray(InputStream input) throws XmpParsingException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			IOUtils.copyLarge(input, bos);
		} catch (IOException e) {
			throw new XmpParsingException("An error has occured when processing the underlying XMP source", e);
		} finally {
			IOUtils.closeQuietly(bos);
			IOUtils.closeQuietly(input);
		}
		return bos.toByteArray();
	}
	
	public void addPreprocessor(XMPDocumentPreprocessor processor) {
		this.preprocessors.add(processor);
	}

	/**
	 * Check InitialXPacket and build metadata object with these information
	 * 
	 * @param data
	 *            data corresponding to Initial XPacket Processing Instruction
	 *            Processing Information corresponding to Inital XPacket data
	 * @return Metadata with specified information
	 * @throws XmpInitialXPacketParsingException
	 *             When Initial XPacket missing or is incorrect
	 * @throws CreateXMPMetadataException
	 *             If DOM Document associated could not be created
	 */
	protected XMPMetadata parseInitialXpacket(String data)
			throws XmpInitialXPacketParsingException,
			CreateXMPMetadataException {
		StringTokenizer tokens = new StringTokenizer(data, " ");
		String id = null;
		String begin = null;
		String bytes = null;
		String encoding = null;
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken();
			if (!token.endsWith("\"") && !token.endsWith("\'")) {
				throw new XmpInitialXPacketParsingException(
						"Cannot understand PI data part : '" + token + "'");
			}
			String quote = token.substring(token.length()-1);
			int pos = token.indexOf("="+quote);
			if (pos <= 0) {
				throw new XmpInitialXPacketParsingException(
						"Cannot understand PI data part : '" + token + "'");
			}
			String name = token.substring(0, pos);
			String value = token.substring(pos + 2, token.length() - 1);
			if ("id".equals(name)) {
				id = value;
			} else if ("begin".equals(name)) {
				begin = value;
			} else if ("bytes".equals(name)) {
				bytes = value;
			} else if ("encoding".equals(name)) {
				encoding = value;
			} else {
				throw new XmpInitialXPacketParsingException(
						"Unknown attribute in xpacket PI : '" + token + "'");
			}
		}
		return new XMPMetadata(begin, id, bytes, encoding);
	}

	/**
	 * Check the next element type. all comments are ignored.
	 * 
	 * @param expectType
	 *            Type of xml element expected
	 * @param message
	 *            Error message if problems occur
	 * @throws XmpParsingException
	 *             When element expected not found
	 * @throws XmpUnexpectedTypeException
	 *             When DOM Element type found unexpected When DOM Element type
	 *             found unexpected
	 * @throws XMLStreamException
	 *             When error during reading the rest of xmp stream When error
	 *             during reading the rest of xmp stream
	 */
	private void expectNext(int expectType, String message)
			throws XmpParsingException, XmpUnexpectedTypeException,
			XMLStreamException {
		try {
			int type = reader.get().next();
			while (type == XMLStreamReader.COMMENT || type == XMLStreamReader.SPACE) {
				type = reader.get().next();
			}
			if (type != expectType) {
				throw new XmpUnexpectedTypeException(message);
			}
		} catch (NoSuchElementException e) {
			// unexpected end of stream
			throw new XmpParsingException(
					"XMP Stream did not end in a good way, invalid content");
		}
	}

	/**
	 * Check the next element type. White spaces , Comments and Processing
	 * Instructions are ignored.
	 * 
	 * @param type
	 *            Type of xml element expected
	 * @param message
	 *            Error message if problems occur
	 * @throws XmpParsingException
	 *             When element expected not found
	 * @throws XmpUnexpectedTypeException
	 *             When DOM Element type found unexpected
	 * @throws XMLStreamException
	 *             When error during reading the rest of xmp stream
	 */
	private void expectNextTag(int type, String message)
			throws XmpParsingException, XmpUnexpectedTypeException,
			XMLStreamException {
		try {
			if (!(reader.get().nextTag() == type)) {
				throw new XmpUnexpectedTypeException(message);
			}
		} catch (NoSuchElementException e) {
			// unexpected end of stream
			throw new XmpParsingException(
					"XMP Stream did not end in a good way, invalid content");
		}
	}

	/**
	 * check if qualified name of current element is what is expected
	 * 
	 * @param namespace
	 *            namespace URI
	 * @param name
	 *            current element name
	 * @throws XmpUnexpectedElementQualifiedNameException
	 *             When a qualifiedName found and is not that expected
	 * 
	 */
	private void expectName(String namespace, String name)
			throws XmpUnexpectedElementQualifiedNameException {
		if (!reader.get().getNamespaceURI().equals(namespace)) {
			throw new XmpUnexpectedElementQualifiedNameException("Expected '"
					+ namespace + "' and found '"
					+ reader.get().getNamespaceURI() + "'");
		}
		if (!reader.get().getLocalName().equals(name)) {
			throw new XmpUnexpectedElementQualifiedNameException("Expected '"
					+ name + "' and found '" + reader.get().getLocalName()
					+ "'");
		}
	}

	/**
	 * Check the current element type.
	 * 
	 * @param type
	 *            XML element type expected
	 * @param message
	 *            Error Message if problems occur
	 * @throws XmpUnexpectedTypeException
	 *             When DOM Element type found unexpected
	 */
	private void expectType(int type, String message)
			throws XmpUnexpectedTypeException {
		if (!(type == reader.get().getEventType())) {
			throw new XmpUnexpectedTypeException("Expected type " + type
					+ " and found " + reader.get().getEventType() + " : "
					+ message);
		}
	}

	/**
	 * Check if rdf:about attribute is declared for rdf description and add all
	 * attributes to the schema
	 * 
	 * @param metadata
	 *            Metadata to attach new elements
	 * @param schema
	 *            Schema corresponding to the rdf:Description use
	 * @throws XmpExpectedRdfAboutAttribute
	 *             When rdf:Description not contains rdf:about attribute
	 */
	protected final void treatDescriptionAttributes(XMPMetadata metadata,
			XMPSchema schema) throws XmpExpectedRdfAboutAttribute {
		int cptAtt = reader.get().getAttributeCount();
		if (cptAtt < 1) {
			System.out.println(reader.get().getLocalName());
			throw new XmpExpectedRdfAboutAttribute(
					"Expected rdf:about attribute not found");

		} else {
			int i = 0;
			boolean rdfAboutFound = false;
			String prefix;
			while (i < cptAtt) {
				// rdf:about attribute must be here and can be presented by
				// rdf:about and about
				// according to
				// http://www.w3.org/TR/1999/REC-rdf-syntax-19990222/#basic
				// (2.2. Basic RDF Syntax)
				if (reader.get().getAttributeLocalName(i).equals("about")) {
					prefix = reader.get().getAttributePrefix(i);
					if (prefix != null) {
						if (!prefix.equals("") && !prefix.equals("rdf")) {
							// System.out.println("prefix de l'attribut "+reader.get().getAttributeLocalName(i)+": "+prefix);
							throw new XmpExpectedRdfAboutAttribute(
									"An about attribute is present but have an invalid prefix (it must be 'rdf')");
						}
					}
					rdfAboutFound = true;
				}
				schema.setAttribute(new Attribute(null, reader.get()
						.getAttributePrefix(i), reader.get()
						.getAttributeLocalName(i), reader.get()
						.getAttributeValue(i)));

				i++;
			}
			if (!rdfAboutFound) {
				System.out.println(reader.get().getLocalName());
				throw new XmpExpectedRdfAboutAttribute(
						"Expected rdf:about attribute not found");
			}
		}
	}

	/**
	 * Treat each rdf:Description (which must represent a schema), instanciate
	 * class representation of this schema and add it to metadata
	 * 
	 * @param metadata
	 *            Metadata to attach new elements
	 * @throws XmpParsingException
	 *             When element expected not found
	 * @throws XMLStreamException
	 *             When error during reading the rest of xmp stream
	 * @throws XmpSchemaException
	 *             When instancing schema object failed or in PDF/A Extension
	 *             case, if its namespace miss
	 * @throws XmpUnknownValueTypeException
	 *             When ValueType found not correspond to basic type and not has
	 *             been declared in current schema
	 * @throws XmpExpectedRdfAboutAttribute
	 *             When rdf:Description not contains rdf:about attribute
	 * @throws BadFieldValueException
	 *             When a bad value found in Schema description content
	 */
	protected void parseDescription(XMPMetadata metadata)
			throws XmpParsingException, XMLStreamException, XmpSchemaException,
			XmpUnknownValueTypeException, XmpExpectedRdfAboutAttribute,
			BadFieldValueException {
		nsMap.resetComplexBasicTypesDeclarationInSchemaLevel();
		int cptNS = reader.get().getNamespaceCount();
		HashMap<String, String> namespaces = new HashMap<String, String>();
		for (int i = 0; i < cptNS; i++) {
			namespaces.put(reader.get().getNamespacePrefix(i), reader.get()
					.getNamespaceURI(i));
			if (nsMap.isComplexBasicTypes(reader.get().getNamespaceURI(i))) {
				// System.out.println("in parseDesc method: prefix:"+reader.get().getNamespacePrefix(i)+", nsURI:"+reader.get().getNamespaceURI(i));
				nsMap.setComplexBasicTypesDeclarationForLevelSchema(reader
						.get().getNamespaceURI(i), reader.get()
						.getNamespacePrefix(i));
			}
		}
		// Different treatment for PDF/A Extension schema
		// System.out.println(PDFAExtensionSchema.PDFAEXTENSION+";"+PDFAExtensionSchema.PDFAPROPERTY+";"+PDFAExtensionSchema.PDFASCHEMA);
		if (namespaces.containsKey(PDFAExtensionSchema.PDFAEXTENSION)) {
			if (namespaces.containsKey(PDFAExtensionSchema.PDFAPROPERTY)
					&& namespaces.containsKey(PDFAExtensionSchema.PDFASCHEMA)) {
				if (namespaces
						.containsValue(PDFAExtensionSchema.PDFAEXTENSIONURI)
						&& namespaces
								.containsValue(PDFAExtensionSchema.PDFAPROPERTYURI)
						&& namespaces
								.containsValue(PDFAExtensionSchema.PDFASCHEMAURI)) {
					PDFAExtensionSchema schema = metadata
							.createAndAddPDFAExtensionSchemaWithNS(namespaces);
					treatDescriptionAttributes(metadata, schema);
					parseExtensionSchema(schema, metadata);

				} else {
					throw new XmpUnexpectedNamespaceURIException(
							"Unexpected namespaceURI in PDFA Extension Schema encountered");
				}
			} else {
				throw new XmpUnexpectedNamespacePrefixException(
						"Unexpected namespace Prefix in PDFA Extension Schema");
			}

		} else {
			// TODO Considering first namespace is that corresponding to the
			// schema (see if it must be changed)
			String namespaceUri = reader.get().getNamespaceURI(0);
			String namespacePrefix = reader.get().getNamespacePrefix(0);
			XMPSchema schema = nsMap.getAssociatedSchemaObject(metadata, namespaceUri, namespacePrefix);
			if (schema != null) {
				namespaces.remove(namespacePrefix);
			} else {
				schema = metadata.createAndAddDefaultSchema(namespacePrefix,namespaceUri);
			}
			for (int i = 1; i < cptNS; i++) {
				schema.setAttribute(new Attribute(XMPSchema.NS_NAMESPACE,
						"xmlns", reader.get().getNamespacePrefix(i), reader.get().getNamespaceURI(i)));
			}
			treatDescriptionAttributes(metadata, schema);
			while (reader.get().nextTag() == XMLStreamReader.START_ELEMENT) {
				parseProperty(schema, metadata);
			}
		}

	}

	/**
	 * Check the next element type and its expected value
	 * 
	 * @param type
	 *            expected type of xml element
	 * @param localNameExpected
	 *            The property name (local) expected
	 * @param message
	 *            Error message if problems occur
	 * @throws XmpUnexpectedTypeException
	 *             When DOM Element type found unexpected
	 * @throws XmpParsingException
	 *             When element expected not found
	 * @throws XMLStreamException
	 *             When error during reading the rest of xmp stream
	 */
	private void expectNextSpecificTag(int type, String localNameExpected,
			String message) throws XmpUnexpectedTypeException,
			XmpParsingException, XMLStreamException {
		expectNextTag(type, message);
		expectCurrentLocalName(localNameExpected);
	}

	/**
	 * check that the current local name is that expected
	 * 
	 * @param localNameExpected
	 *            The name expected
	 * @throws XmpUnexpectedElementException
	 *             When Element is not that expected
	 */
	private void expectCurrentLocalName(String localNameExpected)
			throws XmpUnexpectedElementException {
		if (!reader.get().getLocalName().equals(localNameExpected)) {
			throw new XmpUnexpectedElementException("'" + localNameExpected
					+ "' expected and '" + reader.get().getLocalName()
					+ "' found");
		}
	}

	/**
	 * Treat a PDFAExtension schema
	 * 
	 * @param schema
	 *            PDFA/Extension schema where save information found
	 * @param metadata
	 *            Metadata to attach new elements
	 * @throws XmpParsingException
	 *             When element expected not found
	 * @throws XMLStreamException
	 *             When error during reading the rest of xmp stream
	 * @throws XmpUnknownValueTypeException
	 *             When ValueType found not correspond to basic type and not has
	 *             been declared in current schema
	 * @throws BadFieldValueException
	 *             When one of a field property include to describe a property
	 *             in Schema Description contain an incorrect value
	 */
	protected final void parseExtensionSchema(PDFAExtensionSchema schema,
			XMPMetadata metadata) throws XmpParsingException,
			XMLStreamException, XmpUnknownValueTypeException,
			BadFieldValueException {
		// <pdfaExtension:schemas>
		expectNextSpecificTag(XMLStreamReader.START_ELEMENT, "schemas",
				"Cannot find container declaration of schemas descriptions ");
		// <rdf:Bag>
		expectNextSpecificTag(XMLStreamReader.START_ELEMENT, "Bag",
				"Cannot find bag declaration for container of schemas descriptions");
		// now work on each rdf:li corresponding to each schema description
		int type = reader.get().nextTag();
		while (type == XMLStreamReader.START_ELEMENT) {
			parseSchemaDescription(schema, metadata);
			type = reader.get().nextTag();
		}
		expectNextSpecificTag(XMLStreamReader.END_ELEMENT, "schemas",
				"Cannot find end of container declaration in schemas descriptions ");
		expectNextSpecificTag(XMLStreamReader.END_ELEMENT, "Description",
				"Cannot find end of PDF/A Extension definition ");

	}

	/**
	 * Treat one Schema description defined in the extension Schema found
	 * 
	 * @param schema
	 *            PDFA/Extension schema where save information found
	 * @param metadata
	 *            Metadata to attach new elements
	 * @throws XMLStreamException
	 *             When error during reading the rest of xmp stream
	 * @throws XmpParsingException
	 *             When element expected not found
	 * @throws XmpUnknownValueTypeException
	 *             When ValueType found not correspond to basic type and not has
	 *             been declared in current schema
	 * @throws BadFieldValueException
	 *             When one of a field property contain an incorrect value
	 */
	private void parseSchemaDescription(PDFAExtensionSchema schema,
			XMPMetadata metadata) throws XMLStreamException,
			XmpParsingException, XmpUnknownValueTypeException,
			BadFieldValueException {
		expectCurrentLocalName("li");
		SchemaDescription desc = schema.createSchemaDescription();
		int type = reader.get().nextTag();
		while (type == XMLStreamReader.START_ELEMENT) {
			if (reader.get().getLocalName().equals("schema")
					|| reader.get().getLocalName().equals("namespaceURI")
					|| reader.get().getLocalName().equals("prefix")) {
				try {
					// System.out.println(reader.get().getPrefix()+";"+reader.get().getLocalName()+";"+reader.get().getElementText());
					desc.addProperty(new TextType(metadata, reader.get()
							.getPrefix(), reader.get().getLocalName(), reader
							.get().getElementText()));
				} catch (IllegalArgumentException e) {
					throw new XmpPropertyFormatException(
							"Unexpected value for '"
									+ reader.get().getLocalName()
									+ "' property");
				}
			} else if (reader.get().getLocalName().equals("property")) {
				parsePropertyDefinition(desc);
			} else if (reader.get().getLocalName().equals("valueType")) {
				parseValueTypeDefinition(desc, metadata);

			} else {
				throw new XmpUnexpectedElementException(
						"Unexpected property definition in one of PDF/A Extension schemas description");
			}
			type = reader.get().nextTag();
		}
		schema.addSchemaDescription(desc);
		nsMap.setNamespaceDefinition(desc);

	}

	/**
	 * Treat value type definition for a specific Schema Description
	 * 
	 * @param desc
	 *            the current Schema Description analyzed
	 * @param metadata
	 *            Metadata to attach new elements
	 * @throws XmpParsingException
	 *             When element expected not found
	 * @throws XMLStreamException
	 *             When error during reading the rest of xmp stream
	 */
	private void parseValueTypeDefinition(SchemaDescription desc,
			XMPMetadata metadata) throws XmpParsingException,
			XMLStreamException {
		// <rdf:Seq>
		expectNextSpecificTag(XMLStreamReader.START_ELEMENT, "Seq",
				"Expected Seq Declaration");
		int elmtType = reader.get().nextTag();
		String type, namespaceURI, prefix, description;
		List<PDFAFieldDescription> fields;
		while (elmtType == XMLStreamReader.START_ELEMENT) {
			type = null;
			namespaceURI = null;
			prefix = null;
			description = null;
			fields = null;
			expectCurrentLocalName("li");
			elmtType = reader.get().nextTag();
			while (elmtType == XMLStreamReader.START_ELEMENT) {
				if (reader.get().getLocalName().equals("type")) {
					type = reader.get().getElementText();
				} else if (reader.get().getLocalName().equals("namespaceURI")) {
					namespaceURI = reader.get().getElementText();
				} else if (reader.get().getLocalName().equals("prefix")) {
					prefix = reader.get().getElementText();
				} else if (reader.get().getLocalName().equals("description")) {
					description = reader.get().getElementText();
				} else if (reader.get().getLocalName().equals("field")) {
					fields = parseFieldDescription(metadata);

				} else {
					throw new XmpUnexpectedElementException(
							"Unexpected property definition in one of ValueType Descriptions of PDF/A Extension schemas description");
				}
				elmtType = reader.get().nextTag();
			}
			if ((type != null) && (namespaceURI != null) && (prefix != null)
					&& (description != null)) {
				desc.addValueType(type, namespaceURI, prefix, description,
						fields);
			} else {
				throw new XmpRequiredPropertyException(
						"one property declaration in PDF/A Extension is not complete");
			}
			elmtType = reader.get().nextTag();
		}
		expectNextSpecificTag(XMLStreamReader.END_ELEMENT, "valueType",
				"Expected End of ValueType Declaration");

	}

	/**
	 * Treat field description on the current analyzed value type description
	 * 
	 * @param metadata
	 *            Metadata to attach new elements
	 * @return A list of parsed fields
	 * @throws XMLStreamException
	 *             When error during reading the rest of xmp stream
	 * @throws XmpParsingException
	 *             When element expected not found
	 */
	private List<PDFAFieldDescription> parseFieldDescription(
			XMPMetadata metadata) throws XmpParsingException,
			XMLStreamException {
		List<PDFAFieldDescription> fields = new ArrayList<PDFAFieldDescription>();
		// <rdf:Seq>
		expectNextSpecificTag(XMLStreamReader.START_ELEMENT, "Seq",
				"Expected Seq Declaration");
		int elmtType = reader.get().nextTag();
		String name, type, description;
		while (elmtType == XMLStreamReader.START_ELEMENT) {
			expectCurrentLocalName("li");
			elmtType = reader.get().nextTag();
			name = null;
			type = null;
			description = null;

			while (elmtType == XMLStreamReader.START_ELEMENT) {
				if (reader.get().getLocalName().equals("name")) {
					name = reader.get().getElementText();
				} else if (reader.get().getLocalName().equals("valueType")) {
					type = reader.get().getElementText();
				} else if (reader.get().getLocalName().equals("description")) {
					description = reader.get().getElementText();
				} else {
					throw new XmpUnexpectedElementException(
							"Unexpected property definition in one of ValueType Field Descriptions of PDF/A Extension schemas description");
				}
				elmtType = reader.get().nextTag();
			}
			if ((name != null) && (type != null) && (description != null)) {
				PDFAFieldDescription tmp = new PDFAFieldDescription(metadata);
				tmp.setNameValue(name);
				tmp.setValueTypeValue(type);
				tmp.setDescriptionValue(description);
				fields.add(tmp);
			} else {
				throw new XmpRequiredPropertyException(
						"One valuetype field declaration in PDF/A Extension is not complete");
			}
			// expectNextTag(XMLStreamReader.END_ELEMENT,"Expected element end");
			elmtType = reader.get().nextTag();
		}
		expectNextSpecificTag(XMLStreamReader.END_ELEMENT, "field",
				"Expected End of Properties Declaration");
		if (fields.size() != 0) {
			return fields;
		}
		return null;
	}

	/**
	 * Treat a property definition for a specific Schema Description
	 * 
	 * @param desc
	 *            the current Schema Description analyzed
	 * @throws XmpParsingException
	 *             When element expected not found
	 * @throws XMLStreamException
	 *             When error during reading the rest of xmp stream
	 * @throws BadFieldValueException
	 *             When one of a field property contain an incorrect value
	 */
	private void parsePropertyDefinition(SchemaDescription desc)
			throws XmpParsingException, XMLStreamException,
			BadFieldValueException {
		// <rdf:Seq>
		expectNextSpecificTag(XMLStreamReader.START_ELEMENT, "Seq",
				"Expected Seq Declaration");
		// Each property definition
		int elmtType = reader.get().nextTag();
		String name, type, category, description;
		while (elmtType == XMLStreamReader.START_ELEMENT) {
			expectCurrentLocalName("li");
			elmtType = reader.get().nextTag();
			name = null;
			type = null;
			category = null;
			description = null;

			while (elmtType == XMLStreamReader.START_ELEMENT) {
				if (reader.get().getLocalName().equals("name")) {
					name = reader.get().getElementText();
				} else if (reader.get().getLocalName().equals("valueType")) {
					type = reader.get().getElementText();
				} else if (reader.get().getLocalName().equals("category")) {
					category = reader.get().getElementText();
				} else if (reader.get().getLocalName().equals("description")) {
					description = reader.get().getElementText();
				} else {
					throw new XmpUnexpectedElementException(
							"Unexpected property definition in one of Properties Descriptions of PDF/A Extension schemas description");
				}
				elmtType = reader.get().nextTag();
			}
			if ((name != null) && (type != null) && (category != null)
					&& (description != null)) {
				desc.addProperty(name, type, category, description);
			} else {
				throw new XmpRequiredPropertyException(
						"one property declaration in PDF/A Extension is not complete");
			}
			// expectNextTag(XMLStreamReader.END_ELEMENT,"Expected element end");
			elmtType = reader.get().nextTag();
		}
		expectNextSpecificTag(XMLStreamReader.END_ELEMENT, "property",
				"Expected End of Properties Declaration");
	}

	/**
	 * Check for all namespaces declared for the specified schema if the
	 * property searched exists and return its type or null
	 * 
	 * @param schema
	 *            The Schema to analyze
	 * @param prop
	 *            The property Qualified Name
	 * @return The property value type or null if not found in schema
	 * @throws XmpParsingException
	 *             When element expected not found
	 */
	private String getPropertyDeclarationInNamespaces(XMPSchema schema,
			QName prop) throws XmpParsingException {

		Iterator<Attribute> it = schema.getAllAttributes().iterator();
		Attribute tmp;
		ArrayList<Attribute> list = new ArrayList<Attribute>();
		while (it.hasNext()) {
			tmp = it.next();
			if (tmp.getPrefix() != null) {
				if (tmp.getPrefix().equals("xmlns")) {
					list.add(tmp);
				}
			}
		}
		it = list.iterator();
		String type;
		StringBuffer unknownNS = new StringBuffer();
		while (it.hasNext()) {
			String namespace = it.next().getValue();
			if (!nsMap.isContainedNamespace(namespace)) {
				unknownNS.append(" '").append(namespace).append("' ");
				continue;
			}
			type = nsMap.getSpecifiedPropertyType(namespace, prop);
			if (type != null) {
				return type;
			}
		}
		String uns = unknownNS.toString().trim();
		if ((uns == null) || "".equals(uns)) {
			throw new XmpUnknownPropertyException(
					"Cannot find a description for '" + prop.getLocalPart()
							+ "' property");
		} else {
			throw new XmpUnknownSchemaException(
					"Cannot find a definition for the namespace " + uns + " ");
		}

	}

	/**
	 * Build a property with the specific type defined in schema or complex
	 * property and add it to the object representation
	 * 
	 * @param metadata
	 *            Metadata to attach new elements
	 * @param propertyName
	 *            The fully qualified name of the property
	 * @param stype
	 *            Type of the property
	 * @param container
	 *            the entity where place the property representation
	 * @throws XmpUnknownPropertyTypeException
	 *             Value Type property is incorrect or the basic value type
	 *             can't be treat at the moment
	 * @throws XmpPropertyFormatException
	 *             Unexpected type found (IllegalArgumentException)
	 * @throws XMLStreamException
	 *             When error during reading the rest of xmp stream
	 */
	protected void parseXmpSimpleProperty(XMPMetadata metadata,
			QName propertyName, XmpPropertyType stype,
			ComplexPropertyContainer container)
			throws XmpUnknownPropertyTypeException, XmpPropertyFormatException,
			XMLStreamException {
		try {
			AbstractSimpleProperty prop = null;
			ArrayList<Attribute> attributes = new ArrayList<Attribute>();
			int cpt = reader.get().getAttributeCount();
			for (int i = 0; i < cpt; i++) {
				attributes.add(new Attribute(null, reader.get()
						.getAttributePrefix(i), reader.get()
						.getAttributeLocalName(i), reader.get()
						.getAttributeValue(i)));
			}
			if (stype == XmpPropertyType.Text) {
				prop = new TextType(metadata, propertyName.getPrefix(),
						propertyName.getLocalPart(), reader.get()
								.getElementText());
			} else if (stype == XmpPropertyType.Integer) {
				prop = new IntegerType(metadata, propertyName.getPrefix(),
						propertyName.getLocalPart(), reader.get()
								.getElementText());
			} else if (stype == XmpPropertyType.Date) {
				prop = new DateType(metadata, propertyName.getPrefix(),
						propertyName.getLocalPart(), reader.get()
								.getElementText());
			} else if (stype == XmpPropertyType.Boolean) {
				prop = new BooleanType(metadata, propertyName.getPrefix(),
						propertyName.getLocalPart(), reader.get()
								.getElementText());
			} else if (stype == XmpPropertyType.Real) {
				prop = new RealType(metadata, propertyName.getPrefix(),
						propertyName.getLocalPart(), reader.get()
								.getElementText());
			}
			if (prop != null) {
				container.addProperty(prop);
				// ADD ATTRIBUTES
				for (Attribute att : attributes) {
					prop.setAttribute(att);
				}
			} else {
				throw new XmpUnknownPropertyTypeException(
						"Unknown simple type found");
			}
		} catch (IllegalArgumentException e) {
			throw new XmpPropertyFormatException(
					"Unexpected type found for the property '"
							+ propertyName.getLocalPart() + "'", e);
		}
	}

	/**
	 * Parse a bag property (unordered array) with the specific type defined in
	 * schema or complex property and add it to the object representation
	 * 
	 * @param metadata
	 *            Metadata to attach new elements
	 * @param bagName
	 *            name of bag property
	 * @param stype
	 *            type of values contained in this bag
	 * @param container
	 *            the entity where place the property representation
	 * @throws XmpUnexpectedTypeException
	 *             When DOM Element type found unexpected
	 * @throws XmpParsingException
	 *             When element expected not found
	 * @throws XMLStreamException
	 *             When error during reading the rest of xmp stream
	 * @throws XmpUnknownPropertyTypeException
	 *             Value Type property is incorrect or the basic value type
	 *             can't be treat at the moment
	 * @throws XmpPropertyFormatException
	 *             Unexpected type found (IllegalArgumentException)
	 */
	protected void parseBagProperty(XMPMetadata metadata, QName bagName,
			XmpPropertyType stype, ComplexPropertyContainer container)
			throws XmpUnexpectedTypeException, XmpParsingException,
			XMLStreamException, XmpUnknownPropertyTypeException,
			XmpPropertyFormatException {
		ComplexProperty bag = new ComplexProperty(metadata,
				bagName.getPrefix(), bagName.getLocalPart(),
				ComplexProperty.UNORDERED_ARRAY);
		container.addProperty(bag);
		// <rdf:Bag>
		expectNextSpecificTag(XMLStreamReader.START_ELEMENT, "Bag",
				"Expected Seq Declaration");
		// Each property definition
		int elmtType = reader.get().nextTag();
		while ((elmtType != XMLStreamReader.END_ELEMENT)
				&& !reader.get().getName().getLocalPart().equals("Bag")) {
			parseXmpSimpleProperty(metadata, reader.get().getName(), stype, bag
					.getContainer());
			elmtType = reader.get().nextTag();

		}
		expectNextSpecificTag(XMLStreamReader.END_ELEMENT, bagName
				.getLocalPart(), "Expected end of Bag property");

	}

	/**
	 * Parse a seq property (ordered array) with the specific type defined in
	 * schema or complex property and add it to the object representation
	 * 
	 * @param metadata
	 *            Metadata to attach new elements
	 * @param seqName
	 *            name of the seq
	 * @param stype
	 *            type of values contained in this bag
	 * @param container
	 *            the entity where place the property representation
	 * @throws XmpUnexpectedTypeException
	 *             When DOM Element type found unexpected
	 * @throws XmpParsingException
	 *             When element expected not found
	 * @throws XMLStreamException
	 *             When error during reading the rest of xmp stream
	 * @throws XmpUnknownPropertyTypeException
	 *             Value Type property is incorrect or the basic value type
	 *             can't be treat at the moment
	 * @throws XmpPropertyFormatException
	 *             Unexpected type found (IllegalArgumentException)
	 */
	protected void parseSeqProperty(XMPMetadata metadata, QName seqName,
			XmpPropertyType stype, ComplexPropertyContainer container)
			throws XmpUnexpectedTypeException, XmpParsingException,
			XMLStreamException, XmpUnknownPropertyTypeException,
			XmpPropertyFormatException {
		ComplexProperty seq = new ComplexProperty(metadata,
				seqName.getPrefix(), seqName.getLocalPart(),
				ComplexProperty.ORDERED_ARRAY);
		container.addProperty(seq);
		// <rdf:Bag>
		expectNextSpecificTag(XMLStreamReader.START_ELEMENT, "Seq",
				"Expected Seq Declaration");
		// Each property definition
		int elmtType = reader.get().nextTag();
		while ((elmtType != XMLStreamReader.END_ELEMENT)
				&& !reader.get().getName().getLocalPart().equals("Seq")) {
			parseXmpSimpleProperty(metadata, reader.get().getName(), stype, seq
					.getContainer());
			elmtType = reader.get().nextTag();

		}
		expectNextSpecificTag(XMLStreamReader.END_ELEMENT, seqName
				.getLocalPart(), "Expected end of Seq property");
	}

	/**
	 * Parse Alt property (Alternative language property) with the specific type
	 * defined in schema or complex property and add it to the object
	 * representation
	 * 
	 * @param metadata
	 *            Metadata to attach new elements
	 * @param altName
	 *            name of Alt property
	 * @param stype
	 *            type of values contained in this bag
	 * @param container
	 *            the entity where place the property representation
	 * @throws XmpUnexpectedTypeException
	 *             When DOM Element type found unexpected
	 * @throws XmpParsingException
	 *             When element expected not found
	 * @throws XMLStreamException
	 *             When error during reading the rest of xmp stream
	 * @throws XmpUnknownPropertyTypeException
	 *             Value Type property is incorrect or the basic value type
	 *             can't be treat at the moment
	 * @throws XmpPropertyFormatException
	 *             Unexpected type found (IllegalArgumentException)
	 */
	protected void parseAltProperty(XMPMetadata metadata, QName altName,
			XmpPropertyType stype, ComplexPropertyContainer container)
			throws XmpUnexpectedTypeException, XmpParsingException,
			XMLStreamException, XmpUnknownPropertyTypeException,
			XmpPropertyFormatException {
		ComplexProperty alt = new ComplexProperty(metadata,
				altName.getPrefix(), altName.getLocalPart(),
				ComplexProperty.ALTERNATIVE_ARRAY);
		container.addProperty(alt);
		// <rdf:Alt>
		expectNextSpecificTag(XMLStreamReader.START_ELEMENT, "Alt",
				"Expected Alt Declaration");
		int elmtType = reader.get().nextTag();
		while (!((elmtType == XMLStreamReader.END_ELEMENT) && reader.get()
				.getName().getLocalPart().equals("Alt"))) {
			parseXmpSimpleProperty(metadata, reader.get().getName(), stype, alt
					.getContainer());
			elmtType = reader.get().nextTag();

		}
		// <dc:description><rdf:Alt><rdf:li>sujet</rdf:li></rdf:Alt></dc:description>
		expectNextSpecificTag(XMLStreamReader.END_ELEMENT, altName
				.getLocalPart(), "Expected end of alt property");

	}

	/**
	 * Create a property in a specified container (complexproperty or schema)
	 * 
	 * @param metadata
	 *            Metadata to attach new elements
	 * @param type
	 *            type of value contained in the property
	 * @param container
	 *            the entity where place the property representation
	 * @return True if property has been treated (according to its type)
	 * @throws XmpParsingException
	 *             When element expected not found
	 * @throws XmpUnexpectedTypeException
	 *             When DOM Element type found unexpected
	 * @throws XmpUnknownPropertyTypeException
	 *             Value Type property is incorrect or the basic value type
	 *             can't be treat at the moment
	 * @throws XmpPropertyFormatException
	 *             Unexpected type found (IllegalArgumentException)
	 * @throws XMLStreamException
	 *             When error during reading the rest of xmp stream
	 */
	private boolean createAndAddPropertyToContainer(XMPMetadata metadata,
			String type, ComplexPropertyContainer container)
			throws XmpParsingException, XmpUnexpectedTypeException,
			XmpUnknownPropertyTypeException, XmpPropertyFormatException,
			XMLStreamException {
		if (type.equals("Text")) {
			parseXmpSimpleProperty(metadata, reader.get().getName(),
					XmpPropertyType.Text, container);
		} else if (type.equals("Integer")) {
			parseXmpSimpleProperty(metadata, reader.get().getName(),
					XmpPropertyType.Integer, container);

		} else if (type.equals("Boolean")) {
			parseXmpSimpleProperty(metadata, reader.get().getName(),
					XmpPropertyType.Boolean, container);

		} else if (type.equals("Real")) {
			parseXmpSimpleProperty(metadata, reader.get().getName(),
					XmpPropertyType.Real, container);
		} else if (type.equals("Date")) {
			parseXmpSimpleProperty(metadata, reader.get().getName(),
					XmpPropertyType.Date, container);

		} else if (type.equals("URI")) {
			parseXmpSimpleProperty(metadata, reader.get().getName(),
					XmpPropertyType.Text, container);

		} else if (type.equals("URL")) {
			parseXmpSimpleProperty(metadata, reader.get().getName(),
					XmpPropertyType.Text, container);

		} else if (type.equals("bag Text")) {
			parseBagProperty(metadata, reader.get().getName(),
					XmpPropertyType.Text, container);
		} else if (type.equals("bag ProperName")) {
			parseBagProperty(metadata, reader.get().getName(),
					XmpPropertyType.Text, container);
		} else if (type.equals("bag Job")) {
			parseBagProperty(metadata, reader.get().getName(),
					XmpPropertyType.Text, container);
		} else if (type.equals("bag Xpath")) {
			parseBagProperty(metadata, reader.get().getName(),
					XmpPropertyType.Text, container);
		} else if (type.equals("seq Text")) {
			parseSeqProperty(metadata, reader.get().getName(),
					XmpPropertyType.Text, container);
		} else if (type.equals("seq Field")) {
			parseSeqProperty(metadata, reader.get().getName(),
					XmpPropertyType.Text, container);
		} else if (type.equals("seq Date")) {
			parseSeqProperty(metadata, reader.get().getName(),
					XmpPropertyType.Date, container);
		} else if (type.equals("Lang Alt")) {
			parseAltProperty(metadata, reader.get().getName(),
					XmpPropertyType.Text, container);
		} else {
			return false;
		}
		return true;
	}

	/**
	 * Parse a specific field
	 * 
	 * @param metadata
	 *            Metadata to attach new elements
	 * @param propertyName
	 *            the full qualified name of this property
	 * @param schema
	 *            The schema where save this property
	 * @throws XmpUnexpectedTypeException
	 *             When DOM Element type found unexpected
	 * @throws XmpParsingException
	 *             When element expected not found
	 * @throws XMLStreamException
	 *             When error during reading the rest of xmp stream
	 * @throws XmpUnknownPropertyTypeException
	 *             Value Type property is incorrect or the basic value type
	 *             can't be treat at the moment
	 * @throws XmpPropertyFormatException
	 *             Unexpected type found (IllegalArgumentException)
	 */
	protected void parseFieldProperty(XMPMetadata metadata, QName propertyName,
			XMPSchema schema) throws XmpUnexpectedTypeException,
			XmpPropertyFormatException, XmpParsingException,
			XMLStreamException, XmpUnknownPropertyTypeException {
		ComplexPropertyContainer field = new ComplexPropertyContainer(metadata,
				propertyName.getPrefix(), propertyName.getLocalPart());
		schema.addProperty(field);
		field.setAttribute(new Attribute(null, "rdf", "parseType", "Resource"));
		String type;
		int elmtType = reader.get().nextTag();
		while ((elmtType != XMLStreamReader.END_ELEMENT)
				&& !reader.get().getName().getLocalPart().equals("Seq")) {

			type = getPropertyDeclarationInNamespaces(schema, reader.get()
					.getName());
			if (!createAndAddPropertyToContainer(metadata, type, field)) {
				if (type.equals("Field")) {
					String stype = getPropertyDeclarationInNamespaces(schema,
							reader.get().getName());
					createAndAddPropertyToContainer(metadata, stype, field);
				} else {
					throw new XmpUnknownPropertyTypeException("Unknown type : "
							+ type);
				}
			}
			elmtType = reader.get().nextTag();
		}
		expectCurrentLocalName(propertyName.getLocalPart());

		// expectNextSpecificTag(XMLStreamReader.END_ELEMENT,
		// propertyName.getLocalPart(), "Expected end of field declaration");

	}

	/**
	 * analyze one property in the stream, retrieve its type according to the
	 * schema information and call its object representation building
	 * 
	 * @param schema
	 *            The schema where find information
	 * @param metadata
	 *            Metadata to attach new elements
	 * @throws XmpParsingException
	 *             When element expected not found
	 * @throws XMPUnexpectedTypeException
	 *             When DOM Element type found unexpected
	 * @throws XMLStreamException
	 *             When error during reading the rest of xmp stream
	 * @throws XmpUnknownPropertyTypeException
	 *             Value Type property is incorrect or the basic value type
	 *             can't be treat at the moment
	 * @throws XmpPropertyFormatException
	 *             Unexpected type found (IllegalArgumentException)
	 */
	protected void parseProperty(XMPSchema schema, XMPMetadata metadata)
			throws XmpParsingException, XmpPropertyFormatException,
			XmpUnexpectedTypeException, XMLStreamException,
			XmpUnknownPropertyTypeException {
		QName propertyName = reader.get().getName();
		nsMap.resetComplexBasicTypesDeclarationInPropertyLevel();
		int cptNs = reader.get().getNamespaceCount();
		for (int i = 0; i < cptNs; i++) {
			if (nsMap.isComplexBasicTypes(reader.get().getNamespaceURI(i))) {
				nsMap.setComplexBasicTypesDeclarationForLevelSchema(reader
						.get().getNamespaceURI(i), reader.get()
						.getNamespacePrefix(i));
			}
		}
		String type = getPropertyDeclarationInNamespaces(schema, propertyName);
		if (type.equals("Unmanaged")) {
		    // do not parse the property, no validation, no reserialization
			boolean cont = true;
			while (cont) {
				int t = reader.get().next();
				if (t==XMLStreamReader.END_ELEMENT) {
					if (propertyName.equals(reader.get().getName())) {
						cont = false;
					}
				}
			}
		} else if (type.equals("Text")) {
			parseXmpSimpleProperty(metadata, propertyName,
					XmpPropertyType.Text, schema.getContent());
		} else if (type.equals("Integer")) {
			parseXmpSimpleProperty(metadata, propertyName,
					XmpPropertyType.Integer, schema.getContent());

		} else if (type.equals("Boolean")) {
			parseXmpSimpleProperty(metadata, propertyName,
					XmpPropertyType.Boolean, schema.getContent());

		} else if (type.equals("Real")) {
			parseXmpSimpleProperty(metadata, propertyName,
					XmpPropertyType.Real, schema.getContent());
		} else if (type.equals("Date")) {
			parseXmpSimpleProperty(metadata, propertyName,
					XmpPropertyType.Date, schema.getContent());

		} else if (type.equals("URI")) {
			parseXmpSimpleProperty(metadata, propertyName,
					XmpPropertyType.Text, schema.getContent());

		} else if (type.equals("URL")) {
			parseXmpSimpleProperty(metadata, propertyName,
					XmpPropertyType.Text, schema.getContent());

		} else if (type.equals("bag Text")) {
			parseBagProperty(metadata, propertyName, XmpPropertyType.Text,
					schema.getContent());
		} else if (type.equals("bag ProperName")) {
			parseBagProperty(metadata, propertyName, XmpPropertyType.Text,
					schema.getContent());
		} else if (type.equals("bag Job")) {
			parseBagProperty(metadata, propertyName, XmpPropertyType.Text,
					schema.getContent());
		} else if (type.equals("bag Xpath")) {
			parseBagProperty(metadata, propertyName, XmpPropertyType.Text,
					schema.getContent());
		} else if (type.equals("seq Text")) {
			parseSeqProperty(metadata, propertyName, XmpPropertyType.Text,
					schema.getContent());
		} else if (type.equals("seq Date")) {
			parseSeqProperty(metadata, propertyName, XmpPropertyType.Date,
					schema.getContent());
		} else if (type.equals("Lang Alt")) {
			parseAltProperty(metadata, propertyName, XmpPropertyType.Text,
					schema.getContent());
		} else if (type.equals("Field")) {
			parseFieldProperty(metadata, propertyName, schema);
		} else if (type.equals("Thumbnail")) {
			parseThumbnailProperty(metadata, propertyName, schema.getContent());
		} else if (type.equals("Alt Thumbnail")) {
			parseAltThumbnailProperty(metadata, propertyName, schema
					.getContent());
		} else {
			System.out.println(reader.get().getName().getLocalPart()
					+ " de type " + type);
			throw new XmpUnknownPropertyTypeException("Unknown type : " + type);
		}

	}

	/**
	 * Treat Alternative Thumbnails property
	 * 
	 * @param metadata
	 *            Metadata to attach new elements
	 * @param altName
	 *            name of thumbnails alternative property
	 * @param container
	 *            the container where record this representation
	 * @throws XmpUnexpectedTypeException
	 *             When DOM Element type found unexpected
	 * @throws XmpParsingException
	 *             When element expected not found
	 * @throws XMLStreamException
	 *             When error during reading the rest of xmp stream
	 * @throws XmpUnknownPropertyTypeException
	 *             Value Type property is incorrect or the basic value type
	 *             can't be treat at the moment
	 * @throws XmpPropertyFormatException
	 *             Unexpected type found (IllegalArgumentException)
	 */
	private void parseAltThumbnailProperty(XMPMetadata metadata, QName altName,
			ComplexPropertyContainer container)
			throws XmpUnexpectedTypeException, XmpParsingException,
			XMLStreamException, XmpUnknownPropertyTypeException,
			XmpPropertyFormatException {
		ComplexProperty alt = new ComplexProperty(metadata,
				altName.getPrefix(), altName.getLocalPart(),
				ComplexProperty.ALTERNATIVE_ARRAY);
		container.addProperty(alt);
		// <rdf:Alt>
		expectNextSpecificTag(XMLStreamReader.START_ELEMENT, "Alt",
				"Expected Alt Declaration");
		int elmtType = reader.get().nextTag();
		while (!((elmtType == XMLStreamReader.END_ELEMENT) && reader.get()
				.getName().getLocalPart().equals("Alt"))) {
			parseThumbnailProperty(metadata, reader.get().getName(), alt
					.getContainer());
			elmtType = reader.get().nextTag();
		}

		// <dc:description><rdf:Alt><rdf:li>sujet</rdf:li></rdf:Alt></dc:description>
		expectNextSpecificTag(XMLStreamReader.END_ELEMENT, altName
				.getLocalPart(), "Expected end of alt property");
	}

	/**
	 * * Treat a thumbnail property
	 * 
	 * @param metadata
	 *            Metadata to attach new elements
	 * @param altName
	 *            name of thumbnail property
	 * @param container
	 *            The container where save property representation
	 * @throws XmpUnexpectedTypeException
	 *             When DOM Element type found unexpected
	 * @throws XmpParsingException
	 *             When element expected not found
	 * @throws XMLStreamException
	 *             When error during reading the rest of xmp stream
	 * @throws XmpUnknownPropertyTypeException
	 *             Value Type property is incorrect or the basic value type
	 *             can't be treat at the moment
	 * @throws XmpPropertyFormatException
	 *             Unexpected type found (IllegalArgumentException)
	 */
	private void parseThumbnailProperty(XMPMetadata metadata, QName altName,
			ComplexPropertyContainer container)
			throws XmpUnexpectedTypeException, XmpParsingException,
			XMLStreamException, XmpUnknownPropertyTypeException,
			XmpPropertyFormatException {
		expectCurrentLocalName("li");
		ThumbnailType thumbnail = new ThumbnailType(metadata, altName
				.getPrefix(), altName.getLocalPart());
		int elmtType = reader.get().nextTag();
		QName eltName;
		String eltContent;
		while (!((elmtType == XMLStreamReader.END_ELEMENT) && reader.get()
				.getName().getLocalPart().equals("li"))) {
			eltName = reader.get().getName();
			eltContent = reader.get().getElementText();
			if (eltName.getLocalPart().equals("height")) {
				thumbnail.setHeight(eltName.getPrefix(),
						eltName.getLocalPart(), Integer.valueOf(eltContent));
			} else if (eltName.getLocalPart().equals("width")) {
				thumbnail.setWidth(eltName.getPrefix(), eltName.getLocalPart(),
						Integer.valueOf(eltContent));
			} else if (eltName.getLocalPart().equals("image")) {
				thumbnail.setImg(eltName.getPrefix(), eltName.getLocalPart(),
						eltContent);
			} else if (eltName.getLocalPart().equals("format")) {
				thumbnail.setFormat(eltName.getPrefix(),
						eltName.getLocalPart(), eltContent);
			} else {
				throw new XmpParsingException(
						"Unknown property name for a thumbnail element : "
								+ eltName.getLocalPart());
			}
			elmtType = reader.get().nextTag();
		}
		container.addProperty(thumbnail);
	}

}
