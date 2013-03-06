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

import org.apache.xmpbox.XMPMetadata;

@StructuredType(preferedPrefix = "pdfaSchema", namespace = "http://www.aiim.org/pdfa/ns/schema#")
public class PDFASchemaType extends AbstractStructuredType
{

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String SCHEMA = "schema";

    @PropertyType(type = Types.URI, card = Cardinality.Simple)
    public static final String NAMESPACE_URI = "namespaceURI";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String PREFIX = "prefix";

    @PropertyType(type = Types.PDFAProperty, card = Cardinality.Seq)
    public static final String PROPERTY = "property";

    @PropertyType(type = Types.PDFAType, card = Cardinality.Seq)
    public static final String VALUE_TYPE = "valueType";

    public PDFASchemaType(XMPMetadata metadata)
    {
        super(metadata);
    }

    public String getNamespaceURI()
    {
        URIType tt = (URIType) getProperty(NAMESPACE_URI);
        return tt == null ? null : tt.getStringValue();
    }

    public String getPrefixValue()
    {
        TextType tt = (TextType) getProperty(PREFIX);
        return tt == null ? null : tt.getStringValue();
    }

    public ArrayProperty getProperty()
    {
        return (ArrayProperty) getArrayProperty(PROPERTY);
    }

    public ArrayProperty getValueType()
    {
        return (ArrayProperty) getArrayProperty(VALUE_TYPE);
    }

}
