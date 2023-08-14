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

import java.util.Calendar;
import java.util.stream.Stream;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class XMPBasicTest
{
    private XMPMetadata metadata;
    private XMPSchema schema;
    private Class<?> schemaClass;

    @BeforeEach
    void initMetadata()
    {
        metadata = XMPMetadata.createXMPMetadata();
        schema = metadata.createAndAddXMPBasicSchema();
        schemaClass = XMPBasicSchema.class;
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
            Arguments.of("Advisory", XMPSchemaTester.createPropertyType(Types.XPath, Cardinality.Bag), new String[] { "xpath1", "xpath2" }),
            Arguments.of("BaseURL", XMPSchemaTester.createPropertyType(Types.URL), "URL"),
            Arguments.of("CreateDate", XMPSchemaTester.createPropertyType(Types.Date), Calendar.getInstance()),
            Arguments.of("CreatorTool", XMPSchemaTester.createPropertyType(Types.AgentName), "CreatorTool"),
            Arguments.of("Identifier", XMPSchemaTester.createPropertyType(Types.Text, Cardinality.Bag), new String[] { "id1", "id2" }),
            Arguments.of("Label", XMPSchemaTester.createPropertyType(Types.Text), "label"),
            Arguments.of("MetadataDate", XMPSchemaTester.createPropertyType(Types.Date), Calendar.getInstance()),
            Arguments.of("ModifyDate", XMPSchemaTester.createPropertyType(Types.Date), Calendar.getInstance()),
            Arguments.of("Nickname", XMPSchemaTester.createPropertyType(Types.Text), "nick name"),
            Arguments.of("Rating", XMPSchemaTester.createPropertyType(Types.Integer), 7),
            Arguments.of("Thumbnails", XMPSchemaTester.createPropertyType(Types.Thumbnail, Cardinality.Alt), null)
        );
    }
}
