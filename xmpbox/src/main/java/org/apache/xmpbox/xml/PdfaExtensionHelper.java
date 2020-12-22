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

    public static void validateNaming(final XMPMetadata meta, final Element description) throws XmpParsingException
    {
        final NamedNodeMap nnm = description.getAttributes();
        for (int i = 0; i < nnm.getLength(); i++)
        {
            final Attr attr = (Attr) nnm.item(i);
            checkNamespaceDeclaration(attr, PDFAExtensionSchema.class);
            checkNamespaceDeclaration(attr, PDFAFieldType.class);
            checkNamespaceDeclaration(attr, PDFAPropertyType.class);
            checkNamespaceDeclaration(attr, PDFASchemaType.class);
            checkNamespaceDeclaration(attr, PDFATypeType.class);
        }
    }

    private static void checkNamespaceDeclaration(final Attr attr, final Class<? extends AbstractStructuredType> clz)
            throws XmpParsingException
    {
        final String prefix = attr.getLocalName();
        final String namespace = attr.getValue();
        final String cprefix = clz.getAnnotation(StructuredType.class).preferedPrefix();
        final String cnamespace = clz.getAnnotation(StructuredType.class).namespace();
        // check extension
        if (cprefix.equals(prefix) && !cnamespace.equals(namespace))
        {
            throw new XmpParsingException(ErrorType.InvalidPdfaSchema, "Invalid PDF/A namespace definition");
        } 
        if (cnamespace.equals(namespace) && !cprefix.equals(prefix))
        {
            throw new XmpParsingException(ErrorType.InvalidPdfaSchema, "Invalid PDF/A namespace definition");
        }
    }

    public static void populateSchemaMapping(final XMPMetadata meta) throws XmpParsingException
    {
        final List<XMPSchema> schems = meta.getAllSchemas();
        final TypeMapping tm = meta.getTypeMapping();
        final StructuredType stPdfaExt = PDFAExtensionSchema.class.getAnnotation(StructuredType.class);
        for (final XMPSchema xmpSchema : schems)
        {
            if (xmpSchema.getNamespace().equals(stPdfaExt.namespace()))
            {
                // ensure the prefix is the preferred one (cannot use other definition)
                if (!xmpSchema.getPrefix().equals(stPdfaExt.preferedPrefix()))
                {
                    throw new XmpParsingException(ErrorType.InvalidPrefix,
                            "Found invalid prefix for PDF/A extension, found '" + xmpSchema.getPrefix()
                                    + "', should be '" + stPdfaExt.preferedPrefix() + "'");
                }
                // create schema and types
                final PDFAExtensionSchema pes = (PDFAExtensionSchema) xmpSchema;
                final ArrayProperty sp = pes.getSchemasProperty();
                for (final AbstractField af : sp.getAllProperties())
                {
                    if (af instanceof PDFASchemaType)
                    {
                        populatePDFASchemaType(meta, (PDFASchemaType) af, tm);
                    } // TODO unmanaged ?
                }
            }
        }
    }

    private static void populatePDFASchemaType(final XMPMetadata meta, final PDFASchemaType st, final TypeMapping tm)
            throws XmpParsingException
    {
        final String namespaceUri = st.getNamespaceURI().trim();
        final String prefix = st.getPrefixValue();
        final ArrayProperty properties = st.getProperty();
        final ArrayProperty valueTypes = st.getValueType();
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
            for (final AbstractField af2 : valueTypes.getAllProperties())
            {
                if (af2 instanceof PDFATypeType)
                {
                    populatePDFAType(meta, (PDFATypeType) af2, tm);
                }
            }
        }
        // populate properties
        if (properties == null)
        {
            throw new XmpParsingException(ErrorType.RequiredProperty,
                    "Missing pdfaSchema:property in type definition");
        }
        for (final AbstractField af2 : properties.getAllProperties())
        {
            if (af2 instanceof PDFAPropertyType)
            {
                populatePDFAPropertyType((PDFAPropertyType) af2, tm, xsf);
            } // TODO unmanaged ?
        }
    }

    private static void populatePDFAPropertyType(final PDFAPropertyType property, final TypeMapping tm, final XMPSchemaFactory xsf)
            throws XmpParsingException
    {
        final String pname = property.getName();
        final String ptype = property.getValueType();
        final String pdescription = property.getDescription();
        final String pCategory = property.getCategory();
        // check all mandatory fields are OK
        if (pname == null || ptype == null || pdescription == null || pCategory == null)
        {
            // all fields are mandatory
            throw new XmpParsingException(ErrorType.RequiredProperty,
                    "Missing field in property definition");
        }
        // check ptype existence
        final PropertyType pt = transformValueType(tm, ptype);
        if (pt == null)
        {
            throw new XmpParsingException(ErrorType.NoValueType, "Unknown property value type : " + ptype);
        }
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
    }

    private static void populatePDFAType(final XMPMetadata meta, final PDFATypeType type, final TypeMapping tm)
            throws XmpParsingException
    {
        final String ttype = type.getType();
        final String tns = type.getNamespaceURI();
        final String tprefix = type.getPrefixValue();
        final String tdescription = type.getDescription();
        final ArrayProperty fields = type.getFields();
        if (ttype == null || tns == null || tprefix == null || tdescription == null)
        {
            // all fields are mandatory
            throw new XmpParsingException(ErrorType.RequiredProperty,
                    "Missing field in type definition");
        }
        // create the structured type
        final DefinedStructuredType structuredType = new DefinedStructuredType(meta, tns, tprefix, null); // TODO
        // maybe a name exists
        if (fields != null)
        {
            final List<AbstractField> definedFields = fields.getAllProperties();
            for (final AbstractField af3 : definedFields)
            {
                if (af3 instanceof PDFAFieldType)
                {
                    populatePDFAFieldType((PDFAFieldType) af3, structuredType);
                }
                // else TODO
            }
        }
        // add the structured type to list
        final PropertiesDescription pm = new PropertiesDescription();
        structuredType.getDefinedProperties().forEach(pm::addNewProperty);
        tm.addToDefinedStructuredTypes(ttype, tns, pm);
    }

    private static void populatePDFAFieldType(final PDFAFieldType field, final DefinedStructuredType structuredType)
            throws XmpParsingException
    {
        final String fName = field.getName();
        final String fDescription = field.getDescription();
        final String fValueType = field.getValueType();
        if (fName == null || fDescription == null || fValueType == null)
        {
            throw new XmpParsingException(ErrorType.RequiredProperty, "Missing field in field definition");
        }
        try
        {
            final Types fValue = Types.valueOf(fValueType);
            structuredType.addProperty(fName, TypeMapping.createPropertyType(fValue, Cardinality.Simple));
        }
        catch (final IllegalArgumentException e)
        {
            throw new XmpParsingException(ErrorType.NoValueType, "Type not defined : " + fValueType, e);
            // TODO could fValueType be a structured type ?
        }
    }

    private static PropertyType transformValueType(final TypeMapping tm, String valueType)
    {
        if ("Lang Alt".equals(valueType))
        {
            return TypeMapping.createPropertyType(Types.LangAlt, Cardinality.Simple);
        }
        // else all other cases
        if (valueType.startsWith(CLOSED_CHOICE))
        {
            valueType = valueType.substring(CLOSED_CHOICE.length());
        }
        else if (valueType.startsWith(OPEN_CHOICE))
        {
            valueType = valueType.substring(OPEN_CHOICE.length());
        }
        final int pos = valueType.indexOf(' ');
        Cardinality card = Cardinality.Simple;
        if (pos > 0)
        {
            final String scard = valueType.substring(0, pos);
            switch (scard)
            {
                case "seq":
                    card = Cardinality.Seq;
                    break;
                case "bag":
                    card = Cardinality.Bag;
                    break;
                case "alt":
                    card = Cardinality.Alt;
                    break;
                default:
                    return null;
            }
        }
        final String vt = valueType.substring(pos + 1);
        Types type = null;
        try
        {
            type = pos < 0 ? Types.valueOf(valueType) : Types.valueOf(vt);
        }
        catch (final IllegalArgumentException e)
        {
            if (tm.isDefinedType(vt))
            {
                type = Types.DefinedType;
            }
        }
        return TypeMapping.createPropertyType(type, card);
    }

}
