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
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.Types;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class XMPMediaManagementTest
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
        XMPSchema schema = metadata.createAndAddXMPMediaManagementSchema();
        Class<?> schemaClass = XMPMediaManagementSchema.class;

        return new XMPSchemaTester[] {
            new XMPSchemaTester(metadata, schema, schemaClass, "DocumentID", XMPSchemaTester.createPropertyType(Types.URI), "uuid:FB031973-5E75-11B2-8F06-E7F5C101C07A"),
            new XMPSchemaTester(metadata, schema, schemaClass, "Manager", XMPSchemaTester.createPropertyType(Types.AgentName), "Raoul"),
            new XMPSchemaTester(metadata, schema, schemaClass, "ManageTo", XMPSchemaTester.createPropertyType(Types.URI), "uuid:36"),
            new XMPSchemaTester(metadata, schema, schemaClass, "ManageUI", XMPSchemaTester.createPropertyType(Types.URI), "uuid:3635"),
            new XMPSchemaTester(metadata, schema, schemaClass, "InstanceID", XMPSchemaTester.createPropertyType(Types.URI), "uuid:42"),
            new XMPSchemaTester(metadata, schema, schemaClass, "OriginalDocumentID", XMPSchemaTester.createPropertyType(Types.Text), "uuid:142"),
            new XMPSchemaTester(metadata, schema, schemaClass, "RenditionParams", XMPSchemaTester.createPropertyType(Types.Text), "my params"),
            new XMPSchemaTester(metadata, schema, schemaClass, "VersionID", XMPSchemaTester.createPropertyType(Types.Text), "14"),
            new XMPSchemaTester(metadata, schema, schemaClass, "Versions", XMPSchemaTester.createPropertyType(Types.Version, Cardinality.Seq), new String[] { "1", "2", "3" }),
            new XMPSchemaTester(metadata, schema, schemaClass, "History", XMPSchemaTester.createPropertyType(Types.Text, Cardinality.Seq),new String[] { "action 1", "action 2", "action 3" }),
            new XMPSchemaTester(metadata, schema, schemaClass, "Ingredients", XMPSchemaTester.createPropertyType(Types.Text, Cardinality.Bag), new String[] { "resource1", "resource2" })
        };
    }
}
