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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.type.AbstractField;
import org.apache.padaf.xmpbox.type.AbstractSimpleProperty;
import org.apache.padaf.xmpbox.type.AbstractStructuredType;
import org.apache.padaf.xmpbox.type.ComplexPropertyContainer;
import org.apache.padaf.xmpbox.type.PropertyType;
import org.apache.padaf.xmpbox.type.TypeDescription;
import org.apache.padaf.xmpbox.type.TypeMapping;

public class StructuredPropertyParser {

	private XMPDocumentBuilder builder = null;

	private Class<? extends AbstractStructuredType> typeClass = null;

	private Constructor<? extends AbstractStructuredType> typeConstructor = null;

	private Map<String,PropertyDescription> propDesc = null;

	//	private static Class<?> [] propertyConstructorParams = new Class [] {XMPMetadata.class,String.class};
	private static Class<?> [] propertyConstructorParams = new Class [] {XMPMetadata.class};

	public StructuredPropertyParser(XMPDocumentBuilder builder,Class<? extends AbstractStructuredType> propertyTypeClass) 
			throws XmpPropertyFormatException 
			{
		this.builder = builder;
		this.typeClass = propertyTypeClass;
		this.propDesc = new HashMap<String, PropertyDescription>();
		// retrieve xmp properties
		Field [] fields = typeClass.getFields();
		for (Field field : fields) {
			if (field.getAnnotation(PropertyType.class)!=null) {
				PropertyDescription pd = new PropertyDescription();
				pd.propertyType = field.getAnnotation(PropertyType.class);
				//				pd.fieldName = field.getName();
				try {
					pd.propertyName = field.get(null).toString();
				} catch (IllegalArgumentException e1) {
					throw new XmpPropertyFormatException("Failed to parse structured type : "+typeClass.getName(),e1);
				} catch (IllegalAccessException e1) {
					throw new XmpPropertyFormatException("Failed to parse structured type : "+typeClass.getName(),e1);
				}
				propDesc.put(pd.propertyName, pd);
			}
		}
		// retrieve constructor
		try {
			typeConstructor = typeClass.getConstructor(propertyConstructorParams);
		} catch (SecurityException e) {
			throw new XmpPropertyFormatException("Failed to initialize structured type parser : "+typeClass.getName(),e);
		} catch (NoSuchMethodException e) {
			throw new XmpPropertyFormatException("Failed to initialize structured type parser : "+typeClass.getName(),e);
		}

			}

	private AbstractStructuredType instanciateProperty (XMPMetadata metadata) throws XmpParsingException {
		try {
			//			return typeConstructor.newInstance(metadata,prefix);
			return typeConstructor.newInstance(metadata);
		} catch (IllegalArgumentException e) {
			throw new XmpParsingException("Failed to instanciate structured type : "+typeClass.getName(),e);
		} catch (InstantiationException e) {
			throw new XmpParsingException("Failed to instanciate structured type : "+typeClass.getName(),e);
		} catch (IllegalAccessException e) {
			throw new XmpParsingException("Failed to instanciate structured type : "+typeClass.getName(),e);
		} catch (InvocationTargetException e) {
			throw new XmpParsingException("Failed to instanciate structured type : "+typeClass.getName(),e);
		}
	}



	private boolean isParseTypeResource () {
		XMLStreamReader reader = builder.getReader();
		int count = reader.getAttributeCount();
		for (int i=0; i < count ; i++) {
			if ("parseType".equals(reader.getAttributeLocalName(i))) {
				if ("Resource".equals(reader.getAttributeValue(i))) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	public void parse(XMPMetadata metadata, QName altName,
			ComplexPropertyContainer container)
					throws XmpUnexpectedTypeException, XmpParsingException,
					XMLStreamException, XmpUnknownPropertyTypeException,
					XmpPropertyFormatException {
		builder.expectCurrentLocalName("li");
		XMLStreamReader reader = builder.getReader();
		// check if parseType is defined
		boolean skipDescription = isParseTypeResource();

		int elmtType = reader.nextTag();

		if (!skipDescription) {
			// rdf:Description 
			builder.expectCurrentLocalName("Description");
			elmtType = reader.nextTag();
		}
		AbstractStructuredType property = instanciateProperty(metadata);

		QName eltName;
		String structuredEndName = skipDescription?"li":"Description";

		while (!((elmtType == XMLStreamReader.END_ELEMENT) && reader.getName().getLocalPart().equals(structuredEndName))) {
			// read element name, then text content
			eltName = reader.getName();
			String eltContent = reader.getElementText();
			// check if property is expected
			String localPart = eltName.getLocalPart();
			if (propDesc.containsKey(localPart)) {
				PropertyDescription description = propDesc.get(localPart);

				AbstractField a = instanciateSimple(
						description.propertyType.propertyType(), 
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
		}
		if (!skipDescription) {
			// closing rdf:Description element
			reader.nextTag();
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
		TypeDescription description = TypeMapping.getTypeDescription(type);
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

		return TypeMapping.instanciateSimpleProperty(metadata, null, prefix, propertyName, value, type);
	}



	protected class PropertyDescription {

		//		private String fieldName;
		//
		private String propertyName;

		private PropertyType propertyType;

	}



}
