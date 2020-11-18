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

import java.util.stream.Stream;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class XMPMediaManagementTest
{
    private XMPMetadata metadata;
    private XMPSchema schema;
    private Class<?> schemaClass;

    @BeforeEach
    void initMetadata()
    {
        metadata = XMPMetadata.createXMPMetadata();
        schema = metadata.createAndAddXMPMediaManagementSchema();
        schemaClass = XMPMediaManagementSchema.class;
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
        return Stream.of(
            Arguments.of("DocumentID", XMPSchemaTester.createPropertyType(Types.URI), "uuid:FB031973-5E75-11B2-8F06-E7F5C101C07A"),
            Arguments.of("Manager", XMPSchemaTester.createPropertyType(Types.AgentName), "Raoul"),
            Arguments.of("ManageTo", XMPSchemaTester.createPropertyType(Types.URI), "uuid:36"),
            Arguments.of("ManageUI", XMPSchemaTester.createPropertyType(Types.URI), "uuid:3635"),
            Arguments.of("InstanceID", XMPSchemaTester.createPropertyType(Types.URI), "uuid:42"),
            Arguments.of("OriginalDocumentID", XMPSchemaTester.createPropertyType(Types.Text), "uuid:142"),
            Arguments.of("RenditionParams", XMPSchemaTester.createPropertyType(Types.Text), "my params"),
            Arguments.of("VersionID", XMPSchemaTester.createPropertyType(Types.Text), "14"),
            Arguments.of("Versions", XMPSchemaTester.createPropertyType(Types.Version, Cardinality.Seq), new String[] { "1", "2", "3" }),
            Arguments.of("History", XMPSchemaTester.createPropertyType(Types.Text, Cardinality.Seq),new String[] { "action 1", "action 2", "action 3" }),
            Arguments.of("Ingredients", XMPSchemaTester.createPropertyType(Types.Text, Cardinality.Bag), new String[] { "resource1", "resource2" })
        );
    }
}
