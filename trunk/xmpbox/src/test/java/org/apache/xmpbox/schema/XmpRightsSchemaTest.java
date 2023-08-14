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
import java.util.stream.Stream;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class XmpRightsSchemaTest
{
    private XMPMetadata metadata;
    private XMPSchema schema;
    private Class<?> schemaClass;

    @BeforeEach
    void initMetadata()
    {
        metadata = XMPMetadata.createXMPMetadata();
        schema = metadata.createAndAddXMPRightsManagementSchema();
        schemaClass = XMPRightsManagementSchema.class;
    }
    
    @ParameterizedTest
    @MethodSource("initializeParameters")
    void testElementValue(String property, PropertyType type, Object value) throws Exception
    {
        XMPSchemaTester xmpSchemaTester = new XMPSchemaTester(metadata, schema, schemaClass, property, type, value);
        xmpSchemaTester.testGetSetValue();
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void testElementProperty(String property, PropertyType type, Object value) throws Exception
    {
        XMPSchemaTester xmpSchemaTester = new XMPSchemaTester(metadata, schema, schemaClass, property, type, value);
        xmpSchemaTester.testGetSetProperty();
    }

    static Stream<Arguments> initializeParameters() throws Exception
    {
        Map<String, String> desc = new HashMap<>(2);
        desc.put("fr", "Termes d'utilisation");
        desc.put("en", "Usage Terms");

        return Stream.of(
            Arguments.of("Certificate", XMPSchemaTester.createPropertyType(Types.URL), "http://une.url.vers.un.certificat/moncert.cer"),
            Arguments.of("Marked", XMPSchemaTester.createPropertyType(Types.Boolean), true),
            Arguments.of("Owner", XMPSchemaTester.createPropertyType(Types.ProperName, Cardinality.Bag), new String[] { "OwnerName" }),
            Arguments.of("UsageTerms", XMPSchemaTester.createPropertyType(Types.LangAlt), desc),
            Arguments.of("WebStatement", XMPSchemaTester.createPropertyType(Types.URL), "http://une.url.vers.une.page.fr/")
        );
    }
}
