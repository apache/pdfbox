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

import java.util.HashMap;
import java.util.Map;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.Types;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class XmpRightsSchemaTest
{
    @ParameterizedTest
    @MethodSource("initializeParameters")
    public void testElementValue(XMPSchemaTester xmpSchemaTester) throws Exception
    {
        xmpSchemaTester.testGetSetValue();
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    public void testElementProperty(XMPSchemaTester xmpSchemaTester) throws Exception
    {
        xmpSchemaTester.testGetSetProperty();
    }

    static XMPSchemaTester[] initializeParameters() throws Exception
    {
        XMPMetadata metadata = XMPMetadata.createXMPMetadata();
        XMPSchema schema = metadata.createAndAddXMPRightsManagementSchema();
        Class<?> schemaClass = XMPRightsManagementSchema.class;

        Map<String, String> desc = new HashMap<>(2);
        desc.put("fr", "Termes d'utilisation");
        desc.put("en", "Usage Terms");

        return new XMPSchemaTester[] {
            new XMPSchemaTester(metadata, schema, schemaClass, "Certificate", XMPSchemaTester.createPropertyType(Types.URL), "http://une.url.vers.un.certificat/moncert.cer"),
            new XMPSchemaTester(metadata, schema, schemaClass, "Marked", XMPSchemaTester.createPropertyType(Types.Boolean), true),
            new XMPSchemaTester(metadata, schema, schemaClass, "Owner", XMPSchemaTester.createPropertyType(Types.ProperName, Cardinality.Bag), new String[] { "OwnerName" }),
            new XMPSchemaTester(metadata, schema, schemaClass, "UsageTerms", XMPSchemaTester.createPropertyType(Types.LangAlt), desc),
            new XMPSchemaTester(metadata, schema, schemaClass, "WebStatement", XMPSchemaTester.createPropertyType(Types.URL), "http://une.url.vers.une.page.fr/"),
        };
    }
}
