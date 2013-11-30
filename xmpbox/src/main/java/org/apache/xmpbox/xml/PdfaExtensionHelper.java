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

package org.apache.xmpbox.xml;

import java.util.List;
import java.util.Map;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.PDFAExtensionSchema;
import org.apache.xmpbox.schema.XMPSchema;
import org.apache.xmpbox.schema.XMPSchemaFactory;
import org.apache.xmpbox.type.AbstractField;
import org.apache.xmpbox.type.AbstractStructuredType;
import org.apache.xmpbox.type.ArrayProperty;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.DefinedStructuredType;
import org.apache.xmpbox.type.PDFAFieldType;
import org.apache.xmpbox.type.PDFAPropertyType;
import org.apache.xmpbox.type.PDFASchemaType;
import org.apache.xmpbox.type.PDFATypeType;
import org.apache.xmpbox.type.PropertiesDescription;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.StructuredType;
import org.apache.xmpbox.type.TypeMapping;
import org.apache.xmpbox.type.Types;
import org.apache.xmpbox.xml.XmpParsingException.ErrorType;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public final class PdfaExtensionHelper
{

    public static final String CLOSED_CHOICE = "closed Choice of ";

    public static final String OPEN_CHOICE = "open Choice of ";

    private PdfaExtensionHelper()
    {
    }

    public static void validateNaming(XMPMetadata meta, Element description) throws XmpParsingException
    {
        NamedNodeMap nnm = description.getAttributes();
        for (int i = 0; i < nnm.getLength(); i++)
        {
            Attr attr = (Attr) nnm.item(i);
            checkNamespaceDeclaration(attr, PDFAExtensionSchema.class);
            checkNamespaceDeclaration(attr, PDFAFieldType.class);
            checkNamespaceDeclaration(attr, PDFAPropertyType.class);
            checkNamespaceDeclaration(attr, PDFASchemaType.class);
            checkNamespaceDeclaration(attr, PDFATypeType.class);
        }
    }

    private static void checkNamespaceDeclaration(Attr attr, Class<? extends AbstractStructuredType> clz)
            throws XmpParsingException
    {
        String prefix = attr.getLocalName();
        String namespace = attr.getValue();
        String cprefix = clz.getAnnotation(StructuredType.class).preferedPrefix();
        String cnamespace = clz.getAnnotation(StructuredType.class).namespace();
        // check extension
        if (cprefix.equals(prefix) && !cnamespace.equals(namespace))
        {
            throw new XmpParsingException(ErrorType.InvalidPdfaSchema, "Invalid PDF/A namespace definition");
        } // else good match
        if (cnamespace.equals(namespace) && !cprefix.equals(prefix))
        {
            throw new XmpParsingException(ErrorType.InvalidPdfaSchema, "Invalid PDF/A namespace definition");
        } // else good match

    }

    public static void populateSchemaMapping(XMPMetadata meta) throws XmpParsingException
    {
        List<XMPSchema> schems = meta.getAllSchemas();
        TypeMapping tm = meta.getTypeMapping();
        StructuredType stPdfaExt = PDFAExtensionSchema.class.getAnnotation(StructuredType.class);
        for (XMPSchema xmpSchema : schems)
        {
            if (xmpSchema.getNamespace().equals(stPdfaExt.namespace()))
            {
                // ensure the prefix is the preferred one (cannot use other
                // definition)
                if (!xmpSchema.getPrefix().equals(stPdfaExt.preferedPrefix()))
                {
                    throw new XmpParsingException(ErrorType.InvalidPrefix,
                            "Found invalid prefix for PDF/A extension, found '" + xmpSchema.getPrefix()
                                    + "', should be '" + stPdfaExt.preferedPrefix() + "'");
                }
                // create schema and types
                PDFAExtensionSchema pes = (PDFAExtensionSchema) xmpSchema;
                ArrayProperty sp = pes.getSchemasProperty();
                for (AbstractField af : sp.getAllProperties())
                {
                    if (af instanceof PDFASchemaType)
                    {
                        PDFASchemaType st = (PDFASchemaType) af;
                        String namespaceUri = st.getNamespaceURI().trim();
                        String prefix = st.getPrefixValue();
                        ArrayProperty properties = st.getProperty();
                        ArrayProperty valueTypes = st.getValueType();
                        XMPSchemaFactory xsf = tm.getSchemaFactory(namespaceUri);
                        // retrieve namespaces
                        if (xsf == null)
                        {
                            // create namespace with no field
                            tm.addNewNameSpace(namespaceUri, prefix);
                            xsf = tm.getSchemaFactory(namespaceUri);
                        }
                        // populate value type
                        if (valueTypes != null)
                        {
                            for (AbstractField af2 : valueTypes.getAllProperties())
                            {
                                if (af2 instanceof PDFATypeType)
                                {
                                    PDFATypeType type = (PDFATypeType) af2;
                                    String ttype = type.getType();
                                    String tns = type.getNamespaceURI();
                                    String tprefix = type.getPrefixValue();
                                    String tdescription = type.getDescription();
                                    ArrayProperty fields = type.getFields();
                                    if (ttype == null || tns == null || tprefix == null || tdescription == null)
                                    {
                                        // all fields are mandatory
                                        throw new XmpParsingException(ErrorType.RequiredProperty,
                                                "Missing field in type definition");
                                    }
                                    // create the structured type
                                    DefinedStructuredType structuredType = new DefinedStructuredType(meta, tns,
                                            tprefix, null); // TODO
                                                            // maybe
                                                            // a name
                                                            // exists
                                    if (fields != null)
                                    {
                                        List<AbstractField> definedFields = fields.getAllProperties();
                                        for (AbstractField af3 : definedFields)
                                        {
                                            if (af3 instanceof PDFAFieldType)
                                            {
                                                PDFAFieldType field = (PDFAFieldType) af3;
                                                String fName = field.getName();
                                                String fDescription = field.getDescription();
                                                String fValueType = field.getValueType();
                                                if (fName == null || fDescription == null || fValueType == null)
                                                {
                                                    throw new XmpParsingException(ErrorType.RequiredProperty,
                                                            "Missing field in field definition");
                                                }
                                                try
                                                {
                                                    Types fValue = Types.valueOf(fValueType);
                                                    structuredType.addProperty(fName,
                                                            TypeMapping.createPropertyType(fValue, Cardinality.Simple));
                                                }
                                                catch (IllegalArgumentException e)
                                                {
                                                    throw new XmpParsingException(ErrorType.NoValueType,
                                                            "Type not defined : " + fValueType, e);
                                                    // TODO could fValueType be
                                                    // a structured type ?
                                                }
                                            } // else TODO
                                        }
                                    }
                                    // add the structured type to list
                                    PropertiesDescription pm = new PropertiesDescription();
                                    for (Map.Entry<String, PropertyType> entry : structuredType.getDefinedProperties()
                                            .entrySet())
                                    {
                                        pm.addNewProperty(entry.getKey(), entry.getValue());
                                    }
                                    tm.addToDefinedStructuredTypes(ttype, tns, pm);
                                }
                            }
                        }
                        // populate properties
                        for (AbstractField af2 : properties.getAllProperties())
                        {
                            if (af2 instanceof PDFAPropertyType)
                            {
                                PDFAPropertyType property = (PDFAPropertyType) af2;
                                String pname = property.getName();
                                String ptype = property.getValueType();
                                String pdescription = property.getDescription();
                                String pCategory = property.getCategory();
                                // check all mandatory fields are OK
                                if (pname == null || ptype == null || pdescription == null || pCategory == null)
                                {
                                    // all fields are mandatory
                                    throw new XmpParsingException(ErrorType.RequiredProperty,
                                            "Missing field in property definition");
                                }
                                // check ptype existance
                                PropertyType pt = transformValueType(tm, ptype);
                                if (pt.type() == null)
                                {
                                    throw new XmpParsingException(ErrorType.NoValueType, "Type not defined : " + ptype);
                                }
                                else if (pt.type().isSimple() || pt.type().isStructured()
                                        || pt.type() == Types.DefinedType)
                                {
                                    xsf.getPropertyDefinition().addNewProperty(pname, pt);
                                }
                                else
                                {
                                    throw new XmpParsingException(ErrorType.NoValueType, "Type not defined : " + ptype);
                                }

                            } // TODO unmanaged ?
                        }
                    } // TODO unmanaged ?
                }
            }
        }
    }

    private static PropertyType transformValueType(TypeMapping tm, String valueType) throws XmpParsingException
    {
        if ("Lang Alt".equals(valueType))
        {
            return TypeMapping.createPropertyType(Types.LangAlt, Cardinality.Simple);
        }
        // else all other cases
        if (valueType.startsWith(CLOSED_CHOICE)) {
            valueType = valueType.substring(CLOSED_CHOICE.length());
        } else if (valueType.startsWith(OPEN_CHOICE)) {
            valueType = valueType.substring(OPEN_CHOICE.length());
        }
        int pos = valueType.indexOf(' ');
        Cardinality card = Cardinality.Simple;
        if (pos > 0)
        {
            String scard = valueType.substring(0, pos);
            if ("seq".equals(scard))
            {
                card = Cardinality.Seq;
            }
            else if ("bag".equals(scard))
            {
                card = Cardinality.Bag;
            }
            else if ("alt".equals(scard))
            {
                card = Cardinality.Alt;
            }
            else
            {
                return null;
            }
        }
        String vt = valueType.substring(pos + 1);
        Types type = null;
        try
        {
            type = pos < 0 ? Types.valueOf(valueType) : Types.valueOf(vt);
        }
        catch (IllegalArgumentException e)
        {
            if (tm.isDefinedType(vt))
            {
                type = Types.DefinedType;
            }
        }
        return TypeMapping.createPropertyType(type, card);
    }

}
