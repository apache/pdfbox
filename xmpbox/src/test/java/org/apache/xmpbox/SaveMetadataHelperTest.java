/*

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

package org.apache.xmpbox;

import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.junit.jupiter.api.Test;

class SaveMetadataHelperTest
{

    @Test
    void testSchemaParsing() throws Exception
    {
        final DublinCoreSchema dc = new DublinCoreSchema(XMPMetadata.createXMPMetadata());
        dc.setCoverage("coverage");
        dc.addContributor("contributor1");
        dc.addContributor("contributor2");
        dc.addDescription("x-default", "Description");
    }

    @Test
    void testMetadataParsing() throws Exception
    {
        final XMPMetadata meta = XMPMetadata.createXMPMetadata();

        final DublinCoreSchema dc = meta.createAndAddDublinCoreSchema();
        dc.setCoverage("coverage");
        dc.addContributor("contributor1");
        dc.addContributor("contributor2");
        dc.addDescription("x-default", "Description");

        final AdobePDFSchema pdf = meta.createAndAddAdobePDFSchema();
        pdf.setProducer("Producer");
        pdf.setPDFVersion("1.4");

    }

}
