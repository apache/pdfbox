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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

import org.apache.padaf.xmpbox.CreateXMPMetadataException;
import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.XmpConstants;
import org.apache.padaf.xmpbox.schema.NSMapping;
import org.apache.padaf.xmpbox.schema.PDFAExtensionSchema;
import org.apache.padaf.xmpbox.schema.PDFAFieldDescription;
import org.apache.padaf.xmpbox.schema.SchemaDescription;
import org.apache.padaf.xmpbox.schema.SchemaMapping;
import org.apache.padaf.xmpbox.schema.XMPSchema;
import org.apache.padaf.xmpbox.type.AbstractField;
import org.apache.padaf.xmpbox.type.AbstractSimpleProperty;
import org.apache.padaf.xmpbox.type.AbstractStructuredType;
import org.apache.padaf.xmpbox.type.Attribute;
import org.apache.padaf.xmpbox.type.BadFieldValueException;
import org.apache.padaf.xmpbox.type.ArrayProperty;
import org.apache.padaf.xmpbox.type.ComplexPropertyContainer;
import org.apache.padaf.xmpbox.type.TextType;
import org.apache.padaf.xmpbox.type.TypeDescription;
import org.apache.padaf.xmpbox.type.TypeMapping;
import org.apache.pdfbox.io.IOUtils;


/**
 * Parse serialized XMP (in XML/RDF Format) to the XmpBox representation.
 * 
 * @author a183132
 * 
 */
public class XMPDocumentBuilder {

	private NSMapping nsMap;
	
	private TypeMapping typeMapping = null;
	
	private SchemaMapping schemaMapping = null;

	private ThreadLocal<XMLStreamReader> reader = new ThreadLocal<XMLStreamReader>();

	private List<XMPDocumentPreprocessor> preprocessors = new ArrayList<XMPDocumentPreprocessor>();

	public static final String VALUE_TYPE_NAME = "valueType";
	
	/**
	 * RDF namespace constant
	 */

	/**
	 * Constructor of a XMPDocumentBuilder
	 * 
	 * @throws XmpSchemaException
	 *             When instancing schema object failed or in PDF/A Extension
	 *             case, if its namespace miss
	 */
	public XMPDocumentBuilder() throws XmpSchemaException {
		nsMap = new NSMapping(this);
		this.typeMapping = new TypeMapping();
		this.schemaMapping = new SchemaMapping();
	}

	public XMPMetadata createXMPMetadata () throws CreateXMPMetadataException {
		return new WrappedXMPMetadata(this);
	}
	
	public XMPMetadata createXMPMetadata (String begin, String id, String bytes, String encoding) throws CreateXMPMetadataException {
		return new WrappedXMPMetadata(this,begin, id, bytes, encoding);

	}
	protected class WrappedXMPMetadata extends XMPMetadata {

		public WrappedXMPMetadata(XMPDocumentBuilder builder) throws CreateXMPMetadataException {
			super(builder);
		}

		public WrappedXMPMetadata(XMPDocumentBuilder builder, String xpacketBegin, String xpacketId,
				String xpacketBytes, String xpacketEncoding)
				throws CreateXMPMetadataException {
			super(builder, xpacketBegin, xpacketId, xpacketBytes, xpacketEncoding);
			// TODO Auto-generated constructor stub
		}
		
		
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
			expectName(XmpConstants.RDF_NAMESPACE, "RDF");

			nsMap.resetComplexBasicTypesDeclarationInEntireXMPLevel();
			// add all namespaces which could declare nsURI of a basicValueType
			// all others declarations are ignored
			int nsCount = reader.get().getNamespaceCount();
			for (int i = 0; i < nsCount; i++) {
				if (typeMapping.isStructuredTypeNamespace(reader.get().getNamespaceURI(i))) {
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
			expectName(XmpConstants.RDF_NAMESPACE, "RDF");

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
			IOUtils.copy(input, bos);
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
		return new WrappedXMPMetadata(this,begin, id, bytes, encoding);
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
					"XMP Stream did not end in a good way, invalid content",e);
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
	private void expectNextTag(int type, String ... message)
			throws XmpParsingException, XmpUnexpectedTypeException,
			XMLStreamException {
		try {
			if (!(reader.get().nextTag() == type)) {
				StringBuilder sb = new StringBuilder();
				for (String string : message) {
					sb.append(string);
				}
				throw new XmpUnexpectedTypeException(sb.toString());
			}
		} catch (NoSuchElementException e) {
			// unexpected end of stream
			throw new XmpParsingException(
					"XMP Stream did not end in a good way, invalid content",e);
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
	 * @throws XmpUnexpectedTypeException if the attribute is known 
	 * as an expected property but the property type isn't a Simple type.
	 */
	protected final void treatDescriptionAttributes(XMPMetadata metadata, XMPSchema schema) 
			throws XmpExpectedRdfAboutAttribute, XmpUnexpectedTypeException {
		int cptAtt = reader.get().getAttributeCount();
		int i = 0;
		boolean rdfAboutFound = false;
		String prefix;
		while (i < cptAtt) {
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

			Attribute attr = new Attribute(null, reader.get()
					.getAttributePrefix(i), reader.get()
					.getAttributeLocalName(i), reader.get()
					.getAttributeValue(i));

			if (!addAttributeAsProperty(metadata, schema, attr)) {
				// attribute isn't a property, so add the attribute
				schema.setAttribute(attr);	
			}

			i++;
		}
		if (!rdfAboutFound) {
			// create rdf:about if not found
			Attribute attr = new Attribute(null,"rdf","about","");
			schema.setAttribute(attr);
		}
	}

	/**
	 * If the attribute has same the name as an expected property of the Schema, then the property is created using the attributes fields.
	 * 
	 * @param metadata Metadata to attach new elements
	 * @param schema Schema corresponding to the rdf:Description use
	 * @param attr the attribute used to create the property
	 * @return true if the attribute has been converted into Property
	 */
	private boolean addAttributeAsProperty(XMPMetadata metadata, XMPSchema schema, Attribute attr) {
		boolean added = false;
		String schemaNamespace = schema.getNamespaceValue();
		String prefix = attr.getPrefix() != null ? attr.getPrefix() : schema.getPrefix();
		String type = this.nsMap.getSpecifiedPropertyType(schemaNamespace, new QName(schemaNamespace, attr.getLocalName(), prefix));
		
		if (type != null) {
			AbstractSimpleProperty prop = typeMapping.instanciateSimpleProperty(metadata, null, prefix, attr.getLocalName(), attr.getValue(), type);
			schema.getContent().addProperty(prop);
			added = true;
		}
		return added;
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
			if (typeMapping.isStructuredTypeNamespace(reader.get().getNamespaceURI(i))) {
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
			int c = 0;
			String namespaceUri = reader.get().getNamespaceURI(c);
			String namespacePrefix = reader.get().getNamespacePrefix(c);
			c++;
			XMPSchema schema = nsMap.getAssociatedSchemaObject(metadata, namespaceUri, namespacePrefix);
			while (c<reader.get().getNamespaceCount() && schema==null) {
				// try next
				namespaceUri = reader.get().getNamespaceURI(c);
				namespacePrefix = reader.get().getNamespacePrefix(c);
				schema = nsMap.getAssociatedSchemaObject(metadata, namespaceUri, namespacePrefix);
				c++;
			}

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
				parseProperty(schema);
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
	protected void expectNextSpecificTag(int type, String localNameExpected,
			String ... message) throws XmpUnexpectedTypeException,
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
	protected void expectCurrentLocalName(String localNameExpected)
			throws XmpUnexpectedElementException {
		if (!reader.get().getLocalName().equals(localNameExpected)) {
			throw new XmpUnexpectedElementException("'" + localNameExpected
					+ "' expected and '" + reader.get().getLocalName()
					+ "' found at "+reader.get().getLocation());
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
		expectNextSpecificTag(XMLStreamReader.START_ELEMENT, ArrayProperty.UNORDERED_ARRAY,
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
		if ("Resource".equals(reader.get().getAttributeValue(XmpConstants.RDF_NAMESPACE, "parseType"))) {
			fillSchemaDescription(desc, metadata);
		} else {
			int type = reader.get().nextTag();
			if (type == XMLStreamReader.START_ELEMENT && reader.get().getLocalName().equals("Description")) {
				fillSchemaDescription(desc, metadata);  
				// read the end tag
				reader.get().nextTag();
			}
		}

		schema.addSchemaDescription(desc);
		nsMap.setNamespaceDefinition(desc);

	}

	protected void fillSchemaDescription(SchemaDescription desc, XMPMetadata metadata)
			throws XMLStreamException, XmpParsingException, 
			XmpUnknownValueTypeException, BadFieldValueException {
		int type = reader.get().nextTag();
		while (type == XMLStreamReader.START_ELEMENT) {
			if (reader.get().getLocalName().equals("schema")
					|| reader.get().getLocalName().equals("namespaceURI")
					|| reader.get().getLocalName().equals("prefix")) {
				try {
					// System.out.println(reader.get().getPrefix()+";"+reader.get().getLocalName()+";"+reader.get().getElementText());
					desc.addProperty(new TextType(metadata, null, reader.get()
							.getPrefix(), reader.get().getLocalName(), reader
							.get().getElementText()));
				} catch (IllegalArgumentException e) {
					StringBuilder message = new StringBuilder(50);
					message.append("Unexpected value for '");
					message.append(reader.get().getLocalName()).append("' property");
					throw new XmpPropertyFormatException(
							message.toString(),e
							);
				}
			} else if (reader.get().getLocalName().equals("property")) {
				parsePropertyDefinition(desc);
			} else if (reader.get().getLocalName().equals(VALUE_TYPE_NAME)) {
				parseValueTypeDefinition(desc, metadata);
			} else {
				throw new XmpUnexpectedElementException(
						"Unexpected property definition in one of PDF/A Extension schemas description");
			}
			type = reader.get().nextTag();
		}
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
		expectNextSpecificTag(XMLStreamReader.START_ELEMENT, ArrayProperty.ORDERED_ARRAY,
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
		expectNextSpecificTag(XMLStreamReader.END_ELEMENT, VALUE_TYPE_NAME,
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
		expectNextSpecificTag(XMLStreamReader.START_ELEMENT, ArrayProperty.ORDERED_ARRAY,
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
				} else if (reader.get().getLocalName().equals(VALUE_TYPE_NAME)) {
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
			throws XmpParsingException, XMLStreamException,	BadFieldValueException {
		// <rdf:Seq>
		expectNextSpecificTag(XMLStreamReader.START_ELEMENT, ArrayProperty.ORDERED_ARRAY, "Expected Seq Declaration");
		// Each property definition
		int elmtType = reader.get().nextTag();
		while (elmtType == XMLStreamReader.START_ELEMENT) {
			expectCurrentLocalName("li");
			if ("Resource".equals(reader.get().getAttributeValue(XmpConstants.RDF_NAMESPACE, "parseType"))) {
				fillDescription(desc);
			} else {
				elmtType = reader.get().nextTag();
				if (elmtType == XMLStreamReader.START_ELEMENT && reader.get().getLocalName().equals("Description")) {
					fillDescription(desc);  
					// read the end tag
					reader.get().nextTag();
				}
			}
			// expectNextTag(XMLStreamReader.END_ELEMENT,"Expected element end");
			elmtType = reader.get().nextTag();
		}
		expectNextSpecificTag(XMLStreamReader.END_ELEMENT, "property",
				"Expected End of Properties Declaration");
	}

	protected void fillDescription(SchemaDescription desc)
			throws XmpParsingException, XMLStreamException,	BadFieldValueException {
		int elmtType = reader.get().nextTag();
		String name = null;
		String type = null;
		String category = null;
		String description = null;

		while (elmtType == XMLStreamReader.START_ELEMENT) {
			if (reader.get().getLocalName().equals("name")) {
				name = reader.get().getElementText();
			} else if (reader.get().getLocalName().equals(VALUE_TYPE_NAME)) {
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
		if ((name != null) && (type != null) && (category != null)&& (description != null)) {
			desc.addProperty(name, type, category, description);
		} else {
			throw new XmpRequiredPropertyException("one property declaration in PDF/A Extension is not complete");
		}
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
		StringBuilder unknownNS = new StringBuilder();
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

	private void parseSimpleProperty(XMPMetadata metadata,	QName propertyName, 
			Class<? extends AbstractSimpleProperty> typeclass, ComplexPropertyContainer container)	
			throws XmpUnknownPropertyTypeException, XmpPropertyFormatException,	XMLStreamException {
		Class<? extends AbstractSimpleProperty> tclass = (Class<? extends AbstractSimpleProperty>)typeclass;
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
			Class<?> [] constParams = new Class<?> [] {XMPMetadata.class,String.class,String.class,String.class,Object.class};
			Constructor<? extends AbstractSimpleProperty> constructor = tclass.getConstructor(constParams);
			
			prop = constructor.newInstance(metadata, null,propertyName.getPrefix(),
					propertyName.getLocalPart(), reader.get()
					.getElementText());
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
		} catch (SecurityException e) {
			throw new XmpPropertyFormatException("Failed to create property",e);
		} catch (NoSuchMethodException e) {
			throw new XmpPropertyFormatException("Failed to create property",e);
		} catch (InstantiationException e) {
			throw new XmpPropertyFormatException("Failed to create property",e);
		} catch (IllegalAccessException e) {
			throw new XmpPropertyFormatException("Failed to create property",e);
		} catch (InvocationTargetException e) {
			throw new XmpPropertyFormatException("Failed to create property",e);
		}
	}

	
	protected void parseStructuredPropertyArray(XMPMetadata metadata, QName name, String ctype, StructuredPropertyParser complexParser,
			ComplexPropertyContainer container)
					throws XmpUnexpectedTypeException, XmpParsingException,
					XMLStreamException, XmpUnknownPropertyTypeException,
					XmpPropertyFormatException {
		ArrayProperty cp = new ArrayProperty(metadata,null,
				name.getPrefix(), name.getLocalPart(),
				ctype);
		container.addProperty(cp);
		// <rdf:Bag>
		expectNextSpecificTag(XMLStreamReader.START_ELEMENT, ctype,
				"Expected declaration of ",ctype);
		// Each property definition
		int elmtType = reader.get().nextTag();
		while ((elmtType != XMLStreamReader.END_ELEMENT)
				&& !reader.get().getName().getLocalPart().equals(ctype)) {
			complexParser.parse(metadata, reader.get().getName(), cp.getContainer());
			elmtType = reader.get().nextTag();

		}
		expectNextSpecificTag(XMLStreamReader.END_ELEMENT, name
				.getLocalPart(), "Expected end of property ",ctype);

	}

	


	private void parseSimplePropertyArray(XMPMetadata metadata, QName name, String ctype,
			Class<? extends AbstractSimpleProperty> stype, ComplexPropertyContainer container)
					throws XmpUnexpectedTypeException, XmpParsingException,
					XMLStreamException, XmpUnknownPropertyTypeException,
					XmpPropertyFormatException {
		ArrayProperty cp = new ArrayProperty(metadata,null,
				name.getPrefix(), name.getLocalPart(),
				ctype);
		container.addProperty(cp);
		// <rdf:Bag>
		expectNextSpecificTag(XMLStreamReader.START_ELEMENT, ctype,
				"Expected '"+ctype+"' Declaration");
		// Each property definition
		int elmtType = reader.get().nextTag();
		while ((elmtType != XMLStreamReader.END_ELEMENT)
				&& !reader.get().getName().getLocalPart().equals(ctype)) {
			parseSimpleProperty(metadata, reader.get().getName(), stype, cp
					.getContainer());
			elmtType = reader.get().nextTag();

		}
		expectNextSpecificTag(XMLStreamReader.END_ELEMENT, name
				.getLocalPart(), "Expected end of '"+ctype+"' property");
	}
	

	private boolean createAndAddPropertyToContainer(XMPMetadata metadata,
			String type, ComplexPropertyContainer container)
					throws XmpParsingException, XmpUnexpectedTypeException,
					XmpUnknownPropertyTypeException, XmpPropertyFormatException,
					XMLStreamException {
		TypeDescription typeDesc = typeMapping.getTypeDescription(type);
		
		if (type.equals("Lang alt")) {
			parseSimplePropertyArray(metadata, reader.get().getName(),
					ArrayProperty.ALTERNATIVE_ARRAY, TextType.class, container);
		} else if (typeMapping.isSimpleType(typeDesc.getType())) {
			Class<? extends AbstractSimpleProperty> tcn = (Class<? extends AbstractSimpleProperty>)typeDesc.getTypeClass();
			parseSimpleProperty(metadata, reader.get().getName(),
					tcn, container);
		} else if (typeMapping.isStructuredType(type)) {
			QName propertyName = reader.get().getName();
			TypeDescription tclass = typeMapping.getTypeDescription(type);
			StructuredPropertyParser parser = new StructuredPropertyParser(
					this, (Class<? extends AbstractStructuredType>)tclass.getTypeClass());
			parseStructuredProperty(metadata, parser, container);
		} else if (typeMapping.getArrayType(type)!=null) {
			QName propertyName = reader.get().getName();
			// retrieve array type and content type
			int pos = type.indexOf(' ');
			String arrayType = typeMapping.getArrayType(type);
			String typeInArray = type.substring(pos+1);
			TypeDescription tclass = typeMapping.getTypeDescription(typeInArray);
			Class<? extends AbstractField> tcn = tclass.getTypeClass();
			
			if (AbstractSimpleProperty.class.isAssignableFrom(tcn)) {
				// array of simple
				parseSimplePropertyArray(
						metadata, 
						propertyName, 
						arrayType, 
						(Class<? extends AbstractSimpleProperty>)tcn,
						container);
			} else if (AbstractStructuredType.class.isAssignableFrom(tcn)) {
				// array of structured
				StructuredPropertyParser parser = new StructuredPropertyParser(
						this, (Class<? extends AbstractStructuredType>)tcn);
				parseStructuredPropertyArray(metadata, propertyName, arrayType, parser, container);
			} else {
				// invalid case
				throw new XmpUnexpectedTypeException("Unknown type : "+type);
			}
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
		ComplexPropertyContainer field = new ComplexPropertyContainer(metadata,null,
				propertyName.getPrefix(), propertyName.getLocalPart());
		schema.addProperty(field);
		field.setAttribute(new Attribute(null, "rdf", "parseType", "Resource"));
		String type;
		int elmtType = reader.get().nextTag();
		while ((elmtType != XMLStreamReader.END_ELEMENT)
				&& !reader.get().getName().getLocalPart().equals(ArrayProperty.ORDERED_ARRAY)) {

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
	protected void parseProperty(XMPSchema schema)
			throws XmpParsingException, XmpPropertyFormatException,
			XmpUnexpectedTypeException, XMLStreamException,
			XmpUnknownPropertyTypeException {
		XMPMetadata metadata = schema.getMetadata();
		QName propertyName = reader.get().getName();
		nsMap.resetComplexBasicTypesDeclarationInPropertyLevel();
		int cptNs = reader.get().getNamespaceCount();
		for (int i = 0; i < cptNs; i++) {
			if (typeMapping.isStructuredTypeNamespace(reader.get().getNamespaceURI(i))) {
				nsMap.setComplexBasicTypesDeclarationForLevelSchema(reader
						.get().getNamespaceURI(i), reader.get()
						.getNamespacePrefix(i));
			}
		}
		try {
			String type = getPropertyDeclarationInNamespaces(schema, propertyName);
			// found type, manage it
			if (type.equals("Lang Alt")) {
				parseSimplePropertyArray(metadata, propertyName, ArrayProperty.ALTERNATIVE_ARRAY, TextType.class, schema.getContent());
			} else if (typeMapping.isSimpleType(type)) {
				TypeDescription tclass = typeMapping.getTypeDescription(type);
				Class<? extends AbstractSimpleProperty> tcn = (Class<? extends AbstractSimpleProperty>)tclass.getTypeClass();
				parseSimpleProperty(metadata, propertyName, tcn, schema.getContent());
			} else if (typeMapping.isStructuredType(type)) {
				TypeDescription tclass = typeMapping.getTypeDescription(type);
				StructuredPropertyParser parser = new StructuredPropertyParser(
						this, (Class<? extends AbstractStructuredType>)tclass.getTypeClass());
				parseStructuredProperty(metadata, parser, schema.getContent());
			} else if (typeMapping.getArrayType(type)!=null) {
				// retrieve array type and content type
				int pos = type.indexOf(' ');
				String arrayType = typeMapping.getArrayType(type);
				String typeInArray = type.substring(pos+1);
				TypeDescription tclass = typeMapping.getTypeDescription(typeInArray);
				Class<? extends AbstractField> tcn = tclass.getTypeClass();
				
				if (AbstractSimpleProperty.class.isAssignableFrom(tcn)) {
					// array of simple
					parseSimplePropertyArray(
							metadata, 
							propertyName, 
							arrayType, 
							(Class<? extends AbstractSimpleProperty>)tcn,
							schema.getContent());
				} else if (AbstractStructuredType.class.isAssignableFrom(tcn)) {
					// array of structured
					StructuredPropertyParser parser = new StructuredPropertyParser(
							this, (Class<? extends AbstractStructuredType>)tcn);
					parseStructuredPropertyArray(metadata, propertyName, arrayType, parser, schema.getContent());
				} else {
					// invalid case
					throw new XmpUnknownPropertyTypeException("Unknown type : "+type);
				}
			} else if (type.equals("Field")) {
				parseFieldProperty(metadata, propertyName, schema);
			} else {
				throw new XmpUnknownPropertyTypeException("Unknown type : " + type);
			}

		} catch (XmpUnknownPropertyException e) {
//			// this property is not managed in the workspace
//			// do not parse the property, no validation, no reserialization
//			boolean cont = true;
//			while (cont) {
//				int t = reader.get().next();
//				if (t==XMLStreamReader.END_ELEMENT) {
//					if (propertyName.equals(reader.get().getName())) {
//						cont = false;
//					}
//				}
//			}
			throw e;
		}

	}


	private void parseStructuredProperty(XMPMetadata metadata, StructuredPropertyParser complexParser,
			ComplexPropertyContainer container)
					throws XmpUnexpectedTypeException, XmpParsingException,
					XMLStreamException, XmpUnknownPropertyTypeException,
					XmpPropertyFormatException {
		int elmtType = reader.get().nextTag();
		while ((elmtType != XMLStreamReader.END_ELEMENT)
				&& !reader.get().getName().getLocalPart().equals("li")) {
			complexParser.parse(metadata, reader.get().getName(), container);
			elmtType = reader.get().nextTag();

		}
	}

	public NSMapping getNsMap() {
		return nsMap;
	}

	public XMLStreamReader getReader() {
		return reader.get();
	}

	public TypeMapping getTypeMapping () {
		return this.typeMapping;
	}

	public SchemaMapping getSchemaMapping() {
		return schemaMapping;
	}

	
}
