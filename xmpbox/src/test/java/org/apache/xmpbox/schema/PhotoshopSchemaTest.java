/*****************************************************************************
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
import org.apache.xmpbox.type.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PhotoshopSchemaTest
{    
    private XMPMetadata metadata;
    private Class<?> schemaClass;

    @BeforeEach
    void initMetadata()
    {
        metadata = XMPMetadata.createXMPMetadata();
        schemaClass = PhotoshopSchema.class;
    }
    
    @ParameterizedTest
    @MethodSource("initializeParameters")
    void testInitializedToNull(String fieldName, Types type, Cardinality card) throws Exception
    {
        SchemaTester schemaTester = new SchemaTester(metadata, schemaClass, fieldName, type, card);
        schemaTester.testInitializedToNull();
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void testSettingValue(String fieldName, Types type, Cardinality card) throws Exception
    {
        SchemaTester schemaTester = new SchemaTester(metadata, schemaClass, fieldName, type, card);
        schemaTester.testSettingValue();
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void testRandomSettingValue(String fieldName, Types type, Cardinality card) throws Exception
    {
        SchemaTester schemaTester = new SchemaTester(metadata, schemaClass, fieldName, type, card);
        schemaTester.testRandomSettingValue();
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void testSettingValueInArray(String fieldName, Types type, Cardinality card) throws Exception
    {
        SchemaTester schemaTester = new SchemaTester(metadata, schemaClass, fieldName, type, card);
        schemaTester.testSettingValueInArray();
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void testRandomSettingValueInArray(String fieldName, Types type, Cardinality card) throws Exception
    {
        SchemaTester schemaTester = new SchemaTester(metadata, schemaClass, fieldName, type, card);
        schemaTester.testRandomSettingValueInArray();
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void testPropertySetterSimple(String fieldName, Types type, Cardinality card) throws Exception
    {
        SchemaTester schemaTester = new SchemaTester(metadata, schemaClass, fieldName, type, card);
        schemaTester.testPropertySetterSimple();
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void testRandomPropertySetterSimple(String fieldName, Types type, Cardinality card) throws Exception
    {
        SchemaTester schemaTester = new SchemaTester(metadata, schemaClass, fieldName, type, card);
        schemaTester.testRandomPropertySetterSimple();
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void testPropertySetterInArray(String fieldName, Types type, Cardinality card) throws Exception
    {
        SchemaTester schemaTester = new SchemaTester(metadata, schemaClass, fieldName, type, card);
        schemaTester.testPropertySetterInArray();
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void testRandomPropertySetterInArray(String fieldName, Types type, Cardinality card) throws Exception
    {
        SchemaTester schemaTester = new SchemaTester(metadata, schemaClass, fieldName, type, card);
        schemaTester.testRandomPropertySetterInArray();
    }

    private static Stream<Arguments> initializeParameters()
    {        
        return Stream.of(
            Arguments.of("AncestorID", Types.URI, Cardinality.Simple),
            Arguments.of("AuthorsPosition", Types.Text, Cardinality.Simple),
            Arguments.of("CaptionWriter", Types.ProperName, Cardinality.Simple),
            Arguments.of("Category", Types.Text, Cardinality.Simple),
            Arguments.of("City", Types.Text, Cardinality.Simple),
            Arguments.of("ColorMode", Types.Integer, Cardinality.Simple),
            Arguments.of("Country", Types.Text, Cardinality.Simple),
            Arguments.of("Credit", Types.Text, Cardinality.Simple),
            Arguments.of("DateCreated", Types.Date, Cardinality.Simple),
            Arguments.of("Headline", Types.Text, Cardinality.Simple),
            Arguments.of("History", Types.Text, Cardinality.Simple),
            Arguments.of("ICCProfile", Types.Text, Cardinality.Simple),
            Arguments.of("Instructions", Types.Text, Cardinality.Simple),
            Arguments.of("Source", Types.Text, Cardinality.Simple),
            Arguments.of("State", Types.Text, Cardinality.Simple),
            Arguments.of("SupplementalCategories", Types.Text, Cardinality.Simple),
            Arguments.of("TransmissionReference", Types.Text, Cardinality.Simple),
            Arguments.of("Urgency", Types.Integer, Cardinality.Simple)
        );
    }
}
