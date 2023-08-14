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
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PDFAIdentificationTest
{
    private XMPMetadata metadata;
    private XMPSchema schema;
    private Class<?> schemaClass;

    @BeforeEach
    void initMetadata()
    {
        metadata = XMPMetadata.createXMPMetadata();
        schema = metadata.createAndAddPDFAIdentificationSchema();
        schemaClass = PDFAIdentificationSchema.class;
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
            Arguments.of("part", XMPSchemaTester.createPropertyType(Types.Integer), 1),
            Arguments.of("amd", XMPSchemaTester.createPropertyType(Types.Text), "2005"),
            Arguments.of("conformance", XMPSchemaTester.createPropertyType(Types.Text), "B")
        );
    }
}
