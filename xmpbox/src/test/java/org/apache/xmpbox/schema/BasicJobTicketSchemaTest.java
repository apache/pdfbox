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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.JobType;
import org.apache.xmpbox.type.StructuredType;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpSerializer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BasicJobTicketSchemaTest
{

    protected static DomXmpParser builder;

    protected XMPMetadata metadata;

    protected XmpSerializer serializer;

    @BeforeClass
    public static void bc() throws Exception
    {
        builder = new DomXmpParser();
    }

    @Before
    public void initTempMetaData() throws Exception
    {
        metadata = XMPMetadata.createXMPMetadata();
        serializer = new XmpSerializer();
    }

    private InputStream transfer(ByteArrayOutputStream out)
    {
        IOUtils.closeQuietly(out);
        ByteArrayInputStream bis = new ByteArrayInputStream(out.toByteArray());
        return bis;
    }

    @Test
    public void testAddTwoJobs() throws Exception
    {

        XMPBasicJobTicketSchema basic = metadata.createAndAddBasicJobTicketSchema();

        basic.addJob("zeid1", "zename1", "zeurl1", "aaa");
        basic.addJob("zeid2", "zename2", "zeurl2");

        // serializer.serialize(metadata, System.out, true);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        serializer.serialize(metadata, bos, true);
        InputStream is = transfer(bos);
        XMPMetadata rxmp = builder.parse(is);

        XMPBasicJobTicketSchema jt = rxmp.getBasicJobTicketSchema();
        Assert.assertNotNull(jt);
        Assert.assertEquals(2, jt.getJobs().size());

    }

    @Test
    public void testAddWithDefaultPrefix() throws Exception
    {

        XMPBasicJobTicketSchema basic = metadata.createAndAddBasicJobTicketSchema();

        basic.addJob("zeid2", "zename2", "zeurl2");

        // serializer.serialize(metadata, System.out, true);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        serializer.serialize(metadata, bos, true);
        InputStream is = transfer(bos);
        XMPMetadata rxmp = builder.parse(is);

        XMPBasicJobTicketSchema jt = rxmp.getBasicJobTicketSchema();
        Assert.assertNotNull(jt);
        Assert.assertEquals(1, jt.getJobs().size());
        StructuredType stjob = JobType.class.getAnnotation(StructuredType.class);

        JobType job = jt.getJobs().get(0);
        Assert.assertEquals("zeid2", job.getId());
        Assert.assertEquals("zename2", job.getName());
        Assert.assertEquals("zeurl2", job.getUrl());
        // Assert.assertEquals("Invalid namespace",stjob.namespace(),
        // job.getNamespace());
        // Assert.assertEquals(stjob.preferedPrefix(), job.getPrefix());

    }

    @Test
    public void testAddWithDefinedPrefix() throws Exception
    {

        XMPBasicJobTicketSchema basic = metadata.createAndAddBasicJobTicketSchema();

        basic.addJob("zeid2", "zename2", "zeurl2", "aaa");

        // SaveMetadataHelper.serialize(metadata, System.out);

        // serializer.serialize(metadata, System.out, true);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        serializer.serialize(metadata, bos, true);
        InputStream is = transfer(bos);
        XMPMetadata rxmp = builder.parse(is);

        XMPBasicJobTicketSchema jt = rxmp.getBasicJobTicketSchema();
        Assert.assertNotNull(jt);
        Assert.assertEquals(1, jt.getJobs().size());

        JobType job = jt.getJobs().get(0);
        // SaveMetadataHelper.serialize(rxmp, System.out);

        // StructuredType stjob =
        // JobType.class.getAnnotation(StructuredType.class);

        Assert.assertEquals("zeid2", job.getId());
        Assert.assertEquals("zename2", job.getName());
        Assert.assertEquals("zeurl2", job.getUrl());
        // Assert.assertEquals(stjob.namespace(), job.getNamespace());
        // Assert.assertEquals("aaa", job.getPrefix());

    }

}
