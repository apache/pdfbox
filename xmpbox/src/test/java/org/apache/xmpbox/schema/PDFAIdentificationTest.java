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
import org.apache.xmpbox.type.Types;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class PDFAIdentificationTest
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
        XMPSchema schema = metadata.createAndAddPFAIdentificationSchema();
        Class<?> schemaClass = PDFAIdentificationSchema.class;

        return new XMPSchemaTester[] {
            new XMPSchemaTester(metadata, schema, schemaClass, "part", XMPSchemaTester.createPropertyType(Types.Integer), 1),
            new XMPSchemaTester(metadata, schema, schemaClass, "amd", XMPSchemaTester.createPropertyType(Types.Text), "2005"),
            new XMPSchemaTester(metadata, schema, schemaClass, "conformance", XMPSchemaTester.createPropertyType(Types.Text), "B")
        };
    }
}
