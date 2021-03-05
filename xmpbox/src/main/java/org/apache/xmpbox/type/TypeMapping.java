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

package org.apache.xmpbox.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.ExifSchema;
import org.apache.xmpbox.schema.PDFAExtensionSchema;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.schema.PhotoshopSchema;
import org.apache.xmpbox.schema.TiffSchema;
import org.apache.xmpbox.schema.XMPBasicJobTicketSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.schema.XMPMediaManagementSchema;
import org.apache.xmpbox.schema.XMPRightsManagementSchema;
import org.apache.xmpbox.schema.XMPSchema;
import org.apache.xmpbox.schema.XMPSchemaFactory;
import org.apache.xmpbox.schema.XMPageTextSchema;
import org.apache.xmpbox.schema.XmpSchemaException;

public final class TypeMapping
{

    private Map<Types, PropertiesDescription> structuredMappings;

    // ns -> type
    private Map<String, Types> structuredNamespaces;

    // ns -> type
    private Map<String, String> definedStructuredNamespaces;

    private Map<String, PropertiesDescription> definedStructuredMappings;

    private final XMPMetadata metadata;

    private Map<String, XMPSchemaFactory> schemaMap;

    public TypeMapping(XMPMetadata metadata)
    {
        this.metadata = metadata;
        initialize();
    }

    private static final Class<?>[] SIMPLEPROPERTYCONSTPARAMS = new Class<?>[] { XMPMetadata.class, String.class,
            String.class, String.class, Object.class };

    private void initialize()
    {
        // structured types
        structuredMappings = new EnumMap<>(Types.class);
        structuredNamespaces = new HashMap<>();
        for (Types type : Types.values())
        {
            if (type.isStructured())
            {
                Class<? extends AbstractStructuredType> clz = type.getImplementingClass().asSubclass(
                        AbstractStructuredType.class);
                StructuredType st = clz.getAnnotation(StructuredType.class);
                String ns = st.namespace();
                PropertiesDescription pm = initializePropMapping(clz);
                structuredNamespaces.put(ns, type);
                structuredMappings.put(type, pm);
            }
        }

        // define structured types
        definedStructuredNamespaces = new HashMap<>();
        definedStructuredMappings = new HashMap<>();

        // schema
        schemaMap = new HashMap<>();
        addNameSpace(XMPBasicSchema.class);
        addNameSpace(DublinCoreSchema.class);
        addNameSpace(PDFAExtensionSchema.class);
        addNameSpace(XMPMediaManagementSchema.class);
        addNameSpace(AdobePDFSchema.class);
        addNameSpace(PDFAIdentificationSchema.class);
        addNameSpace(XMPRightsManagementSchema.class);
        addNameSpace(PhotoshopSchema.class);
        addNameSpace(XMPBasicJobTicketSchema.class);
        addNameSpace(ExifSchema.class);
        addNameSpace(TiffSchema.class);
        addNameSpace(XMPageTextSchema.class);
    }

    public void addToDefinedStructuredTypes(String typeName, String ns, PropertiesDescription pm)
    {
        definedStructuredNamespaces.put(ns, typeName);
        definedStructuredMappings.put(typeName, pm);
    }

    public PropertiesDescription getDefinedDescriptionByNamespace(String namespace)
    {
        String dt = definedStructuredNamespaces.get(namespace);
        return this.definedStructuredMappings.get(dt);
    }

    public AbstractStructuredType instanciateStructuredType(Types type, String propertyName)
            throws BadFieldValueException
    {
        try
        {
            Class<? extends AbstractStructuredType> propertyTypeClass = type.getImplementingClass().asSubclass(
                    AbstractStructuredType.class);
            Constructor<? extends AbstractStructuredType> construct = propertyTypeClass
                    .getDeclaredConstructor(XMPMetadata.class);
            AbstractStructuredType tmp = construct.newInstance(metadata);
            tmp.setPropertyName(propertyName);
            return tmp;
        }
        catch (InvocationTargetException | IllegalArgumentException | InstantiationException |
               IllegalAccessException | SecurityException | NoSuchMethodException e)
        {
            throw new BadFieldValueException("Failed to instantiate structured type : " + type, e);
        }
    }

    public AbstractStructuredType instanciateDefinedType(String propertyName, String namespace)
    {
        return new DefinedStructuredType(metadata, namespace, null, propertyName);
    }

    public AbstractSimpleProperty instanciateSimpleProperty(String nsuri, String prefix, String name, Object value,
            Types type)
    {
        // constructor parameters
        Object[] params = new Object[] { metadata, nsuri, prefix, name, value };
        // type
        Class<? extends AbstractSimpleProperty> clz =
                type.getImplementingClass().asSubclass(AbstractSimpleProperty.class);
        try
        {
            Constructor<? extends AbstractSimpleProperty> cons = clz.getDeclaredConstructor(SIMPLEPROPERTYCONSTPARAMS);
            return cons.newInstance(params);
        }
        catch (NoSuchMethodError | IllegalArgumentException | InstantiationException |
               IllegalAccessException | InvocationTargetException | SecurityException |
               NoSuchMethodException e)
        {
            throw new IllegalArgumentException("Failed to instantiate " + clz.getSimpleName() + " property with value " + value, e);
        }
    }

    public AbstractSimpleProperty instanciateSimpleField(Class<?> clz, String nsuri, String prefix,
            String propertyName, Object value)
    {
        PropertiesDescription pm = initializePropMapping(clz);
        PropertyType simpleType = pm.getPropertyType(propertyName);
        Types type = simpleType.type();
        return instanciateSimpleProperty(nsuri, prefix, propertyName, value, type);
    }

    /**
     * Check if a namespace used reference a complex basic types (like Thumbnails)
     * 
     * @param namespace
     *            The namespace URI to check
     * @return True if namespace URI is a reference for a complex basic type
     */
    public boolean isStructuredTypeNamespace(String namespace)
    {
        return structuredNamespaces.containsKey(namespace);
    }

    public boolean isDefinedTypeNamespace(String namespace)
    {
        return definedStructuredNamespaces.containsKey(namespace);
    }

    public boolean isDefinedType(String name)
    {
        return this.definedStructuredMappings.containsKey(name);
    }

    private void addNameSpace(Class<? extends XMPSchema> classSchem)
    {
        StructuredType st = classSchem.getAnnotation(StructuredType.class);
        String ns = st.namespace();
        schemaMap.put(ns, new XMPSchemaFactory(ns, classSchem, initializePropMapping(classSchem)));
    }

    public void addNewNameSpace(String ns, String preferred)
    {
        PropertiesDescription mapping = new PropertiesDescription();
        schemaMap.put(ns, new XMPSchemaFactory(ns, XMPSchema.class, mapping));
    }

    public PropertiesDescription getStructuredPropMapping(Types type)
    {
        return structuredMappings.get(type);
    }

    /**
     * Return the specialized schema class representation if it's known (create and add it to metadata). In other cases,
     * return null
     * 
     * @param metadata
     *            Metadata to link the new schema
     * @param namespace
     *            The namespace URI
     * @param prefix The namespace prefix
     * @return Schema representation
     * @throws XmpSchemaException
     *             When Instancing specified Object Schema failed
     */
    public XMPSchema getAssociatedSchemaObject(XMPMetadata metadata, String namespace, String prefix)
            throws XmpSchemaException
    {
        if (schemaMap.containsKey(namespace))
        {
            XMPSchemaFactory factory = schemaMap.get(namespace);
            return factory.createXMPSchema(metadata, prefix);
        }
        else
        {
            XMPSchemaFactory factory = getSchemaFactory(namespace);
            return factory != null ? factory.createXMPSchema(metadata, prefix) : null;
        }
    }

    public XMPSchemaFactory getSchemaFactory(String namespace)
    {
        return schemaMap.get(namespace);
    }

    /**
     * Say if a specific namespace is known
     * 
     * @param namespace
     *            The namespace URI checked
     * @return True if namespace URI is known
     */
    public boolean isDefinedSchema(String namespace)
    {
        return schemaMap.containsKey(namespace);
    }

    public boolean isDefinedNamespace(String namespace)
    {
        return isDefinedSchema(namespace) || isStructuredTypeNamespace(namespace) || isDefinedTypeNamespace(namespace);
    }

    /**
     * Give type of specified property in specified schema (given by its namespaceURI)
     * 
     * @param name
     *            the property Qualified Name
     * @return Property type declared for namespace specified, null if unknown
     * @throws org.apache.xmpbox.type.BadFieldValueException if the name was not found.
     */
    public PropertyType getSpecifiedPropertyType(QName name) throws BadFieldValueException
    {
        XMPSchemaFactory factory = getSchemaFactory(name.getNamespaceURI());
        if (factory != null)
        {
            // found in schema
            return factory.getPropertyType(name.getLocalPart());
        }
        else
        {
            // try in structured
            Types st = structuredNamespaces.get(name.getNamespaceURI());
            if (st != null)
            {
                return createPropertyType(st, Cardinality.Simple);
            }
            else
            {
                // try in defined
                String dt = definedStructuredNamespaces.get(name.getNamespaceURI());
                if (dt == null)
                {
                    // not found
                    throw new BadFieldValueException("No descriptor found for " + name);
                }
                else
                {
                    return createPropertyType(Types.DefinedType, Cardinality.Simple);
                }
            }
        }
    }

    public PropertiesDescription initializePropMapping(Class<?> classSchem)
    {
        PropertiesDescription propMap = new PropertiesDescription();
        Field[] fields = classSchem.getFields();
        String propName = null;
        for (Field field : fields)
        {
            if (field.isAnnotationPresent(PropertyType.class))
            {
                try
                {
                    propName = (String) field.get(propName);
                }
                catch (Exception e)
                {
                    throw new IllegalArgumentException(
                            "couldn't read one type declaration, please check accessibility and declaration of fields annotated in "
                                    + classSchem.getName(), e);
                }
                PropertyType propType = field.getAnnotation(PropertyType.class);
                propMap.addNewProperty(propName, propType);
            }
        }
        return propMap;
    }

    public BooleanType createBoolean(String namespaceURI, String prefix, String propertyName, boolean value)
    {
        return new BooleanType(metadata, namespaceURI, prefix, propertyName, value);
    }

    public DateType createDate(String namespaceURI, String prefix, String propertyName, Calendar value)
    {
        return new DateType(metadata, namespaceURI, prefix, propertyName, value);
    }

    public IntegerType createInteger(String namespaceURI, String prefix, String propertyName, int value)
    {
        return new IntegerType(metadata, namespaceURI, prefix, propertyName, value);
    }

    public RealType createReal(String namespaceURI, String prefix, String propertyName, float value)
    {
        return new RealType(metadata, namespaceURI, prefix, propertyName, value);
    }

    public TextType createText(String namespaceURI, String prefix, String propertyName, String value)
    {
        return new TextType(metadata, namespaceURI, prefix, propertyName, value);
    }

    public ProperNameType createProperName(String namespaceURI, String prefix, String propertyName, String value)
    {
        return new ProperNameType(metadata, namespaceURI, prefix, propertyName, value);
    }

    public URIType createURI(String namespaceURI, String prefix, String propertyName, String value)
    {
        return new URIType(metadata, namespaceURI, prefix, propertyName, value);
    }

    public URLType createURL(String namespaceURI, String prefix, String propertyName, String value)
    {
        return new URLType(metadata, namespaceURI, prefix, propertyName, value);
    }

    public RenditionClassType createRenditionClass(String namespaceURI, String prefix, String propertyName, String value)
    {
        return new RenditionClassType(metadata, namespaceURI, prefix, propertyName, value);
    }

    public PartType createPart(String namespaceURI, String prefix, String propertyName, String value)
    {
        return new PartType(metadata, namespaceURI, prefix, propertyName, value);
    }

    public MIMEType createMIMEType(String namespaceURI, String prefix, String propertyName, String value)
    {
        return new MIMEType(metadata, namespaceURI, prefix, propertyName, value);
    }

    public LocaleType createLocale(String namespaceURI, String prefix, String propertyName, String value)
    {
        return new LocaleType(metadata, namespaceURI, prefix, propertyName, value);
    }

    public GUIDType createGUID(String namespaceURI, String prefix, String propertyName, String value)
    {
        return new GUIDType(metadata, namespaceURI, prefix, propertyName, value);
    }

    public ChoiceType createChoice(String namespaceURI, String prefix, String propertyName, String value)
    {
        return new ChoiceType(metadata, namespaceURI, prefix, propertyName, value);
    }

    public AgentNameType createAgentName(String namespaceURI, String prefix, String propertyName, String value)
    {
        return new AgentNameType(metadata, namespaceURI, prefix, propertyName, value);
    }

    public XPathType createXPath(String namespaceURI, String prefix, String propertyName, String value)
    {
        return new XPathType(metadata, namespaceURI, prefix, propertyName, value);
    }

    public ArrayProperty createArrayProperty(String namespace, String prefix, String propertyName, Cardinality type)
    {
        return new ArrayProperty(metadata, namespace, prefix, propertyName, type);
    }

    public static PropertyType createPropertyType(final Types type, final Cardinality card)
    {
        return new PropertyType()
        {

            @Override
            public Class<? extends Annotation> annotationType()
            {
                return null;
            }

            @Override
            public Types type()
            {
                return type;
            }

            @Override
            public Cardinality card()
            {
                return card;
            }
        };
    }
}
