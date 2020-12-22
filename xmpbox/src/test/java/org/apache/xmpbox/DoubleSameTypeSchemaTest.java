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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.XMPSchema;
import org.apache.xmpbox.type.StructuredType;
import org.junit.jupiter.api.Test;

/**
 * Test with 2 dublinCore with different prefix (Test comportment of XMPMetadata)
 * 
 * @author a183132
 * 
 */
class DoubleSameTypeSchemaTest
{

    private final  XMPMetadata metadata = XMPMetadata.createXMPMetadata();

    @Test
    void testDoubleDublinCore() throws Exception
    {
        final DublinCoreSchema dc1 = metadata.createAndAddDublinCoreSchema();
        final String ownPrefix = "test";
        final DublinCoreSchema dc2 = new DublinCoreSchema(metadata, ownPrefix);
        metadata.addSchema(dc2);

        final List<String> creators = new ArrayList<>();
        creators.add("creator1");
        creators.add("creator2");

        final String format = "application/pdf";
        dc1.setFormat(format);
        dc1.addCreator(creators.get(0));
        dc1.addCreator(creators.get(1));

        final String coverage = "Coverage";
        dc2.setCoverage(coverage);
        dc2.addCreator(creators.get(0));
        dc2.addCreator(creators.get(1));

        final StructuredType stDub = DublinCoreSchema.class.getAnnotation(StructuredType.class);

        // We can't use metadata.getDublinCoreSchema() due to specification of
        // XMPBox (see Javadoc of XMPMetadata)
        assertEquals(format,
                ((DublinCoreSchema) metadata.getSchema(stDub.preferedPrefix(), stDub.namespace())).getFormat());
        assertEquals(coverage,
                ((DublinCoreSchema) metadata.getSchema(ownPrefix, stDub.namespace())).getCoverage());

        final List<XMPSchema> schems = metadata.getAllSchemas();
        DublinCoreSchema dc;
        for (final XMPSchema xmpSchema : schems)
        {
            dc = (DublinCoreSchema) xmpSchema;
            assertTrue(dc.getCreators().containsAll(creators));
        }

    }
}
