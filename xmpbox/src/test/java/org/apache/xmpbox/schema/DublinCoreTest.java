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

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.Types;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;


public class DublinCoreTest
{
    @ParameterizedTest
    @MethodSource("initializeParameters")
    public void testInitializedToNull(SchemaTester schemaTester) throws Exception
    {
        schemaTester.testInitializedToNull();
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    public void testSettingValue(SchemaTester schemaTester) throws Exception
    {
        schemaTester.testSettingValue();
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    public void testRandomSettingValue(SchemaTester schemaTester) throws Exception
    {
        schemaTester.testRandomSettingValue();
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    public void testSettingValueInArray(SchemaTester schemaTester) throws Exception
    {
        schemaTester.testSettingValueInArray();
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    public void testRandomSettingValueInArray(SchemaTester schemaTester) throws Exception
    {
        schemaTester.testRandomSettingValueInArray();
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    public void testPropertySetterSimple(SchemaTester schemaTester) throws Exception
    {
        schemaTester.testPropertySetterSimple();
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    public void testRandomPropertySetterSimple(SchemaTester schemaTester) throws Exception
    {
        schemaTester.testRandomPropertySetterSimple();
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    public void testPropertySetterInArray(SchemaTester schemaTester) throws Exception
    {
        schemaTester.testPropertySetterInArray();
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    public void testRandomPropertySetterInArray(SchemaTester schemaTester) throws Exception
    {
        schemaTester.testRandomPropertySetterInArray();
    }
    
    private static SchemaTester[] initializeParameters()
    {
        XMPMetadata metadata = XMPMetadata.createXMPMetadata();
        XMPSchema schema = metadata.createAndAddDublinCoreSchema();
        
        return new SchemaTester[] {
            // data for JobType
            new SchemaTester(metadata, schema, "contributor", Types.ProperName, Cardinality.Bag),
            new SchemaTester(metadata, schema, "coverage", Types.Text, Cardinality.Simple),
            new SchemaTester(metadata, schema, "creator", Types.ProperName, Cardinality.Seq),
            new SchemaTester(metadata, schema, "date", Types.Date, Cardinality.Seq),
            new SchemaTester(metadata, schema, "format", Types.MIMEType, Cardinality.Simple),
            new SchemaTester(metadata, schema, "identifier", Types.Text, Cardinality.Simple),
            new SchemaTester(metadata, schema, "language", Types.Locale, Cardinality.Bag),
            new SchemaTester(metadata, schema, "publisher", Types.ProperName, Cardinality.Bag),
            new SchemaTester(metadata, schema, "relation", Types.Text, Cardinality.Bag),
            new SchemaTester(metadata, schema, "source", Types.Text, Cardinality.Simple),
            new SchemaTester(metadata, schema, "subject", Types.Text, Cardinality.Bag),
            new SchemaTester(metadata, schema, "type", Types.Text, Cardinality.Bag)
        };
    }
}
