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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.AdobePDFSchema;
import org.apache.padaf.xmpbox.schema.DublinCoreSchema;
import org.apache.padaf.xmpbox.schema.PDFAExtensionSchema;
import org.apache.padaf.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.padaf.xmpbox.schema.PhotoshopSchema;
import org.apache.padaf.xmpbox.schema.XMPBasicJobTicketSchema;
import org.apache.padaf.xmpbox.schema.XMPBasicSchema;
import org.apache.padaf.xmpbox.schema.XMPMediaManagementSchema;
import org.apache.padaf.xmpbox.schema.XMPRightsManagementSchema;
import org.apache.padaf.xmpbox.schema.XMPSchema;
import org.apache.padaf.xmpbox.schema.XMPSchemaFactory;
import org.apache.padaf.xmpbox.schema.XmpSchemaException;
import org.apache.padaf.xmpbox.type.TypeDescription.BasicType;

public final class TypeMapping {



	private Map<String,TypeDescription<AbstractSimpleProperty>> basicTypes;

	private Map<Class<? extends AbstractField>,TypeDescription<AbstractSimpleProperty>> basicClasses;

	private Map<String,TypeDescription<AbstractSimpleProperty>> derivedTypes;

	private Map<Class<? extends AbstractField>,TypeDescription<AbstractSimpleProperty>> derivedClasses;

	private Map<String, TypeDescription<AbstractStructuredType>> structuredTypes;

	private Map<Class<? extends AbstractField>,TypeDescription<AbstractStructuredType>> structuredClasses;

	private Map<String,TypeDescription<AbstractStructuredType>> structuredNamespaces;

	private Map<String, String> schemaUriToPrefered;

	private XMPMetadata metadata;

	private Map<String, XMPSchemaFactory> schemaMap;


	public TypeMapping(XMPMetadata metadata) {
		this.metadata = metadata;
		initialize();
	}


	private static Class<?> [] simplePropertyConstParams = new Class<?> [] {
		XMPMetadata.class,
		String.class,
		String.class,
		String.class,
		Object.class
	};

	private void initialize () {
		// basic
		basicTypes = new HashMap<String,TypeDescription<AbstractSimpleProperty>>();
		basicClasses = new HashMap<Class<? extends AbstractField>, TypeDescription<AbstractSimpleProperty>>();
		addToBasicMaps(new TypeDescription<AbstractSimpleProperty>("Text",BasicType.Text,TextType.class));
		addToBasicMaps(new TypeDescription<AbstractSimpleProperty>("Date",BasicType.Date,DateType.class));
		addToBasicMaps(new TypeDescription<AbstractSimpleProperty>("Boolean",BasicType.Boolean,BooleanType.class));
		addToBasicMaps(new TypeDescription<AbstractSimpleProperty>("Integer",BasicType.Integer,IntegerType.class));
		addToBasicMaps(new TypeDescription<AbstractSimpleProperty>("Real",BasicType.Real,RealType.class));

		// derived
		derivedTypes = new HashMap<String,TypeDescription<AbstractSimpleProperty>>();
		derivedClasses = new HashMap<Class<? extends AbstractField>, TypeDescription<AbstractSimpleProperty>>();
		addToDerivedMaps(new TypeDescription<AbstractSimpleProperty>("AgentName",BasicType.Text,AgentNameType.class));
		addToDerivedMaps(new TypeDescription<AbstractSimpleProperty>("Choice",BasicType.Text,ChoiceType.class));
		addToDerivedMaps(new TypeDescription<AbstractSimpleProperty>("GUID",BasicType.Text,GUIDType.class));
		addToDerivedMaps(new TypeDescription<AbstractSimpleProperty>("Lang Alt",BasicType.Text,TextType.class));
		addToDerivedMaps(new TypeDescription<AbstractSimpleProperty>("Locale",BasicType.Text,LocaleType.class));
		addToDerivedMaps(new TypeDescription<AbstractSimpleProperty>("MIMEType",BasicType.Text,MIMEType.class));
		addToDerivedMaps(new TypeDescription<AbstractSimpleProperty>("ProperName",BasicType.Text,ProperNameType.class));
		addToDerivedMaps(new TypeDescription<AbstractSimpleProperty>("RenditionClass",BasicType.Text,RenditionClassType.class));
		addToDerivedMaps(new TypeDescription<AbstractSimpleProperty>("URL",BasicType.Text,URLType.class));
		addToDerivedMaps(new TypeDescription<AbstractSimpleProperty>("URI",BasicType.Text,URIType.class));
		addToDerivedMaps(new TypeDescription<AbstractSimpleProperty>("XPath",BasicType.Text,XPathType.class));
		addToDerivedMaps(new TypeDescription<AbstractSimpleProperty>("Part",BasicType.Text,PartType.class));

		// structured types
		structuredTypes = new HashMap<String, TypeDescription<AbstractStructuredType>>();
		structuredClasses = new HashMap<Class<? extends AbstractField>, TypeDescription<AbstractStructuredType>>();
		structuredNamespaces = new HashMap<String, TypeDescription<AbstractStructuredType>>();
		addToStructuredMaps(new TypeDescription<AbstractStructuredType>("Thumbnail",null,ThumbnailType.class));
		addToStructuredMaps(new TypeDescription<AbstractStructuredType>("Layer",null,LayerType.class));
		addToStructuredMaps(new TypeDescription<AbstractStructuredType>("ResourceEvent",null,ResourceEventType.class));
		addToStructuredMaps(new TypeDescription<AbstractStructuredType>("Job",null,JobType.class));
		addToStructuredMaps(new TypeDescription<AbstractStructuredType>("ResourceRef",null,ResourceRefType.class));
		addToStructuredMaps(new TypeDescription<AbstractStructuredType>("Version",null,VersionType.class));
		// PDF/A structured types
		addToStructuredMaps(new TypeDescription<AbstractStructuredType>("PDFAField",null,PDFAFieldType.class));
		addToStructuredMaps(new TypeDescription<AbstractStructuredType>("PDFAProperty",null,PDFAPropertyType.class));
		addToStructuredMaps(new TypeDescription<AbstractStructuredType>("PDFAType",null,PDFATypeType.class));
		addToStructuredMaps(new TypeDescription<AbstractStructuredType>("PDFASchema",null,PDFASchemaType.class));

		// schema
		schemaUriToPrefered = new HashMap<String, String>();
		schemaMap = new HashMap<String, XMPSchemaFactory>();
		addNameSpace(XMPBasicSchema.class);
		addNameSpace(DublinCoreSchema.class);
		addNameSpace(PDFAExtensionSchema.class);
		addNameSpace(XMPMediaManagementSchema.class);
		addNameSpace(AdobePDFSchema.class);
		addNameSpace(PDFAIdentificationSchema.class);
		addNameSpace(XMPRightsManagementSchema.class);
		addNameSpace(PhotoshopSchema.class);
		addNameSpace(XMPBasicJobTicketSchema.class);

	}

	private void addToBasicMaps (TypeDescription<AbstractSimpleProperty> td) {
		basicTypes.put(td.getType(),td);
		basicClasses.put(td.getTypeClass(), td);
	}

	public void addToDerivedMaps (TypeDescription<AbstractSimpleProperty> td) {
		derivedTypes.put(td.getType(),td);
		derivedClasses.put(td.getTypeClass(), td);
	}

	private void addToStructuredMaps (TypeDescription<AbstractStructuredType> td) {
		Class<? extends AbstractStructuredType> clz = td.getTypeClass();
		StructuredType st = clz.getAnnotation(StructuredType.class);
		String ns = st.namespace();
		PropMapping pm = ReflectHelper.initializePropMapping(ns, clz);
		td.setProperties(pm);
		addToStructuredMaps(td, ns);
	}

	public void addToStructuredMaps (TypeDescription<AbstractStructuredType> td, String ns) {
		structuredTypes.put(td.getType(),td);
		structuredClasses.put(td.getTypeClass(), td);
		structuredNamespaces.put(ns, td);
	}


	public String getType (Class<?> clz) {
		// search in basic
		TypeDescription<AbstractSimpleProperty> td = basicClasses.get(clz);
		// search in derived
		if (td==null) {
			td = derivedClasses.get(clz);
		}
		// return simple
		if (td!=null) {
			return td.getType();
		} else {
			// search in structured
			TypeDescription<AbstractStructuredType> td2 = structuredClasses.get(clz);
			// return type if exists
			return (td2!=null)?td2.getType():null;
		}
	}

	public TypeDescription<AbstractSimpleProperty> getSimpleDescription (String type) {
		if (basicTypes.containsKey(type)) {
			return basicTypes.get(type);
		} else if (derivedTypes.containsKey(type)) {
			return derivedTypes.get(type);
		} else {
			return null;
		}
	}

	public TypeDescription<AbstractStructuredType> getStructuredDescription (String type) {
		return structuredTypes.get(type);
	}


	public AbstractStructuredType instanciateStructuredType (TypeDescription<AbstractStructuredType> td, String propertyName) throws BadFieldValueException {
		try {
			Class<? extends AbstractStructuredType> propertyTypeClass = td.getTypeClass();
			if (propertyTypeClass.equals(DefinedStructuredType.class)) {
				PropMapping pm = td.getProperties();
				return new DefinedStructuredType(metadata, pm.getConcernedNamespace(), null, propertyName);
			} else {
				Constructor<? extends AbstractStructuredType> construct = propertyTypeClass.getConstructor(new Class<?> [] {
						XMPMetadata.class});
				AbstractStructuredType tmp = construct.newInstance(metadata);
				tmp.setPropertyName(propertyName);
				return tmp;
			}
		} catch (InvocationTargetException e) {
			throw new BadFieldValueException("Failed to instanciate structured type : "+td.getType(),e);
		} catch (IllegalArgumentException e) {
			throw new BadFieldValueException("Failed to instanciate structured type : "+td.getType(),e);
		} catch (InstantiationException e) {
			throw new BadFieldValueException("Failed to instanciate structured type : "+td.getType(),e);
		} catch (IllegalAccessException e) {
			throw new BadFieldValueException("Failed to instanciate structured type : "+td.getType(),e);
		} catch (SecurityException e) {
			throw new BadFieldValueException("Failed to instanciate structured type : "+td.getType(),e);
		} catch (NoSuchMethodException e) {
			throw new BadFieldValueException("Failed to instanciate structured type : "+td.getType(),e);
		} 
	}

	public AbstractSimpleProperty instanciateSimpleProperty (String nsuri, String prefix, String name, Object value, String type) {
		// constructor parameters
		Object [] params = new Object [] {
				metadata,	
				nsuri,
				prefix,
				name,
				value
		};
		// type 
		try {
			TypeDescription<AbstractSimpleProperty> description = getSimpleDescription(type);
			Class<? extends AbstractSimpleProperty> clz = (Class<? extends AbstractSimpleProperty>)description.getTypeClass();
			Constructor<? extends AbstractSimpleProperty> cons = clz.getConstructor(simplePropertyConstParams);
			return cons.newInstance(params);
		} catch (NoSuchMethodError e) {
			throw new IllegalArgumentException("Failed to instanciate property", e);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Failed to instanciate property", e);
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Failed to instanciate property", e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Failed to instanciate property", e);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("Failed to instanciate property", e);
		} catch (SecurityException e) {
			throw new IllegalArgumentException("Failed to instanciate property", e);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Failed to instanciate property", e);
		}
	}

	public  AbstractSimpleProperty instanciateSimpleField (Class<?> clz, String nsuri, String prefix,String propertyName, Object value) {
		PropMapping pm = ReflectHelper.initializePropMapping(null, clz);
		String simpleType = pm.getPropertyType(propertyName);
		if (isArrayOfSimpleType(simpleType)) {
			simpleType = simpleType.substring(simpleType.indexOf(' ')+1);
			return instanciateSimpleProperty(nsuri, prefix, propertyName, value, simpleType);
		} else {
			return instanciateSimpleProperty(nsuri, prefix, propertyName, value, simpleType);
		}
	}

	public TypeDescription<AbstractStructuredType> getStructuredTypeName (String namespace) {
		return structuredNamespaces.get(namespace);
	}

	/**
	 * Check if a namespace used reference a complex basic types (like
	 * Thumbnails)
	 * 
	 * @param namespace
	 *            The namespace URI to check
	 * @return True if namespace URI is a reference for a complex basic type
	 */
	public boolean isStructuredTypeNamespace(String namespace) {
		return structuredNamespaces.containsKey(namespace);
	}


	public boolean isArrayOfSimpleType (String type) {
		int pos = type.indexOf(' ');
		if (pos<0) {
			// not array
			return false;
		} else {
			String second = type.substring(pos+1);
			return isSimpleType(second);
		}
	}

	public boolean isArrayType (String type) {
		int pos = type.indexOf(' ');
		return pos>=0;
	}

	
	public String  getArrayType (String type) {
		int pos = type.indexOf(' ');
		if (pos<0) {
			// not array
			return null;
		} else {
			String first = type.substring(0,pos);
			if (first.equalsIgnoreCase(ArrayProperty.UNORDERED_ARRAY)) {
				return ArrayProperty.UNORDERED_ARRAY;
			} else if (first.equalsIgnoreCase(ArrayProperty.ORDERED_ARRAY)) {
				return ArrayProperty.ORDERED_ARRAY;
			} else if (first.equalsIgnoreCase(ArrayProperty.ALTERNATIVE_ARRAY)) {
				return ArrayProperty.ALTERNATIVE_ARRAY;
			} else {
				// else not an array
				return null;
			}
		}
	}

	public String getTypeInArray (String type) {
		int pos = type.indexOf(' ');
		if (pos<0) {
			// not array
			return null;
		} else {
			return type.substring(pos+1);
		}
	}
	
	public boolean isSimpleType(String type) {
		return (basicTypes.containsKey(type) || derivedTypes.containsKey(type));
	}

	public boolean isStructuredType(String type) {
		return structuredTypes.containsKey(type);
	}

//	private static PropMapping initializePropMapping(String ns,
//			DefinedStructuredType dst) {
//		PropMapping propMap = new PropMapping(ns);
//		for (Entry<String, String> entry: dst.getDefinedProperties().entrySet()) {
//			propMap.addNewProperty(entry.getKey(), entry.getValue());
//		}
//		return propMap;
//	}

	private void addNameSpace(Class<? extends XMPSchema> classSchem) {
		StructuredType st = classSchem.getAnnotation(StructuredType.class);
		String ns = st.namespace();
		String pp = st.preferedPrefix();
		schemaMap.put(ns, new XMPSchemaFactory(ns, classSchem,	ReflectHelper.initializePropMapping(ns, classSchem)));
		schemaUriToPrefered.put(ns, pp);
	}

	public void addNewNameSpace(String ns,String prefered) {
		PropMapping mapping = new PropMapping(ns);
		schemaMap.put(ns, new XMPSchemaFactory(ns, XMPSchema.class, mapping));
		schemaUriToPrefered.put(ns, prefered);
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
		if (schemaMap.containsKey(namespace)) {
			XMPSchemaFactory factory = schemaMap.get(namespace);
			return factory.createXMPSchema(metadata, prefix);
		} else {
			XMPSchemaFactory factory = getSchemaFactory(namespace);
			return factory!=null?factory.createXMPSchema(metadata, prefix):null;
		}
	}

	public XMPSchemaFactory getSchemaFactory(String namespace) {
		return schemaMap.get(namespace);
	}

	/**
	 * Say if a specific namespace is known
	 * 
	 * @param namespace
	 *            The namespace URI checked
	 * @return True if namespace URI is known
	 */
	public boolean isDefinedSchema(String namespace) {
		return schemaMap.containsKey(namespace);
	}

	/**
	 * Give type of specified property in specified schema (given by its
	 * namespaceURI)
	 * 
	 * @param prop
	 *            the property Qualified Name
	 * @return Property type declared for namespace specified, null if unknown
	 */
	public String getSpecifiedPropertyType (QName name) {
		XMPSchemaFactory factory =getSchemaFactory(name.getNamespaceURI());
		String result;
		if (factory!=null) {
			// found in schema
			result =  factory.getPropertyType(name.getLocalPart());
		} else {
			TypeDescription<AbstractStructuredType> td = getStructuredTypeName(name.getNamespaceURI());
			result =  td==null?null:td.getType();
		}
		return result;

	}
	
	public BooleanType createBoolean (String namespaceURI, String prefix,
			String propertyName, boolean value) {
		return new BooleanType(metadata, namespaceURI, prefix,propertyName, value);
	}

	public DateType createDate (String namespaceURI, String prefix,
			String propertyName, Calendar value) {
		return new DateType(metadata, namespaceURI, prefix,propertyName, value);
	}

	public IntegerType createInteger (String namespaceURI, String prefix,
			String propertyName, int value) {
		return new IntegerType(metadata, namespaceURI, prefix,propertyName, value);
	}

	public RealType createReal (String namespaceURI, String prefix,
			String propertyName, float value) {
		return new RealType(metadata, namespaceURI, prefix,propertyName, value);
	}

	public TextType createText (String namespaceURI, String prefix,
			String propertyName, String value) {
		return new TextType(metadata, namespaceURI, prefix,propertyName, value);
	}

	public ProperNameType createProperName (String namespaceURI, String prefix,
			String propertyName, String value) {
		return new ProperNameType(metadata, namespaceURI, prefix,propertyName, value);
	}

	public URIType createURI (String namespaceURI, String prefix,
			String propertyName, String value) {
		return new URIType(metadata, namespaceURI, prefix,propertyName, value);
	}

	public URLType createURL (String namespaceURI, String prefix,
			String propertyName, String value) {
		return new URLType(metadata, namespaceURI, prefix,propertyName, value);
	}

	public RenditionClassType createRenditionClass (String namespaceURI, String prefix,
			String propertyName, String value) {
		return new RenditionClassType(metadata, namespaceURI, prefix,propertyName, value);
	}

	public PartType createPart (String namespaceURI, String prefix,
			String propertyName, String value) {
		return new PartType(metadata, namespaceURI, prefix,propertyName, value);
	}

	public MIMEType createMIMEType (String namespaceURI, String prefix,
			String propertyName, String value) {
		return new MIMEType(metadata, namespaceURI, prefix,propertyName, value);
	}

	public LocaleType createLocale (String namespaceURI, String prefix,
			String propertyName, String value) {
		return new LocaleType(metadata, namespaceURI, prefix,propertyName, value);
	}

	public GUIDType createGUID (String namespaceURI, String prefix,
			String propertyName, String value) {
		return new GUIDType(metadata, namespaceURI, prefix,propertyName, value);
	}

	public ChoiceType createChoice (String namespaceURI, String prefix,
			String propertyName, String value) {
		return new ChoiceType(metadata, namespaceURI, prefix,propertyName, value);
	}

	public AgentNameType createAgentName (String namespaceURI, String prefix,
			String propertyName, String value) {
		return new AgentNameType(metadata, namespaceURI, prefix,propertyName, value);
	}

	public XPathType createXPath (String namespaceURI, String prefix,
			String propertyName, String value) {
		return new XPathType(metadata, namespaceURI, prefix,propertyName, value);
	}

	public ArrayProperty createArrayProperty (String namespace, String prefix, String propertyName, String type) {
		return new ArrayProperty(metadata, namespace, prefix, propertyName, type);
	}

		
}
