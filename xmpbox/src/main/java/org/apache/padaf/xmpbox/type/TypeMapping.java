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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.parser.XmpParsingException;
import org.apache.padaf.xmpbox.type.TypeDescription.BasicType;

public final class TypeMapping {



	private Map<String,TypeDescription> BASIC_TYPES;

	private Map<Class<? extends AbstractField>,TypeDescription> BASIC_CLASSES;

	private Map<String,TypeDescription> DERIVED_TYPES;

	private Map<Class<? extends AbstractField>,TypeDescription> DERIVED_CLASSES;

	private Map<String, TypeDescription> STRUCTURED_TYPES;

	private Map<Class<? extends AbstractField>,TypeDescription> STRUCTURED_CLASSES;

	private Map<String,TypeDescription> STRUCTURED_NAMESPACES;

	public TypeMapping() {
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
		BASIC_TYPES = new HashMap<String,TypeDescription>();
		BASIC_CLASSES = new HashMap<Class<? extends AbstractField>, TypeDescription>();
		addToBasicMaps(new TypeDescription("Text",BasicType.Text,TextType.class));
		addToBasicMaps(new TypeDescription("Date",BasicType.Date,DateType.class));
		addToBasicMaps(new TypeDescription("Boolean",BasicType.Boolean,BooleanType.class));
		addToBasicMaps(new TypeDescription("Integer",BasicType.Integer,IntegerType.class));
		addToBasicMaps(new TypeDescription("Real",BasicType.Real,RealType.class));

		// derived
		DERIVED_TYPES = new HashMap<String,TypeDescription>();
		DERIVED_CLASSES = new HashMap<Class<? extends AbstractField>, TypeDescription>();
		addToDerivedMaps(new TypeDescription("AgentName",BasicType.Text,AgentNameType.class));
		addToDerivedMaps(new TypeDescription("Choice",BasicType.Text,ChoiceType.class));
		addToDerivedMaps(new TypeDescription("GUID",BasicType.Text,GUIDType.class));
		addToDerivedMaps(new TypeDescription("Lang Alt",BasicType.Text,TextType.class));
		addToDerivedMaps(new TypeDescription("Locale",BasicType.Text,LocaleType.class));
		addToDerivedMaps(new TypeDescription("MIMEType",BasicType.Text,MIMEType.class));
		addToDerivedMaps(new TypeDescription("ProperName",BasicType.Text,ProperNameType.class));
		addToDerivedMaps(new TypeDescription("RenditionClass",BasicType.Text,RenditionClassType.class));
		addToDerivedMaps(new TypeDescription("URL",BasicType.Text,URLType.class));
		addToDerivedMaps(new TypeDescription("URI",BasicType.Text,URIType.class));
		addToDerivedMaps(new TypeDescription("XPath",BasicType.Text,XPathType.class));
		addToDerivedMaps(new TypeDescription("Part",BasicType.Text,PartType.class));

		// structured types
		STRUCTURED_TYPES = new HashMap<String, TypeDescription>();
		STRUCTURED_CLASSES = new HashMap<Class<? extends AbstractField>, TypeDescription>();
		STRUCTURED_NAMESPACES = new HashMap<String, TypeDescription>();
		addToStructuredMaps(new TypeDescription("Thumbnail",null,ThumbnailType.class));
		addToStructuredMaps(new TypeDescription("Layer",null,LayerType.class));
		addToStructuredMaps(new TypeDescription("ResourceEvent",null,ResourceEventType.class));
		addToStructuredMaps(new TypeDescription("Job",null,JobType.class));
		addToStructuredMaps(new TypeDescription("ResourceRef",null,ResourceRefType.class));
		addToStructuredMaps(new TypeDescription("Version",null,VersionType.class));
		// PDF/A structured types
		addToStructuredMaps(new TypeDescription("PDFAField",null,PDFAFieldType.class));
		addToStructuredMaps(new TypeDescription("PDFAProperty",null,PDFAPropertyType.class));
		addToStructuredMaps(new TypeDescription("PDFAType",null,PDFATypeType.class));
		addToStructuredMaps(new TypeDescription("PDFASchema",null,PDFASchemaType.class));
	}

	private void addToBasicMaps (TypeDescription td) {
		BASIC_TYPES.put(td.getType(),td);
		BASIC_CLASSES.put(td.getTypeClass(), td);
	}

	public void addToDerivedMaps (TypeDescription td) {
		DERIVED_TYPES.put(td.getType(),td);
		DERIVED_CLASSES.put(td.getTypeClass(), td);
	}

	public void addToStructuredMaps (TypeDescription td) {
		try {
			String ns = (String)td.getTypeClass().getField("ELEMENT_NS").get(null);
			Class<? extends AbstractStructuredType> clz = (Class<? extends AbstractStructuredType>)td.getTypeClass();
			if (clz!=null) {
				PropMapping pm = ReflectHelper.initializePropMapping(ns, clz);
				td.setProperties(pm);
			} else {
				PropMapping pm = initializePropMapping(ns, td.getDefinedStructure());
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

	public void addToStructuredMaps (TypeDescription td, String ns) {
		STRUCTURED_TYPES.put(td.getType(),td);
		STRUCTURED_CLASSES.put(td.getTypeClass(), td);
		STRUCTURED_NAMESPACES.put(ns, td);
	}


	public String getType (Class<?> clz) {
		// search in basic
		TypeDescription td = BASIC_CLASSES.get(clz);
		// search in derived
		if (td==null) {
			td = DERIVED_CLASSES.get(clz);
		}
		// search in structured
		if (td==null) {
			td = STRUCTURED_CLASSES.get(clz);
		}
		// return type if exists
		return (td!=null)?td.getType():null;
	}

	/**
	 * Return the type description linked the specified paramater. If the type
	 * parameter is an array, the TypeDescription of the elements of the array
	 * will be returned
	 * 
	 * @param type
	 * @return
	 */
	public TypeDescription getTypeDescription (String type) {
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

	public AbstractStructuredType instanciateStructuredType (XMPMetadata metadata, Class<? extends AbstractStructuredType> propertyTypeClass) throws XmpParsingException {
		try {
			Constructor<? extends AbstractStructuredType> construct = propertyTypeClass.getConstructor(new Class<?> [] {XMPMetadata.class});
			return construct.newInstance(metadata);
		} catch (InvocationTargetException e) {
			throw new XmpParsingException("Failed to instanciate structured type : "+propertyTypeClass,e);
		} catch (IllegalArgumentException e) {
			throw new XmpParsingException("Failed to instanciate structured type : "+propertyTypeClass,e);
		} catch (InstantiationException e) {
			throw new XmpParsingException("Failed to instanciate structured type : "+propertyTypeClass,e);
		} catch (IllegalAccessException e) {
			throw new XmpParsingException("Failed to instanciate structured type : "+propertyTypeClass,e);
		} catch (SecurityException e) {
			throw new XmpParsingException("Failed to instanciate structured type : "+propertyTypeClass,e);
		} catch (NoSuchMethodException e) {
			throw new XmpParsingException("Failed to instanciate structured type : "+propertyTypeClass,e);
		} 
	}

	public AbstractSimpleProperty instanciateSimpleProperty (XMPMetadata xmp,String nsuri, String prefix, String name, Object value, String type) {
		// constructor parameters
		Object [] params = new Object [] {
				xmp,	
				nsuri,
				prefix,
				name,
				value
		};
		// type 
		try {
			TypeDescription description = getTypeDescription(type);
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

	public  AbstractSimpleProperty instanciateSimpleField (Class<?> clz, XMPMetadata xmp, String nsuri, String prefix,String propertyName, Object value) {
		PropMapping pm = ReflectHelper.initializePropMapping(null, clz);
		String simpleType = pm.getPropertyType(propertyName);
		return instanciateSimpleProperty(xmp, nsuri, prefix, propertyName, value, simpleType);
	}

	public TypeDescription getStructuredTypeName (String namespace) {
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

}
