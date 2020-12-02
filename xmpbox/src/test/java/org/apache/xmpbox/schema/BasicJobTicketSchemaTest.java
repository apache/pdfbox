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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.JobType;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpSerializer;
import org.junit.jupiter.api.Test;

class BasicJobTicketSchemaTest
{
    @Test
    void testAddTwoJobs() throws Exception
    {
        XMPMetadata metadata = XMPMetadata.createXMPMetadata();
        XmpSerializer serializer = new XmpSerializer();
        DomXmpParser builder = new DomXmpParser();

        XMPBasicJobTicketSchema basic = metadata.createAndAddBasicJobTicketSchema();
        basic.addJob("zeid1", "zename1", "zeurl1", "aaa");
        basic.addJob("zeid2", "zename2", "zeurl2");

        // serializer.serialize(metadata, System.out, true);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        serializer.serialize(metadata, bos, true);

        XMPMetadata rxmp = builder.parse(bos.toByteArray());

        XMPBasicJobTicketSchema jt = rxmp.getBasicJobTicketSchema();
        assertNotNull(jt);
        assertEquals(2, jt.getJobs().size());
    }

    @Test
    void testAddWithDefaultPrefix() throws Exception
    {
        XMPMetadata metadata = XMPMetadata.createXMPMetadata();
        XmpSerializer serializer = new XmpSerializer();
        DomXmpParser builder = new DomXmpParser();

        XMPBasicJobTicketSchema basic = metadata.createAndAddBasicJobTicketSchema();

        basic.addJob("zeid2", "zename2", "zeurl2");

        // serializer.serialize(metadata, System.out, true);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        serializer.serialize(metadata, bos, true);
        XMPMetadata rxmp = builder.parse(bos.toByteArray());

        XMPBasicJobTicketSchema jt = rxmp.getBasicJobTicketSchema();
        assertNotNull(jt);
        assertEquals(1, jt.getJobs().size());

        JobType job = jt.getJobs().get(0);
        assertEquals("zeid2", job.getId());
        assertEquals("zename2", job.getName());
        assertEquals("zeurl2", job.getUrl());
    }

    @Test
    void testAddWithDefinedPrefix() throws Exception
    {
        XMPMetadata metadata = XMPMetadata.createXMPMetadata();
        XmpSerializer serializer = new XmpSerializer();
        DomXmpParser builder = new DomXmpParser();

        XMPBasicJobTicketSchema basic = metadata.createAndAddBasicJobTicketSchema();

        basic.addJob("zeid2", "zename2", "zeurl2", "aaa");

        // SaveMetadataHelper.serialize(metadata, System.out);

        // serializer.serialize(metadata, System.out, true);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        serializer.serialize(metadata, bos, true);
        XMPMetadata rxmp = builder.parse(bos.toByteArray());

        XMPBasicJobTicketSchema jt = rxmp.getBasicJobTicketSchema();
        assertNotNull(jt);
        assertEquals(1, jt.getJobs().size());

        JobType job = jt.getJobs().get(0);
        // SaveMetadataHelper.serialize(rxmp, System.out);

        // StructuredType stjob =
        // JobType.class.getAnnotation(StructuredType.class);

        assertEquals("zeid2", job.getId());
        assertEquals("zename2", job.getName());
        assertEquals("zeurl2", job.getUrl());
        // assertEquals(stjob.namespace(), job.getNamespace());
        // assertEquals("aaa", job.getPrefix());

    }

}
