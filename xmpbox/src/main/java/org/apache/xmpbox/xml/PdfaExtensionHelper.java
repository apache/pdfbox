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
import java.util.function.Supplier;

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

    private static final String CLOSED_CHOICE = "closed Choice of ";
    private static final String CLOSED_CHOICE_U = "Closed Choice of ";

    private static final String OPEN_CHOICE = "open Choice of ";
    private static final String OPEN_CHOICE_U = "Open Choice of ";

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
        if (attr.getPrefix() == null)
        {
            // PDFBOX-6136: not relevant here
            return;
        }
        String prefix = attr.getLocalName();
        String namespace = attr.getValue();
        String cprefix = clz.getAnnotation(StructuredType.class).preferedPrefix();
        String cnamespace = clz.getAnnotation(StructuredType.class).namespace();
        // check extension
        if (cprefix.equals(prefix) && !cnamespace.equals(namespace))
        {
            throw new XmpParsingException(ErrorType.InvalidPdfaSchema,
                    "Invalid PDF/A namespace definition, "
                            + "prefix: " + prefix + ", namespace: " + namespace);
        } 
        if (cnamespace.equals(namespace) && !cprefix.equals(prefix))
        {
            throw new XmpParsingException(ErrorType.InvalidPdfaSchema,
                    "Invalid PDF/A namespace definition, "
                            + "prefix: " + prefix + ", namespace: " + namespace);
        }
    }

    public static void populateSchemaMapping(XMPMetadata meta, boolean strictParsing) throws XmpParsingException
    {
        List<XMPSchema> schems = meta.getAllSchemas();
        TypeMapping tm = meta.getTypeMapping();
        StructuredType stPdfaExt = PDFAExtensionSchema.class.getAnnotation(StructuredType.class);
        for (XMPSchema xmpSchema : schems)
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
                PDFAExtensionSchema pes = (PDFAExtensionSchema) xmpSchema;
                ArrayProperty sp = pes.getSchemasProperty();
                for (AbstractField af : sp.getAllProperties())
                {
                    if (af instanceof PDFASchemaType)
                    {
                        populatePDFASchemaType(meta, (PDFASchemaType) af, tm, strictParsing);
                    } // TODO unmanaged ?
                }
            }
        }
    }

    private static void populatePDFASchemaType(XMPMetadata meta, PDFASchemaType st, TypeMapping tm, boolean strictParsing)
            throws XmpParsingException
    {
        String namespaceUri = st.getNamespaceURI();
        // PDFBOX-5525
        requireNonNull(namespaceUri, () -> "Missing pdfaSchema:namespaceURI in type definition");
        namespaceUri = namespaceUri.trim();
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
                    populatePDFAType(meta, (PDFATypeType) af2, tm);
                }
            }
        }
        // populate properties
        if (properties == null && !strictParsing)
        {
            return;
        }
        requireNonNull(properties, () -> "Missing pdfaSchema:property in type definition");
        for (AbstractField af2 : properties.getAllProperties())
        {
            if (af2 instanceof PDFAPropertyType)
            {
                populatePDFAPropertyType((PDFAPropertyType) af2, tm, xsf);
            } // TODO unmanaged ?
        }
    }

    private static void populatePDFAPropertyType(PDFAPropertyType property, TypeMapping tm, XMPSchemaFactory xsf)
            throws XmpParsingException
    {
        String pname = property.getName();
        String ptype = property.getValueType();
        // check all mandatory fields are OK
        requireNonNull(pname, () -> String.format("Missing field '%s' in property definition", PDFAPropertyType.NAME));
        requireNonNull(ptype, () -> String.format("Missing field '%s' in property definition", PDFAPropertyType.VALUETYPE));
        requireNonNull(property.getDescription(), () -> String.format("Missing field '%s' in property definition", PDFAPropertyType.DESCRIPTION));
        requireNonNull(property.getCategory(), () -> String.format("Missing field '%s' in property definition", PDFAPropertyType.CATEGORY));

        // check ptype existence
        PropertyType pt = transformValueType(tm, ptype);
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

    private static void populatePDFAType(XMPMetadata meta, PDFATypeType type, TypeMapping tm)
            throws XmpParsingException
    {
        String ttype = type.getType();
        String tns = type.getNamespaceURI();
        String tprefix = type.getPrefixValue();
        // all fields are mandatory
        requireNonNull(ttype, () -> String.format("Missing field '%s' in type definition", PDFATypeType.TYPE));
        requireNonNull(tns, () -> String.format("Missing field '%s' in type definition", PDFATypeType.NS_URI));
        requireNonNull(tprefix, () -> String.format("Missing field '%s' in type definition", PDFATypeType.PREFIX));
        requireNonNull(type.getDescription(), () -> String.format("Missing field '%s' in type definition", PDFATypeType.DESCRIPTION));

        // create the structured type
        DefinedStructuredType structuredType = new DefinedStructuredType(meta, tns, tprefix, null); // TODO
        // maybe a name exists
        ArrayProperty fields = type.getFields();
        if (fields != null)
        {
            List<AbstractField> definedFields = fields.getAllProperties();
            for (AbstractField af3 : definedFields)
            {
                if (af3 instanceof PDFAFieldType)
                {
                    populatePDFAFieldType((PDFAFieldType) af3, structuredType);
                }
                // else TODO
            }
        }
        // add the structured type to list
        PropertiesDescription pm = new PropertiesDescription();
        structuredType.getDefinedProperties().forEach(pm::addNewProperty);
        tm.addToDefinedStructuredTypes(ttype, tns, pm);
    }

    private static void populatePDFAFieldType(PDFAFieldType field, DefinedStructuredType structuredType)
            throws XmpParsingException
    {
        String fName = field.getName();
        String fValueType = field.getValueType();
        requireNonNull(fName, () -> String.format("Missing field '%s' in field definition", PDFAFieldType.NAME));
        requireNonNull(field.getDescription(), () -> String.format("Missing field '%s' in field definition", PDFAFieldType.DESCRIPTION));
        requireNonNull(fValueType, () -> String.format("Missing field '%s' in field definition", PDFAFieldType.VALUETYPE));

        try
        {
            Types fValue = Types.valueOf(fValueType);
            structuredType.addProperty(fName, TypeMapping.createPropertyType(fValue, Cardinality.Simple));
        }
        catch (IllegalArgumentException e)
        {
            throw new XmpParsingException(ErrorType.NoValueType, "Type not defined : " + fValueType, e);
            // TODO could fValueType be a structured type ?
        }
    }

    private static PropertyType transformValueType(TypeMapping tm, String valueType)
    {
        if ("Lang Alt".equals(valueType))
        {
            return TypeMapping.createPropertyType(Types.LangAlt, Cardinality.Simple);
        }
        // else all other cases
        if (valueType.startsWith(CLOSED_CHOICE) || valueType.startsWith(CLOSED_CHOICE_U))
        {
            valueType = valueType.substring(CLOSED_CHOICE.length());
        }
        else if (valueType.startsWith(OPEN_CHOICE) || valueType.startsWith(OPEN_CHOICE_U))
        {
            valueType = valueType.substring(OPEN_CHOICE.length());
        }
        int pos = valueType.indexOf(' ');
        Cardinality card = Cardinality.Simple;
        if (pos > 0)
        {
            String scard = valueType.substring(0, pos).toLowerCase();
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

    private static void requireNonNull(Object value, Supplier<String> message) throws XmpParsingException
    {
        if (value == null)
        {
            throw new XmpParsingException(ErrorType.RequiredProperty, message.get());
        }
    }
}
