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

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.padaf.xmpbox.DateConverter;
import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.type.AbstractField;
import org.apache.padaf.xmpbox.type.AbstractSimpleProperty;
import org.apache.padaf.xmpbox.type.AbstractStructuredType;
import org.apache.padaf.xmpbox.type.ArrayProperty;
import org.apache.padaf.xmpbox.type.BadFieldValueException;
import org.apache.padaf.xmpbox.type.ComplexPropertyContainer;
import org.apache.padaf.xmpbox.type.DefinedStructuredType;
import org.apache.padaf.xmpbox.type.PropMapping;
import org.apache.padaf.xmpbox.type.ReflectHelper;
import org.apache.padaf.xmpbox.type.TypeDescription;

public class StructuredPropertyParser {

	private XMPDocumentBuilder builder = null;

	private TypeDescription description = null;

	private PropMapping propDesc = null;
	
	private boolean isDefinedStructureType = false;
	

	public StructuredPropertyParser(XMPDocumentBuilder builder, TypeDescription td) 
			throws XmpPropertyFormatException {
		this.builder = builder;
		this.description = td;
		// retrieve xmp properties
		this.propDesc = ReflectHelper.initializePropMapping(null, td.getTypeClass());
		this.isDefinedStructureType = DefinedStructuredType.class.isAssignableFrom(td.getTypeClass()); 
	}

	private AbstractStructuredType instanciateProperty (XMPMetadata metadata) throws XmpParsingException {
		try {
			return metadata.getTypeMapping().instanciateStructuredType(metadata, description);
		} catch (BadFieldValueException e) {
			throw new XmpParsingException ("Failed to instanciate property",e);
		}
	}



	private boolean isParseTypeResource (XMLStreamReader reader) {
		int count = reader.getAttributeCount();
		for (int i=0; i < count ; i++) {
			if ("parseType".equals(reader.getAttributeLocalName(i))) {
				return "Resource".equals(reader.getAttributeValue(i)); 
			}
		}
		return false;
	}


	public void parse(XMPMetadata metadata, QName altName,
			ComplexPropertyContainer container)
					throws XmpParsingException, XMLStreamException {
		builder.expectCurrentLocalName("li");
		// check if parseType is defined
		boolean skipDescription = isParseTypeResource(builder.getReader());

		builder.getReader().nextTag();
		parseSimple(metadata, altName, container, skipDescription,"li");
		if (!skipDescription) {
			builder.getReader().nextTag();
		}
	}


	public void parseSimple(XMPMetadata metadata, QName altName,
			ComplexPropertyContainer container, boolean skipDescription, String limiter)
					throws XmpParsingException,	XMLStreamException {

		XMLStreamReader reader = builder.getReader();
		int elmtType = reader.getEventType();

		if (!skipDescription) {
			// rdf:Description 
			builder.expectCurrentLocalName("Description");
			elmtType = reader.nextTag();
		}
		AbstractStructuredType property = null;
		if (isDefinedStructureType) {
			// defined type
			property = new DefinedStructuredType(metadata, null, null);// TODO
		} else {
			property = instanciateProperty(metadata);
		}
		QName eltName;
		String structuredEndName = skipDescription?limiter:"Description";
		while (!((elmtType == XMLStreamReader.END_ELEMENT) && reader.getName().getLocalPart().equals(structuredEndName))) {
			// read element name, then text content
			eltName = reader.getName();

			boolean isSubSkipDescription = false;
			String subExpected = null;
			if (reader.getEventType()==XMLStreamConstants.START_ELEMENT) {
				isSubSkipDescription = isParseTypeResource(reader);
				subExpected = reader.getName().getLocalPart();
			}
			// prepare the text
			elmtType = reader.next();
			// TODO why not a space ??
			if (elmtType == XMLStreamConstants.CHARACTERS && reader.getText().trim().length()>0) {
				// text content
				StringBuilder content = new StringBuilder();
				while(elmtType != XMLStreamConstants.END_ELEMENT ) {
					if(elmtType == XMLStreamConstants.CHARACTERS
							|| elmtType == XMLStreamConstants.CDATA
							|| elmtType == XMLStreamConstants.SPACE
							|| elmtType == XMLStreamConstants.ENTITY_REFERENCE) {
						content.append(reader.getText());
					} else if(elmtType == XMLStreamConstants.PROCESSING_INSTRUCTION
							|| elmtType == XMLStreamConstants.COMMENT) {
						// skipping
					} else if(elmtType == XMLStreamConstants.START_ELEMENT) {
						throw new XMLStreamException(
								"element text content may not contain START_ELEMENT", reader.getLocation());
					} else {
						throw new XMLStreamException(
								"Unexpected event type "+elmtType, reader.getLocation());
					}
					elmtType = reader.next();
				}

				String eltContent = content.toString();
				// check if property is expected
				String localPart = eltName.getLocalPart();
				if (propDesc.containsKey(localPart)) {
					String ptype = propDesc.getPropertyType(localPart);

					AbstractField a = instanciateSimple(
							ptype, 
							metadata, 
							eltName.getPrefix(),
							localPart,
							eltContent);

					property.addProperty(a);
				} else {
					// expect only defined properties are accepted
					// XXX : really the good choice ? 
					// XXX : should we create text properties for unknown types ?
					throw new XmpParsingException(
							"Unknown property name for a job element : "
									+ eltName.getLocalPart());
				}
				elmtType = reader.nextTag();

			} else {	
				if (reader.getEventType()!=XMLStreamConstants.START_ELEMENT && reader.getEventType()!=XMLStreamConstants.END_ELEMENT) {
					reader.nextTag();
				}

				if (reader.getEventType()==XMLStreamConstants.START_ELEMENT) {
					TypeDescription td = metadata.getTypeMapping().getStructuredTypeName(eltName.getNamespaceURI());
					if (td==null) {
						throw new XmpUnexpectedNamespaceURIException("No namespace defined with name "+eltName.getNamespaceURI());
					}

					String ptype = td.getProperties().getPropertyType(eltName.getLocalPart());
					if (metadata.getTypeMapping().isStructuredType(ptype)) {
						TypeDescription tclass = metadata.getTypeMapping().getTypeDescription(ptype);
						StructuredPropertyParser sp = new StructuredPropertyParser(builder, tclass);
						sp.parseSimple(metadata, reader.getName(), property.getContainer(),isSubSkipDescription,subExpected);// TODO
					} else if (metadata.getTypeMapping().getArrayType(ptype)!=null) {
						int pos = ptype.indexOf(' ');
						String arrayType = metadata.getTypeMapping().getArrayType(ptype);
						String typeInArray = ptype.substring(pos+1);
						TypeDescription tclass = metadata.getTypeMapping().getTypeDescription(typeInArray);
						ArrayProperty cp = new ArrayProperty(metadata,null,
								eltName.getPrefix(), eltName.getLocalPart(),
								arrayType);
						property.getContainer().addProperty(cp);

						// array element starting
						builder.expectCurrentLocalName(arrayType);
						reader.nextTag();
						if (reader.getLocalName().equals("li")) {
							// array elements
							while (reader.getEventType()==XMLStreamConstants.START_ELEMENT && reader.getName().getLocalPart().equals("li")) {
								StructuredPropertyParser sp = new StructuredPropertyParser(builder, tclass);
								sp.parse(metadata, reader.getName(), cp.getContainer());
								reader.nextTag();
							}
							// array element ending
							builder.expectCurrentLocalName(arrayType);
							reader.nextTag();
						} // else, it was an empty array
					}


					elmtType = reader.nextTag();
				} else {
					// end element
					//					reader.nextTag();
					elmtType = reader.getEventType();
				}
			}
		}
		if (!skipDescription) {
			// closing rdf:Description element
			builder.expectCurrentLocalName("Description");
			//			reader.nextTag();
		}
		container.addProperty(property);

	}




	private AbstractSimpleProperty instanciateSimple (
			String type, 
			XMPMetadata metadata, 
			String prefix, 
			String propertyName,
			String valueAsString) 
					throws XmpParsingException {
		TypeDescription description = metadata.getTypeMapping().getTypeDescription(type);
		Object value = null;
		switch (description.getBasic()) {
		case Boolean : 
			value =  Boolean.parseBoolean(valueAsString);
			break;
		case Date :
			try {
				value = DateConverter.toCalendar(valueAsString);
			} catch (IOException e) {
				throw new XmpParsingException("Failed to parse date property",e);
			} 
			break;
		case Integer :
			try {
				value = Integer.parseInt(valueAsString);
			} catch (NumberFormatException e) {
				throw new XmpParsingException("Failed to parse integer property",e);
			} 
			break;
		case Real :
			try {
				value = Float.parseFloat(valueAsString);
			} catch (NumberFormatException e) {
				throw new XmpParsingException("Failed to parse real type property",e);
			} 
			break;
		case Text :
			value = valueAsString;
		}

		return metadata.getTypeMapping().instanciateSimpleProperty(metadata, null, prefix, propertyName, value, type);
	}

}
