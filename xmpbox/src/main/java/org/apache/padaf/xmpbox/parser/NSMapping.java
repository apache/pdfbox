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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.AdobePDFSchema;
import org.apache.padaf.xmpbox.schema.DublinCoreSchema;
import org.apache.padaf.xmpbox.schema.PDFAExtensionSchema;
import org.apache.padaf.xmpbox.schema.PDFAFieldDescription;
import org.apache.padaf.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.padaf.xmpbox.schema.PDFAPropertyDescription;
import org.apache.padaf.xmpbox.schema.PDFAValueTypeDescription;
import org.apache.padaf.xmpbox.schema.PropertyAttributesAnnotation;
import org.apache.padaf.xmpbox.schema.PropertyType;
import org.apache.padaf.xmpbox.schema.SchemaDescription;
import org.apache.padaf.xmpbox.schema.XMPBasicSchema;
import org.apache.padaf.xmpbox.schema.XMPMediaManagementSchema;
import org.apache.padaf.xmpbox.schema.XMPRightsManagementSchema;
import org.apache.padaf.xmpbox.schema.XMPSchema;


/**
 * Retrieve information about schemas
 * 
 * @author a183132
 * 
 */
public class NSMapping {

	public static final List<String> BASIC_TYPES;
	public static final HashMap<String, String> COMPLEX_BASIC_TYPES;

	static {
		BASIC_TYPES = new ArrayList<String>();
		BASIC_TYPES.add("Text");
		BASIC_TYPES.add("Integer");
		BASIC_TYPES.add("Boolean");
		BASIC_TYPES.add("Date");
		BASIC_TYPES.add("URI");
		BASIC_TYPES.add("URL");
		BASIC_TYPES.add("bag Text");
		BASIC_TYPES.add("bag ProperName");
		BASIC_TYPES.add("bag Job");
		BASIC_TYPES.add("bag Xpath");
		BASIC_TYPES.add("seq Text");
		BASIC_TYPES.add("seq Field");
		BASIC_TYPES.add("seq Date");
		BASIC_TYPES.add("Lang Alt");

		COMPLEX_BASIC_TYPES = new HashMap<String, String>();
		COMPLEX_BASIC_TYPES.put("http://ns.adobe.com/xap/1.0/g/img/",
				"Thumbnail");
	}

	protected Map<String, XMPSchemaFactory> nsMaps;
	protected Map<String, String> complexBasicTypesDeclarationEntireXMPLevel;
	protected Map<String, String> complexBasicTypesDeclarationSchemaLevel;
	protected Map<String, String> complexBasicTypesDeclarationPropertyLevel;

	/**
	 * Constructor of the NameSpace mapping
	 * 
	 * @throws XmpSchemaException
	 *             When could not read a property data in a Schema Class given
	 */
	public NSMapping() throws XmpSchemaException {
		nsMaps = new HashMap<String, XMPSchemaFactory>();
		complexBasicTypesDeclarationEntireXMPLevel = new HashMap<String, String>();
		complexBasicTypesDeclarationSchemaLevel = new HashMap<String, String>();
		complexBasicTypesDeclarationPropertyLevel = new HashMap<String, String>();
		init();

	}


	/**
	 * Import an NSMapping content.
	 * @param imp
	 */
	public void importNSMapping(NSMapping imp) throws XmpSchemaException {
		mergeNSMap(imp.nsMaps);
		mergeComplexBasicTypesDeclarationEntireXMPLevel(imp.complexBasicTypesDeclarationEntireXMPLevel);
		mergeComplexBasicTypesDeclarationSchemaLevel(imp.complexBasicTypesDeclarationSchemaLevel);
		mergeComplexBasicTypesDeclarationPropertyLevel(imp.complexBasicTypesDeclarationPropertyLevel);
	}
	
	protected void mergeNSMap(Map<String, XMPSchemaFactory> map) throws XmpSchemaException {
		for (Entry<String, XMPSchemaFactory> entry : map.entrySet() ) {
			if (this.nsMaps.containsKey(entry.getKey())) {
				this.nsMaps.get(entry.getKey()).importXMPSchemaFactory(entry.getValue());
			} else {
				this.nsMaps.put(entry.getKey(), entry.getValue());
			}
		}
	}
	
	private void mergeComplexBasicTypesDeclarationEntireXMPLevel(Map<String, String> external) {
		for (Entry<String, String> entry : external.entrySet()) {
			if(!complexBasicTypesDeclarationEntireXMPLevel.containsKey(entry.getKey())) {
				complexBasicTypesDeclarationEntireXMPLevel.put(entry.getKey(), entry.getValue());
			}
		}
	}
	
	private void mergeComplexBasicTypesDeclarationSchemaLevel(Map<String, String> external) {
		for (Entry<String, String> entry : external.entrySet()) {
			if(!complexBasicTypesDeclarationSchemaLevel.containsKey(entry.getKey())) {
				complexBasicTypesDeclarationSchemaLevel.put(entry.getKey(), entry.getValue());
			}
		}
	}

	private void mergeComplexBasicTypesDeclarationPropertyLevel(Map<String, String> external) {
		for (Entry<String, String> entry : external.entrySet()) {
			if(!complexBasicTypesDeclarationPropertyLevel.containsKey(entry.getKey())) {
				complexBasicTypesDeclarationPropertyLevel.put(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * Add mapping of common schemas
	 * 
	 * @throws XmpSchemaException
	 *             When could not read a property data in a Schema Class given
	 */
	private void init() throws XmpSchemaException {
		addNameSpace("http://ns.adobe.com/xap/1.0/", XMPBasicSchema.class);
		addNameSpace("http://purl.org/dc/elements/1.1/", DublinCoreSchema.class);
		addNameSpace("http://www.aiim.org/pdfa/ns/extension/", PDFAExtensionSchema.class);
		addNameSpace("http://ns.adobe.com/xap/1.0/mm/",	XMPMediaManagementSchema.class);
		addNameSpace("http://ns.adobe.com/pdf/1.3/", AdobePDFSchema.class);
		addNameSpace("http://www.aiim.org/pdfa/ns/id/",	PDFAIdentificationSchema.class);
		addNameSpace("http://ns.adobe.com/xap/1.0/rights/",	XMPRightsManagementSchema.class);
	}

	/**
	 * Add a namespace declaration and Schema factory associated
	 * 
	 * @param ns
	 *            the Namespace URI
	 * @param classSchem
	 *            The class representation of the schema linked to the namespace
	 * @throws XmpSchemaException
	 *             When could not read property name in Schema Class given
	 */
	protected void addNameSpace(String ns, Class<? extends XMPSchema> classSchem)
			throws XmpSchemaException {
		nsMaps.put(ns, new XMPSchemaFactory(ns, classSchem,	initializePropMapping(ns, classSchem)));
	}

	/**
	 * Initialize the Property Mapping for a given schema
	 * 
	 * @param ns
	 *            Namespace URI
	 * @param classSchem
	 *            The class representation of the schema linked to the namespace
	 * @return Construct expected properties types representation
	 * @throws XmpSchemaException
	 *             When could not read property name in field with properties
	 *             annotations
	 */
	private PropMapping initializePropMapping(String ns,
			Class<? extends XMPSchema> classSchem) throws XmpSchemaException {
		PropertyType propType;
		PropertyAttributesAnnotation propAtt;
		Field[] fields;
		PropMapping propMap = new PropMapping(ns);
		fields = classSchem.getFields();
		String propName = null;
		for (Field field : fields) {
			if (field.isAnnotationPresent(PropertyType.class)) {
				try {
					propName = (String) field.get(propName);
				} catch (Exception e) {
					throw new XmpSchemaException(
							"couldn't read one type declaration, please check accessibility and declaration of fields annoted in "
									+ classSchem.getName(), e.getCause());
				}
				// System.out.println("nameField:"+propName);
				propType = field.getAnnotation(PropertyType.class);
				// System.out.println("Type '"+propInfo.propertyType()+"' defined for "+propName);
				if (!field
						.isAnnotationPresent(PropertyAttributesAnnotation.class)) {
					propMap.addNewProperty(propName, propType.propertyType(),
							null);
				} else {
					// TODO Case where a special annotation is used to specify
					// attributes
					// NOT IMPLEMENTED YET, JUST TO GIVE A CLUE TO MAKE THIS
					propAtt = field
							.getAnnotation(PropertyAttributesAnnotation.class);
					List<String> attributes = new ArrayList<String>();
					for (String att : propAtt.expectedAttributes()) {
						attributes.add(att);
					}
					propMap.addNewProperty(propName, propType.propertyType(),
							attributes);
				}
			}
		}
		return propMap;
	}

	/**
	 * see if a specific type is known as a basic XMP type
	 * 
	 * @param type
	 *            Type to check
	 * @return True if type is a simple basic type
	 */
	private boolean isBasicType(String type) {
		return BASIC_TYPES.contains(type);

	}

	/**
	 * Say if a specific namespace is known
	 * 
	 * @param namespace
	 *            The namespace URI checked
	 * @return True if namespace URI is known
	 */
	public boolean isContainedNamespace(String namespace) {
		return nsMaps.containsKey(namespace);
	}

	/**
	 * Give type of specified property in specified schema (given by its
	 * namespaceURI)
	 * 
	 * @param namespace
	 *            The namespaceURI to explore
	 * @param prop
	 *            the property Qualified Name
	 * @return Property type declared for namespace specified, null if unknown
	 */
	public String getSpecifiedPropertyType(String namespace, QName prop) {
		if (nsMaps.containsKey(namespace)) {
			return nsMaps.get(namespace).getPropertyType(prop.getLocalPart());
		}
		// check if its a complexbasicValueType and if it's has been declared
		return getComplexBasicValueTypeEffectiveType(prop.getPrefix());

	}

	/**
	 * Check if a non basic value type used is describes in the schema which
	 * inlude a property with a such type
	 * 
	 * @param desc
	 *            The schema description associated to the schema which declare
	 *            a property with specific value type
	 * @param definedValueType
	 *            The value type name to find in value types descriptions
	 * @return The description of this specific value type
	 * @throws XmpUnknownValueTypeException
	 *             If no declaration found
	 */
	private PDFAValueTypeDescription findValueTypeDescription(
			SchemaDescription desc, String definedValueType)
			throws XmpUnknownValueTypeException {
		List<PDFAValueTypeDescription> values = desc.getValueTypes();
		for (PDFAValueTypeDescription val : values) {
			if (definedValueType.equals(val.getTypeNameValue())) {
				return val;
			}
		}
		throw new XmpUnknownValueTypeException("ValueType '" + definedValueType
				+ "' is unknown. no declaration found in this schema");
	}

	/**
	 * Check if valueType used for a specified property description is known (in
	 * case where it's a normal value type or if a value type which has been
	 * defined in PDF/A Extension schema)
	 * 
	 * @param desc
	 *            The schema description associated to the schema which declare
	 *            a property with specific value type
	 * @param definedValueType
	 *            The value type name to find in value types descriptions
	 * @return value type equivalence (value type which can be treat (orginal
	 *         basic value type or specific value type decomposed to find basic
	 *         types)
	 * @throws XmpUnknownValueTypeException
	 *             When Value Type is unknown
	 * 
	 */
	private String getValueTypeEquivalence(SchemaDescription desc,
			String definedValueType) throws XmpUnknownValueTypeException {
		if (isBasicType(definedValueType)) {
			return definedValueType;
		}
		PDFAValueTypeDescription val = findValueTypeDescription(desc,
				definedValueType);
		if (val.getFields().isEmpty()) {
			// if fields value are note defined we suppose the property is a
			// Text type
			return "Text";
		}
		return "Field";
	}

	/**
	 * . For a specific valuetype declared in this schema. This method decompose
	 * it if field are present. and add types expected
	 * 
	 * @param desc
	 *            The schema description associated to the schema which declare
	 *            a property with specific value type
	 * @param valueType
	 *            valueType to analyze
	 * @param prop
	 *            Expected properties types representation
	 * @throws XmpUnknownValueTypeException
	 *             When a Value Type associated is unknown
	 */
	private void declareAssociatedFieldType(SchemaDescription desc,
			String valueType, PropMapping prop)
			throws XmpUnknownValueTypeException {

		PDFAValueTypeDescription val = findValueTypeDescription(desc, valueType);
		for (PDFAFieldDescription field : val.getFields()) {
			// TODO case where a field call another nspace property ???
			String fieldType = getValueTypeEquivalence(desc, field
					.getValueTypeValue());
			if (fieldType.equals("Field")) {
				throw new XmpUnknownValueTypeException(
						"ValueType Field reference a valuetype unknown");
			}
			prop.addNewProperty(field.getNameValue(), fieldType, null);

		}
	}

	/**
	 * Add a new namespace Mapping for specific schema declared in PDF/A
	 * Extension schema
	 * 
	 * @param desc
	 *            The schemaDescription associated to the schema
	 * @throws XmpUnknownValueTypeException
	 *             When a Value Type associated is unknown
	 */
	public void setNamespaceDefinition(SchemaDescription desc)
			throws XmpUnknownValueTypeException {
		PropMapping propMap = new PropMapping(desc.getNameSpaceURI());
		List<PDFAPropertyDescription> props = desc.getProperties();
		for (int i = 0; i < props.size(); i++) {
			String type = getValueTypeEquivalence(desc, props.get(i).getValueTypeValue());
			propMap.addNewProperty(props.get(i).getNameValue(), type, null);
			if (type.equals("Field")) {
				declareAssociatedFieldType(desc, props.get(i).getValueTypeValue(), propMap);
			}
		}
		String nsName = desc.getPrefix();
		String ns = desc.getNameSpaceURI();
		nsMaps.put(ns, new XMPSchemaFactory(nsName, ns, XMPSchema.class, propMap));
	}

	/**
	 * Return the specialized schema class representation if it's known (create
	 * and add it to metadata). In other cases, return null
	 * 
	 * @param metadata
	 *            Metadata to link the new schema
	 * @param namespace
	 *            The namespace URI
	 * @return Schema representation
	 * @throws XmpSchemaException
	 *             When Instancing specified Object Schema failed
	 */
	public XMPSchema getAssociatedSchemaObject(XMPMetadata metadata, String namespace, String prefix) throws XmpSchemaException {
		if (!nsMaps.containsKey(namespace)) {
			return null;
		}
		XMPSchemaFactory factory = nsMaps.get(namespace);
		return factory.createXMPSchema(metadata, prefix);
	}

	/**
	 * Check if a namespace used reference a complex basic types (like
	 * Thumbnails)
	 * 
	 * @param namespace
	 *            The namespace URI to check
	 * @return True if namespace URI is a reference for a complex basic type
	 */
	public boolean isComplexBasicTypes(String namespace) {
		return COMPLEX_BASIC_TYPES.containsKey(namespace);
	}

	/**
	 * Check if a namespace declaration for a complex basic type has been found
	 * and if its valid for the entire XMP stream
	 * 
	 * @param namespace
	 *            the namespace URI
	 * @param prefix
	 *            the prefix associated to this namespace
	 */
	public void setComplexBasicTypesDeclarationForLevelXMP(String namespace,
			String prefix) {
		if (isComplexBasicTypes(namespace)) {
			complexBasicTypesDeclarationEntireXMPLevel.put(prefix, namespace);
		}
	}

	/**
	 * Check if a namespace declaration for a complex basic type has been found
	 * and if its valid for the current schema description (at level of
	 * rdf:Description)
	 * 
	 * @param namespace
	 *            the namespace URI
	 * @param prefix
	 *            the prefix associated to this namespace
	 */
	public void setComplexBasicTypesDeclarationForLevelSchema(String namespace,
			String prefix) {
		if (isComplexBasicTypes(namespace)) {
			complexBasicTypesDeclarationSchemaLevel.put(prefix, namespace);
		}

	}

	/**
	 * Check if a namespace declaration for a complex basic type has been found
	 * and if its valid for the current property description
	 * 
	 * @param namespace
	 *            the namespace URI
	 * @param prefix
	 *            the prefix associated to this namespace
	 */
	public void setComplexBasicTypesDeclarationForLevelProperty(
			String namespace, String prefix) {
		if (isComplexBasicTypes(namespace)) {
			complexBasicTypesDeclarationPropertyLevel.put(prefix, namespace);
		}
	}

	/**
	 * Check for all XMP level if a complexBasicValueType prefix has been
	 * declared
	 * 
	 * @param prefix
	 *            The prefix which may design the namespace URI of the complex
	 *            basic type
	 * @return The type if it is known, else null.
	 */
	public String getComplexBasicValueTypeEffectiveType(String prefix) {
		if (complexBasicTypesDeclarationPropertyLevel.containsKey(prefix)) {
			return COMPLEX_BASIC_TYPES
					.get(complexBasicTypesDeclarationPropertyLevel.get(prefix));
		}
		if (complexBasicTypesDeclarationSchemaLevel.containsKey(prefix)) {
			return COMPLEX_BASIC_TYPES
					.get(complexBasicTypesDeclarationSchemaLevel.get(prefix));
		}
		if (complexBasicTypesDeclarationEntireXMPLevel.containsKey(prefix)) {
			return COMPLEX_BASIC_TYPES
					.get(complexBasicTypesDeclarationEntireXMPLevel.get(prefix));
		}
		return null;
	}

	/**
	 * Reset complex Basic types declaration for property level
	 */
	public void resetComplexBasicTypesDeclarationInPropertyLevel() {
		complexBasicTypesDeclarationPropertyLevel.clear();
	}

	/**
	 * Reset complex Basic types declaration for schema level
	 */
	public void resetComplexBasicTypesDeclarationInSchemaLevel() {
		complexBasicTypesDeclarationSchemaLevel.clear();
	}

	/**
	 * Reset complex Basic types declaration for Entire XMP level
	 */
	public void resetComplexBasicTypesDeclarationInEntireXMPLevel() {
		complexBasicTypesDeclarationEntireXMPLevel.clear();
	}

}
