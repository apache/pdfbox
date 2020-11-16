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

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.Types;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class XMPBasicTest
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
        XMPSchema schema = metadata.createAndAddXMPBasicSchema();
        Class<?> schemaClass = XMPBasicSchema.class;

        return new XMPSchemaTester[] {
            new XMPSchemaTester(metadata, schema, schemaClass, "Advisory", XMPSchemaTester.createPropertyType(Types.XPath, Cardinality.Bag), new String[] { "xpath1", "xpath2" }),
            new XMPSchemaTester(metadata, schema, schemaClass, "BaseURL", XMPSchemaTester.createPropertyType(Types.URL), "URL"),
            new XMPSchemaTester(metadata, schema, schemaClass, "CreateDate", XMPSchemaTester.createPropertyType(Types.Date), Calendar.getInstance()),
            new XMPSchemaTester(metadata, schema, schemaClass, "CreatorTool", XMPSchemaTester.createPropertyType(Types.AgentName), "CreatorTool"),
            new XMPSchemaTester(metadata, schema, schemaClass, "Identifier", XMPSchemaTester.createPropertyType(Types.Text, Cardinality.Bag), new String[] { "id1", "id2" }),
            new XMPSchemaTester(metadata, schema, schemaClass, "Label", XMPSchemaTester.createPropertyType(Types.Text), "label"),
            new XMPSchemaTester(metadata, schema, schemaClass, "MetadataDate", XMPSchemaTester.createPropertyType(Types.Date), Calendar.getInstance()),
            new XMPSchemaTester(metadata, schema, schemaClass, "ModifyDate", XMPSchemaTester.createPropertyType(Types.Date), Calendar.getInstance()),
            new XMPSchemaTester(metadata, schema, schemaClass, "Nickname", XMPSchemaTester.createPropertyType(Types.Text), "nick name"),
            new XMPSchemaTester(metadata, schema, schemaClass, "Rating", XMPSchemaTester.createPropertyType(Types.Integer), 7),
            new XMPSchemaTester(metadata, schema, schemaClass, "Thumbnails", XMPSchemaTester.createPropertyType(Types.Thumbnail, Cardinality.Alt), null)
        };
    }
}
