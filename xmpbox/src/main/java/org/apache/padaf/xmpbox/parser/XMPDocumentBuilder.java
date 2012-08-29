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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.XmpConstants;
import org.apache.padaf.xmpbox.schema.NSMapping;
import org.apache.padaf.xmpbox.schema.PDFAExtensionSchema;
import org.apache.padaf.xmpbox.schema.XMPSchema;
import org.apache.padaf.xmpbox.schema.XMPSchemaFactory;
import org.apache.padaf.xmpbox.schema.XmpSchemaException;
import org.apache.padaf.xmpbox.type.AbstractField;
import org.apache.padaf.xmpbox.type.AbstractSimpleProperty;
import org.apache.padaf.xmpbox.type.AbstractStructuredType;
import org.apache.padaf.xmpbox.type.ArrayProperty;
import org.apache.padaf.xmpbox.type.Attribute;
import org.apache.padaf.xmpbox.type.BadFieldValueException;
import org.apache.padaf.xmpbox.type.ComplexPropertyContainer;
import org.apache.padaf.xmpbox.type.DefinedStructuredType;
import org.apache.padaf.xmpbox.type.PDFAFieldType;
import org.apache.padaf.xmpbox.type.PDFAPropertyType;
import org.apache.padaf.xmpbox.type.PDFASchemaType;
import org.apache.padaf.xmpbox.type.PDFATypeType;
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

	private ThreadLocal<XMLStreamReader> reader = new ThreadLocal<XMLStreamReader>();

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

		XMPDocumentBuilder preproc = new XMPDocumentBuilder();
		XMPMetadata xmpPreproc = preproc.doParsingParsing(xmp,true);
		populateSchemaMapping(xmpPreproc);


		return doParsingParsing(xmp,false);
	}

	public XMPMetadata doParsingParsing(byte[] xmp, boolean parseExtension) throws XmpParsingException,
	XmpSchemaException, XmpUnknownValueTypeException,
	XmpExpectedRdfAboutAttribute, XmpXpacketEndException,
	BadFieldValueException {


		ByteArrayInputStream is = new ByteArrayInputStream(xmp);
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			reader.set(factory.createXMLStreamReader(is,"UTF8"));

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

			// TODO GBL GBA maybe not remove
			metadata.getNsMapping().resetComplexBasicTypesDeclarationInEntireXMPLevel();
			// add all namespaces which could declare nsURI of a basicValueType
			// all others declarations are ignored
			int nsCount = reader.get().getNamespaceCount();
			// TODO MUTUALIZE namespace list loading
			for (int i = 0; i < nsCount; i++) {
				if (metadata.getTypeMapping().isStructuredTypeNamespace(reader.get().getNamespaceURI(i))) {
					metadata.getNsMapping().setComplexBasicTypesDeclarationForLevelXMP(
							reader.get().getNamespaceURI(i), 
							reader.get().getNamespacePrefix(i));
				}
			}

			// now work on each rdf:Description
			int type = reader.get().nextTag();
			while (type == XMLStreamReader.START_ELEMENT) {
				parseDescription(metadata,parseExtension);
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
			parseEndPacket(metadata);
			// return constructed object
			return metadata;
		} catch (XMLStreamException e) {
			throw new XmpParsingException("An error has occured when processing the underlying XMP source", e);
		} finally {
			reader.remove();
			IOUtils.closeQuietly(is);
		}
	}

	private void populateSchemaMapping (XMPMetadata meta) 
			throws XmpRequiredPropertyException,XmpUnknownValueTypeException,XmpUnexpectedNamespacePrefixException {
		List<XMPSchema> schems = meta.getAllSchemas();
		for (XMPSchema xmpSchema : schems) {
			if (xmpSchema.getNamespace().equals(PDFAExtensionSchema.PDFAEXTENSIONURI)) {
				// ensure the prefix is the preferred one (cannot use other definition)
				if (!xmpSchema.getPrefix().equals(PDFAExtensionSchema.PDFAEXTENSION)) {
					throw new XmpUnexpectedNamespacePrefixException("Found invalid prefix for PDF/A extension, found '"+
							xmpSchema.getPrefix()+"', should be '"+PDFAExtensionSchema.PDFAEXTENSION+"'"
							);
				}
				// create schema and types
				PDFAExtensionSchema pes = (PDFAExtensionSchema)xmpSchema;
				ArrayProperty sp = pes.getSchemasProperty();
				for (AbstractField af: sp.getAllProperties()) {
					if (af instanceof PDFASchemaType) {
						PDFASchemaType st = (PDFASchemaType)af;
						String namespaceUri = st.getNamespaceURI();
						ArrayProperty properties = st.getProperty();
						ArrayProperty valueTypes = st.getValueType();
						XMPSchemaFactory xsf = meta.getSchemaMapping().getSchemaFactory(namespaceUri);
						// retrieve namespaces
						if (xsf==null) {
							// create namespace with no field
							meta.getSchemaMapping().addNewNameSpace(namespaceUri);
							xsf = meta.getSchemaMapping().getSchemaFactory(namespaceUri);
						}
						// populate value type
						if (valueTypes!=null) {
							for (AbstractField af2 : valueTypes.getAllProperties()) {
								if (af2 instanceof PDFATypeType) {
									PDFATypeType type = (PDFATypeType)af2;
									String ttype= type.getType();
									String tns = type.getNamespaceURI();
									String tprefix = type.getPrefix();
									String tdescription = type.getDescription();
									ArrayProperty fields = type.getFields();
									if (ttype==null || tns==null || tprefix==null || tdescription==null) {
										// all fields are mandatory
										throw new XmpRequiredPropertyException("Missing field in type definition");
									}
									// create the structured type
									DefinedStructuredType structuredType = new DefinedStructuredType(meta, tns, tprefix);
									if (fields!=null) {
										List<AbstractField> definedFields = fields.getAllProperties();
										for (AbstractField af3 : definedFields) {
											if (af3 instanceof PDFAFieldType) {
												PDFAFieldType field = (PDFAFieldType)af3;
												String fName = field.getName();
												String fDescription = field.getDescription();
												String fValueType = field.getValueType();
												if (fName==null || fDescription==null || fValueType==null) {
													throw new XmpRequiredPropertyException("Missing field in field definition");
												}
												// create the type
//												TypeDescription vtd = meta.getTypeMapping().getTypeDescription(fValueType);
												TypeDescription<?> vtd = meta.getTypeMapping().getTypeDescription(fValueType);
												if (vtd!=null) {
													// a type is found
													String ftype = vtd.getType();
													structuredType.addProperty(fName, ftype);
												} else {
													// unknown type
													throw new XmpUnknownValueTypeException("Type not defined : "+fValueType);
												}
											} // else TODO
										}
									}
									// add the structured type to list
									TypeDescription<AbstractStructuredType> td = new TypeDescription<AbstractStructuredType>(ttype, null, DefinedStructuredType.class);
									meta.getTypeMapping().addToStructuredMaps(td,tns);
								}
							}	
						}
						// populate properties
						for (AbstractField af2 : properties.getAllProperties()) {
							if (af2 instanceof PDFAPropertyType) {
								PDFAPropertyType property = (PDFAPropertyType)af2;
								String pname = property.getName();
								String ptype = property.getValueType();
								String pdescription = property.getDescription();
								String pCategory = property.getCategory();
								// check all mandatory fields are OK
								if (pname==null || ptype==null || pdescription==null || pCategory==null) {
									// all fields are mandatory
									throw new XmpRequiredPropertyException("Missing field in property definition");
								}
								// check ptype existance
//								TypeDescription td = meta.getTypeMapping().getTypeDescription(ptype);
								TypeDescription<?> td = meta.getTypeMapping().getTypeDescription(ptype);
								if (td==null) {
									// type not defined
									throw new XmpUnknownValueTypeException("Type not defined : "+ptype);
								}
								// load the property
								xsf.getPropertyDefinition().addNewProperty(pname, ptype);
							} // TODO unmanaged ?
						}
					} // TODO unmanaged ?
				}
			}
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
		return XMPMetadata.createXMPMetadata(begin, id, bytes, encoding);
	}

	protected void parseEndPacket (XMPMetadata metadata) throws XmpXpacketEndException {
		String xpackData = reader.get().getPIData();
		// end attribute must be present and placed in first
		// xmp spec says Other unrecognized attributes can follow, but
		// should be ignored
		if (xpackData.startsWith("end=")) {
			char end = xpackData.charAt(5);
			// check value (5 for end='X')
			if (end!='r' && end!='w') {
				throw new XmpXpacketEndException(
						"Excepted xpacket 'end' attribute with value 'r' or 'w' ");
			} else {
				metadata.setEndXPacket(Character.toString(end));
			}
		} else {
			// should find end='r/w'
			throw new XmpXpacketEndException(
					"Excepted xpacket 'end' attribute (must be present and placed in first)");
		}
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
			Attribute attr = new Attribute(XmpConstants.RDF_NAMESPACE,"about","");
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
		String schemaNamespace = schema.getNamespace();
		String prefix = /*attr.getPrefix() != null ? attr.getPrefix() :*/ schema.getPrefix();
		String type = metadata.getNsMapping().getSpecifiedPropertyType(schemaNamespace, new QName(schemaNamespace, attr.getLocalName()));

		if (type != null) {
			AbstractSimpleProperty prop = metadata.getTypeMapping().instanciateSimpleProperty(null, prefix, attr.getLocalName(), attr.getValue(), type);
			schema.addProperty(prop);
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
	protected void parseDescription(XMPMetadata metadata, boolean parseExtension)
			throws XmpParsingException, XMLStreamException, XmpSchemaException,
			XmpUnknownValueTypeException, XmpExpectedRdfAboutAttribute,
			BadFieldValueException {
		NSMapping nsMap = metadata.getNsMapping();
		nsMap.resetComplexBasicTypesDeclarationInSchemaLevel();
		int cptNS = reader.get().getNamespaceCount();
		HashMap<String, String> namespaces = new HashMap<String, String>();
		// TODO MUTUALIZE namespace list loading
		for (int i = 0; i < cptNS; i++) {
			namespaces.put(reader.get().getNamespacePrefix(i), reader.get()
					.getNamespaceURI(i));
			if (metadata.getTypeMapping().isStructuredTypeNamespace(reader.get().getNamespaceURI(i))) {
				nsMap.setComplexBasicTypesDeclarationForLevelSchema(reader
						.get().getNamespaceURI(i), reader.get()
						.getNamespacePrefix(i));
			}
		}
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
			schema.setAttribute(new Attribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
					reader.get().getNamespacePrefix(i), reader.get().getNamespaceURI(i)));
		}
		treatDescriptionAttributes(metadata, schema);
		while (reader.get().nextTag() == XMLStreamReader.START_ELEMENT) {
			parseProperty(schema,parseExtension);
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
		NSMapping nsMap = schema.getMetadata().getNsMapping();
		Iterator<Attribute> it = schema.getAllAttributes().iterator();
		Attribute tmp;
		ArrayList<Attribute> list = new ArrayList<Attribute>();
		while (it.hasNext()) {
			tmp = it.next();
			if (tmp.getNamespace() != null) {
				if (tmp.getNamespace().equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
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
			TypeDescription<AbstractSimpleProperty> description, ComplexPropertyContainer container)	
					throws XmpUnknownPropertyTypeException, XmpPropertyFormatException,	XMLStreamException {
		try {
			AbstractSimpleProperty prop = null;
			ArrayList<Attribute> attributes = new ArrayList<Attribute>();
			int cpt = reader.get().getAttributeCount();
			for (int i = 0; i < cpt; i++) {
				attributes.add(new Attribute(null, reader.get()
						.getAttributeLocalName(i), reader.get()
						.getAttributeValue(i)));
			}
			prop = metadata.getTypeMapping().instanciateSimpleProperty( null, propertyName.getPrefix(), 
					propertyName.getLocalPart(), reader.get().getElementText(),description.getType());
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
			TypeDescription<AbstractSimpleProperty> td, ComplexPropertyContainer container)
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
			parseSimpleProperty(metadata, reader.get().getName(), td, cp
					.getContainer());
			elmtType = reader.get().nextTag();

		}
		expectNextSpecificTag(XMLStreamReader.END_ELEMENT, name
				.getLocalPart(), "Expected end of '"+ctype+"' property");
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
	protected void parseProperty(XMPSchema schema, boolean parsingExtension)
			throws XmpParsingException, XmpPropertyFormatException,
			XmpUnexpectedTypeException, XMLStreamException,
			XmpUnknownPropertyTypeException {
		XMPMetadata metadata = schema.getMetadata();
		NSMapping nsMap = metadata.getNsMapping();
		TypeMapping typeMapping = metadata.getTypeMapping();
		QName propertyName = reader.get().getName();
		if (parsingExtension) {
			if (!propertyName.getNamespaceURI().equals(PDFAExtensionSchema.PDFAEXTENSIONURI)) {
				// this schema won't be parsed as extension, skip
				// XXX skip to end of element
				skipCurrentElement();
				return;
			}
		}
		nsMap.resetComplexBasicTypesDeclarationInPropertyLevel();
		int cptNs = reader.get().getNamespaceCount();
		// TODO MUTUALIZE namespace list loading
		for (int i = 0; i < cptNs; i++) {
			if (typeMapping.isStructuredTypeNamespace(reader.get().getNamespaceURI(i))) {
				nsMap.setComplexBasicTypesDeclarationForLevelSchema(reader
						.get().getNamespaceURI(i), reader.get()
						.getNamespacePrefix(i));
			}
		}
		String type = getPropertyDeclarationInNamespaces(schema, propertyName);
		// found type, manage it
		if (type.equals("Lang Alt")) {
			parseSimplePropertyArray(metadata, propertyName, ArrayProperty.ALTERNATIVE_ARRAY, typeMapping.getSimpleDescription("Text"), schema.getContainer());
		} else if (typeMapping.isSimpleType(type)) {
			TypeDescription<AbstractSimpleProperty> tclass = typeMapping.getSimpleDescription(type);
			parseSimpleProperty(metadata, propertyName, tclass, schema.getContainer());
		} else if (typeMapping.isStructuredType(type)) {
			TypeDescription<AbstractStructuredType> tclass = typeMapping.getStructuredDescription(type);
			StructuredPropertyParser parser = new StructuredPropertyParser(
					this, tclass);
			parseStructuredProperty(metadata, parser, schema.getContainer());
		} else if (typeMapping.getArrayType(type)!=null) {
			// retrieve array type and content type
			int pos = type.indexOf(' ');
			String arrayType = typeMapping.getArrayType(type);
			String typeInArray = type.substring(pos+1);
//			if (typeMapping.is)
//		TypeDescription<AbstractStructuredType> tclass = typeMapping.getTypeDescription(typeInArray);
//			Class<? extends AbstractField> tcn = tclass.getTypeClass();

			if (typeMapping.isSimpleType(typeInArray)) {
				// array of simple
				parseSimplePropertyArray(
						metadata, 
						propertyName, 
						arrayType, 
						typeMapping.getSimpleDescription(typeInArray),
						schema.getContainer());
			} else if (typeMapping.isStructuredType(typeInArray)) {
				// array of structured
				StructuredPropertyParser parser = new StructuredPropertyParser(
						this, typeMapping.getStructuredDescription(typeInArray));
				parseStructuredPropertyArray(metadata, propertyName, arrayType, parser, schema.getContainer());
			} else {
				// invalid case
				throw new XmpUnknownPropertyTypeException("Unknown type : "+type);
			}
			//			} else if (type.equals("Field")) {
			//				parseFieldProperty(metadata, propertyName, schema);
		} else {
			throw new XmpUnknownPropertyTypeException("Unknown type : " + type);
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

	public XMLStreamReader getReader() {
		return reader.get();
	}

	public void skipCurrentElement () throws XmpParsingException,XMLStreamException {
		if (!reader.get().isStartElement()) {
			throw new XmpParsingException("SkipElement only start on Start element event");
		}
		// on start element
		int openedTag = 1;
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
