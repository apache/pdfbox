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
import java.util.Map.Entry;

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



	private Map<String,TypeDescription<AbstractSimpleProperty>> BASIC_TYPES;

	private Map<Class<? extends AbstractField>,TypeDescription<AbstractSimpleProperty>> BASIC_CLASSES;

	private Map<String,TypeDescription<AbstractSimpleProperty>> DERIVED_TYPES;

	private Map<Class<? extends AbstractField>,TypeDescription<AbstractSimpleProperty>> DERIVED_CLASSES;

	private Map<String, TypeDescription<AbstractStructuredType>> STRUCTURED_TYPES;

	private Map<Class<? extends AbstractField>,TypeDescription<AbstractStructuredType>> STRUCTURED_CLASSES;

	private Map<String,TypeDescription<AbstractStructuredType>> STRUCTURED_NAMESPACES;
	
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
		BASIC_TYPES = new HashMap<String,TypeDescription<AbstractSimpleProperty>>();
		BASIC_CLASSES = new HashMap<Class<? extends AbstractField>, TypeDescription<AbstractSimpleProperty>>();
		addToBasicMaps(new TypeDescription<AbstractSimpleProperty>("Text",BasicType.Text,TextType.class));
		addToBasicMaps(new TypeDescription<AbstractSimpleProperty>("Date",BasicType.Date,DateType.class));
		addToBasicMaps(new TypeDescription<AbstractSimpleProperty>("Boolean",BasicType.Boolean,BooleanType.class));
		addToBasicMaps(new TypeDescription<AbstractSimpleProperty>("Integer",BasicType.Integer,IntegerType.class));
		addToBasicMaps(new TypeDescription<AbstractSimpleProperty>("Real",BasicType.Real,RealType.class));

		// derived
		DERIVED_TYPES = new HashMap<String,TypeDescription<AbstractSimpleProperty>>();
		DERIVED_CLASSES = new HashMap<Class<? extends AbstractField>, TypeDescription<AbstractSimpleProperty>>();
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
		STRUCTURED_TYPES = new HashMap<String, TypeDescription<AbstractStructuredType>>();
		STRUCTURED_CLASSES = new HashMap<Class<? extends AbstractField>, TypeDescription<AbstractStructuredType>>();
		STRUCTURED_NAMESPACES = new HashMap<String, TypeDescription<AbstractStructuredType>>();
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
		addNameSpace("http://ns.adobe.com/xap/1.0/", XMPBasicSchema.class);
		addNameSpace(DublinCoreSchema.DCURI, DublinCoreSchema.class);
		addNameSpace("http://www.aiim.org/pdfa/ns/extension/", PDFAExtensionSchema.class);
		addNameSpace("http://ns.adobe.com/xap/1.0/mm/", XMPMediaManagementSchema.class);
		addNameSpace("http://ns.adobe.com/pdf/1.3/", AdobePDFSchema.class);
		addNameSpace("http://www.aiim.org/pdfa/ns/id/", PDFAIdentificationSchema.class);
		addNameSpace("http://ns.adobe.com/xap/1.0/rights/",     XMPRightsManagementSchema.class);
		addNameSpace(PhotoshopSchema.PHOTOSHOPURI,      PhotoshopSchema.class);
		addNameSpace(XMPBasicJobTicketSchema.JOB_TICKET_URI,XMPBasicJobTicketSchema.class);

	}

	private void addToBasicMaps (TypeDescription<AbstractSimpleProperty> td) {
		BASIC_TYPES.put(td.getType(),td);
		BASIC_CLASSES.put(td.getTypeClass(), td);
	}

	public void addToDerivedMaps (TypeDescription<AbstractSimpleProperty> td) {
		DERIVED_TYPES.put(td.getType(),td);
		DERIVED_CLASSES.put(td.getTypeClass(), td);
	}

	public void addToStructuredMaps (TypeDescription<AbstractStructuredType> td) {
		try {
			String ns = (String)td.getTypeClass().getField("ELEMENT_NS").get(null);
			Class<? extends AbstractStructuredType> clz = td.getTypeClass();
			if (clz!=null) {
				PropMapping pm = ReflectHelper.initializePropMapping(ns, clz);
				td.setProperties(pm);
			} else {
				PropMapping pm = initializePropMapping(ns, null);
				td.setProperties(pm);
			}
			addToStructuredMaps(td, ns);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Failed to init structured maps for "+td.getTypeClass(), e);
		} catch (SecurityException e) {
			throw new IllegalArgumentException("Failed to init structured maps for "+td.getTypeClass(), e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Failed to init structured maps for "+td.getTypeClass(), e);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException("Failed to init structured maps for "+td.getTypeClass(), e);
		}
	}

	public void addToStructuredMaps (TypeDescription<AbstractStructuredType> td, String ns) {
		STRUCTURED_TYPES.put(td.getType(),td);
		STRUCTURED_CLASSES.put(td.getTypeClass(), td);
		STRUCTURED_NAMESPACES.put(ns, td);
	}


	public String getType (Class<?> clz) {
		// search in basic
		TypeDescription<AbstractSimpleProperty> td = BASIC_CLASSES.get(clz);
		// search in derived
		if (td==null) {
			td = DERIVED_CLASSES.get(clz);
		}
		// return simple
		if (td!=null) {
			return td.getType();
		} else {
			// search in structured
			TypeDescription<AbstractStructuredType> td2 = STRUCTURED_CLASSES.get(clz);
			// return type if exists
			return (td2!=null)?td2.getType():null;
		}
	}

	/**
	 * Return the type description linked the specified paramater. If the type
	 * parameter is an array, the TypeDescription of the elements of the array
	 * will be returned
	 * 
	 * @param type
	 * @return
	 */
	public TypeDescription<?> getTypeDescription (String type) {
		if (BASIC_TYPES.containsKey(type)) {
			return BASIC_TYPES.get(type);
		} else if (DERIVED_TYPES.containsKey(type)) {
			return DERIVED_TYPES.get(type);
		} else if (STRUCTURED_TYPES.containsKey(type)) { 
			return STRUCTURED_TYPES.get(type);
		} else {
			int pos = type.indexOf(' ');
			if (pos>0) {
				return getTypeDescription(type.substring(pos+1));
			} else {
				// unknown type
				return null;
			}
		}
	}

	// TODO ces deux methodes doivent remplacer la précédente
	public TypeDescription<AbstractSimpleProperty> getSimpleDescription (String type) {
		if (BASIC_TYPES.containsKey(type)) {
			return BASIC_TYPES.get(type);
		} else if (DERIVED_TYPES.containsKey(type)) {
			return DERIVED_TYPES.get(type);
		} else {
			return null;
		}
	}

	public TypeDescription<AbstractStructuredType> getStructuredDescription (String type) {
		return STRUCTURED_TYPES.get(type);
	}

	
	public AbstractStructuredType instanciateStructuredType (TypeDescription<AbstractStructuredType> td) throws BadFieldValueException {
		try {
			Class<? extends AbstractStructuredType> propertyTypeClass = td.getTypeClass();
			Constructor<? extends AbstractStructuredType> construct = propertyTypeClass.getConstructor(new Class<?> [] {XMPMetadata.class});
			return construct.newInstance(metadata);
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
			simpleType = simpleType.substring(simpleType.indexOf(" ")+1);
			return instanciateSimpleProperty(nsuri, prefix, propertyName, value, simpleType);
		} else {
			return instanciateSimpleProperty(nsuri, prefix, propertyName, value, simpleType);
		}
	}

	public TypeDescription<AbstractStructuredType> getStructuredTypeName (String namespace) {
		return STRUCTURED_NAMESPACES.get(namespace);
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
		return STRUCTURED_NAMESPACES.containsKey(namespace);
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

	public boolean isSimpleType(String type) {
		return (BASIC_TYPES.containsKey(type) || DERIVED_TYPES.containsKey(type));
	}

	public boolean isStructuredType(String type) {
		return STRUCTURED_TYPES.containsKey(type);
	}

	private static PropMapping initializePropMapping(String ns,
			DefinedStructuredType dst) {
		PropMapping propMap = new PropMapping(ns);
		for (Entry<String, String> entry: dst.getDefinedProperties().entrySet()) {
			propMap.addNewProperty(entry.getKey(), entry.getValue());
		}
		return propMap;
	}

	private void addNameSpace(String ns, Class<? extends XMPSchema> classSchem) {
		schemaMap.put(ns, new XMPSchemaFactory(ns, classSchem,	ReflectHelper.initializePropMapping(ns, classSchem)));
		try {
			schemaUriToPrefered.put(ns, classSchem.getField("PREFERED_PREFIX").get(null).toString());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Failed to init '"+ns+"'", e);
		} catch (SecurityException e) {
			throw new IllegalArgumentException("Failed to init '"+ns+"'", e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Failed to init '"+ns+"'", e);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException("Failed to init '"+ns+"'", e);
		}
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
	public boolean isContainedNamespace(String namespace) {
		boolean found = schemaMap.containsKey(namespace);
		if (!found) {
			found = isStructuredTypeNamespace(namespace);
		}
		return found;
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
		XMPSchemaFactory factory =getSchemaFactory(namespace);
		if (factory!=null) {
			// found in schema
			return factory.getPropertyType(prop.getLocalPart());
		} else {
			TypeDescription<AbstractStructuredType> td = getStructuredTypeName(prop.getPrefix());
			return td==null?null:td.getType();
		}
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
