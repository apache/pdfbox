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

package org.apache.padaf.xmpbox.schema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.parser.XMPDocumentBuilder;
import org.apache.padaf.xmpbox.parser.XmpSerializer;
import org.apache.padaf.xmpbox.type.JobType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BasicJobTicketSchemaTest {

	protected static XMPDocumentBuilder builder;

	protected XMPMetadata metadata;
	
	protected XmpSerializer serializer;

	
	@BeforeClass
	public static void bc () throws Exception {
		builder = new XMPDocumentBuilder();
	}
	
	@Before
	public void initTempMetaData() throws Exception {
		metadata = XMPMetadata.createXMPMetadata();
		serializer = new XmpSerializer();
	}

	private InputStream transfer(ByteArrayOutputStream out) {
		IOUtils.closeQuietly(out);
		ByteArrayInputStream bis = new ByteArrayInputStream(out.toByteArray());
		return bis;
	}
	
	@Test
	public void testAddTwoJobs() throws Exception {
		
		XMPBasicJobTicketSchema basic = metadata.createAndAddBasicJobTicketSchema();

//		basic.addJob("zeid1", "zename1", "zeurl1","aaa"); FIXME the prefix is not used
		basic.addJob("zeid1", "zename1", "zeurl1");
		basic.addJob("zeid2", "zename2", "zeurl2");
		
		serializer.serialize(metadata, System.out, true);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		serializer.serialize(metadata, bos, true);
		InputStream is = transfer(bos);
		XMPMetadata rxmp = builder.parse(is);

		XMPBasicJobTicketSchema jt = rxmp.getBasicJobTicketSchema();
		Assert.assertNotNull(jt);
		Assert.assertEquals(2, jt.getJobs().size());
		
	}

	@Test
	public void testAddWithDefaultPrefix() throws Exception {
		
		XMPBasicJobTicketSchema basic = metadata.createAndAddBasicJobTicketSchema();

		basic.addJob("zeid2", "zename2", "zeurl2");
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		serializer.serialize(metadata, bos, true);
		InputStream is = transfer(bos);
		XMPMetadata rxmp = builder.parse(is);

		XMPBasicJobTicketSchema jt = rxmp.getBasicJobTicketSchema();
		Assert.assertNotNull(jt);
		Assert.assertEquals(1, jt.getJobs().size());
		
		JobType job = jt.getJobs().get(0);
		Assert.assertEquals("zeid2", job.getId());
		Assert.assertEquals("zename2", job.getName());
		Assert.assertEquals("zeurl2", job.getUrl());
		Assert.assertEquals(JobType.ELEMENT_NS, job.getFieldsNamespace());
		Assert.assertEquals(JobType.PREFERED_PREFIX, job.getFieldPrefix());
	
	}

//	@Test
	public void testAddWithDefinedPrefix() throws Exception {
		
		XMPBasicJobTicketSchema basic = metadata.createAndAddBasicJobTicketSchema();

		basic.addJob("zeid2", "zename2", "zeurl2","aaa");
		
//		SaveMetadataHelper.serialize(metadata, System.out);
		
		serializer.serialize(metadata, System.out, true);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		serializer.serialize(metadata, bos, true);
		InputStream is = transfer(bos);
		XMPMetadata rxmp = builder.parse(is);

		XMPBasicJobTicketSchema jt = rxmp.getBasicJobTicketSchema();
		Assert.assertNotNull(jt);
		Assert.assertEquals(1, jt.getJobs().size());
		
		JobType job = jt.getJobs().get(0);
//		SaveMetadataHelper.serialize(rxmp, System.out);

		Assert.assertEquals("zeid2", job.getId());
		Assert.assertEquals("zename2", job.getName());
		Assert.assertEquals("zeurl2", job.getUrl());
		Assert.assertEquals(JobType.ELEMENT_NS, job.getFieldsNamespace());
		Assert.assertEquals("aaa", job.getFieldPrefix());
	
	}

}
