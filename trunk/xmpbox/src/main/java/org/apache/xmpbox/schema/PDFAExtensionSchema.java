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

package org.apache.xmpbox.schema;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.ArrayProperty;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.StructuredType;
import org.apache.xmpbox.type.Types;

/**
 * Representation of a PDF/A Extension schema description schema
 * 
 * @author a183132
 * 
 */
@StructuredType(preferedPrefix = "pdfaExtension", namespace = "http://www.aiim.org/pdfa/ns/extension/")
public class PDFAExtensionSchema extends XMPSchema
{

    @PropertyType(type = Types.PDFASchema, card = Cardinality.Bag)
    public static final String SCHEMAS = "schemas";

    /**
     * Build a new PDFExtension schema
     * 
     * @param metadata
     *            The metadata to attach this schema XMPMetadata
     */
    public PDFAExtensionSchema(XMPMetadata metadata)
    {
        super(metadata);
    }

    public PDFAExtensionSchema(XMPMetadata metadata, String prefix)
    {
        super(metadata, prefix);
    }

    /**
     * 
     * @return the list of subject values
     */
    public ArrayProperty getSchemasProperty()
    {
        return (ArrayProperty) getProperty(SCHEMAS);
    }

}
